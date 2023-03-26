package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
		/*
		//Esta parte provavelmente poderia estar melhor implementada, do that later maybe.
		//Se um ficheiro existir sem chave secreta ou uma chave secreta existir sem respetivo ficheiro
		//lançar warning e apagar o ficheiro. Talvez perguntar à prof o q fazer nesse caso
		File files = new File(fdir);
		File keys = new File(kdir);
		File sigs = new File(sdir);
		
		//se usarmos apenas a opcao -s e dermos restart ao servidor ele apaga o ficheiro
		for(File f : keys.listFiles()) {
			String name = f.getName().substring(0, f.getName().lastIndexOf("."));
			boolean ef = !Files.exists(Path.of(fdir+name+".cifrado")) && !Files.exists(Path.of(fdir+name+".seguro"));
			if(ef) {
				WarnHandler.error(name+": file doesn't have respective ciphered key file. Deleting...");
				f.delete();
			}
		}
		//ao usar apenas a opcao -s e dermos restart ao servidor o ficheiro é apagado
		for(File f : sigs.listFiles()) {
			String name = f.getName().substring(0, f.getName().lastIndexOf("."));
			boolean ef = !Files.exists(Path.of(fdir+name+".assinado")) || !Files.exists(Path.of(fdir+name+".seguro")); 
			boolean es = !Files.exists(Path.of(sdir+name+".assinatura"));
			if((!ef && es) || (ef && !es)) {
				WarnHandler.error(name+": file doesn't have respective signature file. Deleting...");
				f.delete();
			}
		}*/
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
		boolean ee = Files.exists(Path.of(fdir+filename+".seguro"),LinkOption.NOFOLLOW_LINKS);
		boolean ek = Files.exists(Path.of(kdir+filename+".chave_secreta"),LinkOption.NOFOLLOW_LINKS);
		boolean ea = Files.exists(Path.of(sdir+filename+".assinatura"),LinkOption.NOFOLLOW_LINKS);
		return ee && ek && ea;
	}
	
	public boolean existsFile(String filename) {
		return existsCipher(filename) || existsSignature(filename) || existsEnvelope(filename);
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
		try(FileInputStream fis = new FileInputStream(sdir+filename+".assinatura")){
			byte[] sig = fis.readNBytes(256); //chave tem 256 bytes
			CommsHandler.sendFullByteArray(sig, oos);
		}
		try(FileInputStream fis1 = new FileInputStream(fdir+filename+".assinado")){
			CommsHandler.sendAll(fis1, oos);
		}
	}
	
	public void receiveEnvelope(String filename, ObjectInputStream ois) throws Exception {
		
		receiveKey(filename, ois);
		
		try(FileOutputStream fos = new FileOutputStream(fdir + filename + ".seguro");){
			CommsHandler.ReceiveAll(ois, fos);
		}
		
		try(FileOutputStream fos1 = new FileOutputStream(sdir + filename + ".assinatura");){
			byte[] sig = CommsHandler.receiveByte(ois);
			fos1.write(sig);
		}
	}
	
	public void sendEnvelope(String filename, ObjectOutputStream oos) throws Exception {

		sendKey(filename, oos);
		
		try(FileInputStream fis = new FileInputStream(fdir + filename + ".seguro");){
			CommsHandler.sendAll(fis, oos);
		}
		
		try(FileInputStream fis1 = new FileInputStream(sdir + filename + ".assinatura");){
			byte[] sig = fis1.readNBytes(256);
			CommsHandler.sendFullByteArray(sig, oos);
		}	
	}
}