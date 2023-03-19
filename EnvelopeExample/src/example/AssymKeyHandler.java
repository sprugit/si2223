package example;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;

public class AssymKeyHandler {

	public static PublicKey getPublicKey(String keypath, String password, String alias) throws Exception {
		try(FileInputStream kfile = new FileInputStream(keypath);){
			KeyStore kstore = KeyStore.getInstance("PKCS12");
			kstore.load(kfile, password.toCharArray());    
			return kstore.getCertificate(alias).getPublicKey();
		} 
	}
	
	public static PrivateKey getPrivateKey(String keypath, String password, String alias) throws Exception {
		try(FileInputStream kfile = new FileInputStream(keypath);){
			KeyStore kstore = KeyStore.getInstance("PKCS12");
			kstore.load(kfile, password.toCharArray());    
			return (PrivateKey) kstore.getKey(alias, password.toCharArray());
		}
	}
}