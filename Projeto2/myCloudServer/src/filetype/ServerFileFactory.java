package filetype;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import abstracts.ConcreteServerFile;
import shared.Logger;

public class ServerFileFactory {
	
	private static ServerFileFactory instance = null;

	private ServerFileFactory() throws IOException {
		if(!Files.exists(Path.of("myCloudServer/"))) {
			Logger.log("myCloudServer: base directory doesn't exist!");
			Files.createDirectory(Path.of("myCloudServer/"));
			Logger.log("myCloudServer: base directory created!");
		}
		String[] dirs = {"files/","keys/","signatures/"};
		for(String directory : dirs) {
			if(!Files.exists(Path.of("myCloudServer/"+directory))) {
				Logger.log(directory+": base directory doesn't exist!");
				Files.createDirectory(Path.of("myCloudServer/"+directory));
				Logger.log(directory+": base directory created!");
			}
		}
	}
	
	public static ServerFileFactory getInstance() throws IOException {
		if(instance == null) {
			return new ServerFileFactory();
		}
		return instance;
	}
	
	public ConcreteServerFile getServerFile(String filename, String type) throws Exception{
		switch(type) {
		case "-c":
			return new Cifrado(filename);
		case "-s":
			return new Assinado(filename);
		case "-e":
			return new Envelope(filename);
		}
		return null;
	}
	
	public ConcreteServerFile getExistingFile(String filename) throws Exception{
		String[] types = {"-c","-s","-e"};
		for(String type : types) {
			ConcreteServerFile o = getServerFile(filename, type);
			if(o.exists()) {
				return o;
			}
		}
		return null;
	}
}