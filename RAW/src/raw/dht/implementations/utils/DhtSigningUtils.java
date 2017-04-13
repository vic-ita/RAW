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
package raw.dht.implementations.utils;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import raw.blockChain.api.Transaction;
import raw.dht.DhtAddress;
import raw.dht.DhtNode;
import raw.dht.implementations.DefaultDhtAddress;
import raw.logger.Log;

/**
 * Utils class  to sign DHT messages
 * using {@link DhtNode#getPublicKey()}
 * 
 * @author vic
 *
 */
public class DhtSigningUtils {

	private static int pubKeyLength = -1;

	/**
	 * Generate a new RSA {@link KeyPair} to be used signing
	 * {@link Transaction}s
	 *  
	 * @return a new RSA {@link KeyPair}
	 */
	public static KeyPair getSignKeyPair(){
		KeyPairGenerator kGen = null;
		try {
			kGen = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			Log log = Log.getLogger();
			log.exception(e);
		}
		kGen.initialize(2048);
		
		KeyPair kPair = kGen.generateKeyPair();
		return kPair;
	}

	/**
	 * @return the length (in bytes) of a {@link PublicKey} generated through {@link DhtSigningUtils#getSignKeyPair()}
	 */
	public static int getPublicKeyByteLength(){
		if(pubKeyLength != -1){
			return pubKeyLength;
		}
		pubKeyLength = getSignKeyPair().getPublic().getEncoded().length;
		return pubKeyLength;
	}

	/**
	 * Convert a {@link PrivateKey} to a hex string
	 * representation. The key could then be recovered
	 * by means of {@link DhtSigningUtils#regeneratePrivateKey(String)}.
	 * 
	 * @param privateKey the {@link PrivateKey} to convert
	 * @return an hex string representation of <code>privateKey</code>
	 */
	public static String privateKeyHexRepresentation(PrivateKey privateKey){
		byte[] bytes = privateKey.getEncoded();
		String encoded = new String(Hex.encodeHex(bytes));
		return encoded;
	}

	/**
	 * Reconstruct a {@link PrivateKey} from
	 * its bytes obtained issuing {@link PrivateKey#getEncoded()}.
	 * 
	 * @param keyBytes the bytes representation fo a {@link PrivateKey}
	 * @return the {@link PrivateKey}
	 */
	public static PrivateKey regeneratePrivateKey(byte[] keyBytes){
		KeyFactory kf = null;
		try {
			kf = KeyFactory.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			Log log = Log.getLogger();
			log.exception(e);
		}
		PrivateKey privateKey = null;
		try {
			privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
		} catch (InvalidKeySpecException e) {
			Log log = Log.getLogger();
			log.exception(e);
		}
		return privateKey;
	}

	/**
	 * Reconstruct a {@link PrivateKey} from
	 * the string hex representation of
	 *  bytes obtained issuing {@link PrivateKey#getEncoded()}.
	 * 
	 * @param hexString the hex string representation fo a {@link PrivateKey}
	 * @return the {@link PrivateKey}
	 */
	public static PrivateKey regeneratePrivateKey(String hexString){
		byte[] keyBytes = null;
		try {
			keyBytes = Hex.decodeHex(hexString.toCharArray());
		} catch (DecoderException e) {
			Log log = Log.getLogger();
			log.exception(e);
		}
		return regeneratePrivateKey(keyBytes);
	}

	/**
	 * Convert a {@link PublicKey} to a hex string
	 * representation. The key could then be recovered
	 * by means of {@link DhtSigningUtils#regeneratePublicKey(String)}.
	 * <b>NOTE:</b> if a <code>null</code> {@link PublicKey} is provided
	 * an hex string formed only by 0 is returned.
	 * 
	 * @param publicKey the {@link PublicKey} to convert
	 * @return an hex string representation of <code>publicKey</code>
	 */
	public static String publicKeyHexRepresentation(PublicKey publicKey){
//		byte[] bytes = new byte[getPublicKeyByteLength()];
//		Arrays.fill(bytes, (byte)0);
		byte[] bytes = pubKeyZeroBytesArray();
		if(publicKey != null){			
			bytes = publicKey.getEncoded();
		} 
		String encoded = new String(Hex.encodeHex(bytes));
		return encoded;
	}
	
	private static byte[] pubKeyZeroBytesArray(){
		byte[] bytes = new byte[getPublicKeyByteLength()];
		Arrays.fill(bytes, (byte)0);
		return bytes;
	}

	/**
	 * Reconstruct a {@link PublicKey} from
	 * its bytes obtained issuing {@link PublicKey#getEncoded()}.
	 * 
	 * @param keyBytes the bytes representation for a {@link PublicKey}
	 * @return the {@link PublicKey} or null is <code>keyBytes</code> are invalid
	 */
	public static PublicKey regeneratePublicKey(byte[] keyBytes){
		KeyFactory kf = null;
		PublicKey publicKey = null;
		if(!(keyBytes == null || Arrays.equals(keyBytes, pubKeyZeroBytesArray()))){			
			try {
				kf = KeyFactory.getInstance("RSA");
			} catch (NoSuchAlgorithmException e) {
				Log log = Log.getLogger();
				log.exception(e);
			}
			try {
				publicKey = kf.generatePublic(new X509EncodedKeySpec(keyBytes));
			} catch (InvalidKeySpecException e) {
				Log log = Log.getLogger();
				log.exception(e);
				return null;
			}
		}
		return publicKey;
	}

	/**
	 * Reconstruct a {@link PublicKey} from
	 * the string hex representation of
	 *  bytes obtained issuing {@link PublicKey#getEncoded()}.
	 * 
	 * @param hexString the hex string representation for a {@link PublicKey}
	 * @return the {@link PublicKey} or null is <code>hexString</code> are invalid
	 */
	public static PublicKey regeneratePublicKey(String hexString){
		byte[] keyBytes = null;
		try {
			keyBytes = Hex.decodeHex(hexString.toCharArray());
		} catch (DecoderException e) {
			Log log = Log.getLogger();
			log.exception(e);
			return null;
		}
		return regeneratePublicKey(keyBytes);
	}

	/**
	 * Reconstruct a {@link KeyPair} from
	 * bytes obtained issuing {@link PublicKey#getEncoded()} 
	 * and {@link PrivateKey#getEncoded()}.
	 *  
	 * @param publicKeyBytes the bytes representation for a {@link PublicKey}
	 * @param privateKeyBytes the bytes representation for a {@link PrivateKey}
	 * @return a {@link KeyPair}
	 */
	public static KeyPair regenerateKeyPair(byte[] publicKeyBytes, byte[] privateKeyBytes){
		PublicKey publicKey = regeneratePublicKey(publicKeyBytes);
		PrivateKey privateKey = regeneratePrivateKey(privateKeyBytes);
		return new KeyPair(publicKey, privateKey);
	}

	/**
	 * Reconstruct a {@link KeyPair} from
	 * the string hex representations of
	 * bytes obtained issuing {@link PublicKey#getEncoded()} 
	 * and {@link PrivateKey#getEncoded()}.
	 *  
	 * @param publicKeyBytes the bytes representation for a {@link PublicKey}
	 * @param privateKeyBytes the bytes representation for a {@link PrivateKey}
	 * @return a {@link KeyPair}
	 */
	public static KeyPair regenerateKeyPair(String publicKeyHex, String privateKeyHex){
		PublicKey publicKey = regeneratePublicKey(publicKeyHex);
		PrivateKey privateKey = regeneratePrivateKey(privateKeyHex);
		return new KeyPair(publicKey, privateKey);
	}

	/**
	 * Return a byte array representing a signed version of a given
	 * {@link DhtAddress}.
	 * 
	 * @param address the {@link DhtAddress} to be signed
	 * @param privateKey a {@link PrivateKey}
	 * @return a signed byte array
	 */
	public static byte[] signDhtAddress(DhtAddress address, PrivateKey privateKey){
		Signature privSig = null;
		try {
			privSig = Signature.getInstance("SHA512withRSA");
		} catch (NoSuchAlgorithmException e) {
			Log log = Log.getLogger();
			log.exception(e);
		}
		
		try {
			privSig.initSign(privateKey);
		} catch (InvalidKeyException e) {
			Log log = Log.getLogger();
			log.exception(e);
		}
		
		try {
			privSig.update(address.toString().getBytes());
		} catch (SignatureException e) {
			Log log = Log.getLogger();
			log.exception(e);
		}
		
		byte[] signed = null;
		try {
			signed = privSig.sign();
		} catch (SignatureException e) {
			Log log = Log.getLogger();
			log.exception(e);
		}
		return signed;
	}

	/**
	 * Return an hex string representing a signed version of a given
	 * {@link DhtAddress}.
	 * 
	 * @param address the {@link DhtAddress} to be signed
	 * @param privateKey a {@link PrivateKey}
	 * @return a signed hex string
	 */
	public static String hexSignDhtAddress(DhtAddress address, PrivateKey privateKey){
		String encoded = new String(Hex.encodeHex(signDhtAddress(address, privateKey)));
		return encoded;
	}

	/**
	 * Verify that a given <code>signedAddress</code> is the signature
	 * of the provided <code>address</code> {@link DhtAddress} according
	 * to a certain {@link PublicKey}.
	 * 
	 * @param address a {@link DhtAddress} to be verified
	 * @param signedAddress the supposed {@link DhtAddress} signed version
	 * @param publicKey a {@link PublicKey}
	 * @return <code>true</code> if <code>signedAddress</code> decoded with <code>publicKey</code> equals <code>address</code>, <code>false</code> otherwise
	 */
	public static boolean verifySignedDhtAddress(DhtAddress address, byte[] signedAddress, PublicKey publicKey){
		if(!(address instanceof DefaultDhtAddress)){
			return false;
		}
		
		Signature pubSig = null;
		
		try {
			pubSig = Signature.getInstance("SHA512withRSA");
		} catch (NoSuchAlgorithmException e) {
			Log log = Log.getLogger();
			log.exception(e);
		}
		
		try {
			pubSig.initVerify(publicKey);
		} catch (InvalidKeyException e) {
			Log log = Log.getLogger();
			log.exception(e);
		}
		
		try {
			pubSig.update(address.toString().getBytes());
		} catch (SignatureException e) {
			Log log = Log.getLogger();
			log.exception(e);
		}
		
		boolean isVerified = false;
		try {
			isVerified = pubSig.verify(signedAddress);
		} catch (SignatureException e) {
			Log log = Log.getLogger();
			log.exception(e);
		}
		
		return isVerified;
	}

	/**
	 * Verify that a given <code>signedAddress</code> is the signature
	 * of the provided <code>address</code> {@link DhtAddress} according
	 * to a certain {@link PublicKey}.
	 * 
	 * @param address a {@link DhtAddress} to be verified
	 * @param signedAddress the supposed {@link DhtAddress} signed version
	 * @param publicKey a {@link PublicKey}
	 * @return <code>true</code> if <code>signedAddress</code> decoded with <code>publicKey</code> equals <code>address</code>, <code>false</code> otherwise
	 */
	public static boolean verifySignedDhtAddress(DhtAddress address, String signedAddress, PublicKey publicKey){
		byte[] signedBytes = null;
		try {
			signedBytes = Hex.decodeHex(signedAddress.toCharArray());
		} catch (DecoderException e) {
			Log log = Log.getLogger();
			log.exception(e);
		}
		return verifySignedDhtAddress(address, signedBytes, publicKey);
	}

}
