package client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Signature;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import shared.CommsHandler;
import shared.WarnHandler;

public class ClientOperations {

	private static final String localrepo = "local/";
	private static Keystore keystore = null;
	private static ClientOperations instance;
	private static final String ASSYMALGO = "RSA/ECB/PKCS1Padding";
	
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
	
	public byte[] sendFile(String filename, ObjectInputStream in, ObjectOutputStream out) throws Exception {
		byte[] retval = null;
		
		KeyGenerator kg = KeyGenerator.getInstance("AES");
		kg.init(128);
		SecretKey key = kg.generateKey();
		
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.ENCRYPT_MODE, key);

		try(FileInputStream fis = new FileInputStream(filename);
			CipherInputStream cis = new CipherInputStream(fis, c)){
			CommsHandler.sendAll(cis, out);
			retval = key.getEncoded();
		} 
		return retval;
	}

	public void receiveFile(String filename, ObjectInputStream ois, byte[] keyb) throws Exception {
		
		SecretKeySpec key = new SecretKeySpec(keyb, "AES");		
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.DECRYPT_MODE, key);

		try(FileOutputStream fos = new FileOutputStream(localrepo+filename);
			CipherOutputStream cos = new CipherOutputStream(fos, c)){
			CommsHandler.ReceiveAll(ois, cos);
		}
	}
	
	public void sendKey(byte[] keyb, ObjectOutputStream oos) throws Exception {
		
		Cipher c = Cipher.getInstance(ASSYMALGO);
		c.init(Cipher.ENCRYPT_MODE, keystore.getPublicKey());
		
		byte[] keyc = new byte[256]; 
		keyc = c.doFinal(keyb);
		CommsHandler.sendFullByteArray(keyc, oos);
	}
	
	public byte[] receiveKey(ObjectInputStream ois) throws Exception {
		
		Cipher c = Cipher.getInstance(ASSYMALGO);
		c.init(Cipher.DECRYPT_MODE, keystore.getPrivateKey()); //TODO: 
		
		byte[] ckey = CommsHandler.receiveByte(ois);
		
		return c.doFinal(ckey);
	}
	
	public void sendSignature(String filename, ObjectOutputStream oos) throws Exception {
		
		try (FileInputStream fis = new FileInputStream(filename)) {
			//Signature s = Signature.getInstance("NONEwithRSA");
	        Signature s = Signature.getInstance("SHA256withRSA");
	        
	        // Assinatura usa chave privada para assinar
	        s.initSign(keystore.getPrivateKey());

	        byte[] buffer = new byte[1024];
	        int bytesRead;
	        while ((bytesRead = fis.read(buffer)) != -1) {
	            s.update(buffer, 0, bytesRead);
	            CommsHandler.sendNBytes(buffer, bytesRead, oos);
	        }
	        CommsHandler.sendFullByteArray(new byte[0], oos);
	        //sends empty byte array to signal EOF
	        
	        byte[] signature = s.sign();
	        CommsHandler.sendFullByteArray(signature, oos);
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
	
	public void sendEnvelope(String filename, ObjectOutputStream oos) throws Exception {
		
		KeyGenerator kg = KeyGenerator.getInstance("AES");
		kg.init(128);
		SecretKey key = kg.generateKey();
		
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.ENCRYPT_MODE, key);
		
		try(FileInputStream fis = new FileInputStream(filename);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
			
			Signature s = Signature.getInstance("SHA256withRSA");
			s.initSign(keystore.getPrivateKey());
			
			sendKey(key.getEncoded(), oos);
			WarnHandler.log("Key successfully sent to server.");
			
			byte[] buf = new byte[512];
			long read;
			try(CipherOutputStream cos = new CipherOutputStream(baos, c);) {
				do {
					read = fis.read(buf, 0, buf.length);
					s.update(buf, 0, (int) read);
					cos.write(buf, 0, (int) read);
					baos.flush();
					CommsHandler.sendFullByteArray(baos.toByteArray(), oos);
					baos.reset();
				}while(read == buf.length && read % 16 == 0);
			}
			CommsHandler.sendFullByteArray(baos.toByteArray(), oos);
			CommsHandler.sendFullByteArray(new byte[0], oos); //inform server file contents were fully sent
			
			byte[] sig = s.sign();
			CommsHandler.sendFullByteArray(sig, oos);
			WarnHandler.log("Signature generated and sent to server.");
			
			WarnHandler.log("Envelope successfully sent to the server.");
		}
	}
	
	public void receiveEnvelope(String filename, ObjectInputStream ois) throws Exception {
		
		SecretKeySpec key = new SecretKeySpec(receiveKey(ois), "AES");
		WarnHandler.log("Successfully received symkey from remote host.");
		
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.ENCRYPT_MODE, key);
		
		try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
			FileOutputStream fos = new FileOutputStream(localrepo+filename);){
			
			Signature s = Signature.getInstance("SHA256withRSA");
			s.initVerify(keystore.getPublicKey());
			
			byte[] temp;
			try(CipherOutputStream cos = new CipherOutputStream(baos, c);){
				do {
					//fazer controlo do que é enviado no servidor
					//think better about this one tomorrow!
					temp = CommsHandler.receiveByte(ois);
					cos.write(temp);
					temp = baos.toByteArray(); // temp contains bytes already deciphered
					s.update(temp);
					fos.write(temp);
					baos.reset();
				}while(temp.length != 0);
			} //Post cipher close to force cipher to end
			fos.write(baos.toByteArray());
			s.update(baos.toByteArray());
			
			byte[] sig = CommsHandler.receiveByte(ois);
			WarnHandler.log("Signature received from remote host.");
			if(s.verify(sig)) {
				WarnHandler.log("Received file's signature matches generated signature.");
			} else {
				WarnHandler.log("Received fiel's signature does not match generated signature.");
			}
			
		}
		
	}
}