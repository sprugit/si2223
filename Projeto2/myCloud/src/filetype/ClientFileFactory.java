package filetype;

import abstracts.ConcreteClientFile;
import auth.ClientUser;

public class ClientFileFactory {
	
	ClientUser u;

	public ClientFileFactory(ClientUser user) {
		this.u = user;
	}
	
	public ConcreteClientFile getFile(String filename, String target, String option) {
		switch(option) {
		case "-c":
			return new Cifrado(this.u, filename, target);
		case "-s":
			return new Assinado(this.u, filename, target);
		case "-e":
			return new Envelope(this.u, filename, target);
		}
		return null;
	}
}