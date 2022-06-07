package client;

public class ClientPlayer {
	private String username;
	public boolean active = false;
	protected int userID;
	
	public ClientPlayer(String username, int id) {
		this.username = username;
		this.userID = id;
		active = true;
	}
	public String getName() {
		return username;
	}
	
}
