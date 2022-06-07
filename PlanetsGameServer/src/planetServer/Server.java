package planetServer;

import static main.GameEngine.Phase.*;
import static planetServer.Server.Status.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.*;

import main.GameEngine;
import serialization.*;

public class Server {
	public enum Status{
		CHILLIN, ADD_PLAYER, REMOVE_PLAYER, SIGNAL_INACTIVE, SIGNAL_ACTIVE;
	}
	public static final int PORT = 4000;
	public boolean listening = false;
	private Thread listenThread;

	private DatagramSocket socket;
	public Status status = CHILLIN;
	private static final int MAX_PACKET_SIZE = 2048;
	private byte[] receivedDataBuffer = new byte[MAX_PACKET_SIZE*10];

	private int playerCount = 0;
	private List<ServerClient> clients = new ArrayList<>();
	private String weirdClient = "";
	private String[] badClients;

	volatile PDatabase outgoingDB;
	public Lock lock = new ReentrantLock();


	public Server() {

	}
	public void start() {

		outgoingDB = new PDatabase("OutgoingDB");

		listening = true;

		try {
			socket = new DatagramSocket(PORT);

		} catch (IOException e) {
			e.printStackTrace();
		}

		listenThread = new Thread(() -> listen());
		listenThread.start();
	}
	public synchronized int getCount() {
		return playerCount;
	}
	public synchronized String getName(int i) {
		return clients.get(i).username;
	}
	public synchronized String[] getNames() {
		String[] names = new String[playerCount];
		for(int i = 0; i<playerCount; i++)
			names[i] = getName(i);
		return names;
	}
	public synchronized List<ServerClient> getClients(){
		return clients;
	}
	private ServerClient findClient(String name) {
		for(ServerClient c : clients) {
			if(name.equals(c.username))
				return c;
		}
		return null;
	}
	public void sendError(byte errorType, ServerClient sorryBud) {
		BinaryWriter writer = new BinaryWriter();
		System.out.println("error: " + errorType);
		writer.write(new byte[] {0x4a, 0x2c, errorType});
		writer.write((byte)"inGame".length());
		writer.write("inGame".getBytes());
		send(writer.getBuffer(), sorryBud);
	}
	public void sendErrorToAll(byte errorType) {
		for(ServerClient c : clients) {
			sendError(errorType, c);
		}
	}
	private synchronized void addClient(DatagramPacket packet, byte[] data)
	{
		lock.lock();

		String username = new String(data, 4, (int)data[3]);
		//if the username already exists from the same ip address, then set the client back to active
		//i.e. if the same player is rejoining from the same ip address in the same game, reset them to active
		if(GameEngine.phase != LOBBY) {
			sendError((byte)0x02, new ServerClient("", packet.getAddress(), packet.getPort()));
			System.out.println("Sent Error");
			lock.unlock();
			return;
		}
		for(ServerClient sc : clients) {
			if(!sc.isActive() && username.equals(sc.username) && packet.getAddress().equals(sc.addy)) {
				sc.status = true;
				status = SIGNAL_ACTIVE;
				weirdClient = username;

				PDatabase catchUp = new PDatabase("OutgoingDB");
				catchUp.addObject(serializePlayers());
				byte[] cData = new byte[outgoingDB.getSize()];
				if(cData.length != 0) {
					System.out.println("catching up now active player " + sc.username);
					outgoingDB.setBytes(cData, 0);
					lock.unlock();
					send(cData, sc);
				}
				return;
			}
		}

		status = ADD_PLAYER;
		clients.add(new ServerClient(new String(data, 4, (int)data[3]), packet.getAddress(), packet.getPort()));
		System.out.println("Adding player " + username + " addy: " + packet.getAddress() + " port: " + packet.getPort());
		playerCount = clients.size();
		GameEngine.addPlayer(playerCount);

		lock.unlock();
	}
	public synchronized void removeInactives() {
		lock.lock();
		badClients = new String[playerCount-getActiveCount()];
		for(int i = 0; i<playerCount; i++) {
			if(!clients.get(i).isActive()) {
				System.out.println("client: " + clients.get(i).username);
				removeClient(clients.get(i));
				i--;
			}
			else
				clients.get(i).setID(i);
		}
		ServerClient.userIDCounter = clients.size();
		lock.unlock();
	}
	private synchronized void removeClient(ServerClient client)
	{
		if(playerCount > 1) {
			for(int i = 0; i<badClients.length; i++) {
				if(badClients[i] == null) {
					badClients[i] = client.username;
					break;
				}
			}
		}
		clients.remove(client);		
		playerCount = clients.size();
		GameEngine.removePlayer(playerCount);

		for(int i = 0; i<playerCount; i++) {
			clients.get(i).setID(i);
			System.out.println(i);
		}
		status = REMOVE_PLAYER;
	}
	public int getActiveCount()
	{
		int result = 0;
		for(ServerClient c : clients) {
			if(c.isActive())
				result ++;
		}
		return result;
	}
	private void handleAFK(ServerClient c) {
		if(c == null) return;

		System.err.println(c.username + " was disconnected");
		if(GameEngine.phase == LOBBY) {
			badClients = new String[1];
			removeClient(c);
		}
		else {
			c.status = false;
			weirdClient = c.username;
			status = SIGNAL_INACTIVE;
		}
	}
	public void listen() {
		while(listening) {
			DatagramPacket packet = new DatagramPacket(receivedDataBuffer, receivedDataBuffer.length);
			try {
				socket.receive(packet);
				process(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private synchronized void process(DatagramPacket packet) {
		byte[] data = packet.getData();

		if(new String(data, 0, 4).equals("PLDB")) {
			PDatabase database = PDatabase.Deserialize(data);
			process(database);
		} else if(data[0] == 0x4a && data[1] == 0x2c) {
			switch(data[2]) {
			case 0x01: //new player
				addClient(packet, data);
				break;
			case 0x02: //remove player
				String username = new String(data, 4, (int)data[3]);
				handleAFK(findClient(username)); 
				break;
			}
		}
	}
	private synchronized void process(PDatabase database) {
		String clientNameData = database.getName();
		String clientName = clientNameData.substring(0, clientNameData.length()-1);
		int clientID = Integer.parseInt(clientNameData.substring(clientName.length()));

		if(database.findObject("Input") != null) {
			if(clientID < clients.size())
				clients.get(clientID).setInput(database.findObject("Input"));
		}
	}
	public void send(byte[] data, ServerClient client) {
		assert(socket.isConnected());
		DatagramPacket packet = new DatagramPacket(data, data.length, client.addy, client.port);
		try {
			socket.send(packet);
		} catch (IOException e) {
			handleAFK(client);
			//e.printStackTrace();
		}		
	}
	public synchronized void sendDatabase() {
		lock.lock();
		for(ServerClient sc : clients) {
			sendDatabase(outgoingDB, sc);
		}
		if(outgoingDB.firstObject().getName().equals("ActivePlayers"))
		{
			System.out.println("Sending catch up data to " + weirdClient);
			PDatabase catchUp = new PDatabase("OutgoingDB");
			catchUp.reset(serializePlayers());
			sendDatabase(catchUp, findClient(weirdClient));
		}
		lock.unlock();
	}
	public void sendDatabase(PDatabase database) {
		byte[] data = new byte[database.getSize()];
		if(data.length != 0) {
			database.setBytes(data, 0);
			for(ServerClient sc : clients) {
				send(data, sc);
			}
		}
	}
	public void sendDatabase(PDatabase database, ServerClient client) {
		byte[] data = new byte[database.getSize()];
		if(data.length != 0) {
			database.setBytes(data, 0);
			send(data, client);
		}
	}
	public synchronized PDatabase serialize() {
		if(status != CHILLIN)
			System.out.println("Status: " + status);
		if(status == SIGNAL_INACTIVE) {
			PObject inactivePlayers = new PObject("InactivePlayers");
			for(ServerClient c : clients) {
				if(!c.isActive())
					inactivePlayers.addString(new PString(c.username, "setInactive"));
			}

			outgoingDB.reset(inactivePlayers);
			status = CHILLIN;
			return outgoingDB;

		} else if(status == SIGNAL_ACTIVE) {
			PObject activePlayers = new PObject("ActivePlayers");
			for(ServerClient c : clients) {
				if(c.isActive()) {
					activePlayers.addString(new PString(c.username, "setActive"));
				}
			}

			outgoingDB.reset(activePlayers);
			status = CHILLIN;
			return outgoingDB;

		} else if(status != CHILLIN) {
			outgoingDB.reset(serializePlayers());
			status = CHILLIN;
			return outgoingDB;
		}

		//"Lobby", "Transition", "Game", "Finish"
		outgoingDB.reset(GameEngine.getCurrentGamePhase().serialize());
		return outgoingDB;
	}
	private PObject serializePlayers() {
		PObject playerNames = new PObject("NewPlayers");
		for(int i = 0; i<playerCount; i++)
		{
			playerNames.addString(new PString("name", getName(i)));
		}
		if(status == REMOVE_PLAYER) {
			for(int i = 0; i<badClients.length; i++) {
				playerNames.addString(new PString("remove", badClients[i]));
				System.out.println("Sending removed client " + badClients[i]);
			}
		}
		return playerNames;
	}
}
