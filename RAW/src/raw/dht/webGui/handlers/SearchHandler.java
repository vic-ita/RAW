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
import java.util.Collection;

import raw.dht.DhtCore;
import raw.dht.DhtID;
import raw.dht.DhtKey;
import raw.dht.DhtNodeExtended;
import raw.dht.DhtValue;
import raw.dht.implementations.DefaultDhtCore;
import raw.dht.implementations.DefaultDhtID;
import raw.dht.implementations.DefaultDhtKey;
import raw.dht.webGui.HtmlBuilder;
import raw.dht.webGui.WebGuiHandler;

import com.sun.net.httpserver.HttpExchange;

/**
 * @author vic
 *
 */
public class SearchHandler implements WebGuiHandler {
	
	private final String myContext = "/search";
	private final String title = "DHT - Search Page";
	
	private String searchString;
	private final String searchStringKey = "searchText";
	
	private boolean idSearch;
	private boolean idSearchLocal;
	private final String idSearchKey = "searchId";
	private String searchForValue ="sfv";
	private String searchForLocalId ="sfli";
	private String searchForNetworkId ="sfni";

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
	
	private void managePostRequest(HttpExchange exchange) throws IOException{
		InputStreamReader isr = new InputStreamReader(exchange.getRequestBody());
		BufferedReader br = new BufferedReader(isr);
		String query = br.readLine();
		
		HtmlBuilder builder = createUpperForm();
		
		try {
			if(query != null && !query.equals("")){
				String[] pairs = query.split("[&]");
				
				for (String pair : pairs) {
					String[] param = pair.split("[=]");				
					if(param[0].equals(searchStringKey)){
						if(param.length > 1){
							searchString = param[1];							
						} else {
							searchString = "";
						}
					} else if (param[0].equals(idSearchKey)){
						if(param.length > 1){
							idSearch = Boolean.valueOf(param[1]);
							if(param[1].equals(searchForValue)){
								idSearch = false;
							} else {
								idSearch = true;
								if(param[1].equals(searchForLocalId)){
									idSearchLocal = true;
								} else {
									idSearchLocal = false;
								}
							}
						} else {
							idSearch = false;
						}
					}
				}
				
				builder.openH2().text("Search results").closeH2().br()
					.openBold().text("Searched for: ").closeBold().text("\""+searchString+"\"").br()
					.openBold().text("As: ").closeBold();
				if(idSearch){
					builder.text("dht ID hex string. Lookup was performed ").openBold();
					if(idSearchLocal){
						builder.text("locally");
					} else {
						builder.text("on the network");
					}
					builder.closeBold().text(".");
				} else {
					builder.text("dht key string.");
				}
				builder.br().br().br();
			} 
			
			DhtCore dht = DefaultDhtCore.getCore();
			
			if(idSearch){
				DhtID id = new DefaultDhtID(searchString);
				Collection<DhtNodeExtended> results;
				if(idSearchLocal){
					results = dht.lookupInTable(id);					
				} else {
					results = dht.lookup(id);
				}
				if(results.size() == 0){
					builder.openBold().text("No node found").closeBold();
				} else {
					builder.openBold().text("List of nodes close to given ID:").closeBold().br().br();
					for(DhtNodeExtended node : results){
						builder.text(node.toString()).br();
					}
				}
			} else {
				DhtKey key = new DefaultDhtKey(searchString);
				Collection<DhtValue> results = dht.search(key);
				if(results == null || results.size() == 0){
					builder.openBold().text("No value found!").closeBold();
				} else {
					builder.openBold().text("List of values found for given key:").closeBold().br().br();
					for(DhtValue value : results){
						builder.text(value.toString()).br();
					}
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
	
	private void manageGetRequest(HttpExchange exchange) throws IOException{
		String body = createUpperForm().build();
		exchange.sendResponseHeaders(HTTP_OK_STATUS, body.getBytes().length);
		OutputStream os = exchange.getResponseBody();
		os.write(body.getBytes());
		os.close();
	}
	
	private HtmlBuilder createUpperForm() {
		HtmlBuilder builder = new HtmlBuilder(title);
		builder.openH1().text("Dht search page").closeH1().hr()
			.openH2().text("Search form:").closeH2()
			.openForm("."+myContext, true).inputTextForm(searchStringKey, "Search key")
			.space().text("as").space().space()
			.inputRadioForm(idSearchKey, "key", searchForValue, true).inputRadioForm(idSearchKey, "dht ID (<b>network</b> lookup)", searchForNetworkId).inputRadioForm(idSearchKey, "dht ID (<b>local</b> lookup)", searchForLocalId)
			.submitButton("Search").closeForm().hr();
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
