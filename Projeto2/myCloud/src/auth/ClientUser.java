package keystore;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Scanner;

import client.PathDefs;
import shared.Logger;
import shared.User;

public class ClientUser extends User{

	private ClientUser(String u, String p) {
		super(u, p);
	}

	private static final long serialVersionUID = 1L;
	private KeyStore kstore;
	
	public void getKeystore() throws Exception {
		kstore = KeyStore.getInstance("PKCS12");
		try(FileInputStream kfile = new FileInputStream(this.getUsername()+".keystore");){
			kstore.load(kfile, this.getPassword().toCharArray());
		} catch (FileNotFoundException e) {
			throw new Exception("Keystore file doesn't exist!");
		}
	}
	
	public PublicKey getPublicKey() throws Exception {
		return kstore.getCertificate(this.getUsername()).getPublicKey(); 
	}
	
	public PrivateKey getPrivateKey() throws Exception {
		return (PrivateKey) kstore.getKey(this.getUsername(), this.getPassword().toCharArray());
	}
	
	public void regUser(ObjectOutputStream oos) throws Exception {
		oos.writeObject(this.kstore.getCertificate(getUsername()));
		oos.flush();
	}
	
	public String getUserDir() {
		return PathDefs.dir + this.getUsername() + "/";
	}
	
	public static PublicKey getCertKey(String username, ObjectInputStream ois, ObjectOutputStream oos) throws Exception {
		
		oos.writeObject(username);
		Certificate c = (Certificate) ois.readObject();
		if(c!= null) {
			return c.getPublicKey();
		}
		return null;
	}

}