package users;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import filetype.Certificado;
import server.PathDefs;
import shared.User;

public class ServerUser extends User {
	
	protected ServerUser(String u, String p) {
		super(u, p);
	}
	
	public ServerUser(User u) {
		super(u.getUsername(),u.getPassword());
	}
	
	public void register(ObjectInputStream ois) throws Exception {
		//Store Cert
		String filename = username+".cer";
		new Certificado(filename).receive(ois);
		PasswordFile.getFile().newUser(this);
		
	}
	
	public boolean exists() throws Exception{
		return PasswordFile.getFile().exists(this.username) && Files.exists(Path.of(PathDefs.cdir+username+".cer")); 
	}
	
	public static void sendUserCert(String username, ObjectOutputStream oos) throws Exception {
		
		try(ObjectInputStream fis = new ObjectInputStream(new FileInputStream(PathDefs.cdir+username+".cer"));){
			Certificate c = (Certificate) fis.readObject();
			oos.writeObject(c);
		}
		
	}

}