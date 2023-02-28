package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import shared.WarnHandler;

public class ClientOperations {

	private static final String localrepo = "local/";
	private static Keystore keystore = null;
	private static ClientOperations instance;
	
	private ClientOperations() {
		File local = new File(localrepo);
		if(!local.exists()) {
			WarnHandler.log("Local file repository doesn't exist. Creating it now...");
			if(local.mkdirs()){
				WarnHandler.log("Local file repository was created successfully");
			} else {
				WarnHandler.exit("Couldn't create local file repository at this time! Exiting...");
			}
		}
		//Procura pelo ficheiro keystore na diretoria onde foi invocado (default: diretoria do projeto)
		File currdir = new File(System.getProperty("user.dir"));
		for(File temp : currdir.listFiles()) {
			if(temp.getName().contains("keystore.")) {
				keystore = new Keystore(temp.getName());
				WarnHandler.log("Local Keystore file was found:"+keystore.getName());
			}
		}
		if(keystore == null) {
			WarnHandler.exit("No valid keystore file was found in working directory ("+System.getProperty("user.dir")+")");
		}
		WarnHandler.log("Validation complete: Proceeding with user request.");
	}
	
	public static ClientOperations getInstance() {
		if(instance == null) {
			instance = new ClientOperations();
		}
		return instance;
	}
	
	public byte[] cipherFile(String filename, ObjectOutputStream oos) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		byte[] retval = null;
		
		try(FileInputStream fis = new FileInputStream(filename)){
			KeyGenerator kg = KeyGenerator.getInstance("AES");
			kg.init(128);
			SecretKey key = kg.generateKey();
			
			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.ENCRYPT_MODE, key);
			
			try(CipherOutputStream cos = new CipherOutputStream(oos, c);){
				shared.StreamHandler.transferStream(fis, oos);
				retval = key.getEncoded();
			}
		} catch (FileNotFoundException e) {
			WarnHandler.error("File "+filename+" doesn't exist. Skipping...");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retval;
	}
	
	public void decipherFile(String filename, ObjectInputStream ois, byte[] keyb) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		File target = new File(localrepo+filename);
		if(!target.exists()) {
			try(FileOutputStream fos = new FileOutputStream(target)){
				SecretKeySpec key = new SecretKeySpec(keyb, "AES");
				
				Cipher c = Cipher.getInstance("AES");
				c.init(Cipher.DECRYPT_MODE, key);
				
				try(CipherInputStream cis = new CipherInputStream(ois, c)){
					shared.StreamHandler.transferStream(cis, fos);
				}
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			WarnHandler.error("File "+filename+" already exists on disk. Skipping...");
		}
	}
	
	public void voidSendKey(byte[] keyb, ObjectOutputStream oos) throws Exception {
		
		Cipher c = Cipher.getInstance("RSA");
		c.init(Cipher.ENCRYPT_MODE, keystore.getPublicKey());
		
		try(CipherOutputStream cos = new CipherOutputStream(oos, c)){
			cos.write(keyb);
			cos.flush();
		}
	}
	
	public byte[] receiveKey(ObjectInputStream ois) throws Exception {
		
		Cipher c = Cipher.getInstance("RSA");
		c.init(Cipher.DECRYPT_MODE, keystore.getPrivateKey()); //TODO: 
		
		long size = (long) ois.readObject();
		
		try(CipherInputStream cis = new CipherInputStream(ois, c)){
			return cis.readNBytes((int) size); //Figure out exact size of key;
		}
	}
	
	public void sendSignature(String filename, ObjectOutputStream oos) throws Exception {
		
		try(FileInputStream fis = new FileInputStream(filename);){
			//Signature s = Signature.getInstance("NONEwithRSA");
			Signature s = Signature.getInstance("SHA256withRSA");
			
			// Assinatura usa chave privada para assinar
			s.initSign(keystore.getPrivateKey());

			long read = 0;
			byte[] buffer = new byte[1024];
			while (read >= 0) {
				read = fis.read(buffer, 0, buffer.length);
				if (read > -1) {
					s.update(buffer, 0, (int) read);
				}
			}
			oos.write(s.sign());
			oos.flush();
		}
	}
	
	public boolean verifySignature(String filename, ObjectInputStream ois) throws Exception {
		
		try (FileInputStream fis = new FileInputStream(filename);) {
			byte[] signature = ois.readAllBytes(); //read key bytes

			Signature s = Signature.getInstance("SHA256withRSA");

			// Assinatura usa chave privada para assinar
			s.initVerify(keystore.getPublicKey());

			long read = 0;
			byte[] buffer = new byte[1024];
			while (read >= 0) {
				read = fis.read(buffer, 0, buffer.length);
				if (read > -1) {
					s.update(buffer, 0, (int) read);
				}
			}

			return s.verify(signature);
		}
	}	
}
