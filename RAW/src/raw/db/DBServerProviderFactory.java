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

import raw.db.providers.HSQLServerProvider;
import raw.logger.Log;
import raw.settings.DataBaseProperties;
import raw.settings.ModuleProperty;
import raw.settings.PropertiesManager;

public class DBServerProviderFactory {
	
	private SupportedDataBases dbInUse;
		
	private Log log;
	
	public DBServerProviderFactory() {
		DataBaseProperties properties = (DataBaseProperties) PropertiesManager.getManager().getProperties(ModuleProperty.DATABASE);
		dbInUse = properties.getDbEngine();
		
		log = Log.getLogger();
	}
	
	public DBServerProvider getProvider(){
		DBServerProvider provider = null;
		switch (dbInUse) {
		case HSQLDB:
			log.debug("Current db selected by properties is HSQLDB");
			provider = new HSQLServerProvider();
			break;

		default:
			log.debug("Could not determine DB type. Falling back to HSQL.");
			provider = new HSQLServerProvider();
			break;
		}
		return provider;
	}

}
