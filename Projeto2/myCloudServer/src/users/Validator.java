package users;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import shared.Logger;

public class Validator {
	
	private static final String ufilepath = "myCloudServer/users";
	private static final String vfilepath = "myCloudServer/passwords.mac";
	private static Validator instance = null;
	private final String password;
	
	private Validator(String password) {
		this.password = password;
	}
	
	public synchronized static Validator getValidator() throws Exception {
		return getValidator(null);
	}
	
	public synchronized static Validator getValidator(String password) throws Exception {
		if(instance == null){
			if(password == null) {
				throw new Exception("Password for verification can't be null!");
			}
			instance = new Validator(password);
		}
		return instance;
	}
	
	private synchronized byte[] genMAC() throws Exception {
		
		SecretKey key = new SecretKeySpec(this.password.getBytes(), "HmacSHA256"); 
		Mac m; 
		byte[]mac=null; 
		m = Mac.getInstance("HmacSHA256"); 
		m.init(key);
		try(FileInputStream fis = new FileInputStream(ufilepath);){
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
 		try(FileInputStream fis = new FileInputStream(vfilepath);){
			retval = fis.readAllBytes();
		}
 		Logger.log("Stored MAC value is: "+ Base64.getEncoder().encodeToString(retval));
 		return retval;
	}
	
	protected synchronized String writeMAC() throws Exception {
		
		byte[] MAC = genMAC();
		try(FileOutputStream oos = new FileOutputStream(vfilepath);){
			oos.write(MAC, 0, MAC.length);
			oos.flush();
		}
		Logger.log("Updated MAC value for password file.");
		return Base64.getEncoder().encodeToString(MAC);
	}
	
	protected synchronized void verifyMAC() throws Exception {
		
		byte[] read = readMAC();
		byte[] gen = genMAC();
		Logger.log("Generated MAC value is: "+ Base64.getEncoder().encodeToString(gen));
		Logger.log("Read MAC value is: "+ Base64.getEncoder().encodeToString(read));
		if(!Base64.getEncoder().encodeToString(gen).contentEquals(Base64.getEncoder().encodeToString(read))) {
			Logger.elog("System integrity damaged: Mismatching MAC on users file detected.");
		}
		Logger.log("Matching MACs, proceeding...");
	}
}