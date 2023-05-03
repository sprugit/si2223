package filetype;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import abstracts.ComplexServerFile;
import server.PathDefs;

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
	
	public ComplexServerFile getServerFile(String filename, String uploader, String receiver, String type) throws Exception{
		switch(type) {
		case "-c":
			return new Cifrado(filename, uploader, receiver);
		case "-s":
			return new Assinado(filename, uploader, receiver);
		case "-e":
			return new Envelope(filename, uploader, receiver);
		}
		return null;
	}
	
	public ComplexServerFile getExistingFile(String filename, String receiver) throws Exception{
		String[] match = (new File(PathDefs.fdir+receiver+"/").list(new FilenameFilter() {
			
			@Override
			public boolean accept(File arg0, String arg1) {
				return arg1.matches("^"+filename+".\\w.\\w");
			}
		}));
		ComplexServerFile c = null;
		for(String name : match) {
			String[] parts = name.split(".");
			String uploader = parts[parts.length-3];
			switch(parts[parts.length-2]) {
			case "cifrado":
				c = new Cifrado(filename, uploader, receiver);
				break;
			case "assinado":
				c = new Assinado(filename, uploader, receiver);
				break;
			case "seguro":
				c = new Envelope(filename, uploader, receiver);
				break;
			}
		}
		return c;
	}
}