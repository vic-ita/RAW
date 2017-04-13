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
import java.util.Collection;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.sun.net.httpserver.HttpExchange;

import raw.dht.DhtKey;
import raw.dht.DhtKeyHolder;
import raw.dht.DhtValue;
import raw.dht.implementations.DefaultDhtCore;
import raw.dht.implementations.DefaultDhtKeyHolder;
import raw.dht.webGui.HtmlBuilder;
import raw.dht.webGui.WebGuiHandler;

/**
 * @author vic
 *
 */
public class KeysHandler implements WebGuiHandler {
	
	private final String myContext = "/keys";
	private final String title = "DHT - Stored keys inspector";

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
	
	private String createResponseBody(){
		DefaultDhtCore dht = (DefaultDhtCore) DefaultDhtCore.getCore();
		HtmlBuilder builder = new HtmlBuilder(title);
		builder.openH1().text("Keys stored on this node").closeH1();
		
		try {
			DhtKeyHolder h = dht.getKeyHolder();
			if(!(h instanceof DefaultDhtKeyHolder)){
				builder.br().br().br().openH2().text("Sorry. Cannot access this node tables.").closeH2();
				return builder.build();
			}
			
			DefaultDhtKeyHolder holder = (DefaultDhtKeyHolder) h;
			
			builder.openH2().text("Current table").closeH2()
			.text("Keys stored with current seed ("+holder.currentHexSeed()+")").br().br().br();
			
			ImmutableMap<DhtKey, Collection<DhtValue>> table = holder.getCurrentTable().asMap();
			ImmutableSet<DhtKey> keys = table.keySet();
			
			builder = printTable(builder, table, keys);
			
			builder.hr().openH2().text("Last seed table").closeH2()
			.text("Keys stored with last seed ("+holder.lastHexSeed()+")").br().br().br();
			
			table = holder.getLastSeedTable().asMap();
			keys = table.keySet();
			
			builder = printTable(builder, table, keys);
			
			builder.hr().openH2().text("First invalid seed table").closeH2()
			.text("Keys stored with last seed ("+holder.firstInvalidHexSeed()+")").br().br().br();
			
			table = holder.getFirstInvalidSeedTable().asMap();
			keys = table.keySet();
			
			builder = printTable(builder, table, keys);
		} catch (Exception e) {
			builder.hr().hr().hr().openH5().text("Exception!").closeH5();
			StackTraceElement[] stacktrace = e.getStackTrace();
			builder.text(e.toString()).br().br();
			for(StackTraceElement elem : stacktrace){
				builder.text(elem.toString()).br();
			}		
		}
		
		return builder.build();
	}
	
	private HtmlBuilder printTable(HtmlBuilder builder, ImmutableMap<DhtKey, Collection<DhtValue>> table, ImmutableSet<DhtKey> keys){
		if(keys.size() == 0){
			builder.text("...no keys stored in this table...");
		} else {
			for(DhtKey key : keys){
				builder.text(key.toString()).space().text(":").br();
				Collection<DhtValue> values = table.get(key);
				for(DhtValue value : values){
					builder.space().space().space().text(value.toString()).br();
				}
				builder.br().br();
			}
		}
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
