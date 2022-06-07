package client;

import java.io.IOException;
import java.net.*;
import java.util.*;

import main.PlayGame;
import screen.Screen;
import serialization.*;

public class Client {
	private enum cError{
		NONE, BAD_HOST, SOCKET_ERROR
	}
	private int serverPort;
	private String ipHost;
	private cError errorCode = cError.NONE;
	private int clientPort = 12300;
	private String username;
	private boolean caughtUp;

	private InetAddress serverAddy;
	private DatagramSocket socket;

	private Thread listenThread;
	private boolean listening = false;

	private static final int MAX_PACKET_SIZE = 2048;
	private byte[] receivedDataBuffer = new byte[MAX_PACKET_SIZE*10];

	private List<ClientPlayer> players = new ArrayList<ClientPlayer>();
	public static int userID = 0;
	
	/**
	 * @param username, the name of the player/user
	 * @param host 
	 * 		e.g. 127.0.0.1
	 * @param port
	 * 		e.g. 5000
	 * 			
	 */
	public Client(String username, String host, int port) {
		this.username = username;
		switch(host) {
		case " ": host = "localhost"; break;
		case "1": host = "localhost"; clientPort += 1000; break;
		case "2": host = "localhost"; clientPort += 2000; break;
		case "3": host = "localhost"; clientPort += 3000; break;
		case "4": host = "localhost"; clientPort += 4000; break;
		}
		System.out.println(host + ", " + port);
		this.serverPort = port;
		this.ipHost = host;
		caughtUp = false;
	}

	public boolean connect() {
		try {
			serverAddy = InetAddress.getByName(ipHost);
		} catch (UnknownHostException e) {
			System.err.println("Invalid host");
			errorCode = cError.BAD_HOST;
			PlayGame.restartConnect("Invalid Host");
			return false;
		}

		try {
			//Sets up datagram socket on a random port
			socket = new DatagramSocket(clientPort);
			System.out.println("Connected, Client Port: " + socket.getLocalPort());
		} catch (SocketException e) {
			e.printStackTrace();
			System.err.println("Error initializing Datagram Socket");
			PlayGame.restartConnect("Error with creating socket");
			errorCode = cError.SOCKET_ERROR;
			return false;
		}

		sendConnectionPacket();

		listening = true;
		listenThread = new Thread(() -> listen());
		listenThread.start();

		return true;
	}
	public void close() {
		socket.close();
	}
	public ClientPlayer getPlayer(int i) {
		return players.get(i);
	}
	public void listen() {
		while(listening) {
			if(errorCode == cError.NONE) {
				DatagramPacket packet = new DatagramPacket(receivedDataBuffer, receivedDataBuffer.length);
				try {
					socket.receive(packet);
					process(packet);
				} catch (SocketException e) {
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	synchronized void process(DatagramPacket packet) {
		byte[] data = packet.getData();
		if(new String(data, 0, 4).equals("PLDB")) {
			PDatabase database = PDatabase.Deserialize(data);
			process(database);
		} else if(data[0] == 0x4a && data[1] == 0x2c) {
			switch(data[2]) {
			case 0x01: //new player
				Screen.phase = Screen.Phase.CONNECTING;
				errorCode = cError.BAD_HOST;
				System.err.println("Host closed");
				PlayGame.restartConnect("Host closed");
				break;
			case 0x02: //Already in game
				Screen.phase = Screen.Phase.CONNECTING;
				errorCode = cError.BAD_HOST;
				System.err.println("Server is already in a game");
				PlayGame.restartConnect("Game has Already Started");
				break;
			}
		}
	}
	private void process(PDatabase database) {
		PObject obj1 = database.firstObject();
		if(!caughtUp)
			System.out.println("Receiving: " + obj1.getName());
		if(!caughtUp && !obj1.getName().equals("NewPlayers")) {
			return;
		}
		switch(obj1.getName()) {
		case "ActivePlayers": setActivePlayers(obj1); break;
		case "InactivePlayers": setInactivePlayers(obj1); break;
		case "NewPlayers": setPlayers(obj1); break;
		default: PlayGame.screen.process(obj1); return;
		}
		Screen.setPlayers(players);
	}
	public boolean isPlayerActive(int i)
	{
		if(i >= players.size()) return false;
		
		return players.get(i).active;
	}
	private void setActivePlayers(PObject obj) {
		for(ClientPlayer p : players) {
			if(!p.active && obj.findString(p.getName()) != null) {
				p.active = true;
				System.out.println(p.getName() + " is now active");
			}
		}
	}
	private void setInactivePlayers(PObject obj) {
		for(ClientPlayer p : players) {
			if(p.active && obj.findString(p.getName()) != null) {
				p.active = false;
				System.out.println(p.getName() + " is now inactive");
			}
		}
	}
	private void setPlayers(PObject obj) {
		caughtUp = true;
		int index = 0;
		System.out.println("Setting Players");
		for(PString name : obj.getStrings()) {
			if(name.getName().equals("name")) {
				boolean exists = false;
				for(ClientPlayer p : players) {
					if(p.getName().equals(name.getString()))
					{
						exists = true;
						p.userID = index;
					}
				}
				if(!exists) {
					System.out.println("Adding Player " + name.getString());
					players.add(new ClientPlayer(name.getString(), index));
				}
				if(name.getString().equals(username)) {
					userID = index;
				}
				index++;
			} else {
				for(ClientPlayer p : players) {
					if(p.getName().equals(name.getString()))
					{
						System.out.println("Removing Player " + p.getName());
						players.remove(p);
						Screen.playerCount = players.size();
						break;
					}
				}
			}
		}
	}
	public void sendConnectionPacket() {
		BinaryWriter writer = new BinaryWriter();
		writer.write(new byte[] {0x4a, 0x2c, 0x01});
		writer.write((byte)username.length());
		writer.write(username.getBytes());
		send(writer.getBuffer());
	}
	public void sendAFKpacket() {
		System.out.println("Closing");
		BinaryWriter writer = new BinaryWriter();
		writer.write(new byte[] {0x4a, 0x2c, 0x02});
		writer.write((byte)username.length());
		writer.write(username.getBytes());
		send(writer.getBuffer());
		listening = false;
	}

	public void send(byte[] data) {
		assert(socket.isConnected());
		DatagramPacket packet = new DatagramPacket(data, data.length, serverAddy, serverPort);
		try {
			socket.send(packet);
		} catch (IOException e) {
			PlayGame.restartConnect("Could not find host \"" + serverAddy.getHostName() + "\"");
		}		
	}

	public void send(PDatabase database) {
		byte[] data = new byte[database.getSize()];
		database.setBytes(data, 0);
		send(data);
	}

	public void sendInput(PObject input) {
		PDatabase userInput = new PDatabase(username + "" + userID);
		userInput.addObject(input);
		send(userInput);
	}

	public cError getErrorCode() {
		return errorCode;
	}
}
