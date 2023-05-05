package filetype;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import abstracts.AbstractServerFile;
import server.PathDefs;

public class Certificado extends AbstractServerFile {

	public Certificado(String user) throws Exception {
		super(PathDefs.cdir, user, ".cer");
	}
	
	@Override
	public synchronized void receive(ObjectInputStream ois) throws Exception {
		receiveBytes(ois);
		boolean check = false;
		try(FileInputStream fis = new FileInputStream(getPath());){
			Certificate cf = CertificateFactory.getInstance("X.509").generateCertificate(fis);
			String cuser = ((X509Certificate) cf).getIssuerX500Principal().getName().split(",")[0].substring(3);
			check = !cuser.contentEquals(this.filename);
		}
		if(check) {
			Files.delete(Path.of(getPath()));
			throw new Exception("Invalid Certificate File received.");
		}
	}
}