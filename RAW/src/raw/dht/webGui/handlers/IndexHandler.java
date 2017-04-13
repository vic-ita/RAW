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

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;

import raw.dht.webGui.HtmlBuilder;
import raw.dht.webGui.WebGuiHandler;

/**
 * @author vic
 *
 */
public class IndexHandler implements WebGuiHandler {
	
	private final String myContext = "/index";
	private final String title = "DHT - Main Page";

	/* (non-Javadoc)
	 * @see com.sun.net.httpserver.HttpHandler#handle(com.sun.net.httpserver.HttpExchange)
	 */
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String body = createResponseBody();
		exchange.sendResponseHeaders(HTTP_OK_STATUS, body.getBytes().length);
		OutputStream os = exchange.getResponseBody();
		os.write(body.getBytes());
		os.close();
	}

	/* (non-Javadoc)
	 * @see raw.dht.webGui.WebGuiHandler#getContext()
	 */
	@Override
	public String getContext() {
		return myContext;
	}
	
	private String createResponseBody(){
		NodeInfoHandler node = new NodeInfoHandler();
		StoreHandler store = new StoreHandler();
		SearchHandler search = new SearchHandler();
		KeysHandler keys = new KeysHandler();
		HtmlBuilder html = new HtmlBuilder(title);
		html.openH1().text("DHT WEB GUI - main page").closeH1()
			.text("Please note that this webserfice is a ")
			.openBold().text("potentially severe security breach").closeBold()
			.text(" for the whole DHT network. Consider the usage of this tool carefully.")
			.openH2().text("Options").closeH2()
			.text("Node infos: ").linkToNewTab("."+node.getContext(), node.getTitle()).br()
			.text("Store form: ").linkToNewTab("."+store.getContext(), store.getTitle()).br()
			.text("Search form: ").linkToNewTab("."+search.getContext(), search.getTitle()).br()
			.text("Stored keys status: ").linkToNewTab("."+keys.getContext(), keys.getTitle());
		return html.build();
	}

	@Override
	public String getTitle() {
		return title;
	}

}
