package main;

import objects.*;
import planetServer.Server;
import planetServer.ServerClient;
import setWorld.ContactListen;
import setWorld.ScoreBoard;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import gameCycle.*;
import levelPack.LevelSelect;

import static main.GameEngine.Phase.*;

import java.util.*;


public class GameEngine{
	public enum Phase{
		LOBBY, TRANSITION, GAME, FINISH;
	}
	public static World world = new World(new Vec2(0, 0));
	public static float worldStep = 1f/40.0f;
	private static ContactListen coLi = new ContactListen();
	public static int width = 900;
	public static int height = width / 16 * 9;
	public static int playerCount = 0;

	public static Phase phase = LOBBY;

	//setting sockets
	public static Server server;

	//transitions and menus
	public static Lobby lobby;
	public static MatchTransition tran;
	public static GamePlay game;
	public static FinishTransition finish;

	public static LevelSelect levelSel;
	public static ScoreBoard scoreBoard;

	public GameEngine()
	{
		world.setContactListener(coLi);

		levelSel = new LevelSelect();

		lobby = new Lobby();
		tran = new MatchTransition();
		game = new GamePlay();
		finish = new FinishTransition();

		server = new Server();
		scoreBoard = new ScoreBoard(server.getClients());

		server.start();
	}
	public static void addPlayer(int playerCount) {
		GameEngine.playerCount = playerCount;
		scoreBoard.changePlayerCount();
	}
	public static void removePlayer(int playerCount) {
		GameEngine.playerCount = playerCount;
		scoreBoard.changePlayerCount();
	}
	public void update() {
		//Updates the World
		world.step(worldStep, 4, 4);
		SEntity.destroyDeathList();
		//updates player input
		for(int i = 0; i<playerCount; i++) {
			server.getClients().get(i).update();
		}
		//updates the game cycle
		updateGameCycle();
		//Writes the Locations of players to the clients
		if(playerCount > 0) {
			serializeToSocket();
			server.sendDatabase();
		}
	}
	public static void goToLobby() {
		System.out.println("Back to Lobby");
		ServerClient.pressOnce = true;
		server.removeInactives();
		tran.showWinner = false;
		scoreBoard.restartScoreBoard();
		lobby.restart();
		phase = LOBBY;
	}
	private synchronized void updateGameCycle() {
		CycleBase.updatePlayers(server.getClients());
		if((server.getClients().size() < 2 || server.getActiveCount() < 2) && phase != Phase.LOBBY) {
			goToLobby();
		}
		if(phase == LOBBY)
		{
			lobby.update();
			if(!lobby.vis)
			{
				phase = TRANSITION;
				tran.prep();
				ServerClient.pressOnce = true;
			}
		}
		if(phase == TRANSITION)
		{
			tran.update();
			if(!tran.vis) 
			{
				phase = GAME;
				game.prep();
				ServerClient.pressOnce = false;
			}
		}
		if(phase == GAME)
		{
			game.update();
			if(!game.vis)
			{
				phase = FINISH;
				finish.prep();
				levelSel.clearMap();
				ServerClient.pressOnce = true;
			}
		}
		if(phase == FINISH)
		{
			finish.update();
			if(!finish.vis) 
			{
				phase = TRANSITION;
				tran.prep();
			}
		}
	}
	public static synchronized CycleBase getCurrentGamePhase() {
		switch(phase) {
		case LOBBY: return lobby;
		case TRANSITION: return tran;
		case GAME: return game;
		case FINISH: return finish;
		default: return null;
		}
	}
	private void serializeToSocket()
	{
		server.serialize();
	}
}
