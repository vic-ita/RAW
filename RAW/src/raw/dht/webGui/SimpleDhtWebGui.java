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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

import raw.dht.webGui.handlers.IndexHandler;
import raw.dht.webGui.handlers.KeysHandler;
import raw.dht.webGui.handlers.NodeInfoHandler;
import raw.dht.webGui.handlers.SearchHandler;
import raw.dht.webGui.handlers.StoreHandler;
import raw.logger.Log;

import com.sun.net.httpserver.HttpServer;

/**
 * This simple {@link HttpServer} based 
 * will provide a simple and basic interface for
 * the host DHT node.
 * 
 * @author vic
 *
 */
public class SimpleDhtWebGui {
	
	private HttpServer server;
	
	private Log log;
	
	/**
	 * Build a simple {@link HttpServer} based
	 * "web GUI".
	 * 
	 * @param port the port to be used
	 * @param executor {@link Executor} to be used by this server
	 */
	public SimpleDhtWebGui(int port, Executor executor) {
		log = Log.getLogger();
		try {
			server = HttpServer.create(new InetSocketAddress(port), 0);
		} catch (IOException e) {
			log.exception(e);
		}
		IndexHandler index = new IndexHandler();
		server.createContext(index.getContext(), index);
		server.createContext("/", index); // fallback for index
		NodeInfoHandler transactions = new NodeInfoHandler();
		server.createContext(transactions.getContext(), transactions);
		SearchHandler search = new SearchHandler();
		server.createContext(search.getContext(), search);
		StoreHandler store = new StoreHandler();
		server.createContext(store.getContext(), store);
		KeysHandler keys = new KeysHandler();
		server.createContext(keys.getContext(), keys);
		server.setExecutor(executor);
	}
	
	/**
	 * Starts underlying server
	 */
	public void start() {
		server.start();
	}
	
	/**
	 * Stops underlying server
	 */
	public void stop() {
		server.stop(1);
	}

}
