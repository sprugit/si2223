package abstracts;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class AbstractFile {

	protected final static String base_dir = "myCloudServer/";
	protected final String filename;
	
	protected AbstractFile(String filename) throws IOException {
		this.filename = filename;
	}
	
	public abstract boolean exists() throws IOException;
	
	public abstract void receive(ObjectInputStream  ois) throws FileNotFoundException, IOException;
	
	public abstract void send(ObjectOutputStream oos) throws FileNotFoundException, IOException;
	
}
