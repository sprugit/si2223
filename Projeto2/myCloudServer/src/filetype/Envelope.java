package filetype;

import abstracts.ComplexServerFile;

public class Envelope extends ComplexServerFile {
	
	private static final int[] filetypes = {0,4,1};
	
	protected Envelope(String filename, String uploader, String receiver) throws Exception {
		super(filename, uploader, receiver);
	}

	@Override
	public int[] getTypes() {
		return filetypes;
	}
	
}