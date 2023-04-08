package filetype;

import java.nio.file.Files;
import java.nio.file.Path;

import abstracts.ConcreteServerFile;
import shared.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Cifrado extends ConcreteServerFile{

	private static final String ext = ".cifrado";
	
	protected Cifrado(String filename) throws IOException {
		super(filename);
	}

	@Override
	public boolean exists() throws IOException {
		String path = base_dir + dir +this.filename + ext;
		return Files.exists(Path.of(path)) && new Chave(this.filename).exists();
	}

	@Override
	public void receive(ObjectInputStream ois) throws FileNotFoundException, IOException {
		Logger.log("User is uploading encrypted file: "+ filename);
		String path = base_dir + dir +this.filename + ext;
		
		Chave kf = new Chave(this.filename);
		kf.receive(ois);
		Logger.log(filename+": key file received!");
		
		try(FileOutputStream fos = new FileOutputStream(path);){
			
			receiveBytes(ois, fos);
			Logger.log(filename+": encrypted file received!");
	
		} catch (ClassNotFoundException e) {
		}
	}

	@Override
	public void send(ObjectOutputStream oos) throws FileNotFoundException, IOException {
		
		Logger.log("User requested encrypted file for file: "+ filename);
		String path = base_dir + dir +this.filename + ext;
		
		Chave kf = new Chave(this.filename);
		if(!kf.exists()) {
			throw new FileNotFoundException(this.filename+": file has no key to decypher.");
		}
		kf.send(oos);
		Logger.log(filename+": key file sent!");
		
		try(FileInputStream fis = new FileInputStream(path);){
			
			sendBytes(fis, oos, Files.size(Path.of(path)));
			Logger.log(filename+": encrypted file sent!");
		}
	}	
}