package shared;

import java.io.Serializable;

public class Request implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private final String user;
	private final String password;
	private final String action;
	private final String target;
	
	protected Request(String u, String p, String a, String t) throws Exception {
		user = u;
		password = p;
		action = a;
		target = t;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public String getProcedure() {
		return action;
	}

	public String getTarget() {
		return target;
	}

}