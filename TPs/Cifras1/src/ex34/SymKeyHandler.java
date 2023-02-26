package ex34;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;

public class SymKeyHandler {

	public static void writeKey(String filename, byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		
		Cipher c = Cipher.getInstance("RSA");
		c.init(Cipher.ENCRYPT_MODE, AssymKeyHandler.getPublicKey("keystore.chaves", "password", "chaves"));
		
		try(FileOutputStream fos = new FileOutputStream(new File(filename+".key"));
			CipherOutputStream cos = new CipherOutputStream(fos, c)){
			cos.write(key);
			cos.flush();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static byte[] readKey(String filename) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		File keyfile = new File(filename+".key");
		byte[] retval = new byte[0];
		
		Cipher c = Cipher.getInstance("RSA");
		c.init(Cipher.DECRYPT_MODE, AssymKeyHandler.getPrivateKey("keystore.chaves", "password", "chaves"));
		
		try(FileInputStream fis = new FileInputStream(keyfile);
			CipherInputStream cis = new CipherInputStream(fis,c)){
			retval =  cis.readNBytes((int) keyfile.length());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retval;
	}
}
