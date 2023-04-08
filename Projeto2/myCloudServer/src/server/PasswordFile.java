package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Scanner;

import shared.UserPacket;
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

	private PasswordFile() throws Exception {
		File pwfile = new File(basepath);
		if(!pwfile.exists()) {
			log("Password file doens't exist. Creating a new one...");
			File parent = pwfile.getParentFile();
			if(!parent.exists() || parent.isDirectory()) {
				parent.mkdirs();
			}
			FileWriter writer = new FileWriter(pwfile);
			writer.close();
			newUser(new UserPacket("1","Admin","badpwd"));
			log("Password file created successfully");
		} else {
			log("Password file found, validating...");
			validate();
			log("Password file validated successfully.");
		}
	}
	
	private synchronized void validate() throws FileNotFoundException, InvalidPasswordFileException {
		Scanner scn = new Scanner(new File(basepath));
		while(scn.hasNext()) {
			String line =scn.nextLine(); 
			if (!line.matches("^\\d{1,};[\\w\\S]{1,};\\S{1,}$")) {
				//TODO if(scn.hasNextLine()){ assim dá pra concatenar no fim do ficheiro o coiso 
				throw new InvalidPasswordFileException("Password file isn't valid!");
			}
		}
		scn.close();
	}
	
	public static synchronized PasswordFile getFile() throws Exception {
		if (instance == null) {
			instance = new PasswordFile();
		}
		return instance;
	}
	
	private synchronized UserPacket seekUser(String id) throws FileNotFoundException, InvalidPasswordFileException {
		validate();
		UserPacket u = null;
		Scanner scn = new Scanner(new File(basepath));
		while(scn.hasNext()) {
			String ln = scn.nextLine();
			if (ln.matches("^"+id+";[\\w\\S]{1,};\\S{1,}$")) {
				String[] uln = ln.split(";");
				u = new UserPacket(uln[0],uln[1],uln[2]);
			}
		}
		scn.close();
		return u;
	}
	
	public synchronized boolean newUser(UserPacket u) throws Exception {
		UserPacket u1 = seekUser(String.valueOf(u.getId()));
		boolean b = false;
		if(u1 == null) {
			FileWriter fw = new FileWriter(new File(basepath), true);
			fw.write(u.getId()+";"+u.getName()+";"+u.getPasswd()+"\n");
			fw.close();
			b = true;
		} 
		return b;
	}
	
	public synchronized boolean login(UserPacket u) throws FileNotFoundException, InvalidPasswordFileException {
		UserPacket u1 = seekUser(String.valueOf(u.getId()));
		if(u1!= null) {
			return u1.getPasswd().contentEquals(u.getPasswd());
		}
		return false; 
	}	
}