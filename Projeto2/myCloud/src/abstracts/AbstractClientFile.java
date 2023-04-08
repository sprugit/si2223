package abstracts;


import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import client.Keystore;

public abstract class AbstractClientFile{
	
	protected Keystore user;
	protected String filepath;
	protected String filename;
	
	protected AbstractClientFile(Keystore user, String filepath) {
		this.filepath = filepath;
		this.filename = (new File(filepath)).getName();
		this.user = user;
	}
	
	protected long lencrypted() throws Exception {
		long size = Files.size(Path.of(filepath));
		if(size % 16 == 0) {
			return size + 16;
		}
		return (long) (Math.ceil((float) size/16) * 16);
	}
	protected SecretKey getKey(byte[] kbytes) throws Exception {
		SecretKey k = null;
		if(kbytes == null) {
			KeyGenerator kg = KeyGenerator.getInstance("AES");
			kg.init(128);
			k = kg.generateKey();
		} else {
			k = new SecretKeySpec(kbytes, "AES");
		}
		return k;
	}
	
	protected byte[] cipherKey(byte[] kbytes, int mode) throws Exception {
		
		Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		if(mode == Cipher.ENCRYPT_MODE) {
			c.init(mode, this.user.getPublicKey());
		} else {
			c.init(mode, this.user.getPrivateKey());
		}
		return c.doFinal(kbytes);
	}
	
	
	public abstract void send(ObjectOutputStream oos) throws Exception;
	
	public abstract void receive(ObjectInputStream ois) throws Exception;

}