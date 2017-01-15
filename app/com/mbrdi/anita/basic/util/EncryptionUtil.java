package com.mbrdi.anita.basic.util;

import play.Play;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.security.*;
import java.util.Base64;

/**
 * @author JavaDigest
 * 
 */
public class EncryptionUtil {

	/**
	 * String to hold name of the encryption algorithm.
	 */
	public static final String ALGORITHM = "RSA";
	
	/**
	 * String to hold the name of the private key file.
	 */
	public static final String PRIVATE_KEY_FILE = Play.application().configuration().getString("PRIVATE_KEY_FILE"); 
	
	/**
	 * String to hold name of the public key file.
	 */
	public static final String PUBLIC_KEY_FILE = Play.application().configuration().getString("PUBLIC_KEY_FILE");
	
	private static PublicKey publicKey;
	private static  PrivateKey privateKey;
	
	static {
		try {
			// Encrypt the string using the public key
			ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(PUBLIC_KEY_FILE));
			publicKey = (PublicKey) inputStream.readObject();
			
			// Decrypt the cipher text using the private key.
			inputStream = new ObjectInputStream(new FileInputStream(PRIVATE_KEY_FILE));
			privateKey = (PrivateKey) inputStream.readObject();
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * Generate key which contains a pair of private and public key using 1024
	 * bytes. Store the set of keys in Prvate.key and Public.key files.
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private static void generateKey() {
		try {
			final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
			keyGen.initialize(1024);
			final KeyPair key = keyGen.generateKeyPair();

			File privateKeyFile = new File(PRIVATE_KEY_FILE);
			File publicKeyFile = new File(PUBLIC_KEY_FILE);

			// Create files to store public and private key
			if (privateKeyFile.getParentFile() != null) {
				privateKeyFile.getParentFile().mkdirs();
			}
			privateKeyFile.createNewFile();

			if (publicKeyFile.getParentFile() != null) {
				publicKeyFile.getParentFile().mkdirs();
			}
			publicKeyFile.createNewFile();

			// Saving the Public key in a file
			ObjectOutputStream publicKeyOS = new ObjectOutputStream(new FileOutputStream(publicKeyFile));
			publicKeyOS.writeObject(key.getPublic());
			publicKeyOS.close();

			// Saving the Private key in a file
			ObjectOutputStream privateKeyOS = new ObjectOutputStream(new FileOutputStream(privateKeyFile));
			privateKeyOS.writeObject(key.getPrivate());
			privateKeyOS.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * The method checks if the pair of public and private key has been
	 * generated.
	 * 
	 * @return flag indicating if the pair of keys were generated.
	 */
	public static boolean areKeysPresent() {

		File privateKey = new File(PRIVATE_KEY_FILE);
		File publicKey = new File(PUBLIC_KEY_FILE);

		if (privateKey.exists() && publicKey.exists()) {
			return true;
		}
		return false;
	}

	/**
	 * Encrypt the plain text using public key.
	 * 
	 * @param text
	 *            : original plain text
	 *            :The public key
	 * @return Encrypted text
	 * @throws Exception
	 */
	public static String encrypt(String text) {
		byte[] cipherText = null;
		try {
			// get an RSA cipher object and print the provider
			final Cipher cipher = Cipher.getInstance(ALGORITHM);
			// encrypt the plain text using the public key
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			cipherText = cipher.doFinal(text.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cipherText.toString();
	}

	/**
	 * Decrypt text using private key.
	 *
	 * @param text
	 *            :encrypted text
	 *            :The private key
	 * @return plain text
	 * @throws Exception
	 */
	public static String decrypt(byte[] text) {
		byte[] dectyptedText = null;
		try {
			// get an RSA cipher object and print the provider
			final Cipher cipher = Cipher.getInstance(ALGORITHM);

			// decrypt the text using the private key
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			dectyptedText = cipher.doFinal(text);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return new String(dectyptedText);
	}

	/**
	 * Test the EncryptionUtil
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		try {

			// Check if the pair of keys are present else generate those.
			if (!areKeysPresent()) {
				// Method generates a pair of keys using the RSA algorithm and
				// stores it
				// in their respective files
				generateKey();
			}

			final String originalText = "Text to be encrypted ";
			
			final String cipherText = encrypt(originalText);

			final String plainText = decrypt(cipherText.getBytes());

			// Printing the Original, Encrypted and Decrypted Text
			System.out.println("Original: " + originalText);
			System.out.println("Encrypted: " + cipherText.toString());
			System.out.println("Decrypted: " + plainText);

		} catch (Exception e) {
			e.printStackTrace();
		}

		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		keyGenerator.init(128);
		SecretKey secretKey = keyGenerator.generateKey();
		cipher = Cipher.getInstance("AES");

		String plainText = "AES Symmetric Encryption Decryption";
		System.out.println("Plain Text Before Encryption: " + plainText);

		String encryptedText = encryptSymetric(plainText, secretKey);
		System.out.println("Encrypted Text After Encryption: " + encryptedText);

		String decryptedText = decryptSymetric(encryptedText, secretKey);
		System.out.println("Decrypted Text After Decryption: " + decryptedText);
	}

	static Cipher cipher;

	public static String encryptSymetric(String plainText, SecretKey secretKey) throws Exception {
		byte[] plainTextByte = plainText.getBytes();
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] encryptedByte = cipher.doFinal(plainTextByte);
		Base64.Encoder encoder = Base64.getEncoder();
		String encryptedText = encoder.encodeToString(encryptedByte);
		return encryptedText;
	}

	public static String decryptSymetric(String encryptedText, SecretKey secretKey) throws Exception {
		Base64.Decoder decoder = Base64.getDecoder();
		byte[] encryptedTextByte = decoder.decode(encryptedText);
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		byte[] decryptedByte = cipher.doFinal(encryptedTextByte);
		String decryptedText = new String(decryptedByte);
		return decryptedText;
	}
}