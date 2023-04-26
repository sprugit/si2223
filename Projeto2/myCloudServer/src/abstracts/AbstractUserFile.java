package abstracts;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class AbstractUserFile {

	protected final String filename;
	
	protected AbstractUserFile(String filename) throws IOException {
		this.filename = filename;
	}
	
	public abstract boolean exists() throws IOException;
	
	public abstract void receive(ObjectInputStream  ois) throws Exception;
	
	public abstract void send(ObjectOutputStream oos) throws Exception;
	
}
