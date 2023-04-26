package filetype;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Signature;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;

import abstracts.ConcreteClientFile;
import client.PathDefs;
import keystore.ClientUser;
import shared.Logger;

public class Envelope extends ConcreteClientFile {

	protected Envelope(ClientUser user, String filepath) {
		super(user, filepath);
	}

	@Override
	public void send(ObjectOutputStream oos) throws Exception{
		
		Logger.log(filename+": Attempting to upload secure envelope to server.");
		SecretKey key = getKey(null);
		oos.write(cipherKey(key.getEncoded() , Cipher.ENCRYPT_MODE));
		Logger.log(filename+": Key successfully generated and uploaded!");

		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.ENCRYPT_MODE, key);
		
        Signature s = Signature.getInstance("SHA256withRSA");
        s.initSign(this.user.getPrivateKey());
		
        Integer fsize = (int) this.lencrypted();
		Logger.log(filename+": Expected encrypted file size is "+String.valueOf(this.lencrypted())+".");
		oos.writeObject((Integer) fsize);
        
		try(FileInputStream fis = new FileInputStream(filepath);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();){
			
			byte[] buf = new byte[512];
			long read;
			try(CipherOutputStream cos = new CipherOutputStream(baos, c)){
				while((read = fis.read(buf, 0 , buf.length)) > -1) {
					s.update(buf, 0, (int) read);
					cos.write(buf, 0, (int) read);
					oos.write(baos.toByteArray());
					baos.reset();
				}
			} //Terminar o bloco da cifra
			oos.write(baos.toByteArray());
			Logger.log(filename+": Encrypted file successfully uploaded!");
			
			//Enviar para o servidor a assinatura
			oos.write(s.sign());
			Logger.log(filename+": Signature successfully generated and uploaded!");
		}	
	
	}

	@Override
	public void receive(ObjectInputStream ois) throws Exception {
		
		Logger.log(filename+": Attempting to download secure envelope from server.");
		SecretKey key = getKey(cipherKey(ois.readNBytes(256), Cipher.DECRYPT_MODE));
		Logger.log(filename+": Key successfully received!");
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.DECRYPT_MODE, key);
		
		Signature s = Signature.getInstance("SHA256withRSA");
        s.initVerify(this.user.getPublicKey());
        
        int fsize = (Integer) ois.readObject();
        
        try(FileOutputStream fos = new FileOutputStream(PathDefs.dir + user.getUsername() + "/" + this.filepath);
        	ByteArrayOutputStream baos = new ByteArrayOutputStream();){
        	
        	byte[] temp, buf = new byte[512];
        	long read, total = 0;
        	try(CipherOutputStream cos = new CipherOutputStream(baos, c)){
        		do {
        			int toBeRead = (int) (fsize - total > buf.length ? buf.length : fsize - total);
        			read = ois.read(buf, 0, toBeRead);
        			total += read;
        			cos.write(buf, 0 , (int) read);
        			temp = baos.toByteArray();
        			baos.reset();
        			s.update(temp);
        			fos.write(temp);
        		}while(total < fsize);	
        	} // Terminar bloco da cifra
        	
        	temp = baos.toByteArray();
        	s.update(temp);
        	fos.write(temp);
        	Logger.log(filename+": Encrypted file successfully downloaded and decrypted!");
        }
        
        byte[] sig = ois.readNBytes(256);
		String message = "Expected signature doesn't match received signature.";
		if(s.verify(sig)) {
			message = "Expected signature matches received signature.";
		}
		Logger.log(filename+": "+message);	
	}
}