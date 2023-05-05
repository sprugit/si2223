package abstracts;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import auth.ClientUser;

public class ConcreteClientFile extends AbstractClientFile {

	protected final String target;
	
	public ConcreteClientFile(ClientUser user, String filepath, String target) {
		super(user, filepath);
		this.target = target;
	}

	@Override
	public synchronized void send(ObjectOutputStream oos) throws Exception {}

	@Override
	public synchronized void receive(ObjectInputStream ois) throws Exception {}

}