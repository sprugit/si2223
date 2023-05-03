package filetype;

import abstracts.ComplexServerFile;

public class Assinado extends ComplexServerFile {
	
	private final static int[] filetypes = {3,1};
	
	protected Assinado(String filename, String uploader, String receiver) {
		super(filename, uploader, receiver);
	}
	
	@Override
	public int[] getTypes() {
		return filetypes;
	}

}