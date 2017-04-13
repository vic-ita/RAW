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
package raw.runners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import raw.blockChain.BlockChainCore;
import raw.blockChain.services.implementations.DefaultBlockChainCore;
import raw.concurrent.RAWExecutors;
import raw.dht.DhtCore;
import raw.dht.implementations.DefaultDhtCore;
import raw.dht.webGui.SimpleDhtWebGui;
import raw.logger.Log;
import raw.settings.DhtProperties;
import raw.settings.GeneralProperties;
import raw.settings.ModuleProperty;
import raw.settings.PropertiesManager;

/**
 * This class creates a basic callable
 * object that will start RAW according to
 * its properties files. It will quit by
 * typing "q" and pressing enter on the
 * standard input where this callable is
 * running.  
 * 
 * @author vic
 *
 */
public class RAWLauncher implements Callable<Void> {
	
	private Log log;
	
	private String passedBaseDir;
	
	private ArrayBlockingQueue<Boolean> lock;
	
	/**
	 * Creates a {@link RAWLauncher} with a
	 * default base directory.
	 */
	public RAWLauncher() {
		this(null);
	}
	
	/**
	 * Creates a {@link RAWLauncher} with a
	 * specified base directory.
	 * 
	 * @param baseDirectory the proposed base directory
	 */
	public RAWLauncher(String baseDirectory) {
		log = Log.getLogger();
		passedBaseDir = baseDirectory;
	}

	@Override
	public Void call() {
		lock = new ArrayBlockingQueue<Boolean>(1);
		log.debug("Main lock created.");
		
		log.debug("Quitter started.");
		ExecutorService pool = RAWExecutors.newCachedThreadPool();
		Future<?> quitter = pool.submit(new Quitter());
		try {
			quitter.get(1, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e1) {
			log.exception(e1);
		} catch (ExecutionException e1) {
			log.exception(e1);
		} catch (TimeoutException e1) {
			log.verboseDebug("Quitter is running.");
		}
		
		BlockChainCore blockChain = DefaultBlockChainCore.getBlockChainCore();
		log.info("BlockChain core created.");
		blockChain.start();
		log.debug("BlockChain core started.");
		
		GeneralProperties props = (GeneralProperties) PropertiesManager.getManager(passedBaseDir).getProperties(ModuleProperty.GENERAL);
		DhtProperties dhtProps = (DhtProperties) PropertiesManager.getManager().getProperties(ModuleProperty.DHT);
		
		DhtCore dht = null;
		SimpleDhtWebGui dhtGui = null;
		if(!props.isDhtIsOff()){
			long counter = 0;
			while (!blockChain.isNodeUp()) {
				if(counter % 10 == 0){					
					log.debug("Dht initialization is waiting for block chain to come up.");
				}
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					log.exception(e);
				}
				counter++;
			}
			dht = DefaultDhtCore.getCore();
			log.info("DHT core created");
			dht.start();
			log.debug("DHT core started.");
			if(dhtProps.isWebGuiIsOn()){
				while (!dht.isStarted()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						log.exception(e);
					}
				}
				dhtGui = new SimpleDhtWebGui(dhtProps.getWebGuiPort(), pool);
				dhtGui.start();
				log.warning("DHT web gui is up and running.");
			}
		}
				
		log.debug("Main waiting on lock.");
		try {
			lock.take();
		} catch (InterruptedException e) {
			log.exception(e);
		}
		log.debug("Lock released.");
		
		if(!props.isDhtIsOff()){
			if(dhtProps.isWebGuiIsOn()){
				dhtGui.stop();
				log.debug("DHT web gui stopped.");
			}
			dht.stop();
			log.debug("DHT core stopped.");
		}
		
		blockChain.stop();
		log.debug("BlockChain core stopped.");
		
		return null;
	}

	
	private class Quitter implements Runnable {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String line="";
			while (line.equalsIgnoreCase("q") == false) {
				try {
					line = in.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			lock.add(Boolean.TRUE);
		}

	}

}
