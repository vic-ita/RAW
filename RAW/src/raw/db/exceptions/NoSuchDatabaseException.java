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
package raw.db.exceptions;

/**
 * Exception thrown when searching for a certain database
 * that is not hosted on the interrogated server.
 *  
 * @author vic
 *
 */
public class NoSuchDatabaseException extends Exception {

	/**
	 * random generated UID 
	 */
	private static final long serialVersionUID = -2737848288221473351L;
	
	public NoSuchDatabaseException(String msg) {
		super(msg);
	}

}
