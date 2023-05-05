package abstracts;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractServerFile {

	protected final String path;
	protected final String filename;
	protected final String ext;
	
	protected AbstractServerFile(String path, String filename, String ext) {
		this.path = path;
		this.filename = filename;
		this.ext = ext;
	}
	
	protected String getPath() {
		return this.path + this.filename + ext;
	}
	
	public boolean exists(){
		return Files.exists(Path.of(getPath()));
	}
	
	protected synchronized void receiveBytes(ObjectInputStream ois) throws Exception  {
		int fsize = (Integer) ois.readObject();
		try(FileOutputStream fos = new FileOutputStream(getPath())) {
			int read = 0;
			long total = 0;
			byte[] buf = new byte[512];
			do {
				if((read = ois.read(buf, 0, buf.length)) > -1) {
					total += read;
					fos.write(buf, 0 , read);
					fos.flush();
				}
			}while(total < fsize);
		}
	}
	
	protected synchronized void sendBytes(ObjectOutputStream oos) throws Exception {
		try(FileInputStream fis = new FileInputStream(getPath())) {
			oos.writeObject((int) Files.size(Path.of(getPath())));
			long read = 0;
			byte[] buf = new byte[512];
			while((read = fis.read(buf, 0, buf.length)) > 0){
				oos.write(buf, 0, (int) read);
				oos.flush();
			}
		}
	}
	
	public synchronized void receive(ObjectInputStream ois) throws Exception {
		receiveBytes(ois);
	}
	
	public synchronized void send(ObjectOutputStream oos) throws Exception {
		sendBytes(oos);
	};
	
}