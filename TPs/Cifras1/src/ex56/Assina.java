package ex56;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;

import javax.crypto.NoSuchPaddingException;

//https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#Signature
public class Assina {
	
	private static final String input = "files/ex56/assinaturas/";
	
	private static byte[] genKey(File f) {
		byte[] retval = null;
		try(FileInputStream fis = new FileInputStream(f);){
			Signature s = Signature.getInstance("NONEwithRSA");
			
			//Assinatura usa chave privada para assinar
			s.initSign(AssymKeyHandler.getPrivateKey("keystore.chaves", "password", "chaves"));
			
			long read = 0;
			byte[] buffer = new byte[1024];
			while(read >= 0) {
				read = fis.read(buffer, 0, buffer.length);
				s.update(buffer);
			}
			retval = s.sign();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retval;
	}
	
	private static void assina(String filename) {
		
		File target = new File(filename);
		if (!target.exists() || target.isDirectory()) {
			System.err.println("Ficheiro não existe.");
			System.exit(1);
		}
		try(FileOutputStream fos = new FileOutputStream(input+filename+".assinatura");){
			fos.write(genKey(target));
			fos.flush();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	//Verificação está errada I think, check later
	private static boolean verifica(String filename) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, FileNotFoundException, IOException {

		File target = new File(input+filename);
		if (!target.exists() || target.isDirectory()) {
			System.err.println("Ficheiro não existe.");
			System.exit(1);
		}
		
		try(FileInputStream fin = new FileInputStream(target);){
			String sig = Base64.getEncoder().encodeToString(fin.readAllBytes());
			return sig.contentEquals(Base64.getEncoder().encodeToString(genKey(target)));
		} 
	}

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.out.println("Assina ou Verifica ficheiros.\n\nuso:\n'Assina -a <nome_do_ficheiro>' para assinar.\n'Assina -v <nome_do_ficheiro>' para verificar.");
			System.exit(1);
		}
		
		int flag = args[0].contentEquals("-a") ? 0 : 1;
		
		File i = new File(input);
		if(!i.exists()) {
			i.mkdirs();
		}
		
		String filename = args[1];

		switch (flag) {
		case 0:
			assina(filename);
			break;
		case 1:
			System.out.println(verifica(filename));
			break;
		}
	}
}