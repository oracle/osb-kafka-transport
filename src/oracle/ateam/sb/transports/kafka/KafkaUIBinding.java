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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.xmlbeans.XmlObject;

import com.bea.wli.sb.services.BindingTypeInfo;
import com.bea.wli.sb.transports.EndPointConfiguration;
import com.bea.wli.sb.transports.LoadBalancingAlgorithmEnum.Enum;
import com.bea.wli.sb.transports.TransportException;
import com.bea.wli.sb.transports.TransportManagerHelper;
import com.bea.wli.sb.transports.ui.TransportEditField;
import com.bea.wli.sb.transports.ui.TransportUIBinding;
import com.bea.wli.sb.transports.ui.TransportUIContext;
import com.bea.wli.sb.transports.ui.TransportUIError;
import com.bea.wli.sb.transports.ui.TransportUIFactory;
import com.bea.wli.sb.transports.ui.TransportUIGenericInfo;
import com.bea.wli.sb.transports.ui.TransportViewField;
import com.bea.wli.sb.transports.util.UIBindingUtils;

/**
 * @author Ricardo Ferreira
 */
public class KafkaUIBinding implements TransportUIBinding {
	
	private TransportUIContext uiContext;
	private ResourceBundle bundle;
	
	public KafkaUIBinding(TransportUIContext uiContext) {
		
		this.uiContext = uiContext;
		
		this.bundle = ResourceBundle.getBundle(
				getClass().getName(),
				uiContext.getLocale());
		
	}
	
	@Override
	public TransportEditField[] getEditPage(
			EndPointConfiguration endpointConfig,
			BindingTypeInfo bindingTypeInfo) throws TransportException {
		
		List<TransportEditField> fields = null;
		KafkaEndPointConfiguration kafkaEndpointConfig = null;
		String topicName = "test";
		String customProps = "";
		String customPropsLabel = null;
		
		// Consumer Properties...
		short consumerThreads = 1;
		String dispatchPolicy = UIBindingUtils.DEFAULT_PROXY_WORK_MANAGER;
		String groupId = uiContext.getServiceRef().getLocalName();
		
		// Producer properties
		String acks = "1";
		long timeoutMs = 30000;
		
		try {
			
			fields = new ArrayList<TransportEditField>();
			uiContext.put("request-type", bindingTypeInfo.getRequestMessageType());
			
			// Capture all the data previously stored in the service endpoint
			// configuration. If this is the first time its being created then
			// set default values for some built-in UI fields.
			
			if (endpointConfig != null && endpointConfig.isSetProviderSpecific()) {
				
				kafkaEndpointConfig = KafkaUtil.getConfig(endpointConfig);
				topicName = kafkaEndpointConfig.getTopicName();
				customProps = kafkaEndpointConfig.getCustomProps();
				
				if (uiContext.isProxy()) {
					
					dispatchPolicy = kafkaEndpointConfig.getInboundProperties().getDispatchPolicy();
					consumerThreads = kafkaEndpointConfig.getInboundProperties().getConsumerThreads();
					groupId = kafkaEndpointConfig.getInboundProperties().getGroupId();
					
				} else {
					
					acks = kafkaEndpointConfig.getOutboundProperties().getAcks();
					timeoutMs = kafkaEndpointConfig.getOutboundProperties().getTimeoutMs().intValue();
					
				}
				
			} else {
				
				if (!uiContext.isProxy()) {
					
					endpointConfig.getOutboundProperties().setLoadBalancingAlgorithm(Enum.forInt(4));
					endpointConfig.getOutboundProperties().setRetryCount((short) 0);
					endpointConfig.getOutboundProperties().setRetryInterval(0);
					
				}
				
			}
			
			// Now we are going to create all the UI fields...
			
			if (uiContext.isProxy()) {
				
				// Dispatch Policy
				
				TransportEditField dispatchPolicyField = UIBindingUtils.createDispatchPolicyField(
						dispatchPolicy, uiContext, TransportManagerHelper.isOffline());
				
				dispatchPolicyField.setRequired(true);
				fields.add(dispatchPolicyField);
				
				// Consumer Threads
				
				TransportUIFactory.TextBoxObject consumerThreadsTxt =
						TransportUIFactory.createTextBox(String.valueOf(consumerThreads), 14);
				
				TransportEditField consumerThreadsField = TransportUIFactory.createEditField("consumer-threads",
						bundle.getString("CONSUMER_THREADS_LABEL"), bundle.getString("CONSUMER_THREADS_DESC"),
						consumerThreadsTxt);
				
				consumerThreadsField.setRequired(true);
				fields.add(consumerThreadsField);
				
				// Topic Name
				
				TransportUIFactory.TextBoxObject topicNameTxt =
						TransportUIFactory.createTextBox(String.valueOf(topicName), 14);
				
				TransportEditField topicNameField = TransportUIFactory.createEditField("topic-name",
						bundle.getString("TOPIC_NAME_LABEL"), bundle.getString("TOPIC_NAME_DESC"),
						topicNameTxt);
				
				topicNameField.setRequired(true);
				fields.add(topicNameField);
				
				// Group ID
				
				TransportUIFactory.TextBoxObject groupIdTxt =
						TransportUIFactory.createTextBox(String.valueOf(groupId), 14);
				
				TransportEditField groupIdField = TransportUIFactory.createEditField("group-id",
						bundle.getString("GROUP_ID_LABEL"), bundle.getString("GROUP_ID_DESC"),
						groupIdTxt);
				
				groupIdField.setRequired(true);
				fields.add(groupIdField);
				
			} else {
				
				// Topic Name
				
				TransportUIFactory.TextBoxObject topicNameTxt =
						TransportUIFactory.createTextBox(topicName, 14);
				
				TransportEditField topicNameField = TransportUIFactory.createEditField("topic-name",
						bundle.getString("TOPIC_NAME_LABEL"), bundle.getString("TOPIC_NAME_DESC"),
						topicNameTxt);
				
				topicNameField.setRequired(true);
				fields.add(topicNameField);
				
				// Acks
				
				TransportUIFactory.SelectObject acksTxt = TransportUIFactory.createSelectObject(
						new String[]{"0", "1", "all"}, new String[]{"Without Acknowledge", "Leader Acknowledge", "ISRs Acknowledge"},
						String.valueOf(acks), TransportUIFactory.SelectObject.DISPLAY_LIST, false);
				
				TransportEditField acksField = TransportUIFactory.createEditField("acks",
						bundle.getString("ACKS_LABEL"), bundle.getString("ACKS_DESC"), acksTxt);
				
				acksField.setRequired(true);
				fields.add(acksField);
				
				// Timeout
				
				TransportUIFactory.TextBoxObject timeoutMsTxt =
						TransportUIFactory.createTextBox(String.valueOf(timeoutMs), 14);
				
				TransportEditField timeoutMsField = TransportUIFactory.createEditField("timeout-ms",
						bundle.getString("TIMEOUT_MS_LABEL"), bundle.getString("TIMEOUT_MS_DESC"),
						timeoutMsTxt);
				
				timeoutMsField.setRequired(true);
				fields.add(timeoutMsField);
				
			}
			
			// Custom Properties
			
			TransportUIFactory.TextBoxObject customPropsJDevTxt = null;
			TransportUIFactory.TextAreaObject customPropsWebTxt = null;
			TransportEditField customPropsField = null;
			
			if (uiContext.isProxy()) {
				
				customPropsLabel = bundle.getString("CUSTOM_PROPS_LABEL_CONSUMER");
				
			} else {
				
				customPropsLabel = bundle.getString("CUSTOM_PROPS_LABEL_PRODUCER");
				
			}
			
			// Current JDeveloper plugin for Service Bus does not render
			// correctly text area objects, causing them to be too small
			// for the UI. In this case, we render a standard text box
			// that has a default size.
			
			if (TransportManagerHelper.isOffline()) {
				
				customPropsJDevTxt = TransportUIFactory.createTextBox(customProps, 14);
				
				customPropsField = TransportUIFactory.createEditField("custom-props",
						customPropsLabel, bundle.getString("CUSTOM_PROPS_DESC"), customPropsJDevTxt);
				
			} else {
				
				customPropsWebTxt = TransportUIFactory.createTextArea(customProps, 70, 5, false);
				
				customPropsField = TransportUIFactory.createEditField("custom-props",
						customPropsLabel, bundle.getString("CUSTOM_PROPS_DESC"), customPropsWebTxt);
				
			}
			
			customPropsField.setAdvanced(true);
			fields.add(customPropsField);
			
		} catch (Exception ex) {
			
			throw new TransportException(ex);
			
		}
		
		return fields.toArray(new TransportEditField[fields.size()]);

	}

	@Override
	public TransportUIGenericInfo getGenericInfo() {

		TransportUIGenericInfo uiGenericInfo = new TransportUIGenericInfo();
		
		if (uiContext.isProxy()) {
			
			uiGenericInfo.setUriFormat(bundle.getString("PROXY_URI_FORMAT"));
			uiGenericInfo.setUriAutofill(bundle.getString("PROXY_URI_FORMAT"));
			
		} else {

			uiGenericInfo.setUriFormat(bundle.getString("BUSINESS_URI_FORMAT"));
			
		}
		
		return uiGenericInfo;

	}

	@Override
	public XmlObject getProviderSpecificConfiguration(
			TransportEditField[] fields) throws TransportException {
		
		KafkaEndPointConfiguration kafkaEndpointConfig = null;
		KafkaInboundPropertiesType kafkaInboundProps = null;
		KafkaOutboundPropertiesType kafkaOutboundProps = null;
		Map<String, TransportUIFactory.TransportUIObject> map = null;
		
		try {
			
			map = TransportEditField.getObjectMap(fields);
			
			kafkaEndpointConfig = KafkaEndPointConfiguration.Factory.newInstance();
			kafkaEndpointConfig.setTopicName(TransportUIFactory.getStringValue(map, "topic-name"));
			kafkaEndpointConfig.setRequestType(uiContext.get("request-type").toString());
			kafkaEndpointConfig.setResponseType("NONE");
			kafkaEndpointConfig.setCustomProps(TransportUIFactory.getStringValue(map, "custom-props"));
			
			if (uiContext.isProxy()) {
				
				kafkaInboundProps = kafkaEndpointConfig.addNewInboundProperties();
				
				kafkaInboundProps.setDispatchPolicy(TransportUIFactory.getStringValue(map, UIBindingUtils.DISPATCH_POLICY));
				kafkaInboundProps.setConsumerThreads((short) TransportUIFactory.getIntValue(map, "consumer-threads"));
				kafkaInboundProps.setGroupId(TransportUIFactory.getStringValue(map, "group-id"));
				
				kafkaEndpointConfig.setInboundProperties(kafkaInboundProps);
				
			} else {
				
				kafkaOutboundProps = kafkaEndpointConfig.addNewOutboundProperties();
				
				kafkaOutboundProps.setAcks(TransportUIFactory.getStringValue(map, "acks"));
				kafkaOutboundProps.setTimeoutMs(BigInteger.valueOf(TransportUIFactory.getIntValue(map, "timeout-ms")));
				
				kafkaEndpointConfig.setOutboundProperties(kafkaOutboundProps);
				
			}
			
		} catch (Exception ex) {
			
			throw new TransportException(ex);
			
		}
		
		return kafkaEndpointConfig;

	}

	@Override
	public TransportViewField[] getViewPage(EndPointConfiguration endpointConfig)
			throws TransportException {

		return null;

	}

	@Override
	public boolean isServiceTypeSupported(BindingTypeInfo bindingTypeInfo) {

		BindingTypeInfo.BindingTypeEnum bindingTypeEnum = null;
		BindingTypeInfo.MessageTypeEnum reqMsgType = null;
		BindingTypeInfo.MessageTypeEnum resMsgType = null;

		try {

			bindingTypeEnum = bindingTypeInfo.getType();
			reqMsgType = bindingTypeInfo.getRequestMessageType();
			resMsgType = bindingTypeInfo.getResponseMessageType();
			
			if (bindingTypeEnum.equals(BindingTypeInfo.BindingTypeEnum.MIXED)) {
				
				if (reqMsgType != null) {
					
					return !reqMsgType.equals(BindingTypeInfo.MessageTypeEnum.XML) &&
							!reqMsgType.equals(BindingTypeInfo.MessageTypeEnum.MFL) &&
							!reqMsgType.equals(BindingTypeInfo.MessageTypeEnum.JAVA) &&
							resMsgType == null;
					
				}
				
			}
			
			return !bindingTypeEnum.equals(BindingTypeInfo.BindingTypeEnum.SOAP) &&
					!bindingTypeEnum.equals(BindingTypeInfo.BindingTypeEnum.ABSTRACT_SOAP) &&
					!bindingTypeEnum.equals(BindingTypeInfo.BindingTypeEnum.ABSTRACT_XML) &&
					!bindingTypeEnum.equals(BindingTypeInfo.BindingTypeEnum.XML) &&
					!bindingTypeEnum.equals(BindingTypeInfo.BindingTypeEnum.REST);

		} catch (Exception ex) {
			
			KafkaTransportLogger.error("Error while detecting the service types supported.", ex);
			
		}

		return false;
	}

	@Override
	public TransportEditField[] updateEditPage(TransportEditField[] fields,
			String name) throws TransportException {
		
		return fields;

	}

	@Override
	public TransportUIError[] validateMainForm(TransportEditField[] fields) {
		
		List<TransportUIError> errors = new ArrayList<TransportUIError>();
		Map<String, TransportUIFactory.TransportUIObject> map = TransportEditField.getObjectMap(fields);
		
		String endpoints = null;
		
		if (uiContext.isProxy()) {
			
			endpoints = TransportUIFactory.getStringValue(map, PARAM_URI);
			
			if (endpoints == null || endpoints.length() == 0) {
				
				errors.add(new TransportUIError(PARAM_URI, bundle.getString("ENDPOINT_INFO_MISSING")));
				
			}
			
			String[] _endpoints = endpoints.split(",");
			
			if (_endpoints == null || _endpoints.length == 0) {
				
				errors.add(new TransportUIError(PARAM_URI, bundle.getString("ENDPOINT_INFO_MISSING")));
				
			}
			
			for (String endpoint : _endpoints) {
				
				String[] parts = endpoint.split(":");
				
				if (parts == null || parts.length < 2) {
					
					errors.add(new TransportUIError(PARAM_URI, bundle.getString("ENDPOINT_INCORRECT")));
					
				}
				
			}
			
		} else {
			
			String loadBalacing = TransportUIFactory.getStringValue(map, PARAM_LOAD_BALANCING);
			
			if (!loadBalacing.equals("4")) {
				
				errors.add(new TransportUIError(PARAM_LOAD_BALANCING,
						bundle.getString("INVALID_LOAD_BALANCING")));
				
			}
			
			List<String[]> uris = TransportUIFactory.getStringValues(map, PARAM_URI);
			
			for (String[] uriStr : uris) {

				for (String _endpointUri : uriStr) {
					
					String[] _endpoints = _endpointUri.split(",");
					
					if (_endpoints == null || _endpoints.length == 0) {
						
						errors.add(new TransportUIError(PARAM_URI, bundle.getString("ENDPOINT_INFO_MISSING")));
						
					}
					
					for (String endpoint : _endpoints) {
						
						String[] parts = endpoint.split(":");
						
						if (parts == null || parts.length < 2) {
							
							errors.add(new TransportUIError(PARAM_URI, bundle.getString("ENDPOINT_INCORRECT")));
							
						}
						
					}
					
				}

			}
			
		}
		
		return errors.isEmpty() ? null : errors.toArray(
				new TransportUIError[errors.size()]);
		
	}
	
	@Override
	public TransportUIError[] validateProviderSpecificForm(
			TransportEditField[] fields) {
		
		List<TransportUIError> errors = new ArrayList<TransportUIError>();
		Map<String, TransportUIFactory.TransportUIObject> map = TransportEditField.getObjectMap(fields);
		
		if (uiContext.isProxy()) {
			
			int consumerThreads = TransportUIFactory.getIntValue(map, "consumer-threads");
			
			if (consumerThreads <= 0) {
				
				errors.add(new TransportUIError("consumer-threads", bundle.getString("NUMBER_THREADS_INVALID")));
				
			}
			
		} else {
			
			int requestTimeoutMs = TransportUIFactory.getIntValue(map, "timeout-ms");
			
			if (requestTimeoutMs < 1) {
				
				errors.add(new TransportUIError("timeout-ms", bundle.getString("TIMEOUT_INVALID")));
				
			}
			
		}
		
		return errors.isEmpty() ? null : errors.toArray(
				new TransportUIError[errors.size()]);
		
	}
	
}