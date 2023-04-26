package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashMap;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import filetype.Certificado;
import filetype.ClientFileFactory;
import keystore.ClientUser;
import shared.Logger;
import shared.User;

public class myCloud extends Logger {

	public static void main(String[] args) {

		if(args.length == 0) {
			exit("Not enough arguments were given.");
		}

		String address = null;
		int port = 0;
		String action = null;
		String params = null;
		String target;
		User u = null;
		try {
			HashMap<String,String> argus = Arguments.parse(args);
			System.out.println(argus);
			if(argus.containsKey("-a")) {
				String[] remote = argus.get("-a").split(":");
				address = remote[0];
				try {
					port = Integer.parseInt(remote[1]);
				} catch (NumberFormatException e) {
					throw new Exception("Invalid argument: No valid remote port was given.");
				}
				if(1 > port || port > 65535 ) {
					throw new Exception("Invalid argument: Invalid Port: must be between 1 and 65535.");
				}
			} else {
				throw new Exception("Missing parameter: -a . No address was given for remote host.");
			}
			if(!argus.containsKey("action")) {
				throw new Exception("Missing paramter: no action was specified. Valid paramters for action are:\n"
						+ "-au <user> <password> <cert file> : Creates a new user;\n"
						+ "-c <filename>* : Encrypts files and sends them to the remote host.\n"
						+ "-s <filename>* : Signs files and sends them to the remote host.\n"
						+ "-e <filename>* : Encrypts and Signs files, sending both to the remote host.\n");
			} else {
				action = argus.get("action");
				params = argus.get("aparams");
				target = argus.get("-d");
				if(!action.contentEquals("-au")) {
					if(!argus.containsKey("-u") || !argus.containsKey("-p")) {
						throw new Exception("No user was given: missing username or password!");
					}
					u = new User(argus.get("-u"), argus.get("-p"));
					log("Attempting to upload files: " +  params);
				} else {
					String[] user = params.split(" ");
					u = new User(user[0], user[1]);
					log("Attempting to register new user: " + user[0]);
				}
			}
		} catch (Exception e1) {
			exit(e1.getMessage());
		}
		
		System.setProperty("javax.net.ssl.trustStore", "truststore.client");
		System.setProperty("javax.net.ssl.trustStorePassword", "123456");
	  	
		SocketFactory sf = SSLSocketFactory.getDefault( );
		Socket soc = null;
		try {
			soc = sf.createSocket(address, port);
		} catch (Exception e) {
			exit("Connection by remote host was rejected.");
		}

		try (ObjectOutputStream outStream = new ObjectOutputStream(soc.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(soc.getInputStream());) {
			log("Connected to remote host successfully.");

			boolean check;
			
			outStream.writeObject((String) action);
			outStream.flush();
			check = (boolean) inStream.readObject();
			if (!check) {
				exit("Operation Rejected by Server");
			}
			
			outStream.writeObject((User) u);
			check = (boolean) inStream.readObject();
			if("-au".contentEquals(action)) {
				log("Attempting to register user with username: " + u.getUsername());
				if(!check) {
					exit("User with username: " + u.getUsername() + " already exists!");
				}
				new Certificado(u.getUsername()).send(outStream);
				
			} else {
				
				if(!check) {
					exit("User with username: " + u.getUsername() + " doesn't exist!");
				}
				String[] files = params.split(" ");
				ClientUser u1 = (ClientUser) u;
				
				ClientFileFactory cfl = new ClientFileFactory(u1);
				
				if("-c-s-e".contains(action)) {
					
					for (String filename : files) {
						
						if(Files.exists(Path.of(filename),LinkOption.NOFOLLOW_LINKS)) {
							outStream.writeObject((String) filename);
							check = (boolean) inStream.readObject();

							if (check) {
								
								cfl.getFile(filename, action).send(outStream);
							
							} else {
								log("File already exists on the server. Skipping...");
							}
						} else {
							log("File :" + filename + "could not be found locally");
						}
					}
					// Informa o servidor que vamos parar de enviar ficheiros
					outStream.writeObject(false);
				} else {
					for (String filename : files) {
						
						if(Files.exists(Path.of(u1.getUserDir() + filename),LinkOption.NOFOLLOW_LINKS)) {
							log("File already exists locally. Skipping...");
						} else {
							outStream.writeObject((String) filename);
							int type = (int) inStream.readObject();
							String option = null;
							switch(type) {
								case 0:{
									log("File doesn't exist on remote cloud. Skipping...");
									continue;
								}
								case 1:{
									log("Found encrypted file on remote cloud. Downloading...");
									option = "-c";
									break;
								}
								case 2:{
									log("Found signature file on remote cloud. Verifying...");
									option = "-s";
									break;
								}
								case 3:{
									log("Found envelope file on remote cloud. Downloading and Verifying...");
									option = "-e";
									break;
								}
							}
							cfl.getFile(filename, option).receive(inStream);
						}
					}
					outStream.writeObject((boolean) false);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			exit("IOException: connection with remote host was either forcebly closed or timed out.");
		} catch (ClassNotFoundException e) {
			exit("Unexpected Class received.");
		} catch (Exception e) {
			e.printStackTrace();
			exit(e.getMessage());
		}
	}
}