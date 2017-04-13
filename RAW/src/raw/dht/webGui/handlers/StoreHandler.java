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
/**
 * 
 */
package raw.dht.webGui.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;

import raw.dht.DhtCore;
import raw.dht.DhtKey;
import raw.dht.DhtValue;
import raw.dht.implementations.DefaultDhtCore;
import raw.dht.implementations.DefaultDhtKey;
import raw.dht.implementations.DefaultDhtValue;
import raw.dht.webGui.HtmlBuilder;
import raw.dht.webGui.WebGuiHandler;

/**
 * @author vic
 *
 */
public class StoreHandler implements WebGuiHandler {
	
	private final String myContext = "/store";
	private final String title = "DHT - Store Page";
	
	private final String storeKeyStringKey = "storeKeyString";
	private String storeKeyString = null;
	private final String storeValueStringKey = "storeValueString";
	private String storeValueString = null;
	private final String storeAnnotationsStringKey = "storeAnnotationsString";
	private String storeAnnotationsString = null;

	/* (non-Javadoc)
	 * @see com.sun.net.httpserver.HttpHandler#handle(com.sun.net.httpserver.HttpExchange)
	 */
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		if(exchange.getRequestMethod().equalsIgnoreCase("post")){
			managePostRequest(exchange);
		} else {
			manageGetRequest(exchange);
		}
	}
	
	private void manageGetRequest(HttpExchange exchange) throws IOException {
		String body = createUpperForm().build();
		exchange.sendResponseHeaders(HTTP_OK_STATUS, body.getBytes().length);
		OutputStream os = exchange.getResponseBody();
		os.write(body.getBytes());
		os.close();
	}

	private void managePostRequest(HttpExchange exchange) throws IOException {
		storeKeyString = null;
		storeValueString = null;
		storeAnnotationsString = null;
		
		InputStreamReader isr = new InputStreamReader(exchange.getRequestBody());
		BufferedReader br = new BufferedReader(isr);
		String query = br.readLine();
		
		HtmlBuilder builder = createUpperForm();
		
		try {
			if(query != null && !query.equals("")){
				String[] pairs = query.split("[&]");
				
				for (String pair : pairs) {
					String[] param = pair.split("[=]");
					if(param[0].equals(storeKeyStringKey)){
						if(param.length > 1){
							storeKeyString= param[1];							
						}
					} else if(param[0].equals(storeValueStringKey)){
						if(param.length > 1){
							storeValueString = param[1];							
						}
					} else if(param[0].equals(storeAnnotationsStringKey)){
						if(param.length > 1){
							storeAnnotationsString = param[1];							
						}
					}
				}
				
				builder.openH2().text("Store results").closeH2();
				if(storeKeyString != null && storeValueString != null){
					DhtKey key = new DefaultDhtKey(storeKeyString);
					
					DhtValue value;
					if(storeAnnotationsString != null){
						value = new DefaultDhtValue(storeValueString, storeAnnotationsString);
					} else {
						value = new DefaultDhtValue(storeValueString);
					}
					
					DhtCore dht = DefaultDhtCore.getCore();
					boolean result = dht.store(key, value);
					builder.text("Attempted to store key: \"")
						.openItalic().text(storeKeyString).closeItalic()
						.text("\" with value: \"").openItalic().text(storeValueString).closeItalic().text("\"").br().br()
						.text("DHT core store op result to be a ").openBold();
						if(result){
							builder.text("success");
						} else {
							builder.text("failure");
						}
					builder.closeBold();
				} else {
					builder.text("Cannot store empty strings as key or value. Store op aborted.");
				}
			}
		} catch (Exception e) {
			builder.hr().hr().hr().openH5().text("Exception!").closeH5();
			StackTraceElement[] stacktrace = e.getStackTrace();
			builder.text(e.toString()).br().br();
			for(StackTraceElement elem : stacktrace){
				builder.text(elem.toString()).br();
			}		
		}
		
		String body = builder.build();
		exchange.sendResponseHeaders(HTTP_OK_STATUS, body.getBytes().length);
		OutputStream os = exchange.getResponseBody();
		os.write(body.getBytes());
		os.close();
	}

	private HtmlBuilder createUpperForm() {
		HtmlBuilder builder = new HtmlBuilder(title);
		builder.openH1().text("Dht search page").closeH1().hr()
			.openH2().text("Search form:").closeH2()
			.openForm("."+myContext, true)
			.inputTextForm(storeKeyStringKey, "Key to be stored").br()
			.inputTextForm(storeValueStringKey, "Value to be stored").br()
			.inputTextForm(storeAnnotationsStringKey, "Annotations")
			.submitButton("Store").closeForm().hr();
		return builder;
	}

	/* (non-Javadoc)
	 * @see raw.dht.webGui.WebGuiHandler#getContext()
	 */
	@Override
	public String getContext() {
		return myContext;
	}

	/* (non-Javadoc)
	 * @see raw.dht.webGui.WebGuiHandler#getTitle()
	 */
	@Override
	public String getTitle() {
		return title;
	}

}
