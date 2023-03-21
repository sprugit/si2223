package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Base64;

import shared.CommsHandler;
import shared.WarnHandler;

public class ServerOperations {

	private static final String basepath = "myCloudServer";
	private static final String fdir = basepath + "/files/";
	private static final String sdir = basepath + "/signatures/";
	private static final String kdir = basepath + "/keys/";
	private static ServerOperations instance;

	private ServerOperations() {
		String[] dirs = { fdir, sdir, kdir};
		for (String toValidate : dirs) {
			File f = new File(toValidate);
			if (!f.exists()) {
				WarnHandler.log(f.getName() + ": Doesn't exist! Creating it now...");
				if (f.mkdirs()) {
					WarnHandler.log(f.getName() + ": Local directory was created successfully");
				} else {
					WarnHandler.exit(f.getName() + ": Couldn't create local file repository at this time! Exiting...");
				}
			}
		}
		
		//Esta parte provavelmente poderia estar melhor implementada, do that later maybe.
		//Se um ficheiro existir sem chave secreta ou uma chave secreta existir sem respetivo ficheiro
		//lançar warning e apagar o ficheiro. Talvez perguntar à prof o q fazer nesse caso
		File files = new File(fdir);
		File keys = new File(kdir);
		for(File f : files.listFiles()) {
			String name = f.getName().substring(0, f.getName().lastIndexOf("."));
			if(!Files.exists(Path.of(kdir+name+".chave_secreta"))) {
				WarnHandler.error(name+": file doesn't have respective secret key file. Deleting...");
				f.delete();
			}
		}
		for(File f : keys.listFiles()) {
			String name = f.getName().substring(0, f.getName().lastIndexOf("."));
			boolean ef = !Files.exists(Path.of(fdir+name+".cifrado")); 
			if(ef) {
				WarnHandler.error(name+": file doesn't have respective ciphered key file. Deleting...");
				f.delete();
			}
		}
		for(File f : files.listFiles()) {
			String name = f.getName().substring(0, f.getName().lastIndexOf("."));
			boolean ef = !Files.exists(Path.of(fdir+name+".assinado")); 
			boolean es = !Files.exists(Path.of(sdir+name+".assinatura"));
			if(!ef && es) {
				WarnHandler.error(name+": file doesn't have respective signature file. Deleting...");
				f.delete();
			}
		}
		WarnHandler.log("Validation complete.");
	}
	
	public static ServerOperations getInstance() {
		if (instance == null) {
			instance = new ServerOperations();
		}
		return instance;
	}
	
	// && (and) em vez de || (ou) porque para existir o ficheiro tem de ter uma chave e um ficheiro cifrado valido
	//caso não tenha ambos consideramos que não existe logo fazemos overwrite
	public boolean existsCipher(String filename) {
		boolean ec = Files.exists(Path.of(fdir+filename+".cifrado"),LinkOption.NOFOLLOW_LINKS);
		boolean ek = Files.exists(Path.of(kdir+filename+".chave_secreta"),LinkOption.NOFOLLOW_LINKS);
		return ec && ek;
	}
	
	public boolean existsSignature(String filename) {
		boolean ef = Files.exists(Path.of(fdir+filename+".assinado"),LinkOption.NOFOLLOW_LINKS);
		boolean es = Files.exists(Path.of(sdir+filename+".assinatura"),LinkOption.NOFOLLOW_LINKS);
		return ef && es;
	}

	public boolean existsEnvelope(String filename) {
		boolean ee = Files.exists(Path.of(fdir+filename+".envelope"),LinkOption.NOFOLLOW_LINKS);
		boolean ek = Files.exists(Path.of(kdir+filename+".chave_secreta"),LinkOption.NOFOLLOW_LINKS);
		boolean ea = Files.exists(Path.of(sdir+filename+".assinado"),LinkOption.NOFOLLOW_LINKS);
		return ee && ek && ea;
	}
	
	public void receiveCipher(String filename, ObjectInputStream ois) throws Exception {
		try(FileOutputStream fos = new FileOutputStream(fdir+filename+".cifrado")){
			CommsHandler.ReceiveAll(ois, fos);
		}
	}
	
	public void sendCipher(String filename, ObjectOutputStream oos) throws Exception {
		try(FileInputStream fis = new FileInputStream(fdir+filename+".cifrado")){
			CommsHandler.sendAll(fis, oos);
		}
	}

	public void receiveKey(String filename, ObjectInputStream ois) throws Exception {
		try(FileOutputStream fos = new FileOutputStream(kdir+filename+".chave_secreta")){
			byte[] keyc = CommsHandler.receiveByte(ois);
			fos.write(keyc);
			fos.flush();
		}
	}
	
	public void sendKey(String filename, ObjectOutputStream oos) throws Exception {
		try(FileInputStream fis = new FileInputStream(kdir+filename+".chave_secreta")){
			byte[] keyc = fis.readAllBytes();
			CommsHandler.sendFullByteArray(keyc, oos);
		}
	}

	public void receiveSignature(String filename, ObjectInputStream ois) throws Exception {

		try(FileOutputStream signedfile = new FileOutputStream(fdir + filename + ".assinado")){
			CommsHandler.ReceiveAll(ois, signedfile);
		}
		
		try(FileOutputStream signature = new FileOutputStream(sdir + filename + ".assinatura")){
			signature.write(CommsHandler.receiveByte(ois));
			signature.flush();
		}
		
	}
	
	public void sendSignature(String filename, ObjectOutputStream oos) throws Exception {
		try(FileInputStream fis = new FileInputStream(sdir+filename+".assinado")){
			//StreamHandler.transferStream(fis, oos);
		}
	}

	//TODO to be implemented
	public void receiveEnvelope(String filename, ObjectInputStream ois) throws Exception {
		try(FileOutputStream fos = new FileOutputStream(fdir + filename + ".envelope");){
			fos.write(CommsHandler.receiveByte(ois)); //receive key
			CommsHandler.ReceiveAll(ois, fos); //receive file contents
			fos.write(CommsHandler.receiveByte(ois)); //receive signature
		}
		
	}

	//TODO to be implemented (genérico que verifica se um ficheiro existe no formato .seguro ou no formato .cifrado e manda)
	public void sendFile(String filename, ObjectOutputStream oos) {

	}

}
