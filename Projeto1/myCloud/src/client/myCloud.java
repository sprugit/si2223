package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Arrays;

import shared.WarnHandler;

public class myCloud extends WarnHandler {

	public static void main(String[] args) {

		if(args.length == 0) {
			exit("Not enough arguments were given.");
		}
		
		ClientOperations clop = ClientOperations.getInstance();

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

			//Object o;

			boolean check;

			outStream.writeObject((String) args[op]);
			outStream.flush();
			check = (boolean) inStream.readObject();
			if (!check) {
				exit("Operation Rejected by Server");
			}

			switch (args[op]) {
			case "-c":

				for (String filename : files) {

					outStream.writeObject((String) filename);
					check = (boolean) inStream.readObject();

					if (check) {

						byte[] key = clop.sendFile(filename, inStream, outStream);
						clop.sendKey(key, outStream);
						log("File :" + filename + " uploaded successfully.");

					} else {
						log("File already exists on the server. Skipping...");
					}
				}
				// Informa o servidor que vamos parar de enviar ficheiros
				outStream.writeObject((boolean) false);
				break;
			case "-s":
				for (String filename : files) {
					
					// Se não existe localmente dar log e passar para a proxima iteração
					if (!Files.exists(Path.of(filename),LinkOption.NOFOLLOW_LINKS)) {
						log(filename + " not found");
						continue;
					}
					
					outStream.writeObject((String) filename);
					check = (boolean) inStream.readObject();
					
					if (check) {
						
						clop.sendSignature(filename, outStream);
						log(filename + " and signature updated sucessfully.");

					} else {
						log("File already exists on the server. Skipping...");
					}
				}
				// Informa o servidor que vamos parar de enviar ficheiros
				outStream.writeObject((boolean) false);
				break;
			case "-e":

				break;
			case "-g":

				for (String filename : files) {

					outStream.writeObject((String) filename);
					check = (boolean) inStream.readObject();

					if (check) {

						byte[] key = clop.receiveKey(inStream);
						clop.receiveFile(filename, inStream, key);
						log("File :" + filename + " downloaded successfully.");

					} else {
						log("File doesn't exist on the server. Skipping...");
					}
				}
				// Informa o servidor que vamos parar de enviar ficheiros
				outStream.writeObject((boolean) false);

				break;
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