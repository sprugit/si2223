package client;

import java.nio.file.Files;
import java.nio.file.Path;

import shared.Logger;

public class PathDefs {
	
	public static final String dir = "local/";
	public static final String keystores = dir + "keystore/";
	public static final String certificates = dir + "certs/";
	public static final String fdir = dir + "files/";
	

	public static synchronized void initialize() throws Exception {
		
		for(String path : new String[]{keystores, certificates, fdir}) {
			Path p = Path.of(path);
			if(Files.notExists(p)) {
				Logger.log("Directory '"+path+"' didn't exist. Creating...");
				Files.createDirectories(p);
			}
			
		}
		
	}
	
	public static void main(String[] args) throws Exception{
		initialize();
	}
	
}
