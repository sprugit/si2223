package server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import shared.Logger;

/**
 * Class com a definição das paths de todos os ficheiros
 * e diretorias utilizadas. Assim fica tudo num sítio e fica
 * mais facil.
 */
public class PathDefs {

	//Paths base para guardar ficheiros
	public static final String base_dir = "myCloudServer/";
	public static final String fdir = base_dir + "files/";
	public static final String cdir = base_dir + "certificates/";
	
	//Paths relacionadas com o ficheiro de downloads
	public static final String upath = base_dir + "users";
	public static final String vpath = base_dir + "passwords.mac";
	
	//Array de paths para serem validadas
	public static final String[] toValidate = {fdir,cdir};
	
	public static synchronized void initialize() throws Exception {
		
		for(String path : toValidate) {
			Path p = Path.of(path);
			if(Files.notExists(p)) {
				Logger.log("Directory '"+path+"' didn't exist. Creating...");
				Files.createDirectories(p);
			}
			
		}
		
	}
	
}
