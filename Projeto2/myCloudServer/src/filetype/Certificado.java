package filetype;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import abstracts.ConcreteServerFile;
import server.PathDefs;

public class Certificado extends ConcreteServerFile{

	public Certificado(String filename) throws IOException {
		super(filename);
	}
	
	@Override
	public boolean exists() {
		return Files.exists(Path.of(PathDefs.cdir+filename));
	}
	
	@Override
	public void receive(ObjectInputStream ois) throws FileNotFoundException, IOException, ClassNotFoundException {
		try(FileOutputStream fos = new FileOutputStream(PathDefs.cdir + filename);){
			receiveBytes(ois, fos);
		}
	}
	
	@Override
	public void send(ObjectOutputStream oos) throws FileNotFoundException, IOException, CertificateException {
		try(FileInputStream fis = new FileInputStream(PathDefs.cdir + filename);){
			Certificate cf = CertificateFactory.getInstance("X.509").generateCertificate(fis);
			oos.writeObject(cf);
		}
	}
}