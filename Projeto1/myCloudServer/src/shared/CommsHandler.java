package shared;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class CommsHandler {
	
	/**
	 * Envia um array de bytes inteiro para o recetor.
	 * [write(b, 0, b.length)]
	 * 
	 * @param b - byte array por ser enviado
	 * @param out - ObjectOuputStream do servidor
	 * @throws Exception
	 */
	public static void sendFullByteArray(byte[] b, ObjectOutputStream out) throws Exception {
		out.writeObject((Integer) b.length);
		out.write(b, 0 , b.length);
		out.flush();
	}
	
	/**
	 * Envia um array de bytes para o recetor. Este byte array não é enviado por inteiro.
	 * Exemplo: Um byte array com o tamanho máximo de 1024 bytes que apenas tem 635 bytes por enviar.
	 * 
	 * @param b - byte array por ser enviado
	 * @param size - o tamanho do payload contido dentro de b
	 * @param out - ObjectOutputStream
	 * @throws Exception
	 */
	public static void sendNBytes(byte[] b, int size, ObjectOutputStream out) throws Exception {
		out.writeObject((Integer) size);
		out.write(b, 0, size);
		out.flush();
	}

	/**
	 * Recebe um array de bytes vindos de um emissor.
	 * 
	 * @param in - ObjectInputStream 
	 * @return r - byte array recebido.
	 * @throws Exception
	 */
	public static byte[] receiveByte(ObjectInputStream in) throws Exception {
		int size = (Integer) in.readObject();
		byte[] r = new byte[size];
		if(size > 0) {
			in.readNBytes(r, 0, size);
		}
		return r;
	}
	
	/**
	 * Lê todos os bytes disponíveis em InputStream e escreve-los para ObjcetOutputStream. 
	 * 
	 * 
	 * @param in - InputStream (Tipicamente um FileInputStream ou CipherInputStream)
	 * @param out - ObjectOutputStream
	 * @throws Exception
	 */
	public static void sendAll(InputStream in, ObjectOutputStream out) throws Exception{
		byte[] buffer = new byte[512];
		long read;
		do {
			read = in.read(buffer, 0, buffer.length); // ler bytes do ficheiro para o buffer
			sendNBytes(buffer, (int) read,  out);
		}while(read == buffer.length);
		sendFullByteArray(new byte[0], out);
	}
	
	/**
	 * Lê todos os bytes recebidos do ObjectInputStream e escreve-los para OutputStream.
	 * 
	 * @param in - ObjectInputStream
	 * @param out - OuputStream (Tipicamente um FileOutputStream ou CipherOutputStream)
	 * @throws Exception
	 */
	public static void ReceiveAll(ObjectInputStream in, OutputStream out) throws Exception {
		byte[] recv = null;
		do {
			recv = receiveByte(in);
			out.write(recv, 0, recv.length);
		} while(recv.length != 0);
	}
}