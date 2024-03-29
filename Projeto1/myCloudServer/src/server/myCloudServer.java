package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import shared.WarnHandler;

public class myCloudServer extends WarnHandler {

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
			
			ServerOperations.getInstance();

			myCloudServer server = new myCloudServer();
			server.serve();

		} catch (Exception e) {
			if (e instanceof NumberFormatException) {
				exit("Given port value isn't valid");
			} else {
				exit(e.getMessage());
			}
		}
	}

	private void serve() throws IOException {

		try(ServerSocket socket = new ServerSocket(port);){
			log("Server listening @ port:" + port);
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
				
				
				ServerOperations svop = ServerOperations.getInstance();
				Object o = null;
				String op = null;
				log("Connection received from: " + threadSoc.getRemoteSocketAddress().toString());

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

				boolean isReceiving = true;
				boolean fileExists = false;
				switch (op) {
				case "-c":
					log("User is attempting to upload files.");
					
					while(isReceiving) {
						
						o = inStream.readObject();
						if(o instanceof String) {
							
							String filename = (String) o;
							fileExists = !svop.existsFile(filename);
							outStream.writeObject((Boolean) fileExists);
							
							if(fileExists) {
								log("Receiving "+filename+" from user.");
								
								svop.receiveCipher(filename, inStream);
								log("Received file "+filename+" sucessfully from user.");
								svop.receiveKey(filename, inStream);
								log("Received key for file "+filename+" sucessfully from user.");
								
								
							} else {
								log(filename+": already exists! Skipping...");
							}
							
						} else if(o instanceof Boolean){
							isReceiving = false;
						}	
					}
					break;
				case "-s":
					log("User is attempting to sign files.");
					while(isReceiving) {
						o = inStream.readObject();
						if(o instanceof String) {
							String filename = (String) o;
							fileExists = !svop.existsFile(filename);
							outStream.writeObject((Boolean) fileExists);
							
							if(fileExists) {
								log("Receiving signature and file "+filename+" from user.");
								svop.receiveSignature(filename, inStream);
								log("Received signature and file of "+filename+" sucessfully from user.");
								
								
							} else {
								log(filename+": already exists! Skipping...");
							}
							
							
						}else if(o instanceof Boolean){
							isReceiving = false;
						}	
					}
					
					break;
				case "-e":
					log("User is attempting to upload both a file and the respective signature");
					
					while(isReceiving) {
						o = inStream.readObject();
						if(o instanceof String) {
							String filename = (String) o;
							fileExists = !svop.existsFile(filename);
							outStream.writeObject((Boolean) fileExists);
							
							if(fileExists) {
								
								log("Receiving file "+filename+" from user.");
								svop.receiveEnvelope(filename, inStream);
								log("Received "+ filename +" sucessfully from user.");
								
							} else {
								log(filename+": already exists! Skipping...");
							}
										
						}else if(o instanceof Boolean){
							isReceiving = false;
						}	
					}
					
					break;
				case "-g":
					log("User is attempting to download files");
					
					while(isReceiving) {
						
						o = inStream.readObject();
						if(o instanceof String) {
							
							String filename = (String) o;
							Integer type = 0;
							if(svop.existsCipher(filename)) {
								type = 1;
							} else if (svop.existsSignature(filename)) {
								type = 2;
							} else if (svop.existsEnvelope(filename)) {
								type = 3;
							}
							outStream.writeObject(type);
							switch(type) {
								case 0:{
									log("User requested file: "+ filename + " doesn't exist.");
									break;
								}
								case 1:{
									log("User requested ciphered file for file: "+ filename);
									svop.sendKey(filename, outStream);
									svop.sendCipher(filename, outStream);
									break;
								}
								case 2:{
									log("User requested signature for file: "+ filename);
									svop.sendSignature(filename, outStream);
									break;
								}
								case 3:{
									log("User requested envelope for file: "+ filename);
									svop.sendEnvelope(filename, outStream);
									break;
								}
							}
						} else if(o instanceof Boolean){
							isReceiving = false;
						}					
					}
					
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
				error("IOException: connection with remote host was either forcebly closed or timed out.");
			} catch (ClassNotFoundException e) {
				error("Unexpected Object received.");
			} catch (Exception e) {
				error(e.getMessage());
			}
		}
	}
}