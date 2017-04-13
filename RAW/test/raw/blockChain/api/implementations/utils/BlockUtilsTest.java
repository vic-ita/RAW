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

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.codec.binary.Hex;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import raw.blockChain.BlockChainCore;
import raw.blockChain.api.Block;
import raw.blockChain.api.BlockChainConstants;
import raw.blockChain.api.BlockHeader;
import raw.blockChain.api.HashValue;
import raw.blockChain.api.Transaction;
import raw.blockChain.api.implementations.DefaultBlock;
import raw.blockChain.api.implementations.DefaultBlockHeader;
import raw.blockChain.api.implementations.DefaultHashValue;
import raw.blockChain.api.implementations.DefaultHasher;
import raw.blockChain.api.implementations.DefaultMerkler;
import raw.blockChain.api.implementations.DefaultTransaction;
import raw.blockChain.api.implementations.DefaultBlockHeader.Builder;
import raw.blockChain.api.implementations.utils.BlockUtils;
import raw.dht.DhtAddress;
import raw.dht.DhtID;
import raw.dht.implementations.DefaultDhtAddress;
import raw.dht.implementations.DefaultDhtHasher;
import raw.settings.BlockChainProperties;
import raw.settings.ModuleProperty;
import raw.settings.PropertiesManager;

public class BlockUtilsTest {

	private BlockHeader header;
	
	@Before
	public void setUp() throws Exception {
		long timestamp = System.currentTimeMillis();		
		DefaultBlockHeader.Builder builder = new Builder();
		header = builder.setBlockChainName("RAW_STD_BLOCKCHAIN").
		setBlockNumber(42).
		setDifficulty(new BigDecimal(1)).
		setMerkleRoot(new DefaultHashValue("1234567890adcdef")).
		setNonce(1).
		setPrevBlockHash(new DefaultHashValue("1234567890adcdef")).
		setTimestamp(timestamp).
		setMinerSignature("Test signature").build();
	}

	@After
	public void tearDown() throws Exception {
		header = null;
	}

	@Test
	public void testNextBlockNumber() {
		long next = BlockUtils.nextBlockNumber(header);
		assertEquals("Wrong number", 43, next);
	}

	@Test
	public void testMaskDifficultyOne() throws Exception {
		int hashLenght = new DefaultHasher().hashLength();
		
		byte [] maxTarget = new byte[hashLenght];
		Arrays.fill(maxTarget, (byte)0xFF);
		int fullZeroBytes = BlockChainConstants.MAX_TARGET_ZERO_BITS / 8;
		for(int i = 0; i < fullZeroBytes; i++){
			maxTarget[i] = 0;
		}
		if(BlockChainConstants.MAX_TARGET_ZERO_BITS % 8 != 0){
			int offset = BlockChainConstants.MAX_TARGET_ZERO_BITS % 8;
			int intByte = 255; 
			maxTarget[fullZeroBytes] = (byte) (intByte>>>offset);
		}
		HashValue maxTargetHash = new DefaultHashValue(maxTarget);
		
		BigDecimal difficulty = new BigDecimal("1");
		
		HashValue gotMask = BlockUtils.targetMaskFromDifficulty(difficulty);
		
		System.out.println("gotMask = "+gotMask.toHexString());
		
		assertEquals("Masks differ!!!", maxTargetHash, gotMask);
	}
	
	@Test
	public void testWrongMask() throws Exception {
		int hashLenght = new DefaultHasher().hashLength();
		
		byte [] maxTarget = new byte[hashLenght];
		Arrays.fill(maxTarget, (byte)0xFF);
		int fullZeroBytes = BlockChainConstants.MAX_TARGET_ZERO_BITS / 8;
		for(int i = 0; i < fullZeroBytes; i++){
			maxTarget[i] = 0;
		}
		if(BlockChainConstants.MAX_TARGET_ZERO_BITS % 8 != 0){
			int offset = BlockChainConstants.MAX_TARGET_ZERO_BITS % 8;
			int intByte = 255; 
			maxTarget[fullZeroBytes] = (byte) (intByte>>>offset);
		}
		HashValue maxTargetHash = new DefaultHashValue(maxTarget);
		
		BigDecimal difficulty = new BigDecimal("1.1");
		
		HashValue gotMask = BlockUtils.targetMaskFromDifficulty(difficulty);
		
		assertNotEquals("Masks do not differ!!!", maxTargetHash, gotMask);
	}
	
	@Test
	public void testMask01() throws Exception {
		int hashLenght = new DefaultHasher().hashLength();
		
		byte [] target = new byte[hashLenght];
		Arrays.fill(target, (byte)0xFF);
		int fullZeroBytes = BlockChainConstants.MAX_TARGET_ZERO_BITS / 8;
		for(int i = 0; i < fullZeroBytes; i++){
			target[i] = 0;
		}
		target[fullZeroBytes] = 0x0f;
		
		BigDecimal decimalTarget = new BigDecimal(new BigInteger(target));
		HashValue expectedTarget = new DefaultHashValue(target);
		
		byte [] maxTarget = new byte[hashLenght];
		Arrays.fill(maxTarget, (byte)0xFF);
		for(int i = 0; i < fullZeroBytes; i++){
			maxTarget[i] = 0;
		}
		if(BlockChainConstants.MAX_TARGET_ZERO_BITS % 8 != 0){
			int offset = BlockChainConstants.MAX_TARGET_ZERO_BITS % 8;
			int intByte = 255; 
			maxTarget[fullZeroBytes] = (byte) (intByte>>>offset);
		}
		
		BigDecimal decimalMaxTarget = new BigDecimal(new BigInteger(maxTarget));
		
		BigDecimal difficulty = decimalMaxTarget.divide(decimalTarget, 100, RoundingMode.HALF_UP);
		
//		System.out.println("difficulty = "+difficulty);
		
		HashValue returnedTarget = BlockUtils.targetMaskFromDifficulty(difficulty);
		
//		System.out.println("expected traget = " + expectedTarget.toHexString());
//		System.out.println("target returned = " + returnedTarget.toHexString());
		
		assertEquals("Wrong target!", expectedTarget, returnedTarget);
	}
	
	@Test
	public void testMask02() throws Exception {
		int hashLenght = new DefaultHasher().hashLength();
		
		byte [] target = new byte[hashLenght];
		Arrays.fill(target, (byte)0xFF);
		int fullZeroBytes = BlockChainConstants.MAX_TARGET_ZERO_BITS / 8;
		for(int i = 0; i < fullZeroBytes; i++){
			target[i] = 0;
		}
		target[fullZeroBytes] = 0x01;
		
		BigDecimal decimalTarget = new BigDecimal(new BigInteger(target));
		HashValue expectedTarget = new DefaultHashValue(target);
		
		byte [] maxTarget = new byte[hashLenght];
		Arrays.fill(maxTarget, (byte)0xFF);
		for(int i = 0; i < fullZeroBytes; i++){
			maxTarget[i] = 0;
		}
		if(BlockChainConstants.MAX_TARGET_ZERO_BITS % 8 != 0){
			int offset = BlockChainConstants.MAX_TARGET_ZERO_BITS % 8;
			int intByte = 255; 
			maxTarget[fullZeroBytes] = (byte) (intByte>>>offset);
		}
		
		BigDecimal decimalMaxTarget = new BigDecimal(new BigInteger(maxTarget));
		
		BigDecimal difficulty = decimalMaxTarget.divide(decimalTarget, 100, RoundingMode.HALF_UP);
		
//		System.out.println("difficulty = "+difficulty);
		
		HashValue returnedTarget = BlockUtils.targetMaskFromDifficulty(difficulty);
		
//		System.out.println("expected traget = " + expectedTarget.toHexString());
//		System.out.println("target returned = " + returnedTarget.toHexString());
		
		assertEquals("Wrong target!", expectedTarget, returnedTarget);
	}
	
	@Test
	public void testMask03() throws Exception {
		int hashLenght = new DefaultHasher().hashLength();
		
		byte [] target = new byte[hashLenght];
		Arrays.fill(target, (byte)0xFF);
		int fullZeroBytes = BlockChainConstants.MAX_TARGET_ZERO_BITS / 8;
		for(int i = 0; i < fullZeroBytes; i++){
			target[i] = 0;
		}
		target[fullZeroBytes] = 0x1f;
		
		BigDecimal decimalTarget = new BigDecimal(new BigInteger(target));
		HashValue expectedTarget = new DefaultHashValue(target);
		
		byte [] maxTarget = new byte[hashLenght];
		Arrays.fill(maxTarget, (byte)0xFF);
		for(int i = 0; i < fullZeroBytes; i++){
			maxTarget[i] = 0;
		}
		if(BlockChainConstants.MAX_TARGET_ZERO_BITS % 8 != 0){
			int offset = BlockChainConstants.MAX_TARGET_ZERO_BITS % 8;
			int intByte = 255; 
			maxTarget[fullZeroBytes] = (byte) (intByte>>>offset);
		}
		
		BigDecimal decimalMaxTarget = new BigDecimal(new BigInteger(maxTarget));
		
		BigDecimal difficulty = decimalMaxTarget.divide(decimalTarget, 100, RoundingMode.HALF_UP);
		
//		System.out.println("difficulty = "+difficulty);
		
		HashValue returnedTarget = BlockUtils.targetMaskFromDifficulty(difficulty);
		
//		System.out.println("expected traget = " + expectedTarget.toHexString());
//		System.out.println("target returned = " + returnedTarget.toHexString());
		
		assertEquals("Wrong target!", expectedTarget, returnedTarget);
	}
	
	@Test
	public void testMask04() throws Exception {
		int hashLenght = new DefaultHasher().hashLength();
		
		byte [] target = new byte[hashLenght];
		Arrays.fill(target, (byte)0xFF);
		int fullZeroBytes = BlockChainConstants.MAX_TARGET_ZERO_BITS / 8;
		for(int i = 0; i < fullZeroBytes; i++){
			target[i] = 0;
		}
		target[fullZeroBytes] = 0x07;
		
		BigDecimal decimalTarget = new BigDecimal(new BigInteger(target));
		HashValue expectedTarget = new DefaultHashValue(target);
		
		byte [] maxTarget = new byte[hashLenght];
		Arrays.fill(maxTarget, (byte)0xFF);
		for(int i = 0; i < fullZeroBytes; i++){
			maxTarget[i] = 0;
		}
		if(BlockChainConstants.MAX_TARGET_ZERO_BITS % 8 != 0){
			int offset = BlockChainConstants.MAX_TARGET_ZERO_BITS % 8;
			int intByte = 255; 
			maxTarget[fullZeroBytes] = (byte) (intByte>>>offset);
		}
		
		BigDecimal decimalMaxTarget = new BigDecimal(new BigInteger(maxTarget));
		
		BigDecimal difficulty = decimalMaxTarget.divide(decimalTarget, 100, RoundingMode.HALF_UP);
		
//		System.out.println("difficulty = "+difficulty);
		
		HashValue returnedTarget = BlockUtils.targetMaskFromDifficulty(difficulty);
		
//		System.out.println("expected traget = " + expectedTarget.toHexString());
//		System.out.println("target returned = " + returnedTarget.toHexString());
		
		assertEquals("Wrong target!", expectedTarget, returnedTarget);
	}
	
	@Test
	public void testMask05() throws Exception {
		int hashLenght = new DefaultHasher().hashLength();
		
		byte [] target = new byte[hashLenght];
		Arrays.fill(target, (byte)0xFF);
		int fullZeroBytes = BlockChainConstants.MAX_TARGET_ZERO_BITS / 8;
		for(int i = 0; i < fullZeroBytes; i++){
			target[i] = 0;
		}
		target[fullZeroBytes] = 0x7F;
		
		BigDecimal decimalTarget = new BigDecimal(new BigInteger(target));
		HashValue expectedTarget = new DefaultHashValue(target);
		
		byte [] maxTarget = new byte[hashLenght];
		Arrays.fill(maxTarget, (byte)0xFF);
		for(int i = 0; i < fullZeroBytes; i++){
			maxTarget[i] = 0;
		}
		if(BlockChainConstants.MAX_TARGET_ZERO_BITS % 8 != 0){
			int offset = BlockChainConstants.MAX_TARGET_ZERO_BITS % 8;
			int intByte = 255; 
			maxTarget[fullZeroBytes] = (byte) (intByte>>>offset);
		}
		
		BigDecimal decimalMaxTarget = new BigDecimal(new BigInteger(maxTarget));
		
		BigDecimal difficulty = decimalMaxTarget.divide(decimalTarget, 100, RoundingMode.HALF_UP);
		
//		System.out.println("difficulty = "+difficulty);
		
		HashValue returnedTarget = BlockUtils.targetMaskFromDifficulty(difficulty);
		
//		System.out.println("expected traget = " + expectedTarget.toHexString());
//		System.out.println("target returned = " + returnedTarget.toHexString());
		
		assertEquals("Wrong target!", expectedTarget, returnedTarget);
	}
	
	@Test
	public void testMask06() throws Exception {
		int hashLenght = new DefaultHasher().hashLength();
		
		byte [] target = new byte[hashLenght];
		Arrays.fill(target, (byte)0xFF);
		int fullZeroBytes = BlockChainConstants.MAX_TARGET_ZERO_BITS / 8;
		for(int i = 0; i < fullZeroBytes; i++){
			target[i] = 0;
		}
		target[fullZeroBytes] = 0x3F;
		
		BigDecimal decimalTarget = new BigDecimal(new BigInteger(target));
		HashValue expectedTarget = new DefaultHashValue(target);
		
		byte [] maxTarget = new byte[hashLenght];
		Arrays.fill(maxTarget, (byte)0xFF);
		for(int i = 0; i < fullZeroBytes; i++){
			maxTarget[i] = 0;
		}
		if(BlockChainConstants.MAX_TARGET_ZERO_BITS % 8 != 0){
			int offset = BlockChainConstants.MAX_TARGET_ZERO_BITS % 8;
			int intByte = 255; 
			maxTarget[fullZeroBytes] = (byte) (intByte>>>offset);
		}
		
		BigDecimal decimalMaxTarget = new BigDecimal(new BigInteger(maxTarget));
		
		BigDecimal difficulty = decimalMaxTarget.divide(decimalTarget, 100, RoundingMode.HALF_UP);
		
//		System.out.println("difficulty = "+difficulty);
		
		HashValue returnedTarget = BlockUtils.targetMaskFromDifficulty(difficulty);
		
//		System.out.println("expected traget = " + expectedTarget.toHexString());
//		System.out.println("target returned = " + returnedTarget.toHexString());
		
		assertEquals("Wrong target!", expectedTarget, returnedTarget);
	}
	
	@Test
	public void testMask07() throws Exception {
		int hashLenght = new DefaultHasher().hashLength();
		
		byte [] target = new byte[hashLenght];
		Arrays.fill(target, (byte)0xFF);
		int fullZeroBytes = BlockChainConstants.MAX_TARGET_ZERO_BITS / 8;
		for(int i = 0; i < fullZeroBytes; i++){
			target[i] = 0;
		}
		target[fullZeroBytes] = 0x03;
		
		BigDecimal decimalTarget = new BigDecimal(new BigInteger(target));
		HashValue expectedTarget = new DefaultHashValue(target);
		
		byte [] maxTarget = new byte[hashLenght];
		Arrays.fill(maxTarget, (byte)0xFF);
		for(int i = 0; i < fullZeroBytes; i++){
			maxTarget[i] = 0;
		}
		if(BlockChainConstants.MAX_TARGET_ZERO_BITS % 8 != 0){
			int offset = BlockChainConstants.MAX_TARGET_ZERO_BITS % 8;
			int intByte = 255; 
			maxTarget[fullZeroBytes] = (byte) (intByte>>>offset);
		}
		
		BigDecimal decimalMaxTarget = new BigDecimal(new BigInteger(maxTarget));
		
		BigDecimal difficulty = decimalMaxTarget.divide(decimalTarget, 100, RoundingMode.HALF_UP);
		
//		System.out.println("difficulty = "+difficulty);
		
		HashValue returnedTarget = BlockUtils.targetMaskFromDifficulty(difficulty);
		
//		System.out.println("expected traget = " + expectedTarget.toHexString());
//		System.out.println("target returned = " + returnedTarget.toHexString());
		
		assertEquals("Wrong target!", expectedTarget, returnedTarget);
	}
	
	@Test
	public void testMask11() throws Exception {
		int hashLenght = new DefaultHasher().hashLength();
		
		byte [] target = new byte[hashLenght];
		Arrays.fill(target, (byte)0xFF);
		int fullZeroBytes = BlockChainConstants.MAX_TARGET_ZERO_BITS / 8;
		for(int i = 0; i < fullZeroBytes; i++){
			target[i] = 0;
		}
		target[fullZeroBytes] = 0;
		target[fullZeroBytes+1] = 0x0f;
		
		BigDecimal decimalTarget = new BigDecimal(new BigInteger(target));
		HashValue expectedTarget = new DefaultHashValue(target);
		
		byte [] maxTarget = new byte[hashLenght];
		Arrays.fill(maxTarget, (byte)0xFF);
		for(int i = 0; i < fullZeroBytes; i++){
			maxTarget[i] = 0;
		}
		if(BlockChainConstants.MAX_TARGET_ZERO_BITS % 8 != 0){
			int offset = BlockChainConstants.MAX_TARGET_ZERO_BITS % 8;
			int intByte = 255; 
			maxTarget[fullZeroBytes] = (byte) (intByte>>>offset);
		}
		
		BigDecimal decimalMaxTarget = new BigDecimal(new BigInteger(maxTarget));
		
		BigDecimal difficulty = decimalMaxTarget.divide(decimalTarget, 100, RoundingMode.HALF_UP);
		
//		System.out.println("difficulty = "+difficulty);
		
		HashValue returnedTarget = BlockUtils.targetMaskFromDifficulty(difficulty);
		
//		System.out.println("expected traget = " + expectedTarget.toHexString());
//		System.out.println("target returned = " + returnedTarget.toHexString());
		
		assertEquals("Wrong target!", expectedTarget, returnedTarget);
	}
	
	@Test
	public void testMask12() throws Exception {
		int hashLenght = new DefaultHasher().hashLength();
		
		byte [] target = new byte[hashLenght];
		Arrays.fill(target, (byte)0xFF);
		int fullZeroBytes = BlockChainConstants.MAX_TARGET_ZERO_BITS / 8;
		for(int i = 0; i < fullZeroBytes; i++){
			target[i] = 0;
		}
		target[fullZeroBytes] = 0;
		target[fullZeroBytes+1] = 0x01;
		
		BigDecimal decimalTarget = new BigDecimal(new BigInteger(target));
		HashValue expectedTarget = new DefaultHashValue(target);
		
		byte [] maxTarget = new byte[hashLenght];
		Arrays.fill(maxTarget, (byte)0xFF);
		for(int i = 0; i < fullZeroBytes; i++){
			maxTarget[i] = 0;
		}
		if(BlockChainConstants.MAX_TARGET_ZERO_BITS % 8 != 0){
			int offset = BlockChainConstants.MAX_TARGET_ZERO_BITS % 8;
			int intByte = 255; 
			maxTarget[fullZeroBytes] = (byte) (intByte>>>offset);
		}
		
		BigDecimal decimalMaxTarget = new BigDecimal(new BigInteger(maxTarget));
		
		BigDecimal difficulty = decimalMaxTarget.divide(decimalTarget, 100, RoundingMode.HALF_UP);
		
//		System.out.println("difficulty = "+difficulty);
		
		HashValue returnedTarget = BlockUtils.targetMaskFromDifficulty(difficulty);
		
//		System.out.println("expected traget = " + expectedTarget.toHexString());
//		System.out.println("target returned = " + returnedTarget.toHexString());
		
		assertEquals("Wrong target!", expectedTarget, returnedTarget);
	}
	
	@Test
	public void testMask13() throws Exception {
		int hashLenght = new DefaultHasher().hashLength();
		
		byte [] target = new byte[hashLenght];
		Arrays.fill(target, (byte)0xFF);
		int fullZeroBytes = BlockChainConstants.MAX_TARGET_ZERO_BITS / 8;
		for(int i = 0; i < fullZeroBytes; i++){
			target[i] = 0;
		}
		target[fullZeroBytes] = 0;
		target[fullZeroBytes+1] = 0x1f;
		
		BigDecimal decimalTarget = new BigDecimal(new BigInteger(target));
		HashValue expectedTarget = new DefaultHashValue(target);
		
		byte [] maxTarget = new byte[hashLenght];
		Arrays.fill(maxTarget, (byte)0xFF);
		for(int i = 0; i < fullZeroBytes; i++){
			maxTarget[i] = 0;
		}
		if(BlockChainConstants.MAX_TARGET_ZERO_BITS % 8 != 0){
			int offset = BlockChainConstants.MAX_TARGET_ZERO_BITS % 8;
			int intByte = 255; 
			maxTarget[fullZeroBytes] = (byte) (intByte>>>offset);
		}
		
		BigDecimal decimalMaxTarget = new BigDecimal(new BigInteger(maxTarget));
		
		BigDecimal difficulty = decimalMaxTarget.divide(decimalTarget, 100, RoundingMode.HALF_UP);
		
//		System.out.println("difficulty = "+difficulty);
		
		HashValue returnedTarget = BlockUtils.targetMaskFromDifficulty(difficulty);
		
//		System.out.println("expected traget = " + expectedTarget.toHexString());
//		System.out.println("target returned = " + returnedTarget.toHexString());
		
		assertEquals("Wrong target!", expectedTarget, returnedTarget);
	}
	
	@Test
	public void testMask14() throws Exception {
		int hashLenght = new DefaultHasher().hashLength();
		
		byte [] target = new byte[hashLenght];
		Arrays.fill(target, (byte)0xFF);
		int fullZeroBytes = BlockChainConstants.MAX_TARGET_ZERO_BITS / 8;
		for(int i = 0; i < fullZeroBytes; i++){
			target[i] = 0;
		}
		target[fullZeroBytes] = 0;
		target[fullZeroBytes+1] = 0x07;
		
		BigDecimal decimalTarget = new BigDecimal(new BigInteger(target));
		HashValue expectedTarget = new DefaultHashValue(target);
		
		byte [] maxTarget = new byte[hashLenght];
		Arrays.fill(maxTarget, (byte)0xFF);
		for(int i = 0; i < fullZeroBytes; i++){
			maxTarget[i] = 0;
		}
		if(BlockChainConstants.MAX_TARGET_ZERO_BITS % 8 != 0){
			int offset = BlockChainConstants.MAX_TARGET_ZERO_BITS % 8;
			int intByte = 255; 
			maxTarget[fullZeroBytes] = (byte) (intByte>>>offset);
		}
		
		BigDecimal decimalMaxTarget = new BigDecimal(new BigInteger(maxTarget));
		
		BigDecimal difficulty = decimalMaxTarget.divide(decimalTarget, 100, RoundingMode.HALF_UP);
		
//		System.out.println("difficulty = "+difficulty);
		
		HashValue returnedTarget = BlockUtils.targetMaskFromDifficulty(difficulty);
		
//		System.out.println("expected traget = " + expectedTarget.toHexString());
//		System.out.println("target returned = " + returnedTarget.toHexString());
		
		assertEquals("Wrong target!", expectedTarget, returnedTarget);
	}
	
	@Test
	public void testMask15() throws Exception {
		int hashLenght = new DefaultHasher().hashLength();
		
		byte [] target = new byte[hashLenght];
		Arrays.fill(target, (byte)0xFF);
		int fullZeroBytes = BlockChainConstants.MAX_TARGET_ZERO_BITS / 8;
		for(int i = 0; i < fullZeroBytes; i++){
			target[i] = 0;
		}
		target[fullZeroBytes] = 0;
		target[fullZeroBytes+1] = 0x7F;
		
		BigDecimal decimalTarget = new BigDecimal(new BigInteger(target));
		HashValue expectedTarget = new DefaultHashValue(target);
		
		byte [] maxTarget = new byte[hashLenght];
		Arrays.fill(maxTarget, (byte)0xFF);
		for(int i = 0; i < fullZeroBytes; i++){
			maxTarget[i] = 0;
		}
		if(BlockChainConstants.MAX_TARGET_ZERO_BITS % 8 != 0){
			int offset = BlockChainConstants.MAX_TARGET_ZERO_BITS % 8;
			int intByte = 255; 
			maxTarget[fullZeroBytes] = (byte) (intByte>>>offset);
		}
		
		BigDecimal decimalMaxTarget = new BigDecimal(new BigInteger(maxTarget));
		
		BigDecimal difficulty = decimalMaxTarget.divide(decimalTarget, 100, RoundingMode.HALF_UP);
		
//		System.out.println("difficulty = "+difficulty);
		
		HashValue returnedTarget = BlockUtils.targetMaskFromDifficulty(difficulty);
		
//		System.out.println("expected traget = " + expectedTarget.toHexString());
//		System.out.println("target returned = " + returnedTarget.toHexString());
		
		assertEquals("Wrong target!", expectedTarget, returnedTarget);
	}
	
	@Test
	public void testMask16() throws Exception {
		int hashLenght = new DefaultHasher().hashLength();
		
		byte [] target = new byte[hashLenght];
		Arrays.fill(target, (byte)0xFF);
		int fullZeroBytes = BlockChainConstants.MAX_TARGET_ZERO_BITS / 8;
		for(int i = 0; i < fullZeroBytes; i++){
			target[i] = 0;
		}
		target[fullZeroBytes] = 0;
		target[fullZeroBytes+1] = 0x3F;
		
		BigDecimal decimalTarget = new BigDecimal(new BigInteger(target));
		HashValue expectedTarget = new DefaultHashValue(target);
		
		byte [] maxTarget = new byte[hashLenght];
		Arrays.fill(maxTarget, (byte)0xFF);
		for(int i = 0; i < fullZeroBytes; i++){
			maxTarget[i] = 0;
		}
		if(BlockChainConstants.MAX_TARGET_ZERO_BITS % 8 != 0){
			int offset = BlockChainConstants.MAX_TARGET_ZERO_BITS % 8;
			int intByte = 255; 
			maxTarget[fullZeroBytes] = (byte) (intByte>>>offset);
		}
		
		BigDecimal decimalMaxTarget = new BigDecimal(new BigInteger(maxTarget));
		
		BigDecimal difficulty = decimalMaxTarget.divide(decimalTarget, 100, RoundingMode.HALF_UP);
		
//		System.out.println("difficulty = "+difficulty);
		
		HashValue returnedTarget = BlockUtils.targetMaskFromDifficulty(difficulty);
		
//		System.out.println("expected traget = " + expectedTarget.toHexString());
//		System.out.println("target returned = " + returnedTarget.toHexString());
		
		assertEquals("Wrong target!", expectedTarget, returnedTarget);
	}
	
	@Test
	public void testMask17() throws Exception {
		int hashLenght = new DefaultHasher().hashLength();
		
		byte [] target = new byte[hashLenght];
		Arrays.fill(target, (byte)0xFF);
		int fullZeroBytes = BlockChainConstants.MAX_TARGET_ZERO_BITS / 8;
		for(int i = 0; i < fullZeroBytes; i++){
			target[i] = 0;
		}
		target[fullZeroBytes] = 0;
		target[fullZeroBytes+1] = 0x03;
		
		BigDecimal decimalTarget = new BigDecimal(new BigInteger(target));
		HashValue expectedTarget = new DefaultHashValue(target);
		
		byte [] maxTarget = new byte[hashLenght];
		Arrays.fill(maxTarget, (byte)0xFF);
		for(int i = 0; i < fullZeroBytes; i++){
			maxTarget[i] = 0;
		}
		if(BlockChainConstants.MAX_TARGET_ZERO_BITS % 8 != 0){
			int offset = BlockChainConstants.MAX_TARGET_ZERO_BITS % 8;
			int intByte = 255; 
			maxTarget[fullZeroBytes] = (byte) (intByte>>>offset);
		}
		
		BigDecimal decimalMaxTarget = new BigDecimal(new BigInteger(maxTarget));
		
		BigDecimal difficulty = decimalMaxTarget.divide(decimalTarget, 100, RoundingMode.HALF_UP);
		
//		System.out.println("difficulty = "+difficulty);
		
		HashValue returnedTarget = BlockUtils.targetMaskFromDifficulty(difficulty);
		
//		System.out.println("expected traget = " + expectedTarget.toHexString());
//		System.out.println("target returned = " + returnedTarget.toHexString());
		
		assertEquals("Wrong target!", expectedTarget, returnedTarget);
	}
	
	@Test
	public void testMask21() throws Exception {
		int hashLenght = new DefaultHasher().hashLength();
		
		byte [] target = new byte[hashLenght];
		Arrays.fill(target, (byte)0xFF);
		int fullZeroBytes = BlockChainConstants.MAX_TARGET_ZERO_BITS / 8;
		for(int i = 0; i < fullZeroBytes; i++){
			target[i] = 0;
		}
		target[fullZeroBytes] = 0;
		target[fullZeroBytes+1] = 0;
		target[fullZeroBytes+2] = 0;
		target[fullZeroBytes+3] = 0;
		target[fullZeroBytes+4] = 0x0f;
		
		BigDecimal decimalTarget = new BigDecimal(new BigInteger(target));
		HashValue expectedTarget = new DefaultHashValue(target);
		
		byte [] maxTarget = new byte[hashLenght];
		Arrays.fill(maxTarget, (byte)0xFF);
		for(int i = 0; i < fullZeroBytes; i++){
			maxTarget[i] = 0;
		}
		if(BlockChainConstants.MAX_TARGET_ZERO_BITS % 8 != 0){
			int offset = BlockChainConstants.MAX_TARGET_ZERO_BITS % 8;
			int intByte = 255; 
			maxTarget[fullZeroBytes] = (byte) (intByte>>>offset);
		}
		
		BigDecimal decimalMaxTarget = new BigDecimal(new BigInteger(maxTarget));
		
		BigDecimal difficulty = decimalMaxTarget.divide(decimalTarget, 100, RoundingMode.HALF_UP);
		
//		System.out.println("difficulty = "+difficulty);
		
		HashValue returnedTarget = BlockUtils.targetMaskFromDifficulty(difficulty);
		
//		System.out.println("expected traget = " + expectedTarget.toHexString());
//		System.out.println("target returned = " + returnedTarget.toHexString());
		
		assertEquals("Wrong target!", expectedTarget, returnedTarget);
	}
	
	@Test
	public void testMask22() throws Exception {
		int hashLenght = new DefaultHasher().hashLength();
		
		byte [] target = new byte[hashLenght];
		Arrays.fill(target, (byte)0xFF);
		int fullZeroBytes = BlockChainConstants.MAX_TARGET_ZERO_BITS / 8;
		for(int i = 0; i < fullZeroBytes; i++){
			target[i] = 0;
		}
		target[fullZeroBytes] = 0;
		target[fullZeroBytes+1] = 0;
		target[fullZeroBytes+2] = 0;
		target[fullZeroBytes+3] = 0;
		target[fullZeroBytes+4] = 0;
		target[fullZeroBytes+5] = 0;
		target[fullZeroBytes+6] = 0x01;
		
		BigDecimal decimalTarget = new BigDecimal(new BigInteger(target));
		HashValue expectedTarget = new DefaultHashValue(target);
		
		byte [] maxTarget = new byte[hashLenght];
		Arrays.fill(maxTarget, (byte)0xFF);
		for(int i = 0; i < fullZeroBytes; i++){
			maxTarget[i] = 0;
		}
		if(BlockChainConstants.MAX_TARGET_ZERO_BITS % 8 != 0){
			int offset = BlockChainConstants.MAX_TARGET_ZERO_BITS % 8;
			int intByte = 255; 
			maxTarget[fullZeroBytes] = (byte) (intByte>>>offset);
		}
		
		BigDecimal decimalMaxTarget = new BigDecimal(new BigInteger(maxTarget));
		
		BigDecimal difficulty = decimalMaxTarget.divide(decimalTarget, 100, RoundingMode.HALF_UP);
		
//		System.out.println("difficulty = "+difficulty);
		
		HashValue returnedTarget = BlockUtils.targetMaskFromDifficulty(difficulty);
		
//		System.out.println("expected traget = " + expectedTarget.toHexString());
//		System.out.println("target returned = " + returnedTarget.toHexString());
		
		assertEquals("Wrong target!", expectedTarget, returnedTarget);
	}
	
	@Test
	public void testMask23() throws Exception {
		int hashLenght = new DefaultHasher().hashLength();
		
		byte [] target = new byte[hashLenght];
		Arrays.fill(target, (byte)0xFF);
		int fullZeroBytes = BlockChainConstants.MAX_TARGET_ZERO_BITS / 8;
		for(int i = 0; i < fullZeroBytes; i++){
			target[i] = 0;
		}
		target[fullZeroBytes] = 0;
		target[fullZeroBytes+1] = 0;
		target[fullZeroBytes+2] = 0;
		target[fullZeroBytes+3] = 0;
		target[fullZeroBytes+4] = 0;
		target[fullZeroBytes+5] = 0;
		target[fullZeroBytes+6] = 0;
		target[fullZeroBytes+7] = 0;
		target[fullZeroBytes+8] = 0x1f;
		
		BigDecimal decimalTarget = new BigDecimal(new BigInteger(target));
		HashValue expectedTarget = new DefaultHashValue(target);
		
		byte [] maxTarget = new byte[hashLenght];
		Arrays.fill(maxTarget, (byte)0xFF);
		for(int i = 0; i < fullZeroBytes; i++){
			maxTarget[i] = 0;
		}
		if(BlockChainConstants.MAX_TARGET_ZERO_BITS % 8 != 0){
			int offset = BlockChainConstants.MAX_TARGET_ZERO_BITS % 8;
			int intByte = 255; 
			maxTarget[fullZeroBytes] = (byte) (intByte>>>offset);
		}
		
		BigDecimal decimalMaxTarget = new BigDecimal(new BigInteger(maxTarget));
		
		BigDecimal difficulty = decimalMaxTarget.divide(decimalTarget, 100, RoundingMode.HALF_UP);
		
//		System.out.println("difficulty = "+difficulty);
		
		HashValue returnedTarget = BlockUtils.targetMaskFromDifficulty(difficulty);
		
//		System.out.println("expected traget = " + expectedTarget.toHexString());
//		System.out.println("target returned = " + returnedTarget.toHexString());
		
		assertEquals("Wrong target!", expectedTarget, returnedTarget);
	}
	
	@Test
	public void testMask24() throws Exception {
		int hashLenght = new DefaultHasher().hashLength();
		
		byte [] target = new byte[hashLenght];
		Arrays.fill(target, (byte)0xFF);
		int fullZeroBytes = BlockChainConstants.MAX_TARGET_ZERO_BITS / 8;
		for(int i = 0; i < fullZeroBytes; i++){
			target[i] = 0;
		}
		target[fullZeroBytes] = 0;
		target[fullZeroBytes+1] = 0;
		target[fullZeroBytes+2] = 0;
		target[fullZeroBytes+3] = 0;
		target[fullZeroBytes+4] = 0;
		target[fullZeroBytes+5] = 0;
		target[fullZeroBytes+6] = 0;
		target[fullZeroBytes+7] = 0;
		target[fullZeroBytes+8] = 0;
		target[fullZeroBytes+9] = 0x07;
		
		BigDecimal decimalTarget = new BigDecimal(new BigInteger(target));
		HashValue expectedTarget = new DefaultHashValue(target);
		
		byte [] maxTarget = new byte[hashLenght];
		Arrays.fill(maxTarget, (byte)0xFF);
		for(int i = 0; i < fullZeroBytes; i++){
			maxTarget[i] = 0;
		}
		if(BlockChainConstants.MAX_TARGET_ZERO_BITS % 8 != 0){
			int offset = BlockChainConstants.MAX_TARGET_ZERO_BITS % 8;
			int intByte = 255; 
			maxTarget[fullZeroBytes] = (byte) (intByte>>>offset);
		}
		
		BigDecimal decimalMaxTarget = new BigDecimal(new BigInteger(maxTarget));
		
		BigDecimal difficulty = decimalMaxTarget.divide(decimalTarget, 100, RoundingMode.HALF_UP);
		
//		System.out.println("difficulty = "+difficulty);
		
		HashValue returnedTarget = BlockUtils.targetMaskFromDifficulty(difficulty);
		
//		System.out.println("expected traget = " + expectedTarget.toHexString());
//		System.out.println("target returned = " + returnedTarget.toHexString());
		
		assertEquals("Wrong target!", expectedTarget, returnedTarget);
	}
	
	@Test
	public void testMask25() throws Exception {
		int hashLenght = new DefaultHasher().hashLength();
		
		byte [] target = new byte[hashLenght];
		Arrays.fill(target, (byte)0xFF);
		int fullZeroBytes = BlockChainConstants.MAX_TARGET_ZERO_BITS / 8;
		for(int i = 0; i < fullZeroBytes; i++){
			target[i] = 0;
		}
		target[fullZeroBytes] = 0;
		target[fullZeroBytes+1] = 0;
		target[fullZeroBytes+2] = 0;
		target[fullZeroBytes+3] = 0;
		target[fullZeroBytes+4] = 0;
		target[fullZeroBytes+5] = 0x7F;
		
		BigDecimal decimalTarget = new BigDecimal(new BigInteger(target));
		HashValue expectedTarget = new DefaultHashValue(target);
		
		byte [] maxTarget = new byte[hashLenght];
		Arrays.fill(maxTarget, (byte)0xFF);
		for(int i = 0; i < fullZeroBytes; i++){
			maxTarget[i] = 0;
		}
		if(BlockChainConstants.MAX_TARGET_ZERO_BITS % 8 != 0){
			int offset = BlockChainConstants.MAX_TARGET_ZERO_BITS % 8;
			int intByte = 255; 
			maxTarget[fullZeroBytes] = (byte) (intByte>>>offset);
		}
		
		BigDecimal decimalMaxTarget = new BigDecimal(new BigInteger(maxTarget));
		
		BigDecimal difficulty = decimalMaxTarget.divide(decimalTarget, 100, RoundingMode.HALF_UP);
		
//		System.out.println("difficulty = "+difficulty);
		
		HashValue returnedTarget = BlockUtils.targetMaskFromDifficulty(difficulty);
		
//		System.out.println("expected traget = " + expectedTarget.toHexString());
//		System.out.println("target returned = " + returnedTarget.toHexString());
		
		assertEquals("Wrong target!", expectedTarget, returnedTarget);
	}
	
	@Test
	public void testMask26() throws Exception {
		int hashLenght = new DefaultHasher().hashLength();
		
		byte [] target = new byte[hashLenght];
		Arrays.fill(target, (byte)0xFF);
		int fullZeroBytes = BlockChainConstants.MAX_TARGET_ZERO_BITS / 8;
		for(int i = 0; i < fullZeroBytes; i++){
			target[i] = 0;
		}
		target[fullZeroBytes] = 0;
		target[fullZeroBytes+1] = 0;
		target[fullZeroBytes+2] = 0;
		target[fullZeroBytes+3] = 0x3F;
		
		BigDecimal decimalTarget = new BigDecimal(new BigInteger(target));
		HashValue expectedTarget = new DefaultHashValue(target);
		
		byte [] maxTarget = new byte[hashLenght];
		Arrays.fill(maxTarget, (byte)0xFF);
		for(int i = 0; i < fullZeroBytes; i++){
			maxTarget[i] = 0;
		}
		if(BlockChainConstants.MAX_TARGET_ZERO_BITS % 8 != 0){
			int offset = BlockChainConstants.MAX_TARGET_ZERO_BITS % 8;
			int intByte = 255; 
			maxTarget[fullZeroBytes] = (byte) (intByte>>>offset);
		}
		
		BigDecimal decimalMaxTarget = new BigDecimal(new BigInteger(maxTarget));
		
		BigDecimal difficulty = decimalMaxTarget.divide(decimalTarget, 100, RoundingMode.HALF_UP);
		
//		System.out.println("difficulty = "+difficulty);
		
		HashValue returnedTarget = BlockUtils.targetMaskFromDifficulty(difficulty);
		
//		System.out.println("expected traget = " + expectedTarget.toHexString());
//		System.out.println("target returned = " + returnedTarget.toHexString());
		
		assertEquals("Wrong target!", expectedTarget, returnedTarget);
	}
	
	@Test
	public void testMask27() throws Exception {
		int hashLenght = new DefaultHasher().hashLength();
		
		byte [] target = new byte[hashLenght];
		Arrays.fill(target, (byte)0xFF);
		int fullZeroBytes = BlockChainConstants.MAX_TARGET_ZERO_BITS / 8;
		for(int i = 0; i < fullZeroBytes; i++){
			target[i] = 0;
		}
		target[fullZeroBytes] = 0;
		target[fullZeroBytes+1] = 0;
		target[fullZeroBytes+2] = 0;
		target[fullZeroBytes+3] = 0;
		target[fullZeroBytes+4] = 0;
		target[fullZeroBytes+5] = 0;
		target[fullZeroBytes+6] = 0;
		target[fullZeroBytes+7] = 0;
		target[fullZeroBytes+8] = 0;
		target[fullZeroBytes+9] = 0;
		target[fullZeroBytes+10] = 0x03;
		
		BigDecimal decimalTarget = new BigDecimal(new BigInteger(target));
		HashValue expectedTarget = new DefaultHashValue(target);
		
		byte [] maxTarget = new byte[hashLenght];
		Arrays.fill(maxTarget, (byte)0xFF);
		for(int i = 0; i < fullZeroBytes; i++){
			maxTarget[i] = 0;
		}
		if(BlockChainConstants.MAX_TARGET_ZERO_BITS % 8 != 0){
			int offset = BlockChainConstants.MAX_TARGET_ZERO_BITS % 8;
			int intByte = 255; 
			maxTarget[fullZeroBytes] = (byte) (intByte>>>offset);
		}
		
		BigDecimal decimalMaxTarget = new BigDecimal(new BigInteger(maxTarget));
		
		BigDecimal difficulty = decimalMaxTarget.divide(decimalTarget, 100, RoundingMode.HALF_UP);
		
		System.out.println("difficulty = "+difficulty);
		
		HashValue returnedTarget = BlockUtils.targetMaskFromDifficulty(difficulty);
		
		System.out.println("expected traget = " + expectedTarget.toHexString());
		System.out.println("target returned = " + returnedTarget.toHexString());
		
		assertEquals("Wrong target!", expectedTarget, returnedTarget);
	}
	
	//this test is commented because it is very time-consuming.
//	@Test
//	public void testGenerateGenesisBlock() throws Exception {
//		Block genesis = BlockUtils.generateGenesisBlock("Test signature");
//		assertNotNull(genesis);
//		System.out.println("Bytes representation: "+Hex.encodeHexString(genesis.getHeader().getBytes()));
//	}
	
	@Test
	public void testValidateBlock() throws Exception {
//		String stringBytesOfGenesis = "aced0005757200025b42acf317f8060854e0020000787000000040000000067e2ce0296556919a65d1e3ded25e3b9f57b77af1c482a8ab4c5b0ffc31976a932ac1498d1c178bbe3f59c3587fed1a6708eca1e7d5fedf7ec1ddd557737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b02000078700000000174001153345f5354445f424c4f434b434841494e7571007e000000000040d2a944a2d6539ca555a0de36765f110723a97084397b22c6d4dcbcc584bfd364198bd980f90fabbc378e8dc5d2e9e93bd76d3f32155ee62d4843cfcd4434bb617571007e0000000000406fa749bdc2f9c072b410c76b8a2716a1e966034d8fc5ec6aac41e7cb5fb455f562cd6596d9a8335baca8f3fbfa4460ae6a41d86fa8d3c0cfe30aba0d8ba14e9f7372000e6a6176612e6c616e672e4c6f6e673b8be490cc8f23df0200014a000576616c75657871007e000300000000000000007371007e00080000014cbced34fa737200146a6176612e6d6174682e426967446563696d616c54c71557f981284f0300024900057363616c654c0006696e7456616c7400164c6a6176612f6d6174682f426967496e74656765723b7871007e000300000000737200146a6176612e6d6174682e426967496e74656765728cfc9f1fa93bfb1d030006490008626974436f756e744900096269744c656e67746849001366697273744e6f6e7a65726f427974654e756d49000c6c6f776573745365744269744900067369676e756d5b00096d61676e69747564657400025b427871007e0003fffffffffffffffffffffffefffffffe000000017571007e0000000000010178787371007e0002c9732c48";
		String stringBytesOfGenesis = "aced0005757200025b42acf317f8060854e0020000787000000040000000f27e9fa6e675a5718f4d8aff8c670d100d90fdda21f1fe53cb2d7da72fb5436f1cb1e590b03c206ca0b01fa9ae6fbe00cd87235688aa0087573b9be2d8737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b02000078700000000174001153345f5354445f424c4f434b434841494e7571007e0000000000408a76cfaba0cfecd200eeef8263372e689c43c2d5575dd2e5d3d897f2e799ea130a93a0753d57f7c4a3f67c37539981939a56eb6bc71f8e8818fd5cc3083d869d7571007e0000000000406fa749bdc2f9c072b410c76b8a2716a1e966034d8fc5ec6aac41e7cb5fb455f562cd6596d9a8335baca8f3fbfa4460ae6a41d86fa8d3c0cfe30aba0d8ba14e9f7372000e6a6176612e6c616e672e4c6f6e673b8be490cc8f23df0200014a000576616c75657871007e000300000000000000007371007e00080000014dfc40f580737200146a6176612e6d6174682e426967446563696d616c54c71557f981284f0300024900057363616c654c0006696e7456616c7400164c6a6176612f6d6174682f426967496e74656765723b7871007e000300000000737200146a6176612e6d6174682e426967496e74656765728cfc9f1fa93bfb1d030006490008626974436f756e744900096269744c656e67746849001366697273744e6f6e7a65726f427974654e756d49000c6c6f776573745365744269744900067369676e756d5b00096d61676e69747564657400025b427871007e0003fffffffffffffffffffffffefffffffe000000017571007e0000000000010178787371007e00025f6c624574000e54657374207369676e6174757265";
		byte[] bytesOfGenesis = Hex.decodeHex(stringBytesOfGenesis.toCharArray());
		
		ArrayList<Transaction> singleTransactionList = new ArrayList<Transaction>();
		singleTransactionList.add(DefaultTransaction.getNullTransaction());
		
		BlockHeader genesisHeader = new DefaultBlockHeader.Builder(bytesOfGenesis).build();
		
		Block genesis = new DefaultBlock(genesisHeader, singleTransactionList);
		
//		long timestamp = System.currentTimeMillis();
//		System.out.println("timestamp = "+timestamp);
		long timestamp = 1429106372715L;
		
//		String stringBytesOfPrevious = "aced0005757200025b42acf317f8060854e0020000787000000040d2a944a2d6539ca555a0de36765f110723a97084397b22c6d4dcbcc584bfd364198bd980f90fabbc378e8dc5d2e9e93bd76d3f32155ee62d4843cfcd4434bb61737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b02000078700000000174001153345f5354445f424c4f434b434841494e7571007e000000000040000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000007571007e0000000000406fa749bdc2f9c072b410c76b8a2716a1e966034d8fc5ec6aac41e7cb5fb455f562cd6596d9a8335baca8f3fbfa4460ae6a41d86fa8d3c0cfe30aba0d8ba14e9f7372000e6a6176612e6c616e672e4c6f6e673b8be490cc8f23df0200014a000576616c75657871007e0003ffffffffffffffff7371007e00080000014cbced34de737200146a6176612e6d6174682e426967446563696d616c54c71557f981284f0300024900057363616c654c0006696e7456616c7400164c6a6176612f6d6174682f426967496e74656765723b7871007e000300000000737200146a6176612e6d6174682e426967496e74656765728cfc9f1fa93bfb1d030006490008626974436f756e744900096269744c656e67746849001366697273744e6f6e7a65726f427974654e756d49000c6c6f776573745365744269744900067369676e756d5b00096d61676e69747564657400025b427871007e0003fffffffffffffffffffffffefffffffe000000017571007e00000000000101787871007e0004";
		String stringBytesOfPrevious = "aced0005757200025b42acf317f8060854e0020000787000000040000000f27e9fa6e675a5718f4d8aff8c670d100d90fdda21f1fe53cb2d7da72fb5436f1cb1e590b03c206ca0b01fa9ae6fbe00cd87235688aa0087573b9be2d8737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b02000078700000000174001153345f5354445f424c4f434b434841494e7571007e0000000000408a76cfaba0cfecd200eeef8263372e689c43c2d5575dd2e5d3d897f2e799ea130a93a0753d57f7c4a3f67c37539981939a56eb6bc71f8e8818fd5cc3083d869d7571007e0000000000406fa749bdc2f9c072b410c76b8a2716a1e966034d8fc5ec6aac41e7cb5fb455f562cd6596d9a8335baca8f3fbfa4460ae6a41d86fa8d3c0cfe30aba0d8ba14e9f7372000e6a6176612e6c616e672e4c6f6e673b8be490cc8f23df0200014a000576616c75657871007e000300000000000000007371007e00080000014dfc40f580737200146a6176612e6d6174682e426967446563696d616c54c71557f981284f0300024900057363616c654c0006696e7456616c7400164c6a6176612f6d6174682f426967496e74656765723b7871007e000300000000737200146a6176612e6d6174682e426967496e74656765728cfc9f1fa93bfb1d030006490008626974436f756e744900096269744c656e67746849001366697273744e6f6e7a65726f427974654e756d49000c6c6f776573745365744269744900067369676e756d5b00096d61676e69747564657400025b427871007e0003fffffffffffffffffffffffefffffffe000000017571007e0000000000010178787371007e00025f6c624574000e54657374207369676e6174757265";
		byte[] bytesOfPrevious = Hex.decodeHex(stringBytesOfPrevious.toCharArray());
//		long previousTimestamp = 1429098804446L;
		
		BlockHeader previousHeader = new DefaultBlockHeader.Builder(bytesOfPrevious).build();
		
		Block previous = new DefaultBlock(previousHeader, singleTransactionList);
		
		BlockChainCore coreMock = EasyMock.createMock(BlockChainCore.class);
		EasyMock.expect(coreMock.getLastBlockInChain()).andReturn(previous);
		EasyMock.replay(coreMock);
		
		assertTrue("The block should be valid!!", BlockUtils.validateBlock(genesis, timestamp, coreMock));
	}
	
	@Test
	public void testValidateInvalidBlock01() throws Exception {
//		String stringBytesOfGenesis = "aced0005757200025b42acf317f8060854e0020000787000000040000000067e2ce0296556919a65d1e3ded25e3b9f57b77af1c482a8ab4c5b0ffc31976a932ac1498d1c178bbe3f59c3587fed1a6708eca1e7d5fedf7ec1ddd557737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b02000078700000000174001153345f5354445f424c4f434b434841494e7571007e000000000040d2a944a2d6539ca555a0de36765f110723a97084397b22c6d4dcbcc584bfd364198bd980f90fabbc378e8dc5d2e9e93bd76d3f32155ee62d4843cfcd4434bb617571007e0000000000406fa749bdc2f9c072b410c76b8a2716a1e966034d8fc5ec6aac41e7cb5fb455f562cd6596d9a8335baca8f3fbfa4460ae6a41d86fa8d3c0cfe30aba0d8ba14e9f7372000e6a6176612e6c616e672e4c6f6e673b8be490cc8f23df0200014a000576616c75657871007e000300000000000000007371007e00080000014cbced34fa737200146a6176612e6d6174682e426967446563696d616c54c71557f981284f0300024900057363616c654c0006696e7456616c7400164c6a6176612f6d6174682f426967496e74656765723b7871007e000300000000737200146a6176612e6d6174682e426967496e74656765728cfc9f1fa93bfb1d030006490008626974436f756e744900096269744c656e67746849001366697273744e6f6e7a65726f427974654e756d49000c6c6f776573745365744269744900067369676e756d5b00096d61676e69747564657400025b427871007e0003fffffffffffffffffffffffefffffffe000000017571007e0000000000010178787371007e0002c9732c48";
		String stringBytesOfGenesis = "aced0005757200025b42acf317f8060854e0020000787000000040000000f27e9fa6e675a5718f4d8aff8c670d100d90fdda21f1fe53cb2d7da72fb5436f1cb1e590b03c206ca0b01fa9ae6fbe00cd87235688aa0087573b9be2d8737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b02000078700000000174001153345f5354445f424c4f434b434841494e7571007e0000000000408a76cfaba0cfecd200eeef8263372e689c43c2d5575dd2e5d3d897f2e799ea130a93a0753d57f7c4a3f67c37539981939a56eb6bc71f8e8818fd5cc3083d869d7571007e0000000000406fa749bdc2f9c072b410c76b8a2716a1e966034d8fc5ec6aac41e7cb5fb455f562cd6596d9a8335baca8f3fbfa4460ae6a41d86fa8d3c0cfe30aba0d8ba14e9f7372000e6a6176612e6c616e672e4c6f6e673b8be490cc8f23df0200014a000576616c75657871007e000300000000000000007371007e00080000014dfc40f580737200146a6176612e6d6174682e426967446563696d616c54c71557f981284f0300024900057363616c654c0006696e7456616c7400164c6a6176612f6d6174682f426967496e74656765723b7871007e000300000000737200146a6176612e6d6174682e426967496e74656765728cfc9f1fa93bfb1d030006490008626974436f756e744900096269744c656e67746849001366697273744e6f6e7a65726f427974654e756d49000c6c6f776573745365744269744900067369676e756d5b00096d61676e69747564657400025b427871007e0003fffffffffffffffffffffffefffffffe000000017571007e0000000000010178787371007e00025f6c624574000e54657374207369676e6174757265";
		byte[] bytesOfGenesis = Hex.decodeHex(stringBytesOfGenesis.toCharArray());
		
		ArrayList<Transaction> singleTransactionList = new ArrayList<Transaction>();
		singleTransactionList.add(DefaultTransaction.getNullTransaction());
		
		BlockHeader genesisHeader = new DefaultBlockHeader.Builder(bytesOfGenesis).build();
		
		Block genesis = new DefaultBlock(genesisHeader, singleTransactionList);

		long timestamp = 1329106372715L;
		
//		String stringBytesOfPrevious = "aced0005757200025b42acf317f8060854e0020000787000000040d2a944a2d6539ca555a0de36765f110723a97084397b22c6d4dcbcc584bfd364198bd980f90fabbc378e8dc5d2e9e93bd76d3f32155ee62d4843cfcd4434bb61737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b02000078700000000174001153345f5354445f424c4f434b434841494e7571007e000000000040000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000007571007e0000000000406fa749bdc2f9c072b410c76b8a2716a1e966034d8fc5ec6aac41e7cb5fb455f562cd6596d9a8335baca8f3fbfa4460ae6a41d86fa8d3c0cfe30aba0d8ba14e9f7372000e6a6176612e6c616e672e4c6f6e673b8be490cc8f23df0200014a000576616c75657871007e0003ffffffffffffffff7371007e00080000014cbced34de737200146a6176612e6d6174682e426967446563696d616c54c71557f981284f0300024900057363616c654c0006696e7456616c7400164c6a6176612f6d6174682f426967496e74656765723b7871007e000300000000737200146a6176612e6d6174682e426967496e74656765728cfc9f1fa93bfb1d030006490008626974436f756e744900096269744c656e67746849001366697273744e6f6e7a65726f427974654e756d49000c6c6f776573745365744269744900067369676e756d5b00096d61676e69747564657400025b427871007e0003fffffffffffffffffffffffefffffffe000000017571007e00000000000101787871007e0004";
		String stringBytesOfPrevious = "aced0005757200025b42acf317f8060854e0020000787000000040000000f27e9fa6e675a5718f4d8aff8c670d100d90fdda21f1fe53cb2d7da72fb5436f1cb1e590b03c206ca0b01fa9ae6fbe00cd87235688aa0087573b9be2d8737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b02000078700000000174001153345f5354445f424c4f434b434841494e7571007e0000000000408a76cfaba0cfecd200eeef8263372e689c43c2d5575dd2e5d3d897f2e799ea130a93a0753d57f7c4a3f67c37539981939a56eb6bc71f8e8818fd5cc3083d869d7571007e0000000000406fa749bdc2f9c072b410c76b8a2716a1e966034d8fc5ec6aac41e7cb5fb455f562cd6596d9a8335baca8f3fbfa4460ae6a41d86fa8d3c0cfe30aba0d8ba14e9f7372000e6a6176612e6c616e672e4c6f6e673b8be490cc8f23df0200014a000576616c75657871007e000300000000000000007371007e00080000014dfc40f580737200146a6176612e6d6174682e426967446563696d616c54c71557f981284f0300024900057363616c654c0006696e7456616c7400164c6a6176612f6d6174682f426967496e74656765723b7871007e000300000000737200146a6176612e6d6174682e426967496e74656765728cfc9f1fa93bfb1d030006490008626974436f756e744900096269744c656e67746849001366697273744e6f6e7a65726f427974654e756d49000c6c6f776573745365744269744900067369676e756d5b00096d61676e69747564657400025b427871007e0003fffffffffffffffffffffffefffffffe000000017571007e0000000000010178787371007e00025f6c624574000e54657374207369676e6174757265";
		byte[] bytesOfPrevious = Hex.decodeHex(stringBytesOfPrevious.toCharArray());
//		long previousTimestamp = 1429098804446L;
		
		BlockHeader previousHeader = new DefaultBlockHeader.Builder(bytesOfPrevious).build();
		
		Block previous = new DefaultBlock(previousHeader, singleTransactionList);
		
		BlockChainCore coreMock = EasyMock.createMock(BlockChainCore.class);
		EasyMock.expect(coreMock.getLastBlockInChain()).andReturn(previous);
		EasyMock.replay(coreMock);
		
		assertFalse("The block should be valid!!", BlockUtils.validateBlock(genesis, timestamp, coreMock));
	}
	
	@Test
	public void testValidateInvalidBlock02() throws Exception {
//		String stringBytesOfGenesis = "aced0005757200025b42acf317f8060854e0020000787000000040000000067e2ce0296556919a65d1e3ded25e3b9f57b77af1c482a8ab4c5b0ffc31976a932ac1498d1c178bbe3f59c3587fed1a6708eca1e7d5fedf7ec1ddd557737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b02000078700000000174001153345f5354445f424c4f434b434841494e7571007e000000000040d2a944a2d6539ca555a0de36765f110723a97084397b22c6d4dcbcc584bfd364198bd980f90fabbc378e8dc5d2e9e93bd76d3f32155ee62d4843cfcd4434bb617571007e0000000000406fa749bdc2f9c072b410c76b8a2716a1e966034d8fc5ec6aac41e7cb5fb455f562cd6596d9a8335baca8f3fbfa4460ae6a41d86fa8d3c0cfe30aba0d8ba14e9f7372000e6a6176612e6c616e672e4c6f6e673b8be490cc8f23df0200014a000576616c75657871007e000300000000000000007371007e00080000014cbced34fa737200146a6176612e6d6174682e426967446563696d616c54c71557f981284f0300024900057363616c654c0006696e7456616c7400164c6a6176612f6d6174682f426967496e74656765723b7871007e000300000000737200146a6176612e6d6174682e426967496e74656765728cfc9f1fa93bfb1d030006490008626974436f756e744900096269744c656e67746849001366697273744e6f6e7a65726f427974654e756d49000c6c6f776573745365744269744900067369676e756d5b00096d61676e69747564657400025b427871007e0003fffffffffffffffffffffffefffffffe000000017571007e0000000000010178787371007e0002c9732c48";
		String stringBytesOfGenesis = "aced0005757200025b42acf317f8060854e0020000787000000040000000f27e9fa6e675a5718f4d8aff8c670d100d90fdda21f1fe53cb2d7da72fb5436f1cb1e590b03c206ca0b01fa9ae6fbe00cd87235688aa0087573b9be2d8737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b02000078700000000174001153345f5354445f424c4f434b434841494e7571007e0000000000408a76cfaba0cfecd200eeef8263372e689c43c2d5575dd2e5d3d897f2e799ea130a93a0753d57f7c4a3f67c37539981939a56eb6bc71f8e8818fd5cc3083d869d7571007e0000000000406fa749bdc2f9c072b410c76b8a2716a1e966034d8fc5ec6aac41e7cb5fb455f562cd6596d9a8335baca8f3fbfa4460ae6a41d86fa8d3c0cfe30aba0d8ba14e9f7372000e6a6176612e6c616e672e4c6f6e673b8be490cc8f23df0200014a000576616c75657871007e000300000000000000007371007e00080000014dfc40f580737200146a6176612e6d6174682e426967446563696d616c54c71557f981284f0300024900057363616c654c0006696e7456616c7400164c6a6176612f6d6174682f426967496e74656765723b7871007e000300000000737200146a6176612e6d6174682e426967496e74656765728cfc9f1fa93bfb1d030006490008626974436f756e744900096269744c656e67746849001366697273744e6f6e7a65726f427974654e756d49000c6c6f776573745365744269744900067369676e756d5b00096d61676e69747564657400025b427871007e0003fffffffffffffffffffffffefffffffe000000017571007e0000000000010178787371007e00025f6c624574000e54657374207369676e6174757265";
		byte[] bytesOfGenesis = Hex.decodeHex(stringBytesOfGenesis.toCharArray());
		
		ArrayList<Transaction> singleTransactionList = new ArrayList<Transaction>();
		singleTransactionList.add(DefaultTransaction.getNullTransaction());
		
		BlockHeader genesisHeader = new DefaultBlockHeader.Builder(bytesOfGenesis).build();
		
		Block genesis = new DefaultBlock(genesisHeader, singleTransactionList);
		
		long timestamp = 1429106372715L;
		
		long previousTimestamp = 1429098804446L;
		
		DefaultBlockHeader.Builder previouisBuilder = new DefaultBlockHeader.Builder();
		
		HashValue root = new DefaultMerkler(1).getMerkleRoot(singleTransactionList);
		byte[] zeros = new byte[new DefaultHasher().hashLength()];
		Arrays.fill(zeros, (byte)0);
		HashValue zeroHash = new DefaultHashValue(zeros);
		
		BlockChainProperties properties = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
		previouisBuilder.setBlockChainName(properties.getBlockChainName()).
		setBlockNumber(2L).
		setDifficulty(new BigDecimal("1")).
		setMerkleRoot(root).
		setNonce(1).
		setPrevBlockHash(zeroHash).
		setTimestamp(previousTimestamp).
		setMinerSignature("Test signature");
		
		BlockHeader previousHeader = previouisBuilder.build();
		
		Block previous = new DefaultBlock(previousHeader, singleTransactionList);
		
		BlockChainCore coreMock = EasyMock.createMock(BlockChainCore.class);
		EasyMock.expect(coreMock.getLastBlockInChain()).andReturn(previous);
		EasyMock.replay(coreMock);
		
		assertFalse("The block should be valid!!", BlockUtils.validateBlock(genesis, timestamp, coreMock));
	}
	
	@Test
	public void testValidateInvalidBlock03() throws Exception {
//		String stringBytesOfGenesis = "aced0005757200025b42acf317f8060854e0020000787000000040000000067e2ce0296556919a65d1e3ded25e3b9f57b77af1c482a8ab4c5b0ffc31976a932ac1498d1c178bbe3f59c3587fed1a6708eca1e7d5fedf7ec1ddd557737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b02000078700000000174001153345f5354445f424c4f434b434841494e7571007e000000000040d2a944a2d6539ca555a0de36765f110723a97084397b22c6d4dcbcc584bfd364198bd980f90fabbc378e8dc5d2e9e93bd76d3f32155ee62d4843cfcd4434bb617571007e0000000000406fa749bdc2f9c072b410c76b8a2716a1e966034d8fc5ec6aac41e7cb5fb455f562cd6596d9a8335baca8f3fbfa4460ae6a41d86fa8d3c0cfe30aba0d8ba14e9f7372000e6a6176612e6c616e672e4c6f6e673b8be490cc8f23df0200014a000576616c75657871007e000300000000000000007371007e00080000014cbced34fa737200146a6176612e6d6174682e426967446563696d616c54c71557f981284f0300024900057363616c654c0006696e7456616c7400164c6a6176612f6d6174682f426967496e74656765723b7871007e000300000000737200146a6176612e6d6174682e426967496e74656765728cfc9f1fa93bfb1d030006490008626974436f756e744900096269744c656e67746849001366697273744e6f6e7a65726f427974654e756d49000c6c6f776573745365744269744900067369676e756d5b00096d61676e69747564657400025b427871007e0003fffffffffffffffffffffffefffffffe000000017571007e0000000000010178787371007e0002c9732c48";
		String stringBytesOfGenesis = "aced0005757200025b42acf317f8060854e0020000787000000040000000f27e9fa6e675a5718f4d8aff8c670d100d90fdda21f1fe53cb2d7da72fb5436f1cb1e590b03c206ca0b01fa9ae6fbe00cd87235688aa0087573b9be2d8737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b02000078700000000174001153345f5354445f424c4f434b434841494e7571007e0000000000408a76cfaba0cfecd200eeef8263372e689c43c2d5575dd2e5d3d897f2e799ea130a93a0753d57f7c4a3f67c37539981939a56eb6bc71f8e8818fd5cc3083d869d7571007e0000000000406fa749bdc2f9c072b410c76b8a2716a1e966034d8fc5ec6aac41e7cb5fb455f562cd6596d9a8335baca8f3fbfa4460ae6a41d86fa8d3c0cfe30aba0d8ba14e9f7372000e6a6176612e6c616e672e4c6f6e673b8be490cc8f23df0200014a000576616c75657871007e000300000000000000007371007e00080000014dfc40f580737200146a6176612e6d6174682e426967446563696d616c54c71557f981284f0300024900057363616c654c0006696e7456616c7400164c6a6176612f6d6174682f426967496e74656765723b7871007e000300000000737200146a6176612e6d6174682e426967496e74656765728cfc9f1fa93bfb1d030006490008626974436f756e744900096269744c656e67746849001366697273744e6f6e7a65726f427974654e756d49000c6c6f776573745365744269744900067369676e756d5b00096d61676e69747564657400025b427871007e0003fffffffffffffffffffffffefffffffe000000017571007e0000000000010178787371007e00025f6c624574000e54657374207369676e6174757265";
		byte[] bytesOfGenesis = Hex.decodeHex(stringBytesOfGenesis.toCharArray());
		
		ArrayList<Transaction> singleTransactionList = new ArrayList<Transaction>();
		singleTransactionList.add(DefaultTransaction.getNullTransaction());
		
		BlockHeader genesisHeader = new DefaultBlockHeader.Builder(bytesOfGenesis).build();
		
		Block genesis = new DefaultBlock(genesisHeader, singleTransactionList);
		
		long timestamp = 1429106372715L;
		
		long previousTimestamp = 1429098804446L;
		
		DefaultBlockHeader.Builder previouisBuilder = new DefaultBlockHeader.Builder();
		
		HashValue root = new DefaultMerkler(1).getMerkleRoot(singleTransactionList);
		byte[] zeros = new byte[new DefaultHasher().hashLength()];
		Arrays.fill(zeros, (byte)0);
		zeros[0] = 1;
		HashValue zeroHash = new DefaultHashValue(zeros);
		
		BlockChainProperties properties = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
		previouisBuilder.setBlockChainName(properties.getBlockChainName()).
		setBlockNumber(-1L).
		setDifficulty(new BigDecimal("1")).
		setMerkleRoot(root).
		setNonce(1).
		setPrevBlockHash(zeroHash).
		setTimestamp(previousTimestamp).
		setMinerSignature("Test signature");
		
		BlockHeader previousHeader = previouisBuilder.build();
		
		Block previous = new DefaultBlock(previousHeader, singleTransactionList);
		
		BlockChainCore coreMock = EasyMock.createMock(BlockChainCore.class);
		EasyMock.expect(coreMock.getLastBlockInChain()).andReturn(previous);
		EasyMock.replay(coreMock);
		
		assertFalse("The block should be valid!!", BlockUtils.validateBlock(genesis, timestamp, coreMock));
	}
	
	@Test
	public void testValidateInvalidBlock04() throws Exception {
//		String stringBytesOfGenesis = "aced0005757200025b42acf317f8060854e0020000787000000040000000067e2ce0296556919a65d1e3ded25e3b9f57b77af1c482a8ab4c5b0ffc31976a932ac1498d1c178bbe3f59c3587fed1a6708eca1e7d5fedf7ec1ddd557737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b02000078700000000174001153345f5354445f424c4f434b434841494e7571007e000000000040d2a944a2d6539ca555a0de36765f110723a97084397b22c6d4dcbcc584bfd364198bd980f90fabbc378e8dc5d2e9e93bd76d3f32155ee62d4843cfcd4434bb617571007e0000000000406fa749bdc2f9c072b410c76b8a2716a1e966034d8fc5ec6aac41e7cb5fb455f562cd6596d9a8335baca8f3fbfa4460ae6a41d86fa8d3c0cfe30aba0d8ba14e9f7372000e6a6176612e6c616e672e4c6f6e673b8be490cc8f23df0200014a000576616c75657871007e000300000000000000007371007e00080000014cbced34fa737200146a6176612e6d6174682e426967446563696d616c54c71557f981284f0300024900057363616c654c0006696e7456616c7400164c6a6176612f6d6174682f426967496e74656765723b7871007e000300000000737200146a6176612e6d6174682e426967496e74656765728cfc9f1fa93bfb1d030006490008626974436f756e744900096269744c656e67746849001366697273744e6f6e7a65726f427974654e756d49000c6c6f776573745365744269744900067369676e756d5b00096d61676e69747564657400025b427871007e0003fffffffffffffffffffffffefffffffe000000017571007e0000000000010178787371007e0002c9732c48";
		String stringBytesOfGenesis = "aced0005757200025b42acf317f8060854e0020000787000000040000000f27e9fa6e675a5718f4d8aff8c670d100d90fdda21f1fe53cb2d7da72fb5436f1cb1e590b03c206ca0b01fa9ae6fbe00cd87235688aa0087573b9be2d8737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b02000078700000000174001153345f5354445f424c4f434b434841494e7571007e0000000000408a76cfaba0cfecd200eeef8263372e689c43c2d5575dd2e5d3d897f2e799ea130a93a0753d57f7c4a3f67c37539981939a56eb6bc71f8e8818fd5cc3083d869d7571007e0000000000406fa749bdc2f9c072b410c76b8a2716a1e966034d8fc5ec6aac41e7cb5fb455f562cd6596d9a8335baca8f3fbfa4460ae6a41d86fa8d3c0cfe30aba0d8ba14e9f7372000e6a6176612e6c616e672e4c6f6e673b8be490cc8f23df0200014a000576616c75657871007e000300000000000000007371007e00080000014dfc40f580737200146a6176612e6d6174682e426967446563696d616c54c71557f981284f0300024900057363616c654c0006696e7456616c7400164c6a6176612f6d6174682f426967496e74656765723b7871007e000300000000737200146a6176612e6d6174682e426967496e74656765728cfc9f1fa93bfb1d030006490008626974436f756e744900096269744c656e67746849001366697273744e6f6e7a65726f427974654e756d49000c6c6f776573745365744269744900067369676e756d5b00096d61676e69747564657400025b427871007e0003fffffffffffffffffffffffefffffffe000000017571007e0000000000010178787371007e00025f6c624574000e54657374207369676e6174757265";
		byte[] bytesOfGenesis = Hex.decodeHex(stringBytesOfGenesis.toCharArray());
		
		ArrayList<Transaction> singleTransactionList = new ArrayList<Transaction>();
		singleTransactionList.add(DefaultTransaction.getNullTransaction());
		
		BlockHeader genesisHeader = new DefaultBlockHeader.Builder(bytesOfGenesis).build();
		
		Block genesis = new DefaultBlock(genesisHeader, singleTransactionList);
		
		long timestamp = 1429106372715L;
		
		long previousTimestamp = 1429098804446L;
		
		DefaultBlockHeader.Builder previouisBuilder = new DefaultBlockHeader.Builder();
		
		HashValue root = new DefaultMerkler(1).getMerkleRoot(singleTransactionList);
		byte[] zeros = new byte[new DefaultHasher().hashLength()];
		Arrays.fill(zeros, (byte)0);
		HashValue zeroHash = new DefaultHashValue(zeros);
		
		BlockChainProperties properties = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
		previouisBuilder.setBlockChainName(properties.getBlockChainName()+"-Toll").
		setBlockNumber(-1L).
		setDifficulty(new BigDecimal("1")).
		setMerkleRoot(root).
		setNonce(1).
		setPrevBlockHash(zeroHash).
		setTimestamp(previousTimestamp).
		setMinerSignature("Test signature");
		
		BlockHeader previousHeader = previouisBuilder.build();
		
		Block previous = new DefaultBlock(previousHeader, singleTransactionList);
		
		BlockChainCore coreMock = EasyMock.createMock(BlockChainCore.class);
		EasyMock.expect(coreMock.getLastBlockInChain()).andReturn(previous);
		EasyMock.replay(coreMock);
		
		assertFalse("The block should be valid!!", BlockUtils.validateBlock(genesis, timestamp, coreMock));
	}
	
	@Test
	public void testValidateInvalidBlock05() throws Exception {
//		String stringBytesOfGenesis = "aced0005757200025b42acf317f8060854e0020000787000000040000000067e2ce0296556919a65d1e3ded25e3b9f57b77af1c482a8ab4c5b0ffc31976a932ac1498d1c178bbe3f59c3587fed1a6708eca1e7d5fedf7ec1ddd557737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b02000078700000000174001153345f5354445f424c4f434b434841494e7571007e000000000040d2a944a2d6539ca555a0de36765f110723a97084397b22c6d4dcbcc584bfd364198bd980f90fabbc378e8dc5d2e9e93bd76d3f32155ee62d4843cfcd4434bb617571007e0000000000406fa749bdc2f9c072b410c76b8a2716a1e966034d8fc5ec6aac41e7cb5fb455f562cd6596d9a8335baca8f3fbfa4460ae6a41d86fa8d3c0cfe30aba0d8ba14e9f7372000e6a6176612e6c616e672e4c6f6e673b8be490cc8f23df0200014a000576616c75657871007e000300000000000000007371007e00080000014cbced34fa737200146a6176612e6d6174682e426967446563696d616c54c71557f981284f0300024900057363616c654c0006696e7456616c7400164c6a6176612f6d6174682f426967496e74656765723b7871007e000300000000737200146a6176612e6d6174682e426967496e74656765728cfc9f1fa93bfb1d030006490008626974436f756e744900096269744c656e67746849001366697273744e6f6e7a65726f427974654e756d49000c6c6f776573745365744269744900067369676e756d5b00096d61676e69747564657400025b427871007e0003fffffffffffffffffffffffefffffffe000000017571007e0000000000010178787371007e0002c9732c48";
		String stringBytesOfGenesis = "aced0005757200025b42acf317f8060854e0020000787000000040000000f27e9fa6e675a5718f4d8aff8c670d100d90fdda21f1fe53cb2d7da72fb5436f1cb1e590b03c206ca0b01fa9ae6fbe00cd87235688aa0087573b9be2d8737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b02000078700000000174001153345f5354445f424c4f434b434841494e7571007e0000000000408a76cfaba0cfecd200eeef8263372e689c43c2d5575dd2e5d3d897f2e799ea130a93a0753d57f7c4a3f67c37539981939a56eb6bc71f8e8818fd5cc3083d869d7571007e0000000000406fa749bdc2f9c072b410c76b8a2716a1e966034d8fc5ec6aac41e7cb5fb455f562cd6596d9a8335baca8f3fbfa4460ae6a41d86fa8d3c0cfe30aba0d8ba14e9f7372000e6a6176612e6c616e672e4c6f6e673b8be490cc8f23df0200014a000576616c75657871007e000300000000000000007371007e00080000014dfc40f580737200146a6176612e6d6174682e426967446563696d616c54c71557f981284f0300024900057363616c654c0006696e7456616c7400164c6a6176612f6d6174682f426967496e74656765723b7871007e000300000000737200146a6176612e6d6174682e426967496e74656765728cfc9f1fa93bfb1d030006490008626974436f756e744900096269744c656e67746849001366697273744e6f6e7a65726f427974654e756d49000c6c6f776573745365744269744900067369676e756d5b00096d61676e69747564657400025b427871007e0003fffffffffffffffffffffffefffffffe000000017571007e0000000000010178787371007e00025f6c624574000e54657374207369676e6174757265";
		byte[] bytesOfGenesis = Hex.decodeHex(stringBytesOfGenesis.toCharArray());
		
		ArrayList<Transaction> singleTransactionList = new ArrayList<Transaction>();
		singleTransactionList.add(DefaultTransaction.getNullTransaction());
		
		BlockHeader genesisHeader = new DefaultBlockHeader.Builder(bytesOfGenesis).build();
		
		Block genesis = new DefaultBlock(genesisHeader, singleTransactionList);
		
		long timestamp = 1429106372715L;
		
		long previousTimestamp = 1429098804446L;
		
		DefaultBlockHeader.Builder previouisBuilder = new DefaultBlockHeader.Builder();
		
		HashValue root = new DefaultMerkler(1).getMerkleRoot(singleTransactionList);
		byte[] zeros = new byte[new DefaultHasher().hashLength()];
		Arrays.fill(zeros, (byte)0);
		HashValue zeroHash = new DefaultHashValue(zeros);
		
		BlockChainProperties properties = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
		previouisBuilder.setBlockChainName(properties.getBlockChainName()).
		setBlockNumber(-1L).
		setDifficulty(new BigDecimal("10")).
		setMerkleRoot(root).
		setNonce(1).
		setPrevBlockHash(zeroHash).
		setTimestamp(previousTimestamp).
		setMinerSignature("Test signature");
		
		BlockHeader previousHeader = previouisBuilder.build();
		
		Block previous = new DefaultBlock(previousHeader, singleTransactionList);
		
		BlockChainCore coreMock = EasyMock.createMock(BlockChainCore.class);
		EasyMock.expect(coreMock.getLastBlockInChain()).andReturn(previous);
		EasyMock.replay(coreMock);
		
		assertFalse("The block should be valid!!", BlockUtils.validateBlock(genesis, timestamp, coreMock));
	}
	
	@Test
	public void testValidateInvalidBlock06() throws Exception {
//		String stringBytesOfGenesis = "aced0005757200025b42acf317f8060854e0020000787000000040000000067e2ce0296556919a65d1e3ded25e3b9f57b77af1c482a8ab4c5b0ffc31976a932ac1498d1c178bbe3f59c3587fed1a6708eca1e7d5fedf7ec1ddd557737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b02000078700000000174001153345f5354445f424c4f434b434841494e7571007e000000000040d2a944a2d6539ca555a0de36765f110723a97084397b22c6d4dcbcc584bfd364198bd980f90fabbc378e8dc5d2e9e93bd76d3f32155ee62d4843cfcd4434bb617571007e0000000000406fa749bdc2f9c072b410c76b8a2716a1e966034d8fc5ec6aac41e7cb5fb455f562cd6596d9a8335baca8f3fbfa4460ae6a41d86fa8d3c0cfe30aba0d8ba14e9f7372000e6a6176612e6c616e672e4c6f6e673b8be490cc8f23df0200014a000576616c75657871007e000300000000000000007371007e00080000014cbced34fa737200146a6176612e6d6174682e426967446563696d616c54c71557f981284f0300024900057363616c654c0006696e7456616c7400164c6a6176612f6d6174682f426967496e74656765723b7871007e000300000000737200146a6176612e6d6174682e426967496e74656765728cfc9f1fa93bfb1d030006490008626974436f756e744900096269744c656e67746849001366697273744e6f6e7a65726f427974654e756d49000c6c6f776573745365744269744900067369676e756d5b00096d61676e69747564657400025b427871007e0003fffffffffffffffffffffffefffffffe000000017571007e0000000000010178787371007e0002c9732c48";
		String stringBytesOfGenesis = "aced0005757200025b42acf317f8060854e0020000787000000040000000f27e9fa6e675a5718f4d8aff8c670d100d90fdda21f1fe53cb2d7da72fb5436f1cb1e590b03c206ca0b01fa9ae6fbe00cd87235688aa0087573b9be2d8737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b02000078700000000174001153345f5354445f424c4f434b434841494e7571007e0000000000408a76cfaba0cfecd200eeef8263372e689c43c2d5575dd2e5d3d897f2e799ea130a93a0753d57f7c4a3f67c37539981939a56eb6bc71f8e8818fd5cc3083d869d7571007e0000000000406fa749bdc2f9c072b410c76b8a2716a1e966034d8fc5ec6aac41e7cb5fb455f562cd6596d9a8335baca8f3fbfa4460ae6a41d86fa8d3c0cfe30aba0d8ba14e9f7372000e6a6176612e6c616e672e4c6f6e673b8be490cc8f23df0200014a000576616c75657871007e000300000000000000007371007e00080000014dfc40f580737200146a6176612e6d6174682e426967446563696d616c54c71557f981284f0300024900057363616c654c0006696e7456616c7400164c6a6176612f6d6174682f426967496e74656765723b7871007e000300000000737200146a6176612e6d6174682e426967496e74656765728cfc9f1fa93bfb1d030006490008626974436f756e744900096269744c656e67746849001366697273744e6f6e7a65726f427974654e756d49000c6c6f776573745365744269744900067369676e756d5b00096d61676e69747564657400025b427871007e0003fffffffffffffffffffffffefffffffe000000017571007e0000000000010178787371007e00025f6c624574000e54657374207369676e6174757265";
		byte[] bytesOfGenesis = Hex.decodeHex(stringBytesOfGenesis.toCharArray());
		
		ArrayList<Transaction> singleTransactionList = new ArrayList<Transaction>();
		singleTransactionList.add(DefaultTransaction.getNullTransaction());
		
		BlockHeader genesisHeader = new DefaultBlockHeader.Builder(bytesOfGenesis).build();
		
		ArrayList<Transaction> listInvalidatingRoot = new ArrayList<Transaction>();
//		DhtAddress address = new DefaultDhtAddress(InetAddress.getByName("10.0.0.1"), 1024);
//		byte[] transactionbytes = {'f','i','r','s','t'};
//		DhtID id = new DefaultDhtHasher().hashBytes(transactionbytes);
		listInvalidatingRoot.add(DefaultTransaction.getNullTransaction());
		
		Block genesis = new DefaultBlock(genesisHeader, listInvalidatingRoot);
		
		long timestamp = 1429106372715L;
		
		long previousTimestamp = 1429098804446L;
		
		DefaultBlockHeader.Builder previouisBuilder = new DefaultBlockHeader.Builder();
		
		HashValue root = new DefaultMerkler(1).getMerkleRoot(singleTransactionList);
		byte[] zeros = new byte[new DefaultHasher().hashLength()];
		Arrays.fill(zeros, (byte)0);
		HashValue zeroHash = new DefaultHashValue(zeros);
		
		BlockChainProperties properties = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
		previouisBuilder.setBlockChainName(properties.getBlockChainName()).
		setBlockNumber(-1L).
		setDifficulty(new BigDecimal("1")).
		setMerkleRoot(root).
		setNonce(1).
		setPrevBlockHash(zeroHash).
		setTimestamp(previousTimestamp).
		setMinerSignature("Test signature");
		
		BlockHeader previousHeader = previouisBuilder.build();
		
		Block previous = new DefaultBlock(previousHeader, singleTransactionList);
		
		BlockChainCore coreMock = EasyMock.createMock(BlockChainCore.class);
		EasyMock.expect(coreMock.getLastBlockInChain()).andReturn(previous);
		EasyMock.replay(coreMock);
		
		assertFalse("The block should be valid!!", BlockUtils.validateBlock(genesis, timestamp, coreMock));
	}
	
}
