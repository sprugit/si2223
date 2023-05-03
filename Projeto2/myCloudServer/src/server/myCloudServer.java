package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

import abstracts.ComplexServerFile;
import auth.PasswordFile;
import auth.ServerUser;
import filetype.Assinado;
import filetype.Cifrado;
import filetype.Envelope;
import filetype.UserFileFactory;
import shared.FileDescriptor;
import shared.Logger;
import shared.User;

public class myCloudServer {

	private static int port;

	public static void main(String[] args) {

		try {

			Logger.log("Initiating myCloud server...");

			if (args.length < 1) {
				throw new Exception("Missing required arg: port value");
			}
			port = Integer.parseInt(args[0]); // porta como argumento
			if (1 > port || port > 65535) {
				throw new Exception("Port value must be within the range [1,65535]");
			}

			UserFileFactory.getInstance();
			PasswordFile.getFile();
			PathDefs.initialize();

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

		System.setProperty("javax.net.ssl.keyStore", "keystore.server");
		System.setProperty("javax.net.ssl.keyStorePassword", "123456");
		ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();

		try (ServerSocket socket = ssf.createServerSocket(port);) {
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

				try {

					o = inStream.readObject();

					if (o instanceof String) {
						op = (String) o;
						if (!"-c-s-e-g".contains(op) && !op.contentEquals("-au")) {
							throw new Exception(
									"Client attempted to do an invalid server proceedure. Terminating connection.");
						}
					}
					outStream.writeObject(true);
					outStream.flush();
					
					User u = (User) inStream.readObject();
					ServerUser su = new ServerUser(u);

					if (op.contentEquals("-au")) {

						Logger.log("Client is attempting to register a new user: " + su.getUsername());
						if (su.exists()) {
							throw new Exception("User " + su.getUsername() + " already exists!");
						}
						outStream.writeObject(true);
						su.register(inStream);
						Logger.log("User " + su.getUsername() + " was registered successfully!");
						outStream.writeObject(true);
						
					} else {

						UserFileFactory sfl = UserFileFactory.getInstance();

						boolean isReceiving = true;
						boolean fileExists = false;
						if ("-c-s-e".contains(op)) {
							while (isReceiving) {

								o = inStream.readObject();
								if (o instanceof FileDescriptor) {

									FileDescriptor fd = (FileDescriptor) o;
									fileExists = sfl.getExistingFile(fd.getFilename(),fd.getTarget()) != null; // guarda se o ficheiro não

									outStream.writeObject((Boolean) fileExists);

									if (!fileExists) {
										sfl.getServerFile(fd.getFilename(), fd.getSender(), fd.getTarget(), op)
										.receive(inStream, outStream);

									} else {
										Logger.log(fd.getFilename() + ": already exists! Skipping...");
									}

								} else if (o instanceof Boolean) {
									isReceiving = false;
								}
							}
						} else {
							Logger.log("User is attempting to download files");

							while (isReceiving) {

								o = inStream.readObject();
								if (o instanceof FileDescriptor) {

									FileDescriptor fd = (FileDescriptor) o;
									ComplexServerFile toDownload = sfl.getExistingFile(fd.getFilename(),fd.getTarget());
									Integer type = 0;
									if (toDownload instanceof Cifrado) {
										type = 1;
									} else if (toDownload instanceof Assinado) {
										type = 2;
									} else if (toDownload instanceof Envelope) {
										type = 3;
									}
									outStream.writeObject(type);
									if (type == 0) {
										Logger.log("User requested file: " + fd.getFilename() + " doesn't exist.");
									} else {
										toDownload.send(inStream, outStream);
									}
								} else if (o instanceof Boolean) {
									isReceiving = false;
								}
							}
						}
						Logger.log("File transfers complete. User disconnected.");
					}

				} catch (Exception e) {
					Logger.error(e.getMessage());
					outStream.writeObject(false);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}