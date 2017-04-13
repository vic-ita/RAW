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
package raw.dht.webGui;

import com.sun.net.httpserver.HttpHandler;

/**
 * {@link HttpHandler} interface extension 
 * for RAW usage.
 * 
 * @author vic
 *
 */
public interface WebGuiHandler extends HttpHandler {

	static final int HTTP_OK_STATUS = 200;

	
	/**
	 * @return the context to be set for the server in conjunction to this {@link HttpHandler}
	 */
	public String getContext();
	
	/**
	 * @return the title associated with this hander page
	 */
	public String getTitle();

}
