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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlObject;

import com.bea.wli.config.Ref;
import com.bea.wli.config.env.NonQualifiedEnvValue;
import com.bea.wli.sb.transports.EndPointConfiguration;
import com.bea.wli.sb.transports.EndPointOperations;
import com.bea.wli.sb.transports.EndPointOperations.CommonOperation;
import com.bea.wli.sb.transports.EndPointOperations.Create;
import com.bea.wli.sb.transports.EndPointOperations.Delete;
import com.bea.wli.sb.transports.EndPointOperations.Resume;
import com.bea.wli.sb.transports.EndPointOperations.Suspend;
import com.bea.wli.sb.transports.EndPointOperations.Update;
import com.bea.wli.sb.transports.ProviderConfigurationDocument;
import com.bea.wli.sb.transports.ServiceTransportSender;
import com.bea.wli.sb.transports.TransportEndPoint;
import com.bea.wli.sb.transports.TransportException;
import com.bea.wli.sb.transports.TransportManagerHelper;
import com.bea.wli.sb.transports.TransportOptions;
import com.bea.wli.sb.transports.TransportProvider;
import com.bea.wli.sb.transports.TransportProviderConfiguration;
import com.bea.wli.sb.transports.TransportSendListener;
import com.bea.wli.sb.transports.TransportSender;
import com.bea.wli.sb.transports.TransportValidationContext;
import com.bea.wli.sb.transports.ui.TransportUIBinding;
import com.bea.wli.sb.transports.ui.TransportUIContext;

/**
 * @author Ricardo Ferreira
 */
public class KafkaTransportProvider implements TransportProvider {
	
	private Timer startupTimer;
	private Map<Ref, KafkaEndpoint> endpoints;

	private KafkaTransportProvider() {
		
		endpoints = new ConcurrentHashMap<Ref, KafkaEndpoint>();

	}
	
	private void scheduleEndpointsStartup() {
		
		if (startupTimer == null) {
			
			int checkInterval = KafkaUtil.getCheckInterval();
			
			startupTimer = new Timer("Kafka Endpoints Startup");
			startupTimer.schedule(new EndpointsStartupTask(),
					checkInterval, checkInterval);
			
		}
		
	}
	
	public TransportEndPoint createEndPoint(Create context)
			throws TransportException {
		
		Ref serviceRef = context.getRef();
		
		KafkaEndpoint endpoint = new KafkaEndpoint(this,
				serviceRef, context.getEndPointConfiguration());
		
		endpoints.put(serviceRef, endpoint);
		
		return endpoint;
		
	}

	public TransportEndPoint updateEndPoint(Update context)
			throws TransportException {
		
		deleteEndPoint(EndPointOperations.getDeleteFromUpdate(context));
		return createEndPoint(EndPointOperations.getCreateFromUpdate(context));

	}

	public void deleteEndPoint(Delete context) throws TransportException {
		
		Ref serviceRef = context.getRef();
		KafkaEndpoint endpoint = endpoints.get(serviceRef);
		
		if (endpoint != null) {
			
			endpoint.stop();
			endpoints.remove(serviceRef);
			
		}
		
	}

	public void suspendEndPoint(Suspend context) throws TransportException {
		
		// Nothing to do...
		
	}

	public void resumeEndPoint(Resume context) throws TransportException {
		
		// Nothing to do...
		
	}
	
	public void activationComplete(CommonOperation context) {
		
		Ref serviceRef = context.getRef();
		EndPointOperations.EndPointOperationTypeEnum type = context.getType();
		KafkaEndpoint endpoint = endpoints.get(serviceRef);
		
		if (TransportManagerHelper.isRuntimeEnabled() && endpoint != null) {
			
			if (EndPointOperations.EndPointOperationTypeEnum.CREATE.equals(type) ||
					EndPointOperations.EndPointOperationTypeEnum.UPDATE.equals(type) ||
					EndPointOperations.EndPointOperationTypeEnum.RESUME.equals(type)) {
				
				try {
					
					// Before start this endpoint, first we need
					// to make sure the server is running. This
					// verification is necessary because if the
					// endpoint is started during WebLogic bootstrap;
					// some of the Work Managers may be on shutdown
					// state, causing thread scheduling issues.
					
					if (KafkaUtil.isServerRunning()) {
						
						endpoint.start();
						
					} else {
						
						// Delay the endpoints start up until the
						// reaches the 'RUNNING' state. The method
						// below schedules a timer that keep polling
						// the server about it's state. The interval
						// between check's defaults to five seconds,
						// but it can be changed via the following
						// JVM property:
						
						// -Doracle.ateam.sb.transports.kafka.endpoint.startup.checkInterval
						
						// The timer is scheduled when the first
						// endpoint is created. Subsequent calls
						// to this method has no effect since the
						// timer has been created already.
						
						scheduleEndpointsStartup();
						
					}
					
				} catch (Exception ex) {
					
					KafkaTransportLogger.error("Error while starting a Kafka endpoint.", ex);
					
				}
				
			} else if (EndPointOperations.EndPointOperationTypeEnum.SUSPEND.equals(type)) {
				
				try {
					
					endpoint.stop();
					
				} catch (TransportException te) {
					
					KafkaTransportLogger.error("Error while stopping a Kafka endpoint.", te);
					
				}
				
			}
			
		}
		
	}

	public TransportEndPoint getEndPoint(Ref serviceRef) throws TransportException {

		return endpoints.get(serviceRef);

	}

	public Collection<KafkaEndpoint> getEndPoints()
			throws TransportException {

		return Collections.unmodifiableCollection(endpoints.values());

	}
	
	public TransportProviderConfiguration getProviderConfiguration()
			throws TransportException {

		URL configUrl = null;
		ProviderConfigurationDocument providerConfigDoc = null;
		TransportProviderConfiguration providerConfiguration = null;

		try {

			configUrl = this.getClass().getClassLoader().getResource("kafka-config.xml");
			providerConfigDoc = ProviderConfigurationDocument.Factory.parse(configUrl);
			providerConfiguration = providerConfigDoc.getProviderConfiguration();
			
		} catch (Exception ex) {

			throw new TransportException(ex);

		}
		
		return providerConfiguration;

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map getBusinessServicePropertiesForProxy(Ref serviceRef)
			throws TransportException {

		throw new UnsupportedOperationException();

	}

	public XmlObject getProviderSpecificConfiguration(Ref serviceRef,
			Map<String, String> props) throws TransportException {

		throw new UnsupportedOperationException();

	}

	public SchemaType getEndPointConfigurationSchemaType()
			throws TransportException {

		return KafkaEndPointConfiguration.type;

	}

	public SchemaType getRequestMetaDataSchemaType() throws TransportException {

		return KafkaRequestMetaDataXML.type;

	}

	public SchemaType getRequestHeadersSchemaType() throws TransportException {

		return KafkaRequestHeadersXML.type;

	}

	public SchemaType getResponseMetaDataSchemaType() throws TransportException {

		return KafkaResponseMetaDataXML.type;

	}

	public SchemaType getResponseHeadersSchemaType() throws TransportException {

		return KafkaResponseHeadersXML.type;

	}

	public Collection<NonQualifiedEnvValue> getEnvValues(Ref serviceRef,
			EndPointConfiguration endpointConfiguration)
			throws TransportException {

		return new ArrayList<NonQualifiedEnvValue>();

	}

	public void setEnvValues(Ref serviceRef,
			EndPointConfiguration endpointConfiguration,
			Collection<NonQualifiedEnvValue> envValues)
			throws TransportException {

	}

	public void setExternalReferences(Map<Ref, Ref> props,
			EndPointConfiguration endpointConfiguration)
			throws TransportException {

	}

	public Collection<Ref> getExternalReferences(
			EndPointConfiguration endpointConfiguration)
			throws TransportException {

		return new ArrayList<Ref>();

	}

	public String getId() {

		return KafkaConstants.KAFKA_PROVIDER_ID;

	}

	public TransportUIBinding getUIBinding(TransportUIContext uiContext)
			throws TransportException {
		
		return new KafkaUIBinding(uiContext);

	}

	public void sendMessageAsync(TransportSender transportSender,
			TransportSendListener listener, TransportOptions options)
			throws TransportException {
		
		if (transportSender instanceof ServiceTransportSender) {

			ServiceTransportSender sts = (ServiceTransportSender) transportSender;
			KafkaEndpoint endpoint = (KafkaEndpoint) sts.getEndPoint();
			endpoint.sendMessageAsync(transportSender, listener, options);
			
		}
		
	}

	public void shutdown() throws TransportException {
		
		if (TransportManagerHelper.isRuntimeEnabled()) {
			
			Collection<KafkaEndpoint> endpoints = getEndPoints();
			
			if (endpoints != null && !endpoints.isEmpty()) {
				
				for (KafkaEndpoint endpoint : endpoints) {
					
					endpoint.stop();
					
				}
				
			}
			
		}
		
	}

	public void validateEndPointConfiguration(TransportValidationContext tvc) {
	}
	
	private class EndpointsStartupTask extends TimerTask {
		
		private long firstExecution = -1;

		@Override
		public void run() {
			
			long elapsedTime = 0;
			Collection<KafkaEndpoint> endpoints = null;
			
			try {
				
				if (firstExecution == -1) {
					
					firstExecution = System.currentTimeMillis();
					
				}
				
				if (KafkaUtil.isServerRunning()) {
					
					endpoints = getEndPoints();
					
					for (KafkaEndpoint endpoint : endpoints) {
						
						endpoint.start();
						
					}
					
					// Job done here. There is no need anymore
					// to continue with the polling process on
					// the server.
					
					startupTimer.cancel();
					
				}
				
				// This thread cannot keep running for ever.
				// It's assumed that eventually the server will
				// finish it's bootstrap and change it's state to
				// 'RUNNING'. If for some reason this does not
				// occur or takes too long; we need to ensure that
				// this timer thread will timeout. Default timeout
				// is five minutes, but it can be changed via the
				// following JVM property:
				
				// -Doracle.ateam.sb.transports.kafka.endpoint.startup.timeout
				
				elapsedTime = System.currentTimeMillis() - firstExecution;
				
				if (elapsedTime >= KafkaUtil.getStartupTimeout()) {
					
					startupTimer.cancel();
					
				}
				
			} catch (Exception ex) {
				
				KafkaTransportLogger.error(ex.getMessage(), ex);
				
			}
			
		}
		
	}
	
	private static KafkaTransportProvider instance;

	public static KafkaTransportProvider getInstance() {

		if (instance == null) {

			instance = new KafkaTransportProvider();

		}

		return instance;

	}

}