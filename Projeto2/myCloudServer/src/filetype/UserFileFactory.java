package filetype;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import abstracts.ConcreteServerFile;
import shared.Logger;

public class UserFileFactory {
	
	private static UserFileFactory instance = null;

	private UserFileFactory() throws IOException {
	}
	
	public static UserFileFactory getInstance() throws IOException {
		if(instance == null) {
			return new UserFileFactory();
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