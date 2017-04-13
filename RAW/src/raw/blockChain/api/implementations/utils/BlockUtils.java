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
package raw.blockChain.api.implementations.utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import raw.blockChain.BlockChainCore;
import raw.blockChain.api.Block;
import raw.blockChain.api.BlockChainConstants;
import raw.blockChain.api.BlockHeader;
import raw.blockChain.api.HashValue;
import raw.blockChain.api.Hasher;
import raw.blockChain.api.Merkler;
import raw.blockChain.api.Transaction;
import raw.blockChain.api.implementations.DefaultBlockHeader;
import raw.blockChain.api.implementations.DefaultBlockMinerTask;
import raw.blockChain.api.implementations.DefaultHashValue;
import raw.blockChain.api.implementations.DefaultHasher;
import raw.blockChain.api.implementations.DefaultMerkler;
import raw.blockChain.api.implementations.DefaultTransaction;
import raw.blockChain.api.implementations.DefaultBlockHeader.Builder;
import raw.blockChain.exceptions.IllegalBlockHeaderBytesRepresentation;
import raw.blockChain.exceptions.IncompleteBuilderSettingsException;
import raw.blockChain.services.implementations.DefaultBlockChainCore;
import raw.concurrent.RAWExecutors;
import raw.logger.Log;
import raw.settings.BlockChainProperties;
import raw.settings.ModuleProperty;
import raw.settings.PropertiesManager;

import com.google.common.math.BigIntegerMath;

/**
 * Collection of utilities to be used dealing with blocks.
 * 
 * @author vic
 *
 */
public class BlockUtils {
	
	/**
	 * Return {@link BlockHeader#getBlockNumber()} + 1
	 * 
	 * @param header a {@link BlockHeader}
	 * @return header block number + 1
	 */
	public static long nextBlockNumber(BlockHeader header){
		return (header.getBlockNumber() + 1);
	}
	
	/**
	 * Compute the difficulty to be used for the block following
	 * the one identified by <tt>header</tt>
	 * @param header {@link BlockHeader} of previous block
	 * @return the correct difficulty for block following the one of <tt>header</tt>
	 */
	public static BigDecimal nextDifficulty(BlockHeader header){
		return nextDifficulty(header, null);
	}
		
	private static BigDecimal nextDifficulty(BlockHeader header, ArrayList<Block> chainBranch){
		if(nextBlockNumber(header)%BlockChainConstants.BLOCKS_FOR_DIFFICULTY_ADJUSTMENT != 0 || nextBlockNumber(header) == 0 ){
			return header.currentDifficulty();
		}
		long finalTimestamp = header.timestamp() / 1000;

		long firstOfFrameNumber = header.getBlockNumber() - BlockChainConstants.BLOCKS_FOR_DIFFICULTY_ADJUSTMENT + 1;
		BlockHeader firstOfFrame;
		if(chainBranch != null && firstOfFrameNumber >= chainBranch.get(0).getHeader().getBlockNumber()){
			int i = 0;
			while(chainBranch.get(i).getHeader().getBlockNumber() != firstOfFrameNumber){
				i++;
			}
			firstOfFrame = chainBranch.get(i).getHeader();
		} else {			
			BlockChainCore core = DefaultBlockChainCore.getBlockChainCore();
			firstOfFrame = core.getBlockHeaderByNumber(firstOfFrameNumber);
		}
		long firstTimestamp = firstOfFrame.timestamp() / 1000;
		
		BigDecimal coefficient = new BigDecimal(BlockChainConstants.BLOCKS_FOR_DIFFICULTY_ADJUSTMENT
				* BlockChainConstants.EXPECTED_SECONDS_PER_BLOCK).divide(
						new BigDecimal(finalTimestamp - firstTimestamp), 100, RoundingMode.HALF_EVEN);

		BigDecimal newDifficulty = header.currentDifficulty().multiply(
				coefficient).setScale(20, RoundingMode.HALF_EVEN);
		
		if(newDifficulty.compareTo(new BigDecimal(1)) < 0){
			newDifficulty = new BigDecimal(1);
		}

		return newDifficulty;
	}

	/**
	 * Given a difficulty convert it in a 0x00FFFFF like {@link HashValue} mask.
	 * 
	 * @param difficulty the difficulty to convert in a mask
	 * @return an {@link HashValue} mask to validate blocks
	 */
	public static HashValue targetMaskFromDifficulty(BigDecimal difficulty){
		int hashLenght = new DefaultHasher().hashLength();
	
		int maxTargetLength = (hashLenght*8)-BlockChainConstants.MAX_TARGET_ZERO_BITS;
		
		BigInteger integerMaxTarget = (new BigInteger("2")).pow(maxTargetLength).add(new BigInteger("-1"));
		
		BigInteger integerDifficulty = difficulty.
				setScale(1, RoundingMode.HALF_EVEN).
				setScale(0, RoundingMode.CEILING).toBigInteger();

		int shifts = BigIntegerMath.log2(integerDifficulty, RoundingMode.CEILING);
		
		BigInteger integerTarget = integerMaxTarget.shiftRight(shifts);
		byte[] target = integerTarget.toByteArray();
		
		
		if(target.length < hashLenght){
			byte[] newTarget = new byte[hashLenght];
			Arrays.fill(newTarget, (byte)0);
			for(int i = 0; i < target.length; i++){
				newTarget[newTarget.length-1-i] = target[target.length-1-i];
			}
			target = newTarget;
		}
		target[target.length-1] = (byte) 0xff; //avoid all zeros target.
		return new DefaultHashValue(target);
	}
	
	/**
	 * Validate the internal consistency of a block at the <b>current</b> timestamp.
	 * 
	 * @param block the block to be validated
	 * @return <tt>true</tt> if the block is valid. <tt>false</tt> otherwise.
	 */
	public static boolean validateBlock(Block block){
		long currentTimestamp = System.currentTimeMillis();
		return validateBlock(block, currentTimestamp);
	}
	
	/**
	 * Validate the internal consistency of a block without taking in
	 * account any particoular timestamp.
	 * 
	 * @param block the block to be validated
	 * @return <tt>true</tt> if the block is valid. <tt>false</tt> otherwise.
	 */
	public static boolean validateBlockNoTimestamp(Block block){
		BlockChainCore core = DefaultBlockChainCore.getBlockChainCore();
		Block previousBlock = core.getBlockByNumber(block.getHeader().getBlockNumber()-1);
		return validateConsecutiveBlocks(block, previousBlock, false, -1);
	}
	
	/**
	 * Validate the internal consistency of a block with regards of a certain timestamp.
	 * 
	 * @param block the block to be validated
	 * @param timestamp the timestamp to be used validating this block
	 * @return <tt>true</tt> if the block is valid. <tt>false</tt> otherwise.
	 */
	public static boolean validateBlock(Block block, long timestamp){
		BlockChainCore core = DefaultBlockChainCore.getBlockChainCore();
		return validateBlock(block, timestamp, core);
	}
	
	/**
	 * This method is just for unit testing. 
	 * 
	 * @param block
	 * @param timestamp
	 * @param core
	 * @return
	 */
	protected static boolean validateBlock(Block block, long timestamp, BlockChainCore core){
		Block previousBlock = core.getBlockByNumber(block.getHeader().getBlockNumber()-1);
		return validateConsecutiveBlocks(block, previousBlock, true, timestamp);
	}
	
	/**
	 * Validate the consecutive consistency of <tt>candidateBlock</tt> with regards
	 * to <tt>previousBlock</tt>. If  <tt>candidateBlock</tt> block number is 0 then
	 * <tt>previousBlock</tt> is not taken into account. If <tt>checkTimestamp</tt> is
	 * set to false then timestamp is not checked.
	 * 
	 * @param candidateBlock the {@link Block} to validate
	 * @param previousBlock the {@link Block} presumed to be followed by <tt>candidateBlock</tt> 
	 * @param checkTimestamp trigger the check of timestamp
	 * @param timestamp
	 * @return <tt>true</tt> if <tt>candidateBlock</tt> looks to be valid
	 */
	public static boolean validateConsecutiveBlocks(Block candidateBlock, Block previousBlock, boolean checkTimestamp, long timestamp){
		return validateConsecutiveBlocks(candidateBlock, previousBlock, checkTimestamp, timestamp, null);
	}
	
	/**
	 * Validate the consecutive consistency of <tt>candidateBlock</tt> with regards
	 * to <tt>previousBlock</tt>. If  <tt>candidateBlock</tt> block number is 0 then
	 * <tt>previousBlock</tt> is not taken into account. If <tt>checkTimestamp</tt> is
	 * set to false then timestamp is not checked.
	 * 
	 * @param candidateBlock the {@link Block} to validate
	 * @param previousBlock the {@link Block} presumed to be followed by <tt>candidateBlock</tt> 
	 * @param checkTimestamp trigger the check of timestamp
	 * @param timestamp
	 * @param chainBranch a branch in the chain to search for difficulty adjustments
	 * @return <tt>true</tt> if <tt>candidateBlock</tt> looks to be valid
	 */
	public static boolean validateConsecutiveBlocks(Block candidateBlock, Block previousBlock, boolean checkTimestamp, long timestamp, ArrayList<Block> chainBranch){
		BlockHeader header = candidateBlock.getHeader();
		ArrayList<Transaction> transactions = candidateBlock.getTransactions();
		
		Merkler tmpMerkler = new DefaultMerkler(1);
		HashValue root = tmpMerkler.getMerkleRoot(transactions);
		if(!header.merkleRoot().equals(root)){
			return false;
		}
		
		BlockChainProperties properties = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
		String chainName = properties.getBlockChainName();
		if(!header.getBlockChainName().equals(chainName)){
			return false;
		}
		
		Hasher hasher = new DefaultHasher();
		if(!hasher.hashBlockHeader(header).equals(header.hash())){
			return false;
		}
		
		if(header.getBlockNumber() > 0){
			if(previousBlock == null){
				return false;
			}
			
			if(!(header.getBlockNumber() == nextBlockNumber(previousBlock.getHeader()))){
				return false;
			}
			
			if(!header.previousBlock().equals(previousBlock.getHeader().hash())){
				return false;
			}
			
			if(!header.currentDifficulty().equals(nextDifficulty(previousBlock.getHeader(), chainBranch))){
				return false;
			}
		}
		
		HashValue masked = header.hash().maskWith(targetMaskFromDifficulty(header.currentDifficulty()));
		if(!header.hash().equals(masked)){
			return false;
		}
		
		if(checkTimestamp){			
			long currentTimestamp = timestamp;
			
			TimeZone timeZone = TimeZone.getDefault();
			currentTimestamp = currentTimestamp - timeZone.getOffset(currentTimestamp);
			if(Math.abs(currentTimestamp-header.timestamp()) > BlockChainConstants.MAXIMUM_TIMESTAMP_OFFSET){
				return false;
			}
		}
		
		return true;
	}
	
	public static Block generateGenesisBlock(String minerSignature){
		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
		transactions.add(DefaultTransaction.getNullTransaction());
		
		HashValue root = new DefaultMerkler(1).getMerkleRoot(transactions);
		byte[] zeros = new byte[new DefaultHasher().hashLength()];
		Arrays.fill(zeros, (byte)0);
		HashValue zeroHash = new DefaultHashValue(zeros);
		
		long timestamp = System.currentTimeMillis();
		TimeZone timeZone = TimeZone.getDefault();
		timestamp = timestamp - timeZone.getOffset(timestamp);
		
		DefaultBlockHeader.Builder previousHeaderBuilder = new Builder();
		BlockChainProperties properties = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
		previousHeaderBuilder.setBlockChainName(properties.getBlockChainName()).
		setBlockNumber(-1L).
		setDifficulty(new BigDecimal("1")).
		setMerkleRoot(root).
		setNonce(1).
		setPrevBlockHash(zeroHash).
		setTimestamp(timestamp).
		setMinerSignature(minerSignature);
		
		
		BlockHeader previousHeader = null;
		try {
			previousHeader = previousHeaderBuilder.build();
		} catch (ClassNotFoundException e) {
			Log.getLogger().exception(e);
		} catch (IncompleteBuilderSettingsException e) {
			Log.getLogger().exception(e);
		} catch (IOException e) {
			Log.getLogger().exception(e);
		} catch (IllegalBlockHeaderBytesRepresentation e) {
			Log.getLogger().exception(e);
		}
		
		ExecutorService service = RAWExecutors.newSingleThreadExecutor();
		
		Future<Block> previousBlockFuture = service.submit(new DefaultBlockMinerTask(previousHeader, transactions, null, minerSignature));
		
		Block genesis = null; 
		try {
			genesis = previousBlockFuture.get();
		} catch (InterruptedException e) {
			Log.getLogger().exception(e);
		} catch (ExecutionException e) {
			Log.getLogger().exception(e);
		}
		
		return genesis;
	}

}
