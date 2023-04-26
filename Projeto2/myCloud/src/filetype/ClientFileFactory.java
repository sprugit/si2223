package filetype;

import abstracts.ConcreteClientFile;
import keystore.ClientUser;

public class ClientFileFactory {
	
	ClientUser u;

	public ClientFileFactory(ClientUser user) {
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
