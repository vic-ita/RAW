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

/**
 * The level priority (from higher to lower) as follows:
 * <br><br>
 * WARNING,<br>
 * INFO,<br>
 * DEBUG<br>
 * VERBOSE<br>
 * <br>
 * Moreover there is a convenience {@link Level} OFF which will
 * turn off every message and ALL which will set verbosity to the
 * maximum.
 * 
 * @author vic
 *
 */
public enum Level{
	OFF("OFF"),
	EXCEPTION("EXCEPTION"),
	WARNING("WARNING"),
	INFO("INFO"),
	DEBUG("DEBUG"),
	VERBOSE("DEBUG-VERBOSE"),
	ALL("ALL");
	
	private final String level;
	
	private Level(String level) {
		this.level = level;
	}
	
	public String getValue(){
		return level;
	}
	
	/**
	 * Convert a string to a {@link Level}. If
	 * the string is not a valid level then {@link Level#INFO}
	 * is returned by default.
	 * 
	 * @param level a string representation of a desired {@link Level}
	 * @return the requested {@link Level}, or {@link Level#INFO} if <code>level</code> parameter is not valid
	 */
	public static Level fromLevelString(String level){
		for(Level lvl : Level.values()){
			if(lvl.getValue().equals(level)){
				return lvl;
			}
		}
		return INFO;
	}
}