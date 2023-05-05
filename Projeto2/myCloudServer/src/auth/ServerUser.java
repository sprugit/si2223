package auth;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.cert.Certificate;

import filetype.Certificado;
import server.PathDefs;
import shared.User;

public class ServerUser extends User {
	
	private static final long serialVersionUID = 1L;

	protected ServerUser(String u, String p) {
		super(u, p);
	}
	
	public ServerUser(User u) {
		super(u.getUsername(),u.getPassword());
	}
	
	public void register(ObjectInputStream ois) throws Exception {
		//Store Cert
		new Certificado(username).receive(ois);
		PasswordFile.getFile().newUser(this);
		Files.createDirectories(Path.of(PathDefs.fdir+username));
		
	}
	
	public static boolean exists(String username) throws Exception{
		return PasswordFile.getFile().exists(username) && Files.exists(Path.of(PathDefs.cdir+username+".cer")); 
	}
	
	public static void sendUserCert(String username, ObjectOutputStream oos) throws Exception {
		
		try(ObjectInputStream fis = new ObjectInputStream(new FileInputStream(PathDefs.cdir+username+".cer"));){
			Certificate c = (Certificate) fis.readObject();
			oos.writeObject(c);
		}
		
	}

}