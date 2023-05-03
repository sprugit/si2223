package abstracts;


import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import auth.ClientUser;

public abstract class AbstractClientFile{
	
	protected ClientUser user;
	protected String filepath;
	protected String filename;
	
	protected AbstractClientFile(ClientUser user, String filepath) {
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
	
	/**
	 * 
	 * @param kbytes
	 * @param mode
	 * @param k
	 * @return
	 * @throws Exception
	 */
	protected byte[] cipherKey(byte[] kbytes, int mode, Key k) throws Exception {
		Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		c.init(mode, k);
		return c.doFinal(kbytes);
	}
	
	/**
	 * 
	 * @param kbytes
	 * @param mode
	 * @return
	 * @throws Exception
	 */
	protected byte[] cipherKey(byte[] kbytes, int mode) throws Exception {
		Key k;
		if(mode == Cipher.ENCRYPT_MODE) {
			k = this.user.getPublicKey();
		}else {
			k = this.user.getPrivateKey();
		}
		return cipherKey(kbytes, mode, k);
	}
	
	public abstract void send(ObjectOutputStream oos) throws Exception;
	
	public abstract void receive(ObjectInputStream ois) throws Exception;

}