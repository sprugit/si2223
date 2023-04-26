package shared;

import java.io.Serializable;

public class User implements Serializable {

	private static final long serialVersionUID = 1L;
	protected final String username;
	protected final String password;
	
	public User(String u, String p) {
		username = u;
		password = p;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
	
}