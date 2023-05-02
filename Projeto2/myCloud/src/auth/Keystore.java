package client;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Keystore {

	private final String keypath;
	
	//muda no projeto 2
	private static final String alias = "chaves";
	private static final String password = "password";
	private static final String dir = "local/";
	
	public Keystore(String path) {
		keypath = path;
	}
	
	public PublicKey getPublicKey() throws Exception {
		try(FileInputStream kfile = new FileInputStream(keypath);){
			KeyStore kstore = KeyStore.getInstance("PKCS12");
			kstore.load(kfile, password.toCharArray());    
			return kstore.getCertificate(alias).getPublicKey();
		} 
	}
	
	public PrivateKey getPrivateKey() throws Exception {
		try(FileInputStream kfile = new FileInputStream(keypath);){
			KeyStore kstore = KeyStore.getInstance("PKCS12");
			kstore.load(kfile, password.toCharArray());    
			return (PrivateKey) kstore.getKey(alias, password.toCharArray());
		}
	}

	public String getName() {
		return keypath;
	}
	
	public String getUserDir() {
		return dir;
		//projeto 2 - retornar para a diretoria do utilizador
		//return dir + alias 
	}
	
}
