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

import org.apache.xmlbeans.XmlObject;

import com.bea.alsb.platform.PlatformFactory;
import com.bea.alsb.platform.ServerConfiguration;
import com.bea.wli.sb.sources.ByteArraySource;
import com.bea.wli.sb.sources.Source;
import com.bea.wli.sb.sources.StringSource;
import com.bea.wli.sb.sources.TransformException;
import com.bea.wli.sb.sources.TransformOptions;
import com.bea.wli.sb.sources.Transformer;
import com.bea.wli.sb.transports.ALSBTransportManager;
import com.bea.wli.sb.transports.EndPointConfiguration;
import com.bea.wli.sb.transports.TransportException;
import com.bea.wli.sb.transports.TransportManagerHelper;

/**
 * @author Ricardo Ferreira
 */
public class KafkaUtil {
	
	private static ServerConfiguration serverConfig;
	private static boolean printProperties = false;
	private static Transformer transformer = null;
	private static int startupTimeout = 300000;
	private static int checkInterval = 5000;
	
	static {
		
		String startupTimeoutProp = System.getProperty(KafkaConstants.STARTUP_TIMEOUT);
		
		if (startupTimeoutProp != null) {
			
			startupTimeout = Integer.parseInt(startupTimeoutProp);
			
		}
		
		String checkIntervalProp = System.getProperty(KafkaConstants.CHECK_INTERVAL);
		
		if (checkIntervalProp != null) {
			
			checkInterval = Integer.parseInt(checkIntervalProp);
			
			if (checkInterval > 60000) {
				
				// Any Service Bus start up should be measured in terms
				// of minutes, tops. Anything beyond that can be harmful
				// to the transport health since the idea is to have the
				// endpoints listening Kafka topics as soon the start up
				// finishes and WebLogic reaches the 'RUNNING' state.
				
				// In order to ensure the transport health, we set the
				// parameter back to its maximum, which is one minute.
				// That will ensure that no user (carrying good or bad
				// intentions) will hassle the bootstrap using longer
				// checks.
				
				checkInterval = 60000;
				
			}
			
		}
		
		String printPropsProp = System.getProperty(KafkaConstants.PRINT_PROPERTIES);
		
		if (printPropsProp != null) {
			
			printProperties = Boolean.parseBoolean(printPropsProp);
			
		}
		
	}
	
	public static int getStartupTimeout() {
		
		return startupTimeout;
		
	}
	
	public static int getCheckInterval() {
		
		return checkInterval;
		
	}
	
	public static boolean printProperties() {
		
		return printProperties;
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Source transform(Source payload, Class classType,
			String encoding) throws TransportException, TransformException {
		
		if (transformer == null) {
			
			transformer = ALSBTransportManager.getInstance().getTransformer();
			
		}
		
		TransformOptions options = new TransformOptions();
		options.setCharacterEncoding(encoding);
		
		return transformer.transform(payload, classType, options);
		
	}
	
	public static boolean isValidDispatchPolicy(String dispatchPolicy) {
		
		return dispatchPolicy != null && !dispatchPolicy.equals(
				KafkaConstants.DEFAULT_WORK_MANAGER);
		
	}
	
	public static void schedule(Runnable runnable, String dispatchPolicy)
			throws TransportException {
		
		TransportManagerHelper.schedule(runnable, dispatchPolicy);
		
	}
	
	public static KafkaEndPointConfiguration getConfig(
			EndPointConfiguration endpointConfig) throws TransportException {

		XmlObject xbean = endpointConfig.getProviderSpecific();

		if (xbean instanceof KafkaEndPointConfiguration) {

			return (KafkaEndPointConfiguration) xbean;

		} else {

			try {

				return KafkaEndPointConfiguration.Factory.parse(xbean.newInputStream());

			} catch (Exception ex) {

				throw new TransportException(ex);

			}

		}

	}
	
	public static Source getAsText(Source payload, String encoding)
			throws TransportException, TransformException {
		
		return transform(payload, StringSource.class, encoding);
		
	}
	
	public static Source getAsBinary(Source payload, String encoding)
			throws TransportException, TransformException {
		
		return transform(payload, ByteArraySource.class, encoding);
		
	}
	
	public static boolean isServerRunning() throws Exception {
		
		if (serverConfig == null) {
			
			serverConfig = PlatformFactory.get().getServerConfiguration();
			
		}
		
		return serverConfig.isRunning();
		
	}
	
	@SuppressWarnings("rawtypes")
	public static Class loadClass(String className) {
		
		Class classImpl = null;
		
		try {
			
			classImpl = Class.forName(className);
			
		} catch (ClassNotFoundException cnfe) {}
		
		return classImpl;
		
	}
	
}