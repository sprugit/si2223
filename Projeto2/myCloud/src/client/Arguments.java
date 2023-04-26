package client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Arguments {

	public static HashMap<String,String> parse(String[] args) throws Exception {
		
		HashMap<String,String> retval = new HashMap<String, String>();
		int aindex = 0, nindex = 0;
		for(int i = 0; i < args.length; i++) {
			String arg = args[i];
			if("-au-c-s-e-g".contains(arg) && !arg.contentEquals("-a")) {
				if(!retval.containsKey("action") && !retval.containsKey("aparams")) {
					retval.put("action", arg);
					aindex = i;
				} else {
					throw new Exception("Invalid Arguments: receive two actions commands: " +
						arg + " and " + retval.get("action"));
				}
			} else {
				if("-a-u-p-d".contains(arg)) {
					if(aindex != 0 && nindex == 0) {
						nindex = i;
					}
					retval.put(arg, args[i+1]);
				}
			}
		}
		nindex = nindex == 0 ? args.length-1 : nindex - 1;
		List<String> params = new ArrayList<String>();
		for(int i1 = aindex + 1; i1 <= nindex; i1++) {
			params.add(args[i1]);
		}
		retval.put("aparams",String.join(" ",params));
		if(!retval.containsKey("-d")) {
			retval.put("-d", retval.get("-u"));
		}
		return retval;
	}
	
	public static void main(String[] args) throws Exception {
		
		String nuser = "-a 127.0.0.1:21345 -au manel password manel.cert";
		String cfile = "-a 127.0.0.1:21345 -u user -p password -c file1 file2 file3";
		String efile = "-a 127.0.0.1:21345 -u user -p password -e file1 file2 file3 -d user2";
		String[] tests = new String[] {nuser,cfile,efile};
		
		for(String test : tests) {
			HashMap<String, String> hm = parse(test.split(" "));
			for(String key : hm.keySet()) {
				System.out.println((key + ":" + hm.get(key)));
			}
		}	
	}
}