package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import abstracts.ConcreteServerFile;
import filetype.Assinado;
import filetype.Cifrado;
import filetype.Envelope;
import filetype.ServerFileFactory;
import shared.Logger;

public class myCloudServer {

	private static int port;

	public static void main(String[] args) {

		try {

			if (args.length < 1) {
				throw new Exception("Missing required arg: port value");
			}
			port = Integer.parseInt(args[0]); // porta como argumento
			if (1 > port || port > 65535) {
				throw new Exception("Port value must be within the range [1,65535]");
			}
			
			ServerFileFactory.getInstance();
			myCloudServer server = new myCloudServer();
			server.serve();

		} catch (Exception e) {
			if (e instanceof NumberFormatException) {
				Logger.exit("Given port value isn't valid");
			} else {
				Logger.exit(e.getMessage());
			}
		}
	}

	private void serve() throws IOException {

		try(ServerSocket socket = new ServerSocket(port);){
			Logger.log("Server listening @ port:" + port);
			while (true) {
				Socket inputSocket = socket.accept();
				ServThread newThread = new ServThread(inputSocket);
				newThread.start();
			}
		}
	}

	private class ServThread extends Thread {

		private Socket threadSoc = null;

		ServThread(Socket inputSocket) throws IOException {
			threadSoc = inputSocket;
		}

		public void run() {
			try (ObjectInputStream inStream = new ObjectInputStream(threadSoc.getInputStream());
					ObjectOutputStream outStream = new ObjectOutputStream(threadSoc.getOutputStream());) {
				
				Object o = null;
				String op = null;
				Logger.log("Connection received from: " + threadSoc.getRemoteSocketAddress().toString());

				o = inStream.readObject();
				boolean reply = false;
				if (o instanceof String) {
					op = (String) o;
					if("-c-s-e-g".contains(op)) {
						reply = true;
					}	
				}
				outStream.writeObject((boolean) reply);
				outStream.flush();
				
				ServerFileFactory sfl = ServerFileFactory.getInstance();
				
				boolean isReceiving = true;
				boolean fileExists = false;
				if("-c-s-e".contains(op)) {
					while(isReceiving) {
						
						o = inStream.readObject();
						if(o instanceof String) {
							
							String filename = (String) o;
							fileExists = sfl.getExistingFile(filename) == null; //guarda se o ficheiro não existe
							outStream.writeObject((Boolean) fileExists);
							
							if(fileExists) {
								sfl.getServerFile(filename, op).receive(inStream);
								
							} else {
								Logger.log(filename+": already exists! Skipping...");
							}
							
						} else if(o instanceof Boolean){
							isReceiving = false;
						}	
					}
				} else {
					Logger.log("User is attempting to download files");
					
					while(isReceiving) {
						
						o = inStream.readObject();
						if(o instanceof String) {
							
							String filename = (String) o;
							ConcreteServerFile toDownload = sfl.getExistingFile(filename);
							Integer type = 0;
							if(toDownload instanceof Cifrado) {
								type = 1;
							} else if (toDownload instanceof Assinado) {
								type = 2;
							} else if (toDownload instanceof Envelope) {
								type = 3;
							}
							outStream.writeObject(type);
							if(type == 0) {
								Logger.log("User requested file: "+ filename + " doesn't exist.");
							} else {
								toDownload.send(outStream);
							}
						} else if(o instanceof Boolean){
							isReceiving = false;
						}					
					}
				}
				Logger.log("File transfers complete. User disconnected.");
			} catch (IOException e) {
				e.printStackTrace();
				Logger.error("IOException: connection with remote host was either forcebly closed or timed out.");
			} catch (ClassNotFoundException e) {
				Logger.error("Unexpected Object received.");
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error(e.getMessage());
			}
		}
	}
}