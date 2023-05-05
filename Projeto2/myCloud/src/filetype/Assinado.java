package filetype;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.PublicKey;
import java.security.Signature;

import abstracts.ConcreteClientFile;
import auth.ClientUser;
import shared.Logger;

public class Assinado extends ConcreteClientFile {

	protected Assinado(ClientUser user, String filepath, String target) {
		super(user, filepath, target);
	}

	@Override
	public synchronized void send(ObjectOutputStream oos) throws Exception{
		
		Logger.log(filename+": Attempting to upload signature file to server.");
		oos.writeObject((int) Files.size(Path.of(this.filepath)));
		
		try (FileInputStream fis = new FileInputStream(this.filepath)) {
			// Signature s = Signature.getInstance("NONEwithRSA");
			Signature s = Signature.getInstance("SHA256withRSA");

			// Assinatura usa chave privada para assinar
			s.initSign(this.user.getPrivateKey());

			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = fis.read(buffer)) != -1) {
				s.update(buffer, 0, bytesRead);
				oos.write(buffer, 0, bytesRead);
			}
			Logger.log(filename+": Verification file successfully uploaded!");
			byte[] signature = s.sign();
			oos.writeObject((int) 256);
			oos.write(signature);
			Logger.log(filename+": Signature successfully generated and uploaded!");
		}
	}

	@Override
	public synchronized void receive(ObjectInputStream ois, ObjectOutputStream oos) throws Exception{
		
		String uploader = (String) ois.readObject();
		Certificado c = new Certificado(uploader);
		if(!c.exists()) {
			oos.writeObject(true);
			c.receive(ois);
		} else {
			oos.writeObject(false);
		}
		
		Logger.log(filename+": Attempting to download signature file from server.");
		ois.readObject();
		byte[] signature = ois.readNBytes(256); // read key bytes
		Signature s = Signature.getInstance("SHA256withRSA");
		Logger.log(filename+": Signature successfully downloaded!");
		Key k = uploader.contentEquals(user.getUsername()) ?
				this.user.getPublicKey() : new Certificado(uploader).load().getPublicKey();
		s.initVerify((PublicKey) k);
		
		long read, total = 0;
		int fsize = (Integer) ois.readObject();
		byte[] buf = new byte[512];
		do {
			read = ois.read(buf, 0, (int) buf.length);
			total += read;
			s.update(buf, 0, (int) read);
		} while(total < fsize);
		Logger.log(filename+": Verification file successfully downloaded!");

		String message = "Expected signature doesn't match received signature.";
		if(s.verify(signature)) {
			message = "Expected signature matches received signature.";
		}
		Logger.log(filename+": "+message);	
	}
}