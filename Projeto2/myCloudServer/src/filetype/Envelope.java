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

public class Envelope extends ConcreteServerFile {

	private static final String ext = ".seguro";
	
	protected Envelope(String filename) throws IOException {
		super(filename);
	}

	@Override
	public boolean exists() throws IOException {
		String path = PathDefs.base_dir + dir +this.filename + ext;
		return Files.exists(Path.of(path)) && new Chave(this.filename).exists()
				&& new Assinatura(this.filename).exists();
	}

	@Override
	public void receive(ObjectInputStream ois) throws FileNotFoundException, IOException {
		Logger.log("User is uploading envelope file: "+ filename);
		String path = PathDefs.base_dir + dir +this.filename + ext;
		
		Chave kf = new Chave(this.filename);
		kf.receive(ois);
		Logger.log(filename+": key file received!");
		
		try (FileOutputStream fos = new FileOutputStream(path);) {

			receiveBytes(ois, fos);
			Logger.log(filename+": encrypted file received!");

		} catch (ClassNotFoundException e) {
		}
		
		Assinatura a = new Assinatura(this.filename);
		a.receive(ois);
		Logger.log(filename+": signature file received!");
	}

	@Override
	public void send(ObjectOutputStream oos) throws FileNotFoundException, IOException {
		
		Logger.log("User requested envelope for file: "+ filename);
		String path = PathDefs.base_dir + dir +this.filename + ext;
		
		Chave kf = new Chave(this.filename);
		kf.send(oos);
		Logger.log(filename+": key file sent!");

		try (FileInputStream fis = new FileInputStream(path);) {

			sendBytes(fis, oos, Files.size(Path.of(path)));
			Logger.log(filename+": encrypted file sent!");
		}
		
		Assinatura a = new Assinatura(this.filename);
		a.send(oos);
		Logger.log(filename+": signature file sent!");
	}
}