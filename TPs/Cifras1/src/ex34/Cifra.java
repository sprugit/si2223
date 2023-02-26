package ex34;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Certificate;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import ex34.SymKeyHandler.*;

public class Cifra {
	
	private static final String input = "files/ex34/cifrados/";
	private static final String output = "files/ex34/decifrados/";
	
	private static byte[] writeCipher(FileInputStream fis, FileOutputStream fos) throws NoSuchAlgorithmException, IOException, NoSuchPaddingException, InvalidKeyException {
		KeyGenerator kg = KeyGenerator.getInstance("AES");
		kg.init(128);
		SecretKey key = kg.generateKey();
		
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.ENCRYPT_MODE, key);
		
		try(CipherOutputStream cos = new CipherOutputStream(fos, c);){
			byte[] b = new byte[16];
			int i = fis.read(b);
			while (i != -1) {
				cos.write(b, 0, i);
				i = fis.read(b);
			}
		}
		return key.getEncoded();
	}
	
	private static void cifra(String filename) {
		
		File target = new File(filename);
		if (!target.exists() || target.isDirectory()) {
			System.err.println("Ficheiro não existe.");
			System.exit(1);
		}
		try(FileInputStream fis = new FileInputStream(filename);
			FileOutputStream fos = new FileOutputStream(input+filename+".cif");){
			byte[] key = writeCipher(fis, fos); //ciphers file and returns key
			SymKeyHandler.writeKey(input+filename, key);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	private static void decifra(String filename) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {

		File target = new File(input+filename);
		if (!target.exists() || target.isDirectory()) {
			System.err.println("Ficheiro não existe.");
			System.exit(1);
		}
		String truename = filename.substring(0, filename.lastIndexOf("."));
		byte[] keyb = SymKeyHandler.readKey(input+truename);
		SecretKeySpec key = new SecretKeySpec(keyb, "AES");
		
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.DECRYPT_MODE, key);
		
		
		try(FileInputStream fin = new FileInputStream(target);
			CipherInputStream cis = new CipherInputStream(fin, c);
			FileOutputStream fout = new FileOutputStream(new File(output+truename))){
			
			byte[] b = new byte[16];
			int i = cis.read(b);
			while (i != -1) {
				fout.write(b, 0, i);
				i = cis.read(b);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.out.println("Cifra ou Decifra ficheiros.\n\nuso: Cifra <nome_do_ficheiro>");
			System.exit(1);
		}
		
		File i = new File(input);
		File o = new File(output);
		if(!i.exists()) {
			i.mkdirs();
		}
		if(!o.exists()) {
			o.mkdirs();
		}
		
		String filename = args[0];
		int index = filename.lastIndexOf('.');
		int type = 0; // 0 se for pra cifrar, 1 se for pra decifrar
		if (index > 0) {
			String ext = filename.substring(index + 1);
			if (ext.contentEquals("cif")) {
				type = 1;
			}
		}
		switch (type) {
		case 0:
			cifra(filename);
			break;
		case 1:
			decifra(filename);
			break;
		}
	}
}