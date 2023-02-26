package ex56;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class AssymKeyHandler {

	public static PublicKey getPublicKey(String keypath, String password, String alias) {
		try(FileInputStream kfile = new FileInputStream(keypath);){
			KeyStore kstore = KeyStore.getInstance("PKCS12");
			kstore.load(kfile, password.toCharArray());    
			return kstore.getCertificate(alias).getPublicKey();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	}
	
	public static PrivateKey getPrivateKey(String keypath, String password, String alias) {
		try(FileInputStream kfile = new FileInputStream(keypath);){
			KeyStore kstore = KeyStore.getInstance("PKCS12");
			kstore.load(kfile, password.toCharArray());    
			return (PrivateKey) kstore.getKey(alias, password.toCharArray());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private static KeyPair getKeystore(String keypath, String password, String alias) {
		return new KeyPair(getPublicKey(keypath, password, alias),getPrivateKey(keypath, password, alias));
	}
	
}
