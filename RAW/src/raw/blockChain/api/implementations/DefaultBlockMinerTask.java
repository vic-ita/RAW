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
package raw.blockChain.api.implementations;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.TimeZone;

import raw.blockChain.api.Block;
import raw.blockChain.api.BlockHeader;
import raw.blockChain.api.BlockMiner;
import raw.blockChain.api.BlockMinerTask;
import raw.blockChain.api.HashValue;
import raw.blockChain.api.Hasher;
import raw.blockChain.api.Merkler;
import raw.blockChain.api.Transaction;
import raw.blockChain.api.implementations.DefaultBlockHeader.Builder;
import raw.blockChain.exceptions.IllegalBlockHeaderBytesRepresentation;
import raw.blockChain.exceptions.IncompleteBuilderSettingsException;
import raw.logger.Log;
import raw.settings.BlockChainProperties;
import raw.settings.ModuleProperty;
import raw.settings.PropertiesManager;
import static raw.blockChain.api.implementations.utils.BlockUtils.*;

/**
 * Default implementation of {@link BlockMinerTask}
 * 
 * @author vic
 *
 */
public class DefaultBlockMinerTask extends BlockMinerTask {
	
	private Log log;
	private Merkler merkler;
	private Hasher hasher;
	
	private Random rand;
	
	private boolean compute;
	
	private DefaultBlockMiner father;
	
	private String blockChainName;
	
	private String mySignature;

	public DefaultBlockMinerTask(BlockHeader previousBlockHeader,ArrayList<Transaction> candidateTransactions, BlockMiner father, String minerSignature) throws IllegalArgumentException {
		super(previousBlockHeader, candidateTransactions);
		
		log = Log.getLogger();
		
		BlockChainProperties properties = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
		merkler = new DefaultMerkler(properties.getMerkleCacheSize());
		hasher = new DefaultHasher();
		
		rand = new Random(System.currentTimeMillis());
		
		compute = true;
		
		blockChainName = properties.getBlockChainName();
		
		mySignature = minerSignature;
		
		if(father instanceof DefaultBlockMiner){
			this.father = (DefaultBlockMiner) father;
		}
		else{
			this.father = null;
		}
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Block call() throws Exception {
		log.debug("Starting this task.");
		
		BigDecimal difficulty = nextDifficulty(getPreviousBlockHeader());
		HashValue target = targetMaskFromDifficulty(difficulty);
				
		Builder baseBlockHeaderBluilder = new Builder();
		baseBlockHeaderBluilder.setBlockChainName(blockChainName).
		setBlockNumber(nextBlockNumber(getPreviousBlockHeader())).
		setDifficulty(difficulty).
		setPrevBlockHash(getPreviousBlockHeader().hash()).
		setMinerSignature(mySignature);
		
		boolean found = false;
		
		Block foundBlock = null;
		
		log.debug("beginning miner loop...");
		while(compute && !found){
			Collections.shuffle(getCandidateTransactions());
			HashValue merkleRoot = merkler.getMerkleRoot(getCandidateTransactions());
			Builder blockHeaderBluilder = baseBlockHeaderBluilder;
			
			long timestamp = System.currentTimeMillis();
			TimeZone timeZone = TimeZone.getDefault();
			timestamp = timestamp - timeZone.getOffset(timestamp);
			
			blockHeaderBluilder.setTimestamp(timestamp).
			setMerkleRoot(merkleRoot);
			
			HashValue headerHash = null;
			
			int startingNonce = rand.nextInt();
			log.debug("Extracted first nonce = "+startingNonce);
			for(int nonce=startingNonce; nonce < Integer.MAX_VALUE; nonce++){
				if(!compute){
					break;
				}
				headerHash = tryThisNonce(nonce, blockHeaderBluilder);
				HashValue masked = headerHash.maskWith(target);
				if(headerHash.equals(masked)){
					log.info("YAY! found a block header: "+headerHash.toHexString());
					foundBlock = new DefaultBlock(blockHeaderBluilder.build(), getCandidateTransactions());
					found = true;
					break;
				}
			}
			if(!found){
				for(int nonce=Integer.MIN_VALUE; nonce<startingNonce; nonce++){
					if(!compute){
						break;
					}
					headerHash = tryThisNonce(nonce, blockHeaderBluilder);
					HashValue masked = headerHash.maskWith(target);
					if(headerHash.equals(masked)){
						log.info("YAY! found a block header: "+headerHash.toHexString());
						foundBlock = new DefaultBlock(blockHeaderBluilder.build(), getCandidateTransactions());
						found = true;
						break;
					}
				}
			}
		}
		if(father != null && found){
			father.signalBlockIsFound(foundBlock);
		}
		return foundBlock;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.BlockMinerTask#stop()
	 */
	@Override
	public void stop() {
		compute = false;
	}
	
	private HashValue tryThisNonce(int nonce, Builder headerBuilder){
		headerBuilder.setNonce(nonce);
		HashValue computedHash = null;
		try {
			computedHash = hasher.hashBlockHeader(headerBuilder.build());
		} catch (ClassNotFoundException e) {
			log.exception(e);
		} catch (IncompleteBuilderSettingsException e) {
			log.exception(e);
		} catch (IOException e) {
			log.exception(e);
		} catch (IllegalBlockHeaderBytesRepresentation e) {
			log.exception(e);
		}
		return computedHash;
	}

}
