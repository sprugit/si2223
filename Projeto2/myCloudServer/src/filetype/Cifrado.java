package filetype;

import abstracts.ComplexServerFile;

public class Cifrado extends ComplexServerFile{
	
	private static final int[] filetypes = {0,2};
	
	protected Cifrado(String filename, String uploader, String receiver) throws Exception {
		super(filename, uploader, receiver);
	}
	
	@Override
	public int[] getTypes() {
		return filetypes;
	}
	
}