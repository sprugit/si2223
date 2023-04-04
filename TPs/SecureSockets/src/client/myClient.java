package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

public class myClient {

	public static void main(String[] args) {

		String[] argv = { "0.0.0.0", "23456" };
		Socket soc = null;
		try {
			SocketFactory sf = SSLSocketFactory.getDefault();
			soc = sf.createSocket(argv[0], Integer.valueOf(argv[1]));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try (ObjectInputStream in = new ObjectInputStream(soc.getInputStream());
				ObjectOutputStream out = new ObjectOutputStream(soc.getOutputStream())) {
			String user = args[0];
			String pw = args[1];
			out.writeObject((String) user);
			out.writeObject((String) pw);
			out.flush();

			Boolean auth = (Boolean) in.readObject();

			if (!auth) {
				System.err.println("Autenticação falhou");
				System.exit(1);
			}

			String fpath = args[2];
			File f = new File(fpath);
			if (!f.exists()) {
				System.err.println("Uh oh ficheiro não existe");
				System.exit(1);
			}

			long file_size = f.length();
			String file_name = f.getName();

			try (FileInputStream fis = new FileInputStream(f);) {

				long total_bytes_sent = 0;

				out.writeObject((String) file_name);
				out.writeObject((long) file_size);

				byte[] buffer = new byte[1024];

				while (total_bytes_sent <= file_size) { //enquanto o ficheiro não tiver sido enviado na totalidade
					long bytes_read_file = fis.read(buffer, 0, buffer.length); //ler bytes do ficheiro para o buffer
					out.write(buffer, 0, (int) bytes_read_file); //enviar o buffer
					out.flush(); //forçar o buffer a ser enviado pela rede
					total_bytes_sent += bytes_read_file;
				}

				System.out.println("Ficheiro enviado.");

			} catch (Exception e) {
				e.getStackTrace();
			}
		} catch (IOException e) {
			//e.getStackTrace();
			System.err.println("Ligação Terminada pelo anfitrião remoto.");
		} catch (ClassNotFoundException e1) {
			//e1.printStackTrace();
			System.err.println("Objeto recebido desconhecido.");
		}

	}

}
