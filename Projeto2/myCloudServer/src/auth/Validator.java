package auth;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import server.PathDefs;
import shared.Logger;

public class Validator {
	
	private static Validator instance = null;
	private final String password;
	
	private Validator() throws Exception {
		
		String password;
		try(Scanner s = new Scanner(System.in);){
			System.out.println("Please input a password for integrity checks:");
			password = s.nextLine();
			
			if(Files.exists(Path.of(PathDefs.vpath))) {
				
				this.password = password;
				verifyMAC();
				
			} else {
				
				Logger.log("Verification file doesn't exist!");
				String i = "o";
				
				while(!"YyNn".contains(i)){
					System.out.println("Verification file doesn't exist! Create new file? [y/n]");
					i = s.nextLine();
				}
				
				if("Yy".contains(i)) {
					Logger.log("Generating new Verification file.");
					this.password = password;
					writeMAC();
					Logger.log("Verification file successfully created!");
				} else {
					Logger.log("Skipped Verification file creation. Further Verifications are now disabled.");
					this.password = null;
				}
			}
		}
	}
	
	
	public synchronized static Validator getValidator() throws Exception {
		if(instance == null){
			instance = new Validator();
		}
		return instance;
	}
	
	private synchronized byte[] genMAC() throws Exception {
		
		SecretKey key = new SecretKeySpec(this.password.getBytes(), "HmacSHA256"); 
		Mac m; 
		byte[]mac=null; 
		m = Mac.getInstance("HmacSHA256"); 
		m.init(key);
		try(FileInputStream fis = new FileInputStream(PathDefs.upath);){
			long read = 0;
			byte[] buff = new byte[1024];
			do {
				read = fis.read(buff, 0, buff.length);
				m.update(buff);   // susbtituir esta linha por um ciclo para usar o ficheiro
			}while(read > 0);
		}		
		mac = m.doFinal();
		return mac;
	}
	
	private synchronized byte[] readMAC() throws Exception {
		
		byte[] retval = null;
 		try(FileInputStream fis = new FileInputStream(PathDefs.vpath);){
			retval = fis.readAllBytes();
		}

 		return retval;
	}
	
	protected synchronized void writeMAC() throws Exception {
		if(this.password != null) {
			
			byte[] MAC = genMAC();
			try(FileOutputStream oos = new FileOutputStream(PathDefs.vpath);){
				oos.write(MAC, 0, MAC.length);
				oos.flush();
			}
			Logger.log("Updated MAC value for password file.");
			
		}
	}
	
	protected synchronized void verifyMAC() throws Exception {
		if(this.password != null) {
			
			byte[] read = readMAC();
			byte[] gen = genMAC();
			Logger.log("Generated MAC value:  "+ Base64.getEncoder().encodeToString(gen));
			Logger.log("Stored MAC value:  "+ Base64.getEncoder().encodeToString(read));
			if(!Base64.getEncoder().encodeToString(gen).contentEquals(Base64.getEncoder().encodeToString(read))) {
				Logger.elog("System integrity damaged: Mismatching MAC on users file detected.\nTerminating myCloud server execution.");
			}
			Logger.log("Matching MACs, proceeding...");
			
		}
	}

}