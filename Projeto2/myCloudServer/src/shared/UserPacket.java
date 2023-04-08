package shared;

import java.io.Serializable;

public class UserPacket implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private int uid;
	private String uname;
	private String upwd;
	
	public UserPacket(String id, String name, String psswd) throws NumberFormatException{
		
		uid = Integer.parseInt(id);
		uname = name;
		upwd = psswd;
	}
	
	public int getId() {
		return uid;
	}
	
	public String getName() {
		return uname;
	}
	
	public String getPasswd() {
		return upwd;
	}
}
