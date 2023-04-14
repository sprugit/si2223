package users;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Scanner;

import shared.Logger;

public class PasswordFile extends Logger{
	
	private class InvalidPasswordFileException extends Exception{

		private static final long serialVersionUID = 1L;

		public InvalidPasswordFileException(String msg) {
			super(msg);
		}
		
	}

	private static final String basepath = "myCloudServer/users";
	private static PasswordFile instance;

	private PasswordFile(String password) throws Exception {
		Validator v = Validator.getValidator(password);
		if(!Files.exists(Path.of(basepath))) {
			log("Password file doens't exist. Creating a new one...");
			try(FileWriter writer = new FileWriter(basepath);){
				String[] passnsalt = genHashedPassword("badpwd");
				writer.write("admin;"+passnsalt[0]+";"+passnsalt[1]+"\n");
			}
			v.writeMAC();
		} else {
			log("Password file found, validating...");
			validate();
			log("Password file validated successfully.");
		}
	}
	
	private synchronized void validate() throws Exception {
		try(Scanner scn = new Scanner(new File(basepath));){
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
		return getFile(null);
	}
	
	public static synchronized PasswordFile getFile(String password) throws Exception {
		if (instance == null) {
			if(password == null) {
				throw new Exception("Password for verification can't be null!");
			}
			instance = new PasswordFile(password);
		}
		return instance;
	}
	
	private synchronized String[] seekUser(String username) throws Exception {
		validate();
		String[] user = null;
		try(Scanner scn = new Scanner(new File(basepath));){
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
	
	public synchronized void newUser(String[] u) throws Exception {
		if(!u[0].matches("^\\w{1,}$")) {
			throw new Exception("Invalid username.");
		}
		validate();
		String[] u1 = seekUser(String.valueOf(u[0]));
		if(u1 == null) {
			try(FileWriter fw = new FileWriter(new File(basepath), true)){
				String[] passnsalt = genHashedPassword(u[1]);
				fw.write(u[0]+";"+passnsalt[0]+";"+passnsalt[1]+"\n");
			}
			Validator.getValidator().writeMAC();
		}
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
	
	public static void main(String[] args) throws Exception {
		
		PasswordFile pf = PasswordFile.getFile("obanana");
		pf.newUser(new String[]{"manuel","password1"});
		
		
	}
}