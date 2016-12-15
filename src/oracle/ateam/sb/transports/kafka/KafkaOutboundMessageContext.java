/*
** Copyright © 2016 Oracle America, Inc.  All rights reserved.
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

import java.net.URI;
import java.util.UUID;

import com.bea.wli.sb.sources.Source;
import com.bea.wli.sb.transports.OutboundTransportMessageContext;
import com.bea.wli.sb.transports.ResponseMetaData;
import com.bea.wli.sb.transports.TransportException;

/**
 * @author Ricardo Ferreira
 */
public class KafkaOutboundMessageContext implements
		OutboundTransportMessageContext {

	private URI uri;
	@SuppressWarnings("rawtypes")
	private ResponseMetaData responseMetadata;

	public KafkaOutboundMessageContext(URI uri) throws TransportException {

		this.uri = uri;
		this.responseMetadata = new KafkaResponseMetadata();

	}

	public KafkaOutboundMessageContext(URI uri, String topicName,
			int partition, long offset) throws TransportException {

		this(uri);
		
		((KafkaResponseHeaders) responseMetadata.getHeaders()).setTopicName(topicName);
		((KafkaResponseHeaders) responseMetadata.getHeaders()).setPartition(partition);
		((KafkaResponseHeaders) responseMetadata.getHeaders()).setOffset(offset);

	}

	@Override
	public String getMessageId() {

		return UUID.randomUUID().toString();

	}

	@Override
	public URI getURI() {

		return uri;

	}

	@Override
	@SuppressWarnings("rawtypes")
	public ResponseMetaData getResponseMetaData() throws TransportException {

		return responseMetadata;

	}

	@Override
	public Source getResponsePayload() throws TransportException {

		return null;

	}

}