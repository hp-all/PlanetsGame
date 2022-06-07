package gameCycle;


import main.GameEngine;
import planetServer.ServerClient;
import serialization.*;

public class Lobby extends CycleBase{
	public boolean vis = true;

	public static int transitionStep = 0;
	public static int changeStep = 1;

	public static boolean[] confirm = new boolean[] {};

	public static int controllingPlayer = 0;
	private static int selected = 0;

	public void restart()
	{
		transitionStep = 0;
		changeStep = 1;
		GameEngine.scoreBoard.prepScoreBoard();
		for(int i = 0; i<confirm.length; i++)
			confirm[i] = false;
		controllingPlayer = 0;
		vis = true;
	}
	public synchronized void update() {		
		if(playerCount != confirm.length){
			confirm = new boolean[playerCount];
			for(int i = 0; i<playerCount; i++){
				confirm[i] = false;
			}
			transitionStep = 0;
			changeStep = 1;
		}

		boolean canStart = playerCount > 1 && transitionStep > 5;

		if(transitionStep > 5)
		{
			for(ServerClient p : players){
				int i = players.indexOf(p);
				if(p.space())
					confirm[i] = !confirm[i];
				if(!confirm[i])
					canStart = false;
			}
		}
		if(canStart && changeStep != -1){
			transitionStep = 0;
			changeStep = -1;
		}
		if(playerCount > 0 && changeStep != -1){
			ServerClient setter = players.get(controllingPlayer);
			if(setter.up())
				if(selected > 0)
					selected --;
				else
					selected = 4;

			else if(setter.down())
				if(selected < 4)
					selected++;
				else
					selected = 0;
			if(setter.right()){
				if(selected == 0)
					randMap = !randMap;
				if(selected == 1)
					if(matchPoint < 16)
						matchPoint += 2;
					else matchPoint = 2;
				if(selected == 2)
					if(matchTime < 7)
						matchTime += 1;
					else matchTime = 1; 
				if(selected == 3)
					if(matchLives < 5)
						matchLives += 1;
					else matchLives = 1;
				if(selected == 4)
					switch(startingHealth){
					case 1: startingHealth = 50; break;
					case 100: startingHealth = 1; break;
					default: startingHealth = 100; break;
					}
			}
			else if(setter.left()){
				if(selected == 0)
					randMap = !randMap;
				if(selected == 1)
					if(matchPoint > 2)
						matchPoint -= 2;
					else matchPoint = 16;
				if(selected == 2)
					if(matchTime > 1)
						matchTime -= 1;
					else
						matchTime = 7;
				if(selected == 3)
					if(matchLives > 1)
						matchLives -= 1;
					else matchLives = 5;
				if(selected == 4)
					switch(startingHealth){
					case 100: startingHealth = 50; break;
					case 50: startingHealth = 1; break;
					default: startingHealth = 100; break;
					}
			}
		}

		transitionStep += changeStep;
		if(transitionStep < -15)
			vis = false;
	}
	public boolean keyIsPressed(boolean before, boolean now)
	{
		boolean bef = before;
		before = now;
		return(!bef && now);
	}
	public PObject serialize() {
		PObject result = new PObject("Lobby");
		result.addField(PField.Boolean("randomMap", randMap));
		result.addField(PField.Integer("matchPoint", matchPoint));
		result.addField(PField.Integer("matchTime", matchTime));
		result.addField(PField.Integer("matchLives", matchLives));
		result.addField(PField.Integer("initHealth", startingHealth));
		result.addField(PField.Integer("playerControl", controllingPlayer));
		result.addField(PField.Integer("selected", selected));
		result.addField(PField.Integer("tranStep", transitionStep));
		result.addArray(PArray.Boolean("confirms", confirm));
		return result;
	}
	@Override
	public String toString()
	{
		// Sends to socket
		/* boolean randMap
		 * int matchPoint
		 * int matchTime
		 * int matchLives
		 * int startingHealth
		 * int controllingPlayer
		 * int selected
		 * int transitionStep
		 * boolean[] confirms
		 */
		String confirms = "";
		for(Boolean c : confirm)
		{
			confirms += c + ",,";
		}
		return randMap + "//" + matchPoint + "//" + matchTime + "//" + matchLives + "//" + startingHealth + "//" + controllingPlayer + "//" + selected + "//" + transitionStep + "//" + confirms;
	}
}
