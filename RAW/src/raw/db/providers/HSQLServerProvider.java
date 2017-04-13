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
package raw.db.providers;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;

import org.hsqldb.HsqlException;
import org.hsqldb.Server;

import raw.db.DBServerProvider;
import raw.db.exceptions.NoSuchDatabaseException;
import raw.logger.Log;
import raw.settings.DataBaseProperties;
import raw.settings.ModuleProperty;
import raw.settings.PropertiesManager;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.logger.LocalLog;
import com.j256.ormlite.support.ConnectionSource;

public class HSQLServerProvider implements DBServerProvider {
	
	private ArrayList<String> dbNames;
	private String dbDirectory;
	
	private Server dbServer;
	
	private int port;
	
	private Log log;
	
	public HSQLServerProvider() {
		dbNames = new ArrayList<String>();
		DataBaseProperties properties = (DataBaseProperties) PropertiesManager.getManager().getProperties(ModuleProperty.DATABASE);
		dbDirectory = properties.getDbDir();
		
		log = Log.getLogger();
		
		dbServer = new Server();
		
		/*
		 * the following lines are to silence HSQLDB as
		 * well as ORMLite
		 */
		dbServer.setSilent(true);
		dbServer.setTrace(false);
		System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "info"); 
		/*
		 * end of "silencer" lines
		 */
		
		port = properties.getPort();
		dbServer.setPort(port);
		
		dbServer.setNoSystemExit(true);
	}

	/* (non-Javadoc)
	 * @see raw.db.DBServerProvider#startDBServer()
	 */
	@Override
	public void startDBServer() {
		Path myPath = FileSystems.getDefault().getPath(dbDirectory, "");
		log.debug("Checking existence of "+myPath);
		if(!Files.exists(myPath)){
			log.debug("Directory does not exist. Creating it.");
			try {
				Files.createDirectories(myPath);
				log.debug(myPath+ " created.");
			} catch (IOException e) {
				log.exception(e);
			}
		}
		
		dbServer.start();
	    log.info("HSQL DataBase server has started.");
	    
	    try {
			Class.forName("org.hsqldb.jdbcDriver"); //this will load JDBC driver.
		} catch (ClassNotFoundException e) {
			log.exception(e);
		}
	}

	/* (non-Javadoc)
	 * @see raw.db.DBServerProvider#stopDBServer()
	 */
	@Override
	public void stopDBServer() {
		dbServer.stop();
		log.info("DataBase server stop command issued.");
	}
	
	/* (non-Javadoc)
	 * @see raw.db.DBServerProvider#registerDatabase(java.lang.String)
	 */
	@Override
	public void registerDatabase(String dbName) {
		log.debug("Registering a new database name: "+dbName);
		boolean wasRunning = false;
		if(isServerRunning()){
			log.debug("Server is running. Halting it.");
			stopDBServer();
			while(true){
				if(!isServerRunning()){
					break;
				}
			}
			log.debug("Server has stopped.");
			wasRunning = true;
		}
		int index = getDatabaseIndex(dbName);
		dbServer.setDatabasePath(index, dbDirectory+dbName);
		dbServer.setDatabaseName(index, dbName);
		log.debug("Database name "+dbName+" has been set up.");
		if(wasRunning){
			startDBServer();
			while(true){
				if(isServerRunning()){
					break;
				}
			}
			log.debug("Server has restarted.");
		}
	}
	
	private int getDatabaseIndex(String dbName){
		if(!dbNames.contains(dbName)){
			dbNames.add(dbName);
		}
		return dbNames.indexOf(dbName) + 1 ;
	}

	@Override
	public boolean isServerRunning() {
		try {
			dbServer.checkRunning(true);
			return true;
		} catch (HsqlException e) {
			return false;
		}
	}

	@Override
	public ConnectionSource getConnectionSource(String dbName) throws NoSuchDatabaseException {
		if(!dbNames.contains(dbName)){
			throw new NoSuchDatabaseException("Database "+dbName+" is not hosted on this server!");
		}
		String url = "jdbc:hsqldb:hsql://localhost:"+port+"/"+dbName;
		ConnectionSource source = null;
		try {
			source = new JdbcConnectionSource(url);
		} catch (SQLException e) {
			log.exception(e);
		}
		return source;
	}
	
}
