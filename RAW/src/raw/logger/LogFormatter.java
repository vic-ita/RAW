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
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * Convenience extension to {@link SimpleFormatter} class.
 * 
 * @author vic
 *
 */
public class LogFormatter extends SimpleFormatter{
	
	private boolean timestamp;
	private boolean sourceNames;
	
	private String longPad  = "                                   ";
	private String shortPad = "               ";
	
	
	public LogFormatter(boolean timestamp) {
		this(timestamp, true);
	}
	
	public LogFormatter(boolean timestamp, boolean sourceNames) {
		super();
		
		this.timestamp = timestamp;
		this.sourceNames = sourceNames;
	}

	@Override
	public String format(LogRecord record) {
		StringBuilder entry = new StringBuilder();
		
		if(timestamp){
			Date date = new Date(record.getMillis());
			
			entry.append(date.toString());
			entry.append(" - ");
		}
		
		if(sourceNames){
			String source = record.getSourceClassName()+"."+record.getSourceMethodName();
			if(source.length() <= longPad.length() ){
				entry.append(source+longPad.substring(source.length()));				
			}
			else{
				entry.append(source);
			}
			entry.append(" - ");
		}
		
		String tag;
		if(record.getLevel().equals(Level.INFO)){
			tag = "INFO:";
			entry.append(tag+shortPad.substring(tag.length()));
		} else if (record.getLevel().equals(Level.FINER)) {
			tag = "DEBUG-VERBOSE:";
			entry.append(tag+shortPad.substring(tag.length()));
		} else if (record.getLevel().equals(Level.FINE)){
			tag = "DEBUG:";
			entry.append(tag+shortPad.substring(tag.length()));
		} else if (record.getLevel().equals(Level.WARNING)){
			tag = "WARNING:";
			entry.append(tag+shortPad.substring(tag.length()));
		}					
		
		entry.append(record.getMessage());
		
		entry.append("\n");
		
		return entry.toString();
	}
	
}