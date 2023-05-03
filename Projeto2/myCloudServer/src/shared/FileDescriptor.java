package shared;

import java.io.File;
import java.io.Serializable;

public class FileDescriptor implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private final String filename;
	private final String sender;
	private final String target;
	private final long fsize;
	
	public FileDescriptor(String filepath, String s, String t) throws Exception {
		File f = new File(filepath);
		if(!f.exists()) {
			throw new Exception("File doesn't exist!");
		}
		filename = f.getName();
		sender = s;
		target = t;
		fsize = f.length();
	}

	public String getFilename() {
		return filename;
	}

	public String getSender() {
		return sender;
	}

	public String getTarget() {
		return target;
	}

	public long getFsize() {
		return fsize;
	}
}