package aula1v2;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class myClient {

	public static void main(String[] args) {

		String[] argv = { "0.0.0.0", "23456" };
		Socket soc = null;
		try {
			soc = new Socket(argv[0], Integer.valueOf(argv[1]));
		} catch (Exception e) {
			e.getStackTrace();
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

			long fsize = f.length();
			String name = f.getName();

			try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));) {

				long totalSent = 0;

				out.writeObject((String) name);
				out.writeObject((long) fsize);

				byte[] buffer = new byte[1024];

				while (totalSent <= fsize) {
					long read = bis.read(buffer, 0, buffer.length);
					out.write(buffer, 0, (int) read);
					out.flush();
					totalSent += read;
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
