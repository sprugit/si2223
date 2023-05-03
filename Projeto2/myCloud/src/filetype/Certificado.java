package filetype;

import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import abstracts.ConcreteClientFile;
import shared.Logger;

public class Certificado extends ConcreteClientFile {

	public Certificado(String path) {
		super(null, path);
	}
	
	@Override
	public void send(ObjectOutputStream oos) throws Exception {
		
		Logger.log(filename+": uploading cert file to server.");
		try(FileInputStream fis = new FileInputStream(this.filepath)){
			oos.writeObject((int) Files.size(Path.of(this.filepath)));
			byte[] buf = new byte[512];
			long read;
			while((read = fis.read(buf, 0, buf.length)) > 0) {
				oos.write(buf, 0, (int) read);
				oos.flush();
			}
		} 
		Logger.log(filename+": uploaded successfully!");
	}

	
	
}
