package shared;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class CommsHandler {
	
	public static void sendFullByteArray(byte[] b, ObjectOutputStream out) throws Exception {
		out.writeObject((Integer) b.length);
		out.write(b, 0 , b.length);
		out.flush();
	}
	
	public static void sendNBytes(byte[] b, int size, ObjectOutputStream out) throws Exception {
		out.writeObject((Integer) size);
		out.write(b, 0, size);
		out.flush();
	}

	public static byte[] receiveByte(ObjectInputStream in) throws Exception {
		int size = (Integer) in.readObject();
		byte[] r = new byte[size];
		if(size > 0) {
			in.readNBytes(r, 0, size);
		}
		return r;
	}
	
	public static void sendAll(InputStream in, ObjectOutputStream out) throws Exception{
		byte[] buffer = new byte[512];
		long read;
		do {
			read = in.read(buffer, 0, buffer.length); // ler bytes do ficheiro para o buffer
			sendNBytes(buffer, (int) read,  out);
		}while(read == buffer.length);
		sendFullByteArray(new byte[0], out);
	}
	
	public static void ReceiveAll(ObjectInputStream in, OutputStream out) throws Exception {
		byte[] recv = null;
		do {
			recv = receiveByte(in);
			out.write(recv, 0, recv.length);
		} while(recv.length != 0);
	}
}
