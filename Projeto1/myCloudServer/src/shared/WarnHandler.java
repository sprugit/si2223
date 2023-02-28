package shared;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class WarnHandler {

    private synchronized static String getDate() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
    
	public synchronized static void error(String msg) {
		System.err.println("[ERROR] - "+ getDate() + " - " + msg);
	}
	
	public synchronized static void log(String msg) {
		System.out.println("[LOG] - "+ getDate() + " - " + msg);
	}
	
	public synchronized static void exit(String msg) {
		error(msg);
		System.exit(-1);
	}
	
	public synchronized static void elog(String msg) {
		log(msg);
		System.exit(0);
	}
}