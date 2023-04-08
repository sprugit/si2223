package filetype;

import abstracts.ConcreteClientFile;
import client.Keystore;

public class ClientFileFactory {
	
	Keystore u;

	public ClientFileFactory(Keystore user) {
		this.u = user;
	}
	
	public ConcreteClientFile getFile(String filename, String option) {
		switch(option) {
		case "-c":
			return new Cifrado(this.u, filename);
		case "-s":
			return new Assinado(this.u, filename);
		case "-e":
			return new Envelope(this.u, filename);
		}
		return null;
	}
	
	public static void main(String[] args) {
		System.out.println(new ClientFileFactory(null).getFile("texto1.txt","-c"));
		System.out.println(new ClientFileFactory(null).getFile("texto1.txt","-s"));
		System.out.println(new ClientFileFactory(null).getFile("texto1.txt","-e"));
	}
}
