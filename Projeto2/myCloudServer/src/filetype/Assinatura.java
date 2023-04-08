package filetype;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import abstracts.Abstract256;

public class Assinatura extends Abstract256 {
	
	private static final String dir = "signatures/"; 
	private static final String ext = ".assinatura";

	protected Assinatura(String filename) throws IOException {
		super(filename);
	}
	
	@Override
	public boolean exists() {
		return Files.exists(Path.of(base_dir + dir + this.filename + ext));
	}
	
	@Override
	public void receive(ObjectInputStream ois) throws FileNotFoundException, IOException {
		try (FileOutputStream fos = new FileOutputStream(base_dir+dir + this.filename + ext);){
			transferData(ois, fos);
		}
	}

	@Override
	public void send(ObjectOutputStream oos) throws FileNotFoundException, IOException {
		try (FileInputStream fis = new FileInputStream(base_dir+dir + this.filename + ext);) {
			transferData(fis, oos);
		}
	}	
}