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

import com.bea.wli.sb.services.BindingTypeInfo;

/**
 * @author Ricardo Ferreira
 */
public class KafkaConstants {
	
	public static final String DEFAULT_WORK_MANAGER = "default";
	
	public static final String KAFKA_PROVIDER_ID = "kafka";
	
	public static final String TEXT_REQUEST_TYPE =
			BindingTypeInfo.MessageTypeEnum.TEXT.toString();
	
	public static final String BINARY_REQUEST_TYPE =
			BindingTypeInfo.MessageTypeEnum.BINARY.toString();
	
	public static final String CHECK_INTERVAL =
			KafkaConstants.class.getPackage().getName() +
			".endpoint.startup.checkInterval";
	
	public static final String STARTUP_TIMEOUT =
			KafkaConstants.class.getPackage().getName() +
			".endpoint.startup.timeout";
	
	public static final String PRINT_PROPERTIES =
			KafkaConstants.class.getPackage().getName() +
			".endpoint.config.printProperties";
	
	public static final String MESSAGE_KEY = "message-key";
	public static final String PARTITION = "partition";
	public static final String OFFSET = "offset";
	
	public static final String EP_CONSUMER_COMMIT_ASYNC =
			"endpoint.consumer.commitAsync";
	
	public static final String ABNORMAL_CONFIRM_RECEIVED =
			"Abnormal confirmation received. Both record " +
			"metadata and exception objects came empty.";
	
}