/*
** Copyright (c) 2014, 2016 Oracle and/or its affiliates
** The Universal Permissive License (UPL), Version 1.0
*
** Subject to the condition set forth below, permission is hereby granted to any person obtaining a copy of this
** software, associated documentation and/or data (collectively the "Software"), free of charge and under any and
** all copyright rights in the Software, and any and all patent rights owned or freely licensable by each licensor
** hereunder covering either (i) the unmodified Software as contributed to or provided by such licensor, or
** (ii) the Larger Works (as defined below), to deal in both
**
** (a) the Software, and
** (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if one is included with the Software
** (each a “Larger Work” to which the Software is contributed by such licensors),
**
** without restriction, including without limitation the rights to copy, create derivative works of, display,
** perform, and distribute the Software and make, use, sell, offer for sale, import, export, have made, and have
** sold the Software and the Larger Work(s), and to sublicense the foregoing rights on either these or other terms.
**
** This license is subject to the following condition:
** The above copyright notice and either this complete permission notice or at a minimum a reference to the UPL must
** be included in all copies or substantial portions of the Software.
**
** THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
** THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
** AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
** CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
** IN THE SOFTWARE. 
*/

package oracle.ateam.sb.transports.kafka;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import com.bea.wli.config.Ref;
import com.bea.wli.sb.sources.ByteArraySource;
import com.bea.wli.sb.sources.Source;
import com.bea.wli.sb.sources.StringSource;
import com.bea.wli.sb.sources.TransformException;
import com.bea.wli.sb.transports.EndPointConfiguration;
import com.bea.wli.sb.transports.OutboundTransportMessageContext;
import com.bea.wli.sb.transports.RequestHeaders;
import com.bea.wli.sb.transports.TransportException;
import com.bea.wli.sb.transports.TransportManager;
import com.bea.wli.sb.transports.TransportManagerHelper;
import com.bea.wli.sb.transports.TransportOptions;
import com.bea.wli.sb.transports.TransportProvider;
import com.bea.wli.sb.transports.TransportSendListener;
import com.bea.wli.sb.transports.TransportSender;
import com.bea.wli.sb.transports.URIType;
import com.bea.wli.sb.transports.util.AbstractTransportEndPoint;

/**
 * @author Ricardo Ferreira
 */
public class KafkaEndpoint extends AbstractTransportEndPoint {
	
	private final String textType =
			KafkaConstants.TEXT_REQUEST_TYPE;
	
	private final String binaryType =
			KafkaConstants.BINARY_REQUEST_TYPE;
	
	private final String responseWorkManager =
			TransportManagerHelper.DEFAULT_RESPONSE_WORKMANAGER;
	
	private TransportProvider transportProvider;
	private TransportManager transportManager;
	
	private String topicName;
	private String requestType;
	@SuppressWarnings("unused")
	private String responseType;
	private String customProps;
	private String dispatchPolicy;
	private short consumerThreads;
	
	private List<InternalConsumer> internalConsumers;
	private Properties consumerProps;
	private Properties producerProps;
	@SuppressWarnings("rawtypes")
	private KafkaProducer producer;
	
	protected KafkaEndpoint(TransportProvider transportProvider,
			Ref serviceRef, EndPointConfiguration endpointConfig)
					throws TransportException {
		
		super(serviceRef, endpointConfig);
		KafkaEndPointConfiguration kafkaEndpointConfig = null;
		
		try {
			
			this.transportProvider = transportProvider;
			kafkaEndpointConfig = KafkaUtil.getConfig(endpointConfig);
			
			topicName = kafkaEndpointConfig.getTopicName();
			requestType = kafkaEndpointConfig.getRequestType();
			responseType = kafkaEndpointConfig.getResponseType();
			customProps = kafkaEndpointConfig.getCustomProps();
			
			if (isInbound()) {
				
				initConsumerProperties(endpointConfig, kafkaEndpointConfig);
				
			} else {
				
				initProducerProperties(endpointConfig, kafkaEndpointConfig);
				
			}
			
		} catch (Exception ex) {
			
			throw new TransportException(ex);
			
		}
		
	}
	
	private void initConsumerProperties(EndPointConfiguration endpointConfig,
			KafkaEndPointConfiguration kafkaEndpointConfig) {
		
		KafkaInboundPropertiesType inboundProps =
				kafkaEndpointConfig.getInboundProperties();
		
		dispatchPolicy = inboundProps.getDispatchPolicy();
		consumerThreads = inboundProps.getConsumerThreads();
		consumerProps = new Properties();
		
		consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, endpointConfig.getURIArray()[0].getValue());
		consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, inboundProps.getGroupId());
		consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		
		if (requestType.equals(textType)) {
			
			consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
					StringDeserializer.class.getName());
			
		} else if (requestType.equals(binaryType)) {
			
			consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
					ByteArrayDeserializer.class.getName());
			
		}
		
		setCustomProperties(consumerProps);
		
		if (!consumerProps.containsKey(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG)) {
			
			consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.TRUE.toString());
			
		}
		
		checkPrintProperties(consumerProps);
		
	}
	
	private void initProducerProperties(EndPointConfiguration endpointConfig,
			KafkaEndPointConfiguration kafkaEndpointConfig) {
		
		KafkaOutboundPropertiesType outboundProps = kafkaEndpointConfig.getOutboundProperties();
		short retryCount = endpointConfig.getOutboundProperties().getRetryCount();
		int retryInterval = endpointConfig.getOutboundProperties().getRetryInterval();
		producerProps = new Properties();
		
		producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers(endpointConfig.getURIArray()));
		producerProps.put(ProducerConfig.ACKS_CONFIG, outboundProps.getAcks());
		producerProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, String.valueOf(outboundProps.getTimeoutMs()));
		producerProps.put(ProducerConfig.RETRIES_CONFIG, String.valueOf(retryCount));
		producerProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, calculateRetryBackoff(retryCount, retryInterval));
		producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		
		if (requestType.equals(textType)) {
			
			producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
					StringSerializer.class.getName());
			
		} else if (requestType.equals(binaryType)) {
			
			producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
					ByteArraySerializer.class.getName());
			
		}
		
		setCustomProperties(producerProps);
		checkPrintProperties(producerProps);
		
	}
	
	private void setCustomProperties(Properties properties) {
		
		if (customProps != null && customProps.length() > 0) {
			
			String[] _customProps = customProps.split(",");
			
			for (String propEntry : _customProps) {
				
				String[] property = propEntry.split("=");
				
				if (property.length == 2) {
					
					properties.setProperty(property[0], property[1]);
					
				}
				
			}
			
		}
		
	}
	
	private void checkPrintProperties(Properties properties) {
		
		if (KafkaUtil.printProperties()) {
			
			KafkaTransportLogger.log(Level.INFO, "The endpoint '" + getServiceRef().getLocalName() +
					"' is being created using the following properties");
			
			StringBuilder output = new StringBuilder();
			Set<Object> keySet = properties.keySet();
			
			for (Object propKey : keySet) {
				
				output.append(propKey).append("=").append(properties.get(propKey)).append("\n");
				
			}
			
			System.out.println(output.toString());
			
		}
		
	}
	
	private String getBootstrapServers(URIType[] endpoints) {
		
		StringBuilder bootstrapServers = new StringBuilder();
		
		for (int i = 0; i < endpoints.length; i++) {
			
			if (i == endpoints.length - 1) {
				
				bootstrapServers.append(endpoints[i].getValue());
				
			} else {
				
				bootstrapServers.append(endpoints[i].getValue()).append(",");
				
			}
			
		}
		
		return bootstrapServers.toString();
		
	}
	
	/**
	 * This method calculates an approximated retry backoff value
	 * to be set in Kafka's 'retry.backoff.ms' property. Because this
	 * property accepts milliseconds precision, we need to convert
	 * the retry interval set in the 'Retry Interval' property from
	 * seconds to milliseconds. To help in this inference, we use
	 * the 'Retry Count' property.
	 */
	
	private String calculateRetryBackoff(short retryCount, int retryInterval) {
		
		if (retryCount == 0) {
			
			return "0";
			
		}
		
		int retryIntervalInMillis = retryInterval * 1000;
		int retryBackoff = retryIntervalInMillis / retryCount;
		
		return String.valueOf(retryBackoff);
		
	}
	
	private SendDetails getSendDetails(TransportSender transportSender)
			throws TransportException {
		
		RequestHeaders headers = transportSender.getMetaData().getHeaders();
		KafkaRequestHeaders kafkaHeaders = new KafkaRequestHeaders(headers.toXML());
		
		return new SendDetails(kafkaHeaders.getMessageKey(), kafkaHeaders.getPartition());
		
	}
	
	private Object getMessage(TransportSender transportSender)
			throws TransportException {
		
		Object message = null;
		String encoding = null;
		
		if (transportSender.getMetaData() != null) {
			
			encoding = transportSender.getMetaData().getCharacterEncoding();
			
		}
		
		Source payload = transportSender.getPayload();
		
		if (requestType.equals(textType)) {
			
			if (payload instanceof StringSource) {
				
				message = ((StringSource) payload).getString();
				
			} else {
				
				try {
					
					payload = KafkaUtil.getAsText(payload, encoding);
					message = ((StringSource) payload).getString();
					
				} catch (TransformException te) {
					
					throw new TransportException(te);
					
				}
				
			}
			
		} else if (requestType.equals(binaryType)) {
			
			if (payload instanceof ByteArraySource) {
				
				message = ((ByteArraySource) payload).getBytes();
				
			} else {
				
				try {
					
					payload = KafkaUtil.getAsBinary(payload, encoding);
					message = ((ByteArraySource) payload).getBytes();
					
				} catch (TransformException te) {
					
					throw new TransportException(te);
					
				}
				
			}
			
		}
		
		return message;
		
	}

	@Override
	public MessagePatternEnum getMessagePattern() throws TransportException {
		
		return MessagePatternEnum.ONE_WAY;
		
	}

	@Override
	public TransportProvider getProvider() {
		
		return transportProvider;
		
	}

	@Override
	public boolean isTransactional() throws TransportException {
		
		return false;
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void sendMessageAsync(TransportSender transportSender,
			TransportSendListener transportSendListener,
			TransportOptions transportOptions) throws TransportException {
		
		if (isInbound()) {
			
			KafkaInboundMessageContext inboundMsgCtx = new KafkaInboundMessageContext(
					this, transportSender.getMetaData(), transportSender.getPayload());
			
			transportManager.receiveMessage(inboundMsgCtx, transportOptions);
			
			KafkaUtil.schedule(new TestConsoleReplyWork(transportSendListener,
					new KafkaOutboundMessageContext(transportOptions.getURI())),
					responseWorkManager);
			
		} else {
			
			Object message = null;
			SendDetails details = null;
			ProducerRecord record = null;
			
			try {
				
				details = getSendDetails(transportSender);
				message = getMessage(transportSender);
				
				if (requestType.equals(textType)) {
					
					record = new ProducerRecord<String, String>(topicName,
							details.getPartition(), details.getMessageKey(),
							(String) message);
					
				} else if (requestType.equals(binaryType)) {
					
					record = new ProducerRecord<String, byte[]>(topicName,
							details.getPartition(), details.getMessageKey(),
							(byte[]) message);
					
				}
				
				producer.send(record, new SendCallback(
						transportOptions.getURI(),
						transportSendListener));
				
			} catch (Exception ex) {
				
				throw new TransportException(ex);
				
			}
			
		}
		
	}

	public void start() throws TransportException {
		
		InternalConsumer internalConsumer = null;
		
		try {
			
			if (isInbound()) {
				
				transportManager = TransportManagerHelper.getTransportManager();
				
				if (internalConsumers == null) {
					
					internalConsumers = new ArrayList<KafkaEndpoint.InternalConsumer>();
					
				}
				
				for (int i = 0; i < consumerThreads; i++) {
					
					// Creates a internal consumer and schedule it's
					// execution in the self-tuning thread pool. Due
					// the nature of the Kafka consumer API; the thread
					// will block throughout the entire endpoint life
					// cycle, which means that it may be considered
					// stuck by the WebLogic threading system. Thus,
					// to avoid having WebLogic warning about this with
					// several messages in the logs; and hanging while
					// trying to restart or shutdown, always associate
					// the endpoint with a custom work manager capable
					// of ignoring stuck threads.
					
					internalConsumer = new InternalConsumer(
							topicName, this, consumerProps);
					
					internalConsumers.add(internalConsumer);
					KafkaUtil.schedule(internalConsumer,
							dispatchPolicy);
					
				}
				
			} else {
				
				if (requestType.equals(textType)) {
					
					producer = new KafkaProducer<String, String>(producerProps);
					
				} else if (requestType.equals(binaryType)) {
					
					producer = new KafkaProducer<String, byte[]>(producerProps);
					
				}
				
			}
			
		} catch (Exception ex) {
			
			throw new TransportException(ex);
			
		}
		
	}
	
	public void stop() throws TransportException {
		
		if (isInbound()) {
			
			for (InternalConsumer consumer : internalConsumers) {
				
				consumer.shutdown();
				
			}
			
			internalConsumers.clear();
			
		} else {
			
			if (producer != null) {
				
				producer.close();
				
			}
			
		}
		
	}
	
	private class InternalConsumer implements Runnable {
		
		private String topicName;
		private KafkaEndpoint endpoint;
		private boolean autoCommitEnabled = true;
		private boolean commitAsyncEnabled = false;
		private KafkaConsumer<String, Object> consumer;
		
		public InternalConsumer(String topicName,
				KafkaEndpoint endpoint, Properties consumerProps) {
			
			this.topicName = topicName;
			this.endpoint = endpoint;
			String propValue = null;
			
			if (consumerProps.containsKey(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG)) {
				
				propValue = consumerProps.getProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG);
				this.autoCommitEnabled = Boolean.parseBoolean(propValue);
				
			}
			
			if (consumerProps.containsKey(KafkaConstants.EP_CONSUMER_COMMIT_ASYNC)) {
				
				propValue = consumerProps.getProperty(KafkaConstants.EP_CONSUMER_COMMIT_ASYNC);
				this.commitAsyncEnabled = Boolean.parseBoolean(propValue);
				
			}
			
			this.consumer = new KafkaConsumer<String, Object>(consumerProps);
			
		}

		@Override
		public void run() {
			
			String _stringPayload = null;
			byte[] _binaryPayload = null;
			Source requestPayload = null;
			Map<String, Object> headers = null;
			
			KafkaInboundMessageContext inboundMessageContext = null;
			ConsumerRecords<String, Object> records = null;
			
			try {
				
				subscribe(consumer, topicName);
				
				while (true) {
					
					records = consumer.poll(Long.MAX_VALUE);
					
					for (ConsumerRecord<String, Object> record : records) {
						
						if (requestType.equals(textType)) {
							
							_stringPayload = (String) record.value();
							requestPayload = new StringSource(_stringPayload);
							
						} else if (requestType.equals(binaryType)) {
							
							_binaryPayload = (byte[]) record.value();
							requestPayload = new ByteArraySource(_binaryPayload);
							
						}
						
						if (endpoint.getAllHeaders()) {
							
							inboundMessageContext = new KafkaInboundMessageContext(
									endpoint, record.key(), record.partition(),
									record.offset(), requestPayload);
							
						} else {
							
							headers = new HashMap<String, Object>();
							
							for (String header : endpoint.getUserHeaderArray()) {
								
								if (header.equals(KafkaConstants.MESSAGE_KEY)) {
									
									headers.put(KafkaConstants.MESSAGE_KEY, record.key());
									
								}
								
								if (header.equals(KafkaConstants.PARTITION)) {
									
									headers.put(KafkaConstants.PARTITION, record.partition());
									
								}
								
								if (header.equals(KafkaConstants.OFFSET)) {
									
									headers.put(KafkaConstants.OFFSET, record.offset());
									
								}
								
							}
							
							inboundMessageContext = new KafkaInboundMessageContext(
									endpoint, headers, requestPayload);
							
						}
						
						// The verification below checks the user wiling to manually
						// commit every record received. That is controlled by the
						// consumer property 'enable.auto.commit'. Once set, we need
						// to make sure that the user preference is satisfied so we
						// explicitly call for a commit.
						
						// However, we know that committing on every record received
						// will decrease throughput. So we give the user an option
						// to control if those commits are going to be executed in
						// synchronous or asynchronous fashion. That can be controlled
						// by the consumer property 'endpoint.consumer.commitAsync'.
						
						// Another point to consider is that commits should happen
						// before the execution of the service pipeline. It is up to
						// the transport layer provides guarantees about record commits.
						// If something goes wrong during pipeline execution (perhaps
						// due some badly written flow logic) that would give a false
						// impression that the record wasn't received properly, which
						// is not true.
						
						if (isAutoCommitDisabled()) {
							
							if (isCommitAsyncEnabled()) {
								
								consumer.commitAsync();
								
							} else {
								
								consumer.commitSync();
								
							}
							
						}
						
						transportManager.receiveMessage(inboundMessageContext, null);
						
					}
					
				}
				
			} catch (Exception ex) {
				
				KafkaTransportLogger.error(ex.getMessage(), ex);
				
			} finally {
				
				consumer.close();
				
			}
			
		}
		
		public void shutdown() {
			
			consumer.wakeup();
			
		}
		
		/**
		 * The method below needs to initiate consumer subscription
		 * over the topic(s) in a smarter manner. Because there are
		 * API differences between Kafka 0.9 and 0.10, the code needs
		 * to identify which version is being used and invoke the
		 * subscribe() method with the proper signature. In 0.9.X
		 * the subscribe method accepts a java.util.List. On the
		 * other hand; version 0.10.X accepts a java.util.Collection.
		 * 
		 * Because Kafka has been compiled with different binaries,
		 * we need to figure out which version to handle in runtime
		 * and perform the appropriate parameter handling.
		 * 
		 * Hopefully Kafka folks will stop this mess with the client
		 * API and keep it compatible over the years, or at least
		 * be more cautious when changing the method signatures.
		 */
		
		@SuppressWarnings("rawtypes")
		private void subscribe(KafkaConsumer<String, Object> consumer,
				String topicName) throws Exception {
			
			List<String> topicsIn9X = null;
			Collection<String> topicsIn10X = null;
			
			Class classImpl = KafkaUtil.loadClass("org.apache.kafka.clients.consumer.KafkaConsumer");
			Method[] methods = classImpl.getMethods();
			
			for (Method method : methods) {
				
				if (method.getName().equals("subscribe")) {
					
					Class[] paramTypes = method.getParameterTypes();
					
					if (paramTypes.length == 1) {
						
						if (paramTypes[0].equals(List.class)) {
							
							topicsIn9X = Arrays.asList(topicName);
							method.invoke(consumer, topicsIn9X);
							
						} else if (paramTypes[0].equals(Collection.class)) {
							
							topicsIn10X = Arrays.asList(topicName);
							method.invoke(consumer, topicsIn10X);
							
						}
						
					} else {
						
						continue;
						
					}
					
					break;
					
				}
				
			}
			
		}
		
		private boolean isAutoCommitDisabled() {
			
			return autoCommitEnabled == false;
			
		}
		
		private boolean isCommitAsyncEnabled() {
			
			return commitAsyncEnabled;
			
		}
		
	}
	
	private class SendDetails {
		
		private String messageKey;
		private Integer partition;
		
		public SendDetails(String messageKey,
				Integer partition) {
			
			this.messageKey = messageKey;
			this.partition = partition;
			
		}
		
		public String getMessageKey() {
			
			return messageKey;
			
		}
		
		public Integer getPartition() {
			
			return partition;
			
		}
		
	}
	
	private class TestConsoleReplyWork implements Runnable {

		private TransportSendListener transportSendListener;
		private OutboundTransportMessageContext outboundMessageContext;
		
		public TestConsoleReplyWork(TransportSendListener transportSendListener,
				OutboundTransportMessageContext outboundMessageContext) {
			
			this.outboundMessageContext = outboundMessageContext;
			this.transportSendListener = transportSendListener;
			
		}

		public void run() {
			
			transportSendListener.onReceiveResponse(outboundMessageContext);
			
		}
		
	}
	
	private class KafkaReplyWork implements Runnable {
		
		private TransportSendListener transportSendListener;
		private OutboundTransportMessageContext outboundMessageContext;
		private Exception exception;
		
		public KafkaReplyWork(TransportSendListener transportSendListener,
				OutboundTransportMessageContext outboundMessageContext,
				Exception exception) {
			
			this.outboundMessageContext = outboundMessageContext;
			this.transportSendListener = transportSendListener;
			this.exception = exception;
			
		}

		@Override
		public void run() {
			
			if (exception != null) {
				
				transportSendListener.onError(outboundMessageContext,
						TransportManager.TRANSPORT_ERROR_APPLICATION,
						exception.getMessage());
				
			} else {
				
				transportSendListener.onReceiveResponse(outboundMessageContext);
				
			}
			
		}
		
	}
	
	private class SendCallback implements Callback {
		
		private URI uri;
		private TransportSendListener transportSendListener;
		
		public SendCallback(URI uri, TransportSendListener transportSendListener) {
			
			this.uri = uri;
			this.transportSendListener = transportSendListener;
			
		}

		@Override
		public void onCompletion(RecordMetadata recordMetadata,
				Exception exception) {
			
			KafkaOutboundMessageContext outboundMessageContext = null;
			
			try {
				
				if (recordMetadata != null) {
					
					outboundMessageContext = new KafkaOutboundMessageContext(uri,
							recordMetadata.topic(), recordMetadata.partition(),
							recordMetadata.offset());
					
				} else {
					
					outboundMessageContext = new KafkaOutboundMessageContext(uri);
					
					// It is important to consider that the situation below
					// might never happen. However, we need to make sure to
					// effectively communicate such scenario to figure out
					// the root cause of this situation. The internal network
					// I/O threads created by the Producer API make sure to
					// provide the underlying exception when something goes
					// south; and not being able to provide this exception
					// can only means that those internal threads are gone,
					// along with the entire VM.
					
					if (exception == null) {
						
						exception = new RuntimeException(KafkaConstants.ABNORMAL_CONFIRM_RECEIVED);
						
					}
					
				}
				
				KafkaUtil.schedule(new KafkaReplyWork(transportSendListener,
						outboundMessageContext, exception), responseWorkManager);
				
			} catch (TransportException te) {
				
				KafkaTransportLogger.error(te.getMessage(), te);
				
			}
			
		}
		
	}
	
}