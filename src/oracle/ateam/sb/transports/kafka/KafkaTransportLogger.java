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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * 
 * This helper class is intended to be a placeholder only.
 * One might want to integrate with WebLogic's logging
 * system instead. Therefore, this class is the perfect
 * API hook to link that up since it is used throughout
 * the implemented code.
 * 
 * @author Ricardo Ferreira
 */
public class KafkaTransportLogger {
	
	private static final Map<Level, String> cache =
			new ConcurrentHashMap<Level, String>();
	
	private static final SimpleDateFormat sdf =
			new SimpleDateFormat("MMM dd, yyyy h:mm:ss aaa z");
	
	private static final String subsystem =
			KafkaTransportLogger.class.getPackage().getName();
	
	private static String getName(Level level) {
		
		String name = cache.get(level);
		
		if (name == null) {
			
			name = level.getName();
			String firstLetter = name.substring(0, 1).toUpperCase();
			String rest = name.substring(1, name.length()).toLowerCase();
			name = firstLetter + rest;
			
			cache.put(level, name);
			
		}
		
		return name;
		
	}
	
	public static void log(Level level, String message) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("<").append(sdf.format(new Date())).append("> ");
		sb.append("<").append(getName(level)).append("> ");
		sb.append("<").append(subsystem).append("> ");
		sb.append("<OSB-000000> ");
		sb.append("<").append(message).append(">");
		
		System.out.println(sb.toString());
		
	}
	
	public static void error(String message, Throwable throwable) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("<").append(sdf.format(new Date())).append("> ");
		sb.append("<").append("Error").append("> ");
		sb.append("<").append(subsystem).append("> ");
		sb.append("<OSB-000000> ");
		sb.append("<").append(message).append(">");
		
		System.out.println(sb.toString());
		
		if (throwable != null) {
			
			throwable.printStackTrace();
			
		}
		
	}
	
}