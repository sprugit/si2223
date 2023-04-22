package filetype;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;

import abstracts.ConcreteClientFile;
import client.Keystore;
import shared.Logger;

public class Cifrado extends ConcreteClientFile {

	protected Cifrado(Keystore user, String filepath) {
		super(user, filepath);
	}

	@Override
	public void send(ObjectOutputStream oos) throws Exception {
		
		Logger.log(filename+": Attempting to upload encrypted file to server.");
		SecretKey key = getKey(null);
		oos.write(cipherKey(key.getEncoded() , Cipher.ENCRYPT_MODE));
		Logger.log(filename+": Key successfully generated and uploaded!");
		
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.ENCRYPT_MODE, key);
		
		Integer fsize = (int) this.lencrypted();
		Logger.log(filename+": Expected encrypted file size is "+String.valueOf(this.lencrypted())+".");
		oos.writeObject((Integer) fsize);
		
		try (FileInputStream fis = new FileInputStream(this.filepath);
			CipherInputStream cis = new CipherInputStream(fis, c)) {
			long read = 1;
			byte[] buf = new byte[512];
			while ((read = cis.read(buf, 0, buf.length)) > -1) {
				oos.write(buf, 0, (int) read);
				oos.flush();
			}
		}
		Logger.log(filename+": Encrypted file successfully uploaded!");
	}

	@Override
	public void receive(ObjectInputStream ois) throws Exception{
		
		Logger.log(filename+": Attempting to download encrypted file from server.");
		SecretKey key = getKey(cipherKey(ois.readNBytes(256), Cipher.DECRYPT_MODE));
		Logger.log(filename+": Key successfully received!");
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.DECRYPT_MODE, key);

		int fsize = (Integer) ois.readObject();
		
		try (FileOutputStream fos = new FileOutputStream(this.user.getUserDir() + this.filepath);
			CipherOutputStream cos = new CipherOutputStream(fos, c)) {
			long read, total = 0;
			byte[] buf = new byte[512];
			do {
				int toBeRead = (int) (fsize - total > buf.length ? buf.length : fsize - total) ;
				read = ois.read(buf, 0, toBeRead);
				cos.write(buf, 0, (int) read);
				total += read;
			}while(total < fsize);
		}
		Logger.log(filename+": Encrypted file successfully downloaded and decrypted!");
	}
}