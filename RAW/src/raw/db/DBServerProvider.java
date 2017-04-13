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

import raw.db.exceptions.NoSuchDatabaseException;

import com.j256.ormlite.support.ConnectionSource;


/**
 * Objects implementing this interface will
 * be the front-end for DataBase related
 * interactions of {@link DBManager}.
 * 
 * @author vic
 *
 */
public interface DBServerProvider {
	
	/**
	 * Open the DataBase underlying this object.
	 */
	public void startDBServer();
	
	/**
	 * Close the DataBase underlying this object.
	 */
	public void stopDBServer();
	
	/**
	 * Create or open a database by its name.
	 *  
	 * @param dbName the name of the requested database
	 */
	public void registerDatabase(String dbName);
	
	/**
	 * Check if the server is up-and running.
	 * 
	 * @return <tt>true</tt> if the server is running
	 */
	public boolean isServerRunning();
	
	/**
	 * Rerturn a {@link ConnectionSource} to the database specified
	 * by its name.
	 * 
	 * @param dbName the database name
	 * @return a {@link ConnectionSource} to the database
	 * 
	 * @throws NoSuchDatabaseException if dbName identifies a non-existent database
	 */
	public ConnectionSource getConnectionSource(String dbName) throws NoSuchDatabaseException;

}
