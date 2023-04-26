package users;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Scanner;

import server.PathDefs;
import shared.Logger;
import shared.User;

public class PasswordFile extends Logger{
	
	private class InvalidPasswordFileException extends Exception{

		private static final long serialVersionUID = 1L;

		public InvalidPasswordFileException(String msg) {
			super(msg);
		}
		
	}
	
	private static PasswordFile instance;

	private PasswordFile() throws Exception {
		
		if(!Files.exists(Path.of(PathDefs.upath))) {
			
			log("Password file doesn't exist. Creating a new one...");
			try(FileWriter writer = new FileWriter(PathDefs.upath);){
				String[] passnsalt = genHashedPassword("badpwd");
				writer.write("admin;"+passnsalt[0]+";"+passnsalt[1]+"\n");
			}
			Validator.getValidator().writeMAC();
			
		} else {
			
			log("Password file found, validating...");
			validate();
			log("Password file validated successfully.");
			
		}
	}
	
	private synchronized void validate() throws Exception {
		
		try(Scanner scn = new Scanner(new File(PathDefs.upath));){
			
			while(scn.hasNext()) {
				String line =scn.nextLine(); 
				if (!line.matches("^\\w{1,};[a-zA-Z0-9_/+=]{1,};[a-zA-Z0-9_/+=]{1,}")) { 
					throw new InvalidPasswordFileException("Password file isn't valid!");
				}
			}
		}
		Validator.getValidator().verifyMAC();
	}
	
	public static synchronized PasswordFile getFile() throws Exception {
		if (instance == null) {
			instance = new PasswordFile();
		}
		return instance;
	}
	
	private synchronized String[] seekUser(String username) throws Exception {
		
		validate();
		String[] user = null;
		
		try(Scanner scn = new Scanner(new File(PathDefs.upath));){
			
			while(scn.hasNext()) {
				String ln = scn.nextLine();
				if (ln.contains(username)) {
					
					user = ln.split(";");
					Logger.log("User with username "+username+" was found!");
				}
			}
		}
		
		return user;
	}
	
	private synchronized String[] genHashedPassword(String password) throws Exception {
		
		String hash = null;
		String ssalt = null;
		byte[] salt = new byte[16];
		
		new SecureRandom().nextBytes(salt);
		hash = genHashedPasswordWSalt(password, salt);
		ssalt = Base64.getEncoder().encodeToString(salt);
		
		return new String[]{hash,ssalt};
	}
	
	private synchronized String genHashedPasswordWSalt(String password, byte[] salt) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		md.update(salt);
		return Base64.getEncoder().encodeToString(md.digest(password.getBytes()));
	}
	
	public synchronized void newUser(User u) throws Exception {
		
		if(!u.getUsername().matches("^\\w{1,}$")) {
			throw new Exception("Invalid username.");
		}
		
		validate();
		String[] u1 = seekUser(String.valueOf(u.getUsername()));
		if(u1 == null) {
			
			try(FileWriter fw = new FileWriter(new File(PathDefs.upath), true)){
				
				String[] passnsalt = genHashedPassword(u.getPassword());
				fw.write(u.getUsername()+";"+passnsalt[0]+";"+passnsalt[1]+"\n");
			}
			Validator.getValidator().writeMAC();
		} 
	}
	
	public synchronized boolean exists(String username) throws Exception {
		return seekUser(username) != null;
	}
	
	public synchronized boolean login(String[] u) throws Exception {
		
		validate();
		String[] u1 = seekUser(String.valueOf(u[0]));
		if(u1!= null) {
			
			byte[] salt = Base64.getDecoder().decode(u1[2]);
			return u1[1].contentEquals(genHashedPasswordWSalt(u[1], salt));
		}
		return false; 
	}	
}