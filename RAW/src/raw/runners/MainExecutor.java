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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import raw.blockChain.services.utils.MinerNodeAddressBookFile;
import raw.blockChain.services.utils.ThickNodeAddressBookFile;
import raw.concurrent.RAWExecutors;
import raw.db.DBServerProviderFactory;
import raw.dht.implementations.utils.DhtNodeAddressBookFile;
import raw.logger.Level;
import raw.logger.Log;
import raw.settings.BlockChainProperties;
import raw.settings.DataBaseProperties;
import raw.settings.DhtProperties;
import raw.settings.LoggerProperties;
import raw.settings.ModuleProperty;
import raw.settings.PropertiesManager;

/**
 * This class is just the main method wrapper to run 
 * the software just by executing this class. 
 * 
 * 
 * @author vic
 *
 */
public class MainExecutor {
	

	public static void main(String[] args) throws FileNotFoundException {
		Options options = new Options();
		
		Option help = new Option("h", "help", false, "Print this help message");
		
		Option setUpRoutine = new Option("z", "setup", true, "Set up RAW's home directory, the configuration files and then exits. Through baseDirectory a home may be specified, otherwise a default one will be used.");
		setUpRoutine.setArgName("baseDirectory");
		setUpRoutine.setOptionalArg(true);
		
		Option baseDir = new Option("D", "dir", true, "Sets the base directory for RAW");
		baseDir.setArgName("baseDirectory");
		String levels = "";
		for(Level l : Level.values()){
			levels = levels+"; "+l.getValue();
		}
		levels = levels.substring(2, levels.length());
		Option outputLevel = new Option("o", "output", true, "Sets standard output verbosity level (acceptable values: "+levels+")");
		outputLevel.setArgName("verbosity-level");
		Option logLevel = new Option("l", "log", true, "Sets logging verbosity level (acceptable values: "+levels+")");
		logLevel.setArgName("verbosity-level");
		
		Option defaultThickNodes = new Option("N", "defThickNodes", true, "Sets a list of default BlockChain ThickNodes. The passed file MUST be a valid JSON file.");
		defaultThickNodes.setArgName("jsonFilePath");
		
		Option isThickNode = new Option("T", "thickNode", false, "Sets this node as a BlockChain thick one.");
		Option isThinNode = new Option("t", "thinNode", false, "Sets this node as a BlockChain thin one.");
		Option blockChainNodePort = new Option("b", "blockChainPort", true, "Sets the listening port for this BlockChain node.");
		blockChainNodePort.setArgName("portNumber");
		
		Option isMinerNode = new Option("M", "miner", true, "Turn on/off the miner on this BlockChain node.");
		isMinerNode.setArgName("true/false");
		Option minerNodePort = new Option("m", "minerPort", true, "Sets the listening port for this BlockChain miner node.");
		minerNodePort.setArgName("portNumber");
		
		Option dhtPort = new Option("d", "dhtPort", true, "Sets (both UDP and TCP) listening ports for this DHT node.");
		dhtPort.setArgName("portNumber");
		
		Option isWebGuiOn = new Option("W", "webGui", true, "Turn on/off the dht web GUI (WARNING: possible security breach).");
		isWebGuiOn.setArgName("true/false");
		Option webGuiPort = new Option("w", "webGuiPort", true, "Sets the listening port for the web GUI.");
		webGuiPort.setArgName("portNumber");
		
		Option isInfoServerOn = new Option("S", "nodesInfoServer", true, "Turn on/off the nodes info server.");
		isInfoServerOn.setArgName("true/false");
		Option infoServerPort = new Option("s", "nodesInfoServerPort", true, "Sets the listening port for the nodes info server.");
		infoServerPort.setArgName("portNumber");
		
		Option dbPort = new Option("a", "databaseInternalPort", true, "Sets the internal port for the database (HSQLDB) running on this node.");
		dbPort.setArgName("portNumber");
		
		options.addOption(help);
		options.addOption(setUpRoutine);
		options.addOption(baseDir);
		options.addOption(outputLevel);
		options.addOption(logLevel);
		options.addOption(defaultThickNodes);
		options.addOption(blockChainNodePort);
		options.addOption(isThickNode);
		options.addOption(isThinNode);
		options.addOption(isMinerNode);
		options.addOption(minerNodePort);
		options.addOption(dhtPort);
		options.addOption(isWebGuiOn);
		options.addOption(webGuiPort);
		options.addOption(isInfoServerOn);
		options.addOption(infoServerPort);
		options.addOption(dbPort);
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e1) {
			e1.printStackTrace();
			System.exit(-1);
		}
		
		if(cmd.hasOption(help.getOpt())){
			HelpFormatter helpFormatter = new HelpFormatter();
			helpFormatter.printHelp("java -jar RAW.jar [options]", options);
			System.exit(0);
		}
		
		if(cmd.hasOption(setUpRoutine.getOpt())){
			String homeDir = cmd.getOptionValue(setUpRoutine.getOpt(), null);
			setUpDirs(homeDir);
			System.exit(0);
		}
		
		RAWLauncher exec;
		if(cmd.hasOption(baseDir.getOpt())){
			String dir = cmd.getOptionValue(baseDir.getOpt());
			PropertiesManager.getManager(dir); //this will ensure dir is passed before any other calling
			exec = new RAWLauncher(dir);
		} else {
			exec = new RAWLauncher();
		}
		
		Log log = Log.getLogger();
		log.info("RAW main starting!!!");
		
		if(cmd.hasOption(outputLevel.getOpt())){
			String l = cmd.getOptionValue(outputLevel.getOpt());
			LoggerProperties logProp = (LoggerProperties) PropertiesManager.getManager().getProperties(ModuleProperty.LOGGER);
			logProp.setConsoleLogLevel(l);
			Log.setConsoleLoggingLevel(Level.fromLevelString(l));
		}
		if(cmd.hasOption(logLevel.getOpt())){
			String l = cmd.getOptionValue(logLevel.getOpt());
			LoggerProperties logProp = (LoggerProperties) PropertiesManager.getManager().getProperties(ModuleProperty.LOGGER);
			logProp.setFileLogLevel(l);
			Log.setFileLoggingLevel(Level.fromLevelString(l));
		}
		
		if(cmd.hasOption(defaultThickNodes.getOpt())){
			String pathString = cmd.getOptionValue(defaultThickNodes.getOpt());
			File defaultFile = new File(pathString);
			if(!Files.exists(defaultFile.toPath())){
				throw new FileNotFoundException("Specified file ("+pathString+") does not exist.");
			}
			ThickNodeAddressBookFile.copyDefaultListFile(defaultFile);
			log.verboseDebug(pathString+" set as default ThickNodes list.");
		}
		
		if(cmd.hasOption(isThickNode.getOpt())){
			BlockChainProperties bcProps = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
			bcProps.setThickClientIsOn(true);
		}
		
		if(cmd.hasOption(isThinNode.getOpt())){
			BlockChainProperties bcProps = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
			bcProps.setThickClientIsOn(false);
		}
		
		if(cmd.hasOption(blockChainNodePort.getOpt())){
			String value = cmd.getOptionValue(blockChainNodePort.getOpt());
			try {
				int port = Integer.parseInt(value);				
				BlockChainProperties bcProps = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
				bcProps.setListeningSocket(port);
			} catch (NumberFormatException e) {
				log.exception(e);
			}
		}
		
		if(cmd.hasOption(isMinerNode.getOpt())){
			String value = cmd.getOptionValue(isMinerNode.getOpt());
			BlockChainProperties bcProps = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
			if(value.equals("true")){
				bcProps.setMinerIsOn(true);
			} else {
				bcProps.setMinerIsOn(false);
			}
		}
		
		if(cmd.hasOption(minerNodePort.getOpt())){
			String value = cmd.getOptionValue(minerNodePort.getOpt());
			try {
				int port = Integer.parseInt(value);				
				BlockChainProperties bcProps = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
				bcProps.setMinerListeningSocket(port);
			} catch (NumberFormatException e) {
				log.exception(e);
			}
		}
		
		if(cmd.hasOption(dhtPort.getOpt())){
			String value = cmd.getOptionValue(dhtPort.getOpt());
			try {
				int port = Integer.parseInt(value);				
				DhtProperties dhtProps = (DhtProperties) PropertiesManager.getManager().getProperties(ModuleProperty.DHT);
				dhtProps.setTcpListeningSocket(port);
				dhtProps.setUdpListeningSocket(port);
			} catch (NumberFormatException e) {
				log.exception(e);
			}
		}
		
		if(cmd.hasOption(isWebGuiOn.getOpt())){
			String value = cmd.getOptionValue(isWebGuiOn.getOpt());
			DhtProperties dhtProps = (DhtProperties) PropertiesManager.getManager().getProperties(ModuleProperty.DHT);
			if(value.equals("true")){
				dhtProps.setWebGuiIsOn(true);
			} else {
				dhtProps.setWebGuiIsOn(false);
			}
		}
		
		if(cmd.hasOption(webGuiPort.getOpt())){
			String value = cmd.getOptionValue(webGuiPort.getOpt());
			try {
				int port = Integer.parseInt(value);				
				DhtProperties dhtProps = (DhtProperties) PropertiesManager.getManager().getProperties(ModuleProperty.DHT);
				dhtProps.setWebGuiPort(port);
			} catch (NumberFormatException e) {
				log.exception(e);
			}
		}
		
		if(cmd.hasOption(isInfoServerOn.getOpt())){
			String value = cmd.getOptionValue(isInfoServerOn.getOpt());
			DhtProperties dhtProps = (DhtProperties) PropertiesManager.getManager().getProperties(ModuleProperty.DHT);
			if(value.equals("true")){
				dhtProps.setNodesInfoServerRunning(true);
			} else {
				dhtProps.setNodesInfoServerRunning(false);
			}
		}
		
		if(cmd.hasOption(infoServerPort.getOpt())){
			String value = cmd.getOptionValue(infoServerPort.getOpt());
			try {
				int port = Integer.parseInt(value);				
				DhtProperties dhtProps = (DhtProperties) PropertiesManager.getManager().getProperties(ModuleProperty.DHT);
				dhtProps.setNodesInfoServerPort(port);
			} catch (NumberFormatException e) {
				log.exception(e);
			}
		}
		
		if(cmd.hasOption(dbPort.getOpt())){
			String value = cmd.getOptionValue(dbPort.getOpt());
			try {
				int port = Integer.parseInt(value);				
				DataBaseProperties dbProps = (DataBaseProperties) PropertiesManager.getManager().getProperties(ModuleProperty.DATABASE);
				dbProps.setPort(port);
			} catch (NumberFormatException e) {
				log.exception(e);
			}
		}
		
		ExecutorService executor = RAWExecutors.newWorkStealingPool();
		
		Future<?> runner = executor.submit(exec);
		
		try {
			runner.get();
		} catch (InterruptedException e) {
			log.exception(e);
		} catch (ExecutionException e) {
			log.exception(e);
		}
		
		log.info("RAW main ending.");
		log.flushLogger();
		System.out.println("exit");
		System.exit(0);
	}
	
	private static void setUpDirs(String homeDir){
		PropertiesManager manager;
		if(homeDir == null){
			manager = PropertiesManager.getManager();
		} else {
			manager = PropertiesManager.getManager(homeDir);
		}
		Log logger = Log.getLogger();
		logger.info("Building RAW's home and configuration files.........");
		
		BlockChainProperties blockChainProps = (BlockChainProperties) manager.getProperties(ModuleProperty.BLOCK_CHAIN);
		manager.getProperties(ModuleProperty.DATABASE);
		manager.getProperties(ModuleProperty.DHT);
		manager.getProperties(ModuleProperty.GENERAL);
		manager.getProperties(ModuleProperty.LOGGER);
		
		MinerNodeAddressBookFile miners = new MinerNodeAddressBookFile();
		miners.writeToFile();
		@SuppressWarnings("unused")
		ThickNodeAddressBookFile thickNodes = new ThickNodeAddressBookFile();
		String pathString = blockChainProps.getBlockChainDir()+blockChainProps.getThickNodesAddressesFileName()+".json";
		Path path = Paths.get(pathString);
		try {
			Files.deleteIfExists(path);
		} catch (IOException e) {
			logger.exception(e);
		}
		DBServerProviderFactory db = new DBServerProviderFactory();
		db.getProvider().startDBServer();
		db.getProvider().stopDBServer();
		DhtNodeAddressBookFile dhtNodes = new DhtNodeAddressBookFile(false);
		dhtNodes.getDefaultFile();

		logger.info(".............................................. Done.");
		logger.flushLogger();
	}
	
}
