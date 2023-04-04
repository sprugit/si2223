/***************************************************************************
*   Seguranca Informatica
*
*
***************************************************************************/
package server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;


public class myServer{

	public static void main(String[] args) {
		System.out.println("servidor: main");
		myServer server = new myServer();
		server.startServer();
	}

	public void startServer (){
		ServerSocket sSoc = null;
        
		try {
			ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
			sSoc = ssf.createServerSocket(23456);
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
				
					String filename = (String) inStream.readObject();
					long file_size = (long) inStream.readObject();
					
					String path = "ficheirosRecebidos/";
					
					File f = new File(path+filename);
					try(FileOutputStream fos = new FileOutputStream(f);){
						
						long total_bytes_received = 0; //total de bytes recebidos
						byte[] buffer = new byte[1024]; //buffer usado pra ler o ficheiro
						int bytes_to_read; //bytes que faltam ler
						
						while(total_bytes_received < file_size) { //enquanto o ficheiro não tiver sido recebido
							bytes_to_read = (int) (file_size - total_bytes_received); //Calcular quantos bytes do ficheiro faltam
							if(bytes_to_read > 1024) { //se faltam mais que 1024, lê 1024 bytes
								bytes_to_read = 1024;
							} 
							long received_data = inStream.read(buffer, 0, bytes_to_read); //receber o tamanho dos dados recebidos
							fos.write(buffer, 0 , (int) received_data); //escrever os dados guardados em buffer pro ficheiro
							fos.flush(); //certeficar que os dados do socket são enviados pro ficheiro
							total_bytes_received += received_data; //atualizar os dados recebidos
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