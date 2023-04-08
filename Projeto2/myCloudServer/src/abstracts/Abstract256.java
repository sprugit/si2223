package abstracts;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class Abstract256 extends AbstractFile {
	
	protected Abstract256(String filename) throws IOException {
		super(filename);
	}

	protected void transferData(InputStream is, OutputStream os) throws IOException {
		byte[] toWrite = is.readNBytes(256);
		os.write(toWrite);
		os.flush();
	}		
}