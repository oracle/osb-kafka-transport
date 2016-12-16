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

import java.net.URI;
import java.util.Map;
import java.util.UUID;

import org.apache.xmlbeans.XmlObject;

import com.bea.wli.sb.sources.Source;
import com.bea.wli.sb.transports.InboundTransportMessageContext;
import com.bea.wli.sb.transports.RequestMetaData;
import com.bea.wli.sb.transports.ResponseMetaData;
import com.bea.wli.sb.transports.TransportEndPoint;
import com.bea.wli.sb.transports.TransportException;
import com.bea.wli.sb.transports.TransportOptions;

/**
 * @author Ricardo Ferreira
 */
public class KafkaInboundMessageContext implements
		InboundTransportMessageContext {

	@SuppressWarnings("rawtypes")
	private RequestMetaData requestMetadata;
	private Source requestPayload;
	private KafkaEndpoint endpoint;

	@SuppressWarnings("rawtypes")
	public KafkaInboundMessageContext(KafkaEndpoint endpoint,
			RequestMetaData requestMetadata, Source requestPayload) {

		this.endpoint = endpoint;
		this.requestMetadata = requestMetadata;
		this.requestPayload = requestPayload;

	}

	public KafkaInboundMessageContext(KafkaEndpoint endpoint,
			String messageKey, int partition, long offset,
			Source requestPayload) throws TransportException {

		this.endpoint = endpoint;
		this.requestPayload = requestPayload;
		this.requestMetadata = new KafkaRequestMetadata();
		
		((KafkaRequestHeaders) requestMetadata.getHeaders()).setMessageKey(messageKey);
		((KafkaRequestHeaders) requestMetadata.getHeaders()).setPartition(partition);
		((KafkaRequestHeaders) requestMetadata.getHeaders()).setOffset(offset);
		
	}
	
	public KafkaInboundMessageContext(KafkaEndpoint endpoint,
			Map<String, Object> headers, Source requestPayload)
					throws TransportException {
		
		this.endpoint = endpoint;
		this.requestPayload = requestPayload;
		this.requestMetadata = new KafkaRequestMetadata();
		
		if (headers.containsKey(KafkaConstants.MESSAGE_KEY)) {
			
			String messageKey = (String) headers.get(KafkaConstants.MESSAGE_KEY);
			((KafkaRequestHeaders) requestMetadata.getHeaders()).setMessageKey(messageKey);
			
		}
		
		if (headers.containsKey(KafkaConstants.PARTITION)) {
			
			int partition = (Integer) headers.get(KafkaConstants.PARTITION);
			((KafkaRequestHeaders) requestMetadata.getHeaders()).setPartition(partition);
			
		}
		
		if (headers.containsKey(KafkaConstants.OFFSET)) {
			
			long offset = (Long) headers.get(KafkaConstants.OFFSET);
			((KafkaRequestHeaders) requestMetadata.getHeaders()).setOffset(offset);
			
		}
		
	}

	@Override
	public TransportEndPoint getEndPoint() throws TransportException {

		return endpoint;

	}

	@Override
	public Source getRequestPayload() throws TransportException {

		return requestPayload;

	}

	@Override
	public String getMessageId() {

		return UUID.randomUUID().toString();

	}

	@Override
	public URI getURI() {

		return endpoint.getURI()[0];

	}

	@Override
	@SuppressWarnings("rawtypes")
	public RequestMetaData getRequestMetaData() throws TransportException {

		return requestMetadata;

	}

	@Override
	@SuppressWarnings("rawtypes")
	public ResponseMetaData createResponseMetaData() throws TransportException {

		return null;

	}

	@Override
	@SuppressWarnings("rawtypes")
	public ResponseMetaData createResponseMetaData(XmlObject xmlData)
			throws TransportException {

		return null;

	}

	@Override
	@SuppressWarnings("rawtypes")
	public void setResponseMetaData(ResponseMetaData responseMetadata)
			throws TransportException {
	}

	@Override
	public void setResponsePayload(Source responsePayload)
			throws TransportException {
	}
	
	@Override
	public void close(TransportOptions transportOptions) {
	}

}