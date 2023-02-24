package inicial;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Cifra {

	private static void cifra(String filename) {
		
		File target = new File(filename);
		if (!target.exists() || target.isDirectory()) {
			System.err.println("Ficheiro não existe.");
			System.exit(1);
		}
		
		try(FileInputStream fis = new FileInputStream(filename)){
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		// gerar uma chave aleatoria para utilizar com o AES
		KeyGenerator kg = KeyGenerator.getInstance("AES");
		kg.init(128);
		SecretKey key = kg.generateKey();

		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.ENCRYPT_MODE, key);

		 fis;
		FileOutputStream fos;
		CipherOutputStream cos;

		
		fos = new FileOutputStream("a.cif");

		cos = new CipherOutputStream(fos, c);
		byte[] b = new byte[16];
		int i = fis.read(b);
		while (i != -1) {
			cos.write(b, 0, i);
			i = fis.read(b);
		}
		cos.close();

		byte[] keyEncoded = key.getEncoded();
		FileOutputStream kos = new FileOutputStream("a.key");
		kos.write(keyEncoded);
		kos.close();

	}

	private static void decifra(String filename) {

		File target = new File(filename);
		if (!target.exists() || target.isDirectory()) {
			System.err.println("Ficheiro não existe.");
			System.exit(1);
		}
		
		// Dicas para decifrar
		// byte[] keyEncoded2 - lido do ficheiro
		// SecretKeySpec keySpec2 = new SecretKeySpec(keyEncoded2, "AES");
		// c.init(Cipher.DECRYPT_MODE, keySpec2); //SecretKeySpec é subclasse de
		// secretKey
	}

	public static void main(String[] args) {

		if (args.length == 0) {
			System.out.println("Cifra ou Decifra ficheiros.\n\nuso: Cifra <nome_do_ficheiro>");
			System.exit(1);
		}

		String filename = args[0];
		int index = filename.lastIndexOf('.');
		int type = 0; // 0 se for pra cifrar, 1 se for pra decifrar
		if (index > 0) {
			String ext = filename.substring(index + 1);
			if (ext.contentEquals("cif")) {
				type = 1;
			}
		}

		switch (type) {
		case 0:
			cifra(filename);
			break;
		case 1:
			decifra(filename);
			break;
		}

	}
}
