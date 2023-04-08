package abstracts;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import client.Keystore;

public class ConcreteClientFile extends AbstractClientFile {

	public ConcreteClientFile(Keystore user, String filepath) {
		super(user, filepath);
	}

	@Override
	public void send(ObjectOutputStream oos) throws Exception {}

	@Override
	public void receive(ObjectInputStream ois) throws Exception {}

}