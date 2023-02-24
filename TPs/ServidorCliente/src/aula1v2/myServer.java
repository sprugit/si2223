/***************************************************************************
*   Seguranca Informatica
*
*
***************************************************************************/
package aula1v2;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class myServer{

	public static void main(String[] args) {
		System.out.println("servidor: main");
		myServer server = new myServer();
		server.startServer();
	}

	public void startServer (){
		ServerSocket sSoc = null;
        
		try {
			sSoc = new ServerSocket(23456);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		while(true) {
			try {
				Socket inSoc = sSoc.accept();
				ServerThread newServerThread = new ServerThread(inSoc);
				newServerThread.start();
		    }
		    catch (IOException e) {
		        e.printStackTrace();
		    }
		}
		//sSoc.close();
	}


	//Threads utilizadas para comunicacao com os clientes
	class ServerThread extends Thread {

		private Socket socket = null;

		ServerThread(Socket inSoc) {
			socket = inSoc;
			System.out.println("thread do server para cada cliente");
		}
 
		public void run(){
			try(ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());){
				String user = null;
				String passwd = null;
			
				try {
					user = (String) inStream.readObject();
					
					passwd = (String) inStream.readObject();
					
					System.out.println("thread: depois de receber a password e o user");
				}catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}
 			
				//TODO: refazer
				//este codigo apenas exemplifica a comunicacao entre o cliente e o servidor
				//nao faz qualquer tipo de autenticacao
				
				if(!user.contentEquals("utilizador1") || !passwd.contentEquals("password2")) {
					outStream.writeObject( (Boolean) false);
					outStream.flush();
				} else {
					outStream.writeObject( (Boolean) true);
					outStream.flush();
				
					String name = (String) inStream.readObject();
					long fsize = (long) inStream.readObject();
					
					String path = "recebidos/";
					
					File f = new File(path+name);
					try(BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));){
						
						long total = 0;
						byte[] buffer = new byte[1024];
						int toRead;
						
						while(total < fsize) {
							toRead = (int) (fsize - total);
							if(toRead > 1024) {
								toRead = 1024;
							} 
							long received = inStream.read(buffer, 0, toRead);
							bos.write(buffer, 0 , (int) received);
							bos.flush();
							total += received;
						}
					}
					System.out.println("Ficheiro recebido com sucesso");
				}
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}