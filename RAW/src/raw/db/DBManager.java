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
package raw.db;

import java.sql.SQLException;

import raw.db.exceptions.DataBaseNotRunning;
import raw.db.exceptions.NoSuchDatabaseException;
import raw.logger.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * This class is a singleton managing the database
 * for all classes using it.
 * 
 * @author vic
 *
 */
public class DBManager {
	
	private static DBManager theManager = null;
		
	private DBServerProvider server;
	
	private Log log;
	
	private int startCalls;
	
	private ConnectionSource connectionSource;
	
	private DBManager() {
		server = (new DBServerProviderFactory()).getProvider();
		
		startCalls = 0;
		
		log = Log.getLogger();
		
		connectionSource = null;
	}
	
	/**
	 * @return an instance of {@link DBManager}
	 */
	public static DBManager getDBManager(){
		if(theManager == null){
			theManager = new DBManager();
		}
		return theManager;
	}
	
	/**
	 * Start the server (if necessary) and 
	 * register a database by its name.
	 * 
	 * @param dbName the database name
	 */
	public void startDB(String dbName){
		server.registerDatabase(dbName);
		startCalls += 1;
		log.debug("Database "+dbName+" has now been registered.");
		if(!server.isServerRunning()){
			log.debug("Server is not running. Starting it.");
			server.startDBServer();
		}
	}
	
	public void stopDB(String dbName){
		startCalls -= 1;
		if(startCalls == 0){
			log.debug("A stop command was issued for every start. Closing server.");
			stopAllDBs();
		}			
	}
	
	/**
	 * Close all databases and stops the server.
	 */
	public void stopAllDBs(){
		if(server.isServerRunning()){
			server.stopDBServer();
			log.debug("Server has been stopped.");
			return;
		}
		log.debug("Server was not running. Nothing to do here...");
	}
	
	/**
	 * Return a {@link Dao} to perform operations on a given
	 * <tt>dataClass</tt> associated table. 
	 * @param <ID>
	 * 
	 * @param dbName the database name owning the tables
	 * @param dataClass the class associated with a table
	 * @return a {@link Dao} to operate on the tables
	 * @throws SQLException 
	 * @throws NoSuchDatabaseException if <tt>dbName</tt> database does not exists
	 * @throws DataBaseNotRunning if the server is not running
	 */
	public <T, ID> Dao<T, ID> getDAO(String dbName, Class<T> dataClass) throws SQLException, NoSuchDatabaseException, DataBaseNotRunning{
		if(!server.isServerRunning()){
			throw new DataBaseNotRunning("The server for "+dbName+" is not running!");
		}
		
		if(connectionSource == null || !connectionSource.isOpen()){
			connectionSource = server.getConnectionSource(dbName);
		}		
		Dao<T, ID> returnedDao = DaoManager.createDao(connectionSource, dataClass);
		
		if(!returnedDao.isTableExists()){
			TableUtils.createTable(connectionSource, dataClass);
			log.debug("Table for "+dataClass.getCanonicalName()+" created in "+dbName+".");
		}
		
		return returnedDao;
	}
	
	/**
	 * Checks if in database <tt>dbName</tt> there exists a table associated
	 * with <tt>dataClass</tt>. 
	 * 
	 * @param dbName the database name owning the tables
	 * @param dataClass the class associated with a table
	 * @throws SQLException
	 * @throws NoSuchDatabaseException if <tt>dbName</tt> database does not exists
	 */
	public <T> void createTableIfNotExists(String dbName, Class<T> dataClass) throws SQLException, NoSuchDatabaseException{
		if(connectionSource == null || !connectionSource.isOpen()){
			connectionSource = server.getConnectionSource(dbName);
		}		
		Dao<T, ?> classDao = DaoManager.createDao(connectionSource, dataClass);
		
		if(!classDao.isTableExists()){
			TableUtils.createTable(connectionSource, dataClass);
		}
		log.debug("Table for "+dataClass.getCanonicalName()+" created in "+dbName+".");
	}
	
	/**
	 * Builds a {@link TransactionManager} for <tt>dbName</tt> database.
	 * 
	 * @param dbName the database name owning the tables
	 * @return a {@link TransactionManager}
	 * @throws NoSuchDatabaseException if <tt>dbName</tt> database does not exists
	 */
	public TransactionManager getTransactionManager(String dbName) throws NoSuchDatabaseException {
		if(connectionSource == null || !connectionSource.isOpen()){
			connectionSource = server.getConnectionSource(dbName);
		}		
		return new TransactionManager(connectionSource);
	}
}
