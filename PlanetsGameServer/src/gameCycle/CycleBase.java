package gameCycle;

import java.util.List;
import planetServer.ServerClient;
import serialization.PObject;

public abstract class CycleBase {
	public static boolean randMap = true;
	public static int stageNum = 1;
	public static int matchPoint = 2;
	public static int matchTime = 4;
	public static int matchLives = 1;
	public static int startingHealth = 100;
	
	public int[] score;

	protected static List<ServerClient> players;
	protected static int playerCount;
	
	public CycleBase() {
	}
	
	public static void updatePlayers(List<ServerClient> p) {
		players = p;
		playerCount = players.size();
	}
	public abstract PObject serialize();
}
