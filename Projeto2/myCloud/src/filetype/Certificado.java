package filetype;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import abstracts.AbstractClientFile;
import client.PathDefs;
import shared.Logger;

public class Certificado extends AbstractClientFile {

	public Certificado(String user) {
		super(null, (user.matches("[\\/\\w*]*\\/\\w+.cer") ?  user : PathDefs.certificates + user + ".cer"));
	}
	
	
	@Override
	public synchronized void send(ObjectOutputStream oos) throws Exception {
		
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

	@Override
	public synchronized void receive(ObjectInputStream ois) throws Exception {
		
		int fsize = (Integer) ois.readObject();
		try(FileOutputStream fos = new FileOutputStream(this.filepath)) {
			
			long read = 0;
			long total = 0;
			byte[] buf = new byte[512];
			do {
				int toBeRead = (int) (fsize - total > buf.length ? buf.length : fsize - total) ;
				read = ois.read(buf, 0, toBeRead);
				total += read;
				fos.write(buf, 0 , (int) read);
			}while(total < fsize);
		}
	}	
	
	public synchronized Certificate load() throws Exception {
		
		try(FileInputStream fis = new FileInputStream(this.filepath)){
			
			Certificate cf = CertificateFactory.getInstance("X.509").generateCertificate(fis);
			return cf;
		}
	}
}