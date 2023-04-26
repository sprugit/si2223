package abstracts;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import server.PathDefs;
import shared.Logger;

public class ConcreteServerFile extends AbstractUserFile {

	protected ConcreteServerFile(String filename) throws IOException {
		super(filename);
	}
	
	protected void receiveBytes(ObjectInputStream ois, FileOutputStream fos) throws IOException, ClassNotFoundException  {
		Integer fsize = (Integer) ois.readObject();
		Logger.log(filename+": Expected file size is "+String.valueOf(fsize)+".");
		long read = 0;
		long total = 0;
		byte[] buf = new byte[512];
		do {
			int toBeRead = (int) (fsize - total > buf.length ? buf.length : fsize - total) ;
			read = ois.read(buf, 0, toBeRead);
			total += read;
			fos.write(buf, 0 , (int) read);
		}while(total < fsize);
	}
	
	protected void sendBytes(FileInputStream fis, ObjectOutputStream oos, long filesize) throws IOException {
		oos.writeObject((int) filesize);
		long read = 0;
		byte[] buf = new byte[512];
		while((read = fis.read(buf, 0, buf.length)) > 0){
			oos.write(buf, 0, (int) read);
			oos.flush();
		}
	}

	@Override
	public boolean exists() throws IOException {
		return false;
	}

	@Override
	public void receive(ObjectInputStream ois) throws Exception {
		throw new Exception("Not Implemented!");
	}

	@Override
	public void send(ObjectOutputStream oos) throws Exception {
		throw new Exception("Not Implemented!");
	}
}