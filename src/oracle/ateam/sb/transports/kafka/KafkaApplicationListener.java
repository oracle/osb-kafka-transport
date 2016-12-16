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

import java.util.logging.Level;

import com.bea.wli.sb.transports.TransportManager;
import com.bea.wli.sb.transports.TransportManagerHelper;

import weblogic.application.ApplicationException;
import weblogic.application.ApplicationLifecycleEvent;
import weblogic.application.ApplicationLifecycleListener;

/**
 * @author Ricardo Ferreira
 */
public class KafkaApplicationListener extends ApplicationLifecycleListener {
	
	private boolean isDependenciesMissing() {
		
		boolean missing = true;
		
		try {
			
			// Checkings for Kafka Clients API...
			Class.forName("org.apache.kafka.clients.consumer.KafkaConsumer");
			Class.forName("org.apache.kafka.clients.producer.KafkaProducer");
			
			// Checkings for Third-party...
			Class.forName("org.slf4j.ILoggerFactory");
			
			missing = false;
			
		} catch (ClassNotFoundException cnfe) {}
		
		return missing;
		
	}
	
	public void preStart(ApplicationLifecycleEvent appLifecycleEvent)
			throws ApplicationException {
		
		TransportManager transportManager = null;
		
		try {
			
			if (isDependenciesMissing()) {
				
				KafkaTransportLogger.log(Level.WARNING, "Kafka transport " +
						"could not be registered due to missing libraries. " +
						"For using the Kafka transport, its libraries must " +
						"be available in the classpath.");
				
				return;
				
			}
			
			transportManager = TransportManagerHelper.getTransportManager();
			transportManager.registerProvider(KafkaTransportProvider.getInstance(), null);
			
		} catch (Exception ex) {
			
			throw new ApplicationException(ex);
			
		}

	}

}