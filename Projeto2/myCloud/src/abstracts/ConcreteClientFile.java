package abstracts;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import keystore.ClientUser;

public class ConcreteClientFile extends AbstractClientFile {

	public ConcreteClientFile(ClientUser user, String filepath) {
		super(user, filepath);
	}

	@Override
	public void send(ObjectOutputStream oos) throws Exception {}

	@Override
	public void receive(ObjectInputStream ois) throws Exception {}

}