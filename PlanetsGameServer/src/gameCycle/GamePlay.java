package gameCycle;

import java.util.ArrayList;
import java.util.List;

import org.jbox2d.common.Vec2;

import levelPack.LevelSelect;
import main.GameEngine;
import objects.*;
import serialization.*;

public class GamePlay extends CycleBase{
	public static SPlayer[] shooterBois;
	
	public int gameStep = 0;
	public int stepChange = 1;
	private int goShift = 85;
	private int outroShift = 50;
	
	private LevelSelect leSel;

	private long startTime = 0;
	private double timer = 0;

	public boolean vis = false;

	private int gamePhase = 0;
	
	public GamePlay()
	{

	}
	public void prep()
	{
		vis = true;
		timer = matchTime*60;
		gamePhase = 0;
		gameStep = 0;
		stepChange = 1;
		GameEngine.worldStep = 1f/40.0f;
	}
	public int[] randomOrder(int size)
	{
		ArrayList<Integer> orderedSet = new ArrayList<Integer>();
		int[] output = new int[size];
		for(int i = 0; i<size; i++) {
			orderedSet.add(i);
		}
		for(int i = 0; i<size; i++) {
			int index = (int)(Math.random()*orderedSet.size());
			output[i] = orderedSet.get(index);
			orderedSet.remove(index);
		}
		return output;
	}
	public void update()
	{		
		//Initializes the spawn points, map, and resets the players before the game starts
		if(gameStep == 0)
        {
            leSel = GameEngine.levelSel;
            leSel.setMap(stageNum);
            int[] spawnOrder = randomOrder(playerCount);
    		shooterBois = new SPlayer[playerCount];
            SPlayer.startHealth = Lobby.startingHealth;
            for(int i = 0; i<playerCount; i++)
            {
                float x = leSel.currentSpawnPoints[spawnOrder[i]].x;
                float y = leSel.currentSpawnPoints[spawnOrder[i]].y;
                double a = leSel.currentSpawnAngles[spawnOrder[i]];
                shooterBois[i] = new SPlayer(GameEngine.server.getClients().get(i), i, new Vec2(x, y), Lobby.matchLives, a);
            }
        }
		//Updates the game
		if(gameStep >= goShift)
		{
			leSel.update();
		    //Begins the timer for the game
		    if(gameStep == goShift) {
				System.out.println("GO");
	            SPlayer.gameStart = true;
	            startTime = System.nanoTime();
	        }
		    else {
		        for(SPlayer p : shooterBois)
	            {
		        	p.update();
	            }
	            for(Shot s : Shot.shots)
	            {
	                s.move();
	            }
		    }
		    //Will count the time if the match Setting is not equal to infinity ('7')
			if(Lobby.matchTime != 7)
			{
				long ellapse = System.nanoTime() - startTime;
				double secs = (double)(ellapse/1000000000.);
				timer = Lobby.matchTime*60 - secs;
			}
		}

		//Game Phase settings
		int deathCount = 0;
		int sumHealth = 0;
		int lowestHealth = Lobby.startingHealth;
		for(int i = 0; i<playerCount; i++)
		{
			if(shooterBois[i].isDead)
				deathCount ++;
			sumHealth += shooterBois[i].health;
			if(shooterBois[i].health > 0)
				lowestHealth = Math.min(shooterBois[i].health, lowestHealth);
		}
		if(Lobby.startingHealth == 1)
		{
			gamePhase = 2;
		}
		else
		{
			if((double)(sumHealth)/(Lobby.startingHealth*playerCount) < .5)
			{
				gamePhase = 1;
			}
			else if(deathCount == 1 && playerCount > 2 && gamePhase == 0)
			{
				gamePhase = 1;
			}
			if((double)(sumHealth)/(Lobby.startingHealth*playerCount) < .2)
			{
				gamePhase = 2;
			}
			else if(deathCount == playerCount - 2 && (double)(lowestHealth)/(Lobby.startingHealth) <= .2 && gamePhase == 1)
			{
				gamePhase = 2;
			}
		}
		
		//Ends the game if there is only one player left
		if(deathCount == playerCount - 1)
		{
			if(stepChange == 1)
			{
				System.out.println("GAME OVER");
				stepChange = -1;
				gameStep = -1;
				gamePhase = -1;
			}
			else if(gameStep < 0 && gameStep >= -outroShift)
			{
				float fr = (float)map(-gameStep, 0, outroShift, 40, 640);
				GameEngine.worldStep = 1f/fr;
			}
			if(gameStep < -55 - outroShift)
			{
				for(SPlayer p : shooterBois) {
					p.awaitDeath();
				}
				vis = false;
			}
		}
		gameStep += stepChange;
	}
	private double map(double x, double aMin, double aMax, double bMin, double bMax)
	{
		return (x-aMin)/(aMax-aMin)*(bMax-bMin)+bMin;
	}
	public PObject serialize() {
		PObject gameInfo = new PObject("Game");
		gameInfo.addField(PField.Integer("gameStep", gameStep));
		gameInfo.addField(PField.Double("timer", timer));
		gameInfo.addField(PField.Integer("gamePhase", gamePhase));
		
		ArrayObj[] playerData = new ArrayObj[GameEngine.playerCount];
		for(int i = 0; i<shooterBois.length; i++) {
			playerData[i] = shooterBois[i].serializeToSocket();
		}
		gameInfo.addArray(PArray.Object("Players", playerData));
		gameInfo.addArray(leSel.serializeToSocket());

		ArrayObj[] shotData = new ArrayObj[Shot.shots.size()];
		for(int i = 0; i<Shot.shots.size(); i++) {
			shotData[i] = Shot.getShot(i).serializeToSocket();
		}
		if(shotData.length != 0)
			gameInfo.addArray(PArray.Object("Shots", shotData));
		
		return gameInfo;
	}
	@Override
	public String toString()
	{
		String playerList = "";
		String projectileList = "";
		for(SPlayer p : shooterBois)
			playerList += p + "//";
		for(Shot s : Shot.shots)
			projectileList += s + "//";
		return gameStep + "__" + timer + "__" + gamePhase + "__" + playerList + "__" + leSel + "__" + projectileList;

	}
}
