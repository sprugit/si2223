package shared;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamHandler {

	public static void transferStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		long read = in.read(buffer, 0, buffer.length);
		while (read != -1) {
			out.write(buffer, 0, (int) read);
			read = in.read(buffer, 0, buffer.length); //read returns -1 if there's nothing left to be read from socket
		}
	}
	
}
