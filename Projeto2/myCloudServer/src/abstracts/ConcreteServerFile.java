package abstracts;

import server.PathDefs;

public class ConcreteServerFile extends AbstractServerFile {
	
	public final static String[] mtypes = {".chave_segura",".assinatura",".cifrado",".assinado",".seguro"};
	
	public ConcreteServerFile(String filename, String uploader, String receiver, String ext) {
		super(PathDefs.fdir+receiver+"/",filename,ext+"."+uploader);
	}
	
}