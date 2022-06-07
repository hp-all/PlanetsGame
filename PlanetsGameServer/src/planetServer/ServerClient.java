package planetServer;

import java.net.InetAddress;

import org.jbox2d.common.Vec2;

import serialization.PObject;
import setWorld.Score;

public class ServerClient {
	public int userID;
	public String username;
	public InetAddress addy;
	public int port = 0;
	protected boolean status = false;
	protected static int userIDCounter = 0;

	public Vec2 mouseV = new Vec2();
	public static boolean pressOnce = true;
	public static final int COOL_DOWN = 3;
	private boolean[] pressedKey = new boolean[6];
	private boolean[] pressedOnce = new boolean[6];
	public int[] received = {0, 0, 0, 0, 0, 0};

	public Score score = new Score();

	public ServerClient(String name, InetAddress addy, int port) {
		userID = userIDCounter;
		userIDCounter ++;
		username = name;
		this.addy = addy;
		this.port = port;
		status = true;
	}

	public int setID(int id) {
		return userID = id;
	}

	public boolean isActive() {
		return status;
	}
	public void setInput(PObject input) {
		mouseV.x = input.findField("mx").getFloat();
		mouseV.y = input.findField("my").getFloat();
		if(input.findArray("keys") != null) {
			pressedKey = input.findArray("keys").boolData;
		}
		else {
			for(int i = 0; i<pressedKey.length; i++)
				pressedKey[0] = false;
		}
	}
	private static int line;
	
	public void update() {
		for(int i = 0; i<received.length; i++) {
			if(received[i] > 0) {
				if(received[i] > 1 || (received[i] == 1 && !pressedKey[i]))
				{
					pressedOnce[i] = false;
					received[i] --;
				}
			} else if(received[i] == 0) {
				if(pressedKey[i])
					received[i] = COOL_DOWN;
				pressedOnce[i] = pressedKey[i];
			}
		}
		if(pressedOnce[0])
			pressedOnce[0] = true;
	}
	private boolean isKey(int i) {
		if(pressOnce)
			return pressedOnce[i];
		return pressedKey[i];
	}
	public synchronized boolean up() {
		return isKey(0);
	}
	public synchronized boolean down() {
		return isKey(1);
	}
	public synchronized boolean left() {
		return isKey(2);
	}
	public synchronized boolean right() {
		return isKey(3);
	}
	public synchronized boolean space() {
		return isKey(4);
	}
	public synchronized boolean back() {
		return isKey(5);
	}
}
