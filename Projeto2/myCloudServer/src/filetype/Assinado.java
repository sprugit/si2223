package filetype;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import abstracts.ConcreteServerFile;
import server.PathDefs;
import shared.Logger;

public class Assinado extends ConcreteServerFile {
	
	private static final String ext = ".assinado";
	
	protected Assinado(String filename) throws IOException {
		super(filename);
	}

	@Override
	public boolean exists() throws IOException {
		String path = PathDefs.base_dir + dir +this.filename + ext;
		return Files.exists(Path.of(path)) && new Assinatura(this.filename).exists();
	}

	@Override
	public void receive(ObjectInputStream ois) throws FileNotFoundException, IOException {
		Logger.log("User is uploading signature for file: "+ filename);
		String path = PathDefs.base_dir + dir +this.filename + ext;
		
		try(FileOutputStream fos = new FileOutputStream(path);){
			
			receiveBytes(ois, fos);
			Logger.log(filename+": verification file received!");
			
			Assinatura a = new Assinatura(this.filename);
			a.receive(ois);
			Logger.log(filename+": signature file received!");
			
		} catch (ClassNotFoundException e) {
		}
		
	}

	@Override
	public void send(ObjectOutputStream oos) throws FileNotFoundException, IOException {
		
		Logger.log("User requested signature for file: "+ filename);
		String path = PathDefs.base_dir + dir +this.filename + ext;
		
		Assinatura a = new Assinatura(this.filename);
		if(!a.exists()) {
			throw new FileNotFoundException(this.filename+": file has no matching signature file.");
		}
		a.send(oos);
		Logger.log(filename+": signature file sent!");
		
		try(FileInputStream fis = new FileInputStream(path);){
			sendBytes(fis, oos, Files.size(Path.of(path)));
		}
		Logger.log(filename+": verification file sent!");
	}
}