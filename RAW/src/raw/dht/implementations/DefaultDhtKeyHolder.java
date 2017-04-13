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
package raw.dht.implementations;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import raw.blockChain.api.implementations.DefaultHasher;
import raw.dht.DhtConstants;
import raw.dht.DhtCore;
import raw.dht.DhtHasher;
import raw.dht.DhtID;
import raw.dht.DhtKey;
import raw.dht.DhtKeyHolder;
import raw.dht.DhtValue;
import raw.logger.Log;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

/**
 * Default implementation of
 * {@link DhtKeyHolder}.
 * 
 * @author vic
 *
 */
public class DefaultDhtKeyHolder implements DhtKeyHolder {
	
	private DhtCore myCore;
	
	private byte[] currentSeed;
	private ListMultimap<DhtKey, DhtValue> currentTable;
	private byte[] lastSeed;
	private ListMultimap<DhtKey, DhtValue> lastSeedTable;
	private byte[] firstInvalidSeed;
	private ListMultimap<DhtKey, DhtValue> firstInvalidSeedTable;
	private byte[] nullSeed;
	
	private Log log;
	
	public DefaultDhtKeyHolder(DhtCore core) {
		myCore = core;
		int hashLength = (new DefaultHasher()).hashLength();
		currentSeed = new byte[hashLength];
		Arrays.fill(currentSeed, (byte)0);
		lastSeed = new byte[hashLength];
		Arrays.fill(lastSeed, (byte)0);
		firstInvalidSeed = new byte[hashLength];
		Arrays.fill(firstInvalidSeed, (byte)0);
		nullSeed = new byte[hashLength];
		Arrays.fill(nullSeed, (byte)0);
		currentTable = Multimaps.synchronizedListMultimap(ArrayListMultimap.<DhtKey, DhtValue>create());
		lastSeedTable = Multimaps.synchronizedListMultimap(ArrayListMultimap.<DhtKey, DhtValue>create());
		firstInvalidSeedTable = Multimaps.synchronizedListMultimap(ArrayListMultimap.<DhtKey, DhtValue>create());
		
		log = Log.getLogger();
		log.verboseDebug("KeyHolder created.");
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtKeyHolder#store(raw.dht.DhtKey, raw.dht.DhtValue)
	 */
	@Override
	public boolean store(DhtKey key, DhtValue value) {
		checkTablesAndUpdate();
		if(!(isKeyValidForCurrentSeed(key) || isKeyValidForLastSeed(key))){
			log.debug("The received DhtKey is not consistent with current or last seed having a wrong hash. Refusing store.");
			return false;
		}
		boolean ret = false;
		if(isKeyValidForCurrentSeed(key)){			
			synchronized (currentTable) {
				List<DhtValue> storedValues = currentTable.get(key);
				if(storedValues.contains(value)){
					ret = true;
				} else {				
					ret = currentTable.put(key, value);			
				}
			}
		}
		if(!Arrays.equals(lastSeed, nullSeed)){
			if(isKeyValidForLastSeed(key)){				
				synchronized (lastSeedTable) {
					List<DhtValue> storedValues = lastSeedTable.get(key);
					if(storedValues.contains(value)){
						ret = ret || true;
					} else {				
						ret = ret || lastSeedTable.put(key, value);			
					}
				}
			}
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtKeyHolder#get(raw.dht.DhtKey)
	 */
	@Override
	public Collection<DhtValue> get(DhtKey key) {
		List<DhtValue> retrievedValues = internalGet(key);
		if(retrievedValues == null){
			return null;
		}
		
		if(retrievedValues.size() > DhtConstants.NUMBER_OF_VALUES_IN_REPLY){
			Collections.shuffle(retrievedValues);
			
			List<DhtValue> tmp = retrievedValues.subList(0, DhtConstants.NUMBER_OF_VALUES_IN_REPLY);
			retrievedValues = tmp;
		}
		
		return retrievedValues;
	}
	
	/**
	 * Utility method to be used by {@link DefaultDhtKeyHolder#get(DhtKey)}
	 * <b>BEFORE</b> "formatting" the collection
	 * 
	 * @param key the searched {@link DhtKey}
	 * @return the {@link Collection} not yer limited.
	 */
	private List<DhtValue> internalGet(DhtKey key) {
		log.verboseDebug("Looking up for "+key);
		checkTablesAndUpdate();
		if(isKeyValidForCurrentSeed(key)){
			List<DhtValue> values;
			synchronized (currentTable) {				
				values = currentTable.get(key);
			}
			if(values.size() == 0){
				return null;
			}
			return values;
		}
		if(isKeyValidForLastSeed(key)){
			List<DhtValue> values;
			synchronized (lastSeedTable) {				
				values = lastSeedTable.get(key);
			}
			if(values.size() == 0){
				return null;
			}
			return values;
		}
//		if(isKeyValidForFirstInvalidSeed(key)){
//			List<DhtValue> values;
//			synchronized (firstInvalidSeedTable) {				
//				values = firstInvalidSeedTable.get(key);
//			}
//			if(values.size() == 0){
//				return null;
//			}
//			return values;
//		}
		return null;
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtKeyHolder#delete(raw.dht.DhtKey, raw.dht.DhtValue)
	 */
	@Override
	public boolean delete(DhtKey key, DhtValue value) {
		log.verboseDebug("Deleting  "+key+" + "+value);
		checkTablesAndUpdate();
		boolean ret;
		synchronized (currentTable) {
			ret = currentTable.remove(key, value);			
		}
		synchronized (lastSeedTable) {
			ret = ret || lastSeedTable.remove(key, value);
		}
		synchronized (firstInvalidSeedTable) {
			ret = ret || firstInvalidSeedTable.remove(key, value);
		}
//		if(!ret){
//			synchronized (lastSeedTable) {
//				ret = lastSeedTable.remove(key, value);
//			}
//		}
		return ret;
	}
	
	private boolean isKeyValidForCurrentSeed(DhtKey key){
		DhtID expectedKeyId = currentExpectedId(key);
		if(expectedKeyId.equals(key.getKeyId())){
			log.verboseDebug(key+" is valid for current seed!");
			return true;
		}
		log.verboseDebug(key+" is NOT valid for current seed!");
		return false;
	}
	
	private boolean isKeyValidForLastSeed(DhtKey key){
		DhtID expectedKeyId = lastSeedExpectedId(key);
		if(expectedKeyId.equals(key.getKeyId())){
			log.verboseDebug(key+" is valid for last seed!");
			return true;
		}
		log.verboseDebug(key+" is NOT valid for last seed!");
		return false;
	}
	
//	private boolean isKeyValidForFirstInvalidSeed(DhtKey key){
//		DhtID expectedKeyId = firstInvalidSeedExpectedId(key);
//		if(expectedKeyId.equals(key.getKeyId())){
//			log.verboseDebug(key+" is \"valid\" for first invalid seed!");
//			return true;
//		}
//		log.verboseDebug(key+" is \"NOT valid\" for first invalid seed!");
//		return false;
//	}
	
	private DhtID currentExpectedId(DhtKey key){
		DhtHasher hasher = myCore.getCurrentHasher();
		DhtID expectedKeyId = hasher.hashString(key.getKeyString());
		return expectedKeyId;
	}
	
	private DhtID lastSeedExpectedId(DhtKey key){
		DhtHasher hasher = new DefaultDhtHasher(lastSeed);
		DhtID expectedKeyId = hasher.hashString(key.getKeyString());
		return expectedKeyId;
	}
	
//	private DhtID firstInvalidSeedExpectedId(DhtKey key){
//		DhtHasher hasher = new DefaultDhtHasher(firstInvalidSeed);
//		DhtID expectedKeyId = hasher.hashString(key.getKeyString());
//		return expectedKeyId;
//	}
	
	private void checkTablesAndUpdate(){
		if(!currentSeedIsValid()){
			log.verboseDebug("Tables must be swapped.");
			swapTables();
		}
	}
	
	/**
	 * checks if current seed is the right one
	 * 
	 * @return <tt>true</tt> if the current seed is still right, <tt>false</tT> otherwise
	 */
	private boolean currentSeedIsValid(){
		return Arrays.equals(currentSeed, myCore.getCurrentSeed());
	}
	
	/**
	 * replaces current table with a new one and put as
	 * lastSeedTable the old "version"
	 */
	private void swapTables(){
		firstInvalidSeed = lastSeed;
		firstInvalidSeedTable = lastSeedTable;
		lastSeed = currentSeed;
		lastSeedTable = currentTable;
		
		currentSeed = myCore.getCurrentSeed();
		currentTable = Multimaps.synchronizedListMultimap(ArrayListMultimap.<DhtKey, DhtValue>create());
		log.verboseDebug("Tables swapped.");
	}
	
	/**
	 * Utility method.
	 * 
	 * @return a view of the current table held by this {@link DefaultDhtKeyHolder}
	 */
	public ImmutableListMultimap<DhtKey, DhtValue> getCurrentTable(){
		return ImmutableListMultimap.copyOf(currentTable);
	}
	
	/**
	 * Utility method.
	 * 
	 * @return a view of the table associated to the last seed held by this {@link DefaultDhtKeyHolder}
	 */
	public ImmutableListMultimap<DhtKey, DhtValue> getLastSeedTable(){
		return ImmutableListMultimap.copyOf(lastSeedTable);
	}
	
	/**
	 * Utility method.
	 * 
	 * @return a view of the table associated to the first invalid seed held by this {@link DefaultDhtKeyHolder}
	 */
	public ImmutableListMultimap<DhtKey, DhtValue> getFirstInvalidSeedTable(){
		return ImmutableListMultimap.copyOf(firstInvalidSeedTable);
	}
	
	/**
	 * Utility method.
	 * 
	 * @return a hex string representing current seed
	 */
	public String currentHexSeed(){
		DhtID seed = new DefaultDhtID(currentSeed);
		return seed.toHexString(); 
	}

	/**
	 * Utility method.
	 * 
	 * @return a hex string representing the last seed
	 */
	public String lastHexSeed(){
		DhtID seed = new DefaultDhtID(lastSeed);
		return seed.toHexString(); 
	}
	
	/**
	 * Utility method.
	 * 
	 * @return a hex string representing the first invalid seed
	 */
	public String firstInvalidHexSeed(){
		DhtID seed = new DefaultDhtID(firstInvalidSeed);
		return seed.toHexString(); 
	}

}
