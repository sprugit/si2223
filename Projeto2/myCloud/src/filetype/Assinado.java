package filetype;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Signature;

import abstracts.ConcreteClientFile;
import auth.ClientUser;
import shared.Logger;

public class Assinado extends ConcreteClientFile {

	protected Assinado(ClientUser user, String filepath) {
		super(user, filepath);
	}

	@Override
	public void send(ObjectOutputStream oos) throws Exception{
		
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
			oos.write(signature);
			Logger.log(filename+": Signature successfully generated and uploaded!");
		}
	}

	@Override
	public void receive(ObjectInputStream ois) throws Exception{
		
		Logger.log(filename+": Attempting to download signature file from server.");
		byte[] signature = ois.readNBytes(256); // read key bytes
		Signature s = Signature.getInstance("SHA256withRSA");
		Logger.log(filename+": Signature successfully downloaded!");
		s.initVerify(this.user.getPublicKey());
		
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