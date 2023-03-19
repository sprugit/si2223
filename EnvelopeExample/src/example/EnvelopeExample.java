package example;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Signature;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class EnvelopeExample {
	
	private static final String path = "files/";
	
	public static SecretKey getKey() throws Exception {
		KeyGenerator kg = KeyGenerator.getInstance("AES");
		kg.init(128); // 128 bits means 16byte key
		return  kg.generateKey();
	}
	
	public static void crypt(String filename) throws Exception {
		
		Cipher c = Cipher.getInstance("AES");
		SecretKey key = getKey();
		c.init(Cipher.ENCRYPT_MODE, key);
		
		try(FileInputStream fis = new FileInputStream(path+filename);
			FileOutputStream fos = new FileOutputStream(path+filename+".safe");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();){
			
			Signature s = Signature.getInstance("SHA256withRSA");
			s.initSign(AssymKeyHandler.getPrivateKey("keystore.chaves", "password", "chaves"));
			
			fos.write(key.getEncoded());
			System.out.println("Generated key: "+Base64.getEncoder().encodeToString(key.getEncoded()));
			
			byte[] buffer = new byte[512];
			long read;
			try(CipherOutputStream cos = new CipherOutputStream(baos, c);){
				do {
					read = fis.read(buffer, 0, buffer.length);
					s.update(buffer, 0, (int) read);
					cos.write(buffer, 0, (int) read);
					baos.flush();
					fos.write(baos.toByteArray());
					baos.reset();
				}while(read == buffer.length && read % 16 == 0);
			} //Post cipher close to force cipher end
			fos.write(baos.toByteArray());
			
			byte[] sigb = s.sign();
			System.out.println("Generated Signature: "+Base64.getEncoder().encodeToString(sigb));
			fos.write(sigb);
		}
	}
	
	public static void decrypt(String filename) throws Exception {
		
		File toDec = new File(filename+".safe");
		long size = toDec.length() - 256 - 16; //actualfile size = filebytes - signature - symkey
		//long size = toDec.length() - (256*2);
		
		try(FileInputStream fis = new FileInputStream(path+toDec);
			FileOutputStream fos = new FileOutputStream(path+"decrypted_"+filename);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();){
			
			byte[] keyb = fis.readNBytes(16); //
			System.out.println("Read Key: "+Base64.getEncoder().encodeToString(keyb));
			SecretKeySpec key = new SecretKeySpec(keyb, "AES");
			
			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.DECRYPT_MODE, key);
			
			Signature s = Signature.getInstance("SHA256withRSA");
			s.initVerify(AssymKeyHandler.getPublicKey("keystore.chaves", "password", "chaves"));
			
			byte[] buffer = new byte[512];
			byte[] temp;
			long read;
			long bleft; //amount of bytes left to read
			long total = 0; //total bytes read so far
			try(CipherOutputStream cos = new CipherOutputStream(baos, c);){
				do {
					bleft = ( (total + buffer.length) > size ) ? size-total : buffer.length; //Avoid reading the signature!
					read = fis.read(buffer, 0, (int) bleft);
					cos.write(buffer, 0, (int) read);
					temp = baos.toByteArray(); // temp contains bytes already deciphered
					s.update(temp);
					fos.write(temp);
					baos.reset();
				}while(read == buffer.length && read % 16 == 0);
			} //Post cipher close to force cipher to end
			fos.write(baos.toByteArray());
			s.update(baos.toByteArray());
			
			byte[] sig = fis.readNBytes(256);
			System.out.println("Read Signature: " + Base64.getEncoder().encodeToString(sig));
			
			System.out.println("Signature Matching Status: " + s.verify(sig));
		}
	}

	public static void main(String[] args) throws Exception {
		
		String filename = "ooga";
		
		new File(path).mkdirs();
		
		crypt(filename);
		
		decrypt(filename);
		
	}

}
