/*******************************************************************************
 *  Copyright 2017 Vincenzo-Maria Cappelleri <vincenzo.cappelleri@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package raw.logger;

import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import javax.json.Json;
import javax.json.JsonObject;

/**
 * As {@link LogFormatter}. But this class create a json object
 * per line of log.
 * 
 * @author vic
 *
 */
public class LogJsonFormatter extends SimpleFormatter {

	/* (non-Javadoc)
	 * @see java.util.logging.SimpleFormatter#format(java.util.logging.LogRecord)
	 */
	@Override
	public synchronized String format(LogRecord record) {
		Date date = new Date(record.getMillis());
		String timestamp = date.toString();
		String method = record.getSourceMethodName();
		String classCanonicalName = record.getSourceClassName();
		String level = Log.convertJdkLevel(record.getLevel()).getValue();
		String text = record.getMessage();
		
		String[] tokens = classCanonicalName.split("\\.");
		
		String module = tokens[1].toUpperCase();
		String className = tokens[tokens.length-1];
		
		JsonObject json = Json.createObjectBuilder().
				add("timestamp", timestamp).
				add("logLevel", level).
				add("classCanonicalName", classCanonicalName).
				add("module", module).
				add("class", className).
				add("method", method).
				add("msg", text).
				build();
		String n = System.getProperty("line.separator");
		return json.toString()+n;
	}

}
