package ex78;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MACgen {

	private static final String input = "teste1.pdf";
	private static final String MACpath = "files/ex78/teste1.mac";
	
	public static byte[] genMAC(File f) throws Exception {
		
		byte [] pass = "maria12".getBytes(); 
		SecretKey key = new SecretKeySpec(pass, "HmacSHA256"); 
		
		Mac m; 
		byte[]mac=null; 
		m = Mac.getInstance("HmacSHA256"); 
		m.init(key);
		try(FileInputStream fis = new FileInputStream(f);){
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
	
	public static String writeMAC(File f, File MACfile) throws Exception {
		
		byte[] MAC = genMAC(f);
		try(FileOutputStream oos = new FileOutputStream(MACfile);){
			oos.write(MAC, 0, MAC.length);
			oos.flush();
		}
		return Base64.getEncoder().encodeToString(MAC);
	}
	
	public static byte[] readMAC(File f) throws Exception {
		
		byte[] retval = null;
 		try(FileInputStream fis = new FileInputStream(f);){
			retval = fis.readAllBytes();
		}
 		System.out.println("Stored MAC value is: "+ Base64.getEncoder().encodeToString(retval));
 		return retval;
	}
	
	public static void verifyMAC(File f,File MACfile) throws Exception {
		
		byte[] read = readMAC(MACfile);
		byte[] gen = genMAC(f);
		System.out.println("Generated MAC value is: "+ Base64.getEncoder().encodeToString(gen));
		
		System.out.println("Generated MAC == Stored MAC :" + Base64.getEncoder().encodeToString(gen).contentEquals(Base64.getEncoder().encodeToString(read)));
		
	}
	
	public static void main(String[] args) throws Exception {

	
		File toBeRead = new File(input);
		File MACfile = new File(MACpath);
		writeMAC(toBeRead, MACfile);
		verifyMAC(toBeRead, MACfile);
		
		
	}
	
}
