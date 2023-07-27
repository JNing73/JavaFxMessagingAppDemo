package Client;

public class User {
	
	private String username;
	private String ip;
	private String loggedIn;
	private int portNum;
	
	
	/**
	 * This represents a user that has both connected AND logged in
	 * @param ip
	 * @param username
	 */
	public User (String ip, String username, int portNum) {
		
		this.ip = ip;
		this.username = username;
		this.loggedIn = "Logged In";
		this.portNum = portNum;
	}
	
	/**
	 * This represents a user that is connected to the server but has not logged in yet
	 * @param ip
	 */
	public User(String ip, int portNum) {
		
		this.username = "";
		this.ip = ip;
		this.loggedIn = "Not Logged In";
		this.portNum = portNum;
	}
	


	public String getUsername() {
		return username;
	}


	public String getIp() {
		return ip;
	}

	public String getLoggedIn() {
		return loggedIn;
	}

	public int getPortNum() {
		return portNum;
	}
	
	

}
