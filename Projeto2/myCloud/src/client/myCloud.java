package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Arrays;

import filetype.ClientFileFactory;
import shared.Logger;

public class myCloud extends Logger {

	public static void main(String[] args) {

		if(args.length == 0) {
			exit("Not enough arguments were given.");
		}

		String valid = "-c-s-e-g";
		//String received = String.join(" ", args);
		String remote = null;
		int op = 0;

		for (int i = 0; i < args.length; i++) {
			if (args[i].contentEquals("-a")) {
				remote = args[i + 1];
			} else if (valid.contains(args[i])) {
				op = i;
			}
		}

		String[] files = Arrays.copyOfRange(args, op + 1, args.length);

		log("Attempting to upload files: " + String.join(" ", files));
		
		if (remote == null) {
			exit("No remote address was given");
		}
		if (op == 0) {
			exit("No valid operation was given");
		}

		String[] remote_parts = remote.split(":");
		String address = remote_parts[0];
		Integer port = 0;
		try {
			port = Integer.parseInt(remote_parts[1]);
		} catch (NumberFormatException e) {
			exit("Invalid argument: No valid remote port was given.");
		}
		if (1 > port || port > 65535)
			exit("Invalid argument: Invalid Port: must be between 1 and 65535.");

		Socket soc = null;
		try {
			soc = new Socket(address, port);
		} catch (Exception e) {
			exit("Connection by remote host was rejected.");
		}

		try (ObjectOutputStream outStream = new ObjectOutputStream(soc.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(soc.getInputStream());) {
			log("Connected to remote host successfully.");

			boolean check;
			
			ClientFileFactory cfl = new ClientFileFactory(new Keystore("keystore.chaves"));

			outStream.writeObject((String) args[op]);
			outStream.flush();
			check = (boolean) inStream.readObject();
			if (!check) {
				exit("Operation Rejected by Server");
			}
			if("-c-s-e".contains(args[op])) {
				
				for (String filename : files) {
					
					if(Files.exists(Path.of(filename),LinkOption.NOFOLLOW_LINKS)) {
						outStream.writeObject((String) filename);
						check = (boolean) inStream.readObject();

						if (check) {
							
							cfl.getFile(filename, args[op]).send(outStream);
						
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
					
					if(Files.exists(Path.of("local/" + filename),LinkOption.NOFOLLOW_LINKS)) {
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