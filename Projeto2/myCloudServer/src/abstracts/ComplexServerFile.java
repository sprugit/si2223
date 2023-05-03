package abstracts;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import filetype.Certificado;
import shared.Logger;

public class ComplexServerFile {

	private final String uploader;
	private final String receiver;
	private final String filename;
	
	
	public ComplexServerFile(String filename, String uploader, String receiver) {
		this.filename = filename;
		this.uploader = uploader;
		this.receiver = receiver;
	}
	
	public boolean exists() throws Exception {
		for(int type : getTypes()) {
			if(!(new ConcreteServerFile(filename, uploader, receiver, ConcreteServerFile.mtypes[type]).exists())) {
				return false;
			}
		}
		return true;
	}
	
	public void receive(ObjectInputStream ois, ObjectOutputStream oos) throws Exception {
		
		boolean c = (boolean) ois.readObject(); //Esperar que o utilizador diga se precisa do cert ou não
		if(c) {
			Logger.log("Client requested certificate for user "+uploader+".");
			new Certificado(uploader).send(oos);
		}
		
		ConcreteServerFile csf; 
		for(int type : getTypes()) { //Receber pela ordem especificada nas subclasses os ficheiros necessários
			csf = new ConcreteServerFile(filename, uploader, receiver, ConcreteServerFile.mtypes[type]);
			Logger.log(csf.filename+": uploading...");
			csf.receive(ois);
			Logger.log(csf.filename+": uploaded successfully!");
		}
	}
	
	public void send(ObjectInputStream ois, ObjectOutputStream oos) throws Exception {
		
		oos.writeObject(uploader); //Perguntar ao utilizador se precisa do certificado especificado
		boolean c = (boolean) ois.readObject();
		if(c) {
			Logger.log("Client requested certificate for user "+uploader+".");
			new Certificado(uploader).send(oos);
		}
		ConcreteServerFile csf;
		for(int type : getTypes()) { //Enviar pela ordem especificada nas subclasses os ficheiros necessários
			csf = new ConcreteServerFile(filename, uploader, receiver, ConcreteServerFile.mtypes[type]);
			Logger.log(csf.filename+": sending file to user...");
			csf.send(oos);
			Logger.log(csf.filename+": sent to user successfully!");			
		}
	}
	
	protected int[] getTypes() throws Exception {
		throw new Exception("Not Implemented!");
	}
}
