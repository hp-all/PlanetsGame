package gameCycle;

import levelPack.LevelSelect;
import main.GameEngine;
import planetServer.ServerClient;
import serialization.*;

public class MatchTransition extends CycleBase{
	public boolean[] confirm;
	public boolean[] goBack;
	public int[] readyStep;

	public int controllingPlayer = 0;
	public boolean vis = false;
	private int tranStep = 0;
	private int changeStep = 1;
	public boolean showWinner = false;

	public void prep() {
		vis = true;
		tranStep = 0;
		changeStep = 1;
		if(randMap)
		{
			stageNum = (int)(Math.random()*LevelSelect.levelCount)+1;
		}
		updatePlayers();
	}
	public void updatePlayers()
	{
		readyStep = new int[playerCount];
		confirm = new boolean[playerCount];
		goBack = new boolean[playerCount];
		score = GameEngine.scoreBoard.getMainScore();

		for(int i = 0; i<playerCount; i++)
		{
			readyStep[i] = 0;
			confirm[i] = false;
			goBack[i] = false;
		}
	}
	public void update()
	{		
		//Will toggle the confirm or go back setting if the player hits space or backspace respectively
		boolean canStart = playerCount > 1;
		boolean canGoBack = true;
		for(ServerClient p : players)
		{
			int i = players.indexOf(p);
			if(tranStep >= 0)
			{
				if(p.space()) {
					confirm[i] = !confirm[i];
				}
				if(p.back())
					goBack[i] = !goBack[i];
				if(!p.isActive()) {
					goBack[i] = true;//*/
					confirm[i] = true;
				}
			}
			if(confirm[i])
			{
				if(readyStep[i] <= 20)
				{
					readyStep[i] += 1;
					canStart = false;
				}
			}
			else
			{
				canStart = false;
				if(readyStep[i] > 20)
					readyStep[i] = 0;
			}
			if(tranStep < 20 && tranStep >= 0)
			{
				canGoBack = false;
			}
			else if(!goBack[i])
				canGoBack = false;
		}
		for(Integer i : score)
		{
			if(i >= matchPoint)
				showWinner = true;
		}
		if(!showWinner)
		{
			//Will begin the next phase of the game if all players are ready, or will go back
			//to the lobby screen if all players agree to
			if(canStart && changeStep == 1)
			{
				changeStep = -1;
				tranStep = 0;
			}
			if(canGoBack)
			{
				goBackToLobby();
			}
			if(tranStep < -20) {
				vis = false;
			}

			if(!Lobby.randMap)
			{
				ServerClient setter = players.get(controllingPlayer);
				if(setter.right())
				{
					if(stageNum < LevelSelect.levelCount)
						stageNum ++;
					else
						stageNum = 1;
				}
				else if(setter.left())
				{
					if(stageNum > 1)
						stageNum --;
					else
						stageNum = LevelSelect.levelCount;
				}
			}
		}
		else
		{
			if(canStart && changeStep == 1)
			{
				goBackToLobby();
			}
		}
		tranStep += changeStep;
	}
	public void goBackToLobby()
	{
		System.out.println("Match go to lobby");
		GameEngine.goToLobby();
		showWinner = false;
	}
	public PObject serialize() {
		PObject result = new PObject("Transition");
		result.addField(PField.Integer("tranStep", tranStep));
		result.addField(PField.Integer("stageNum", stageNum));
		result.addField(PField.Boolean("randMap", Lobby.randMap));
		result.addField(PField.Integer("playerControl", controllingPlayer));
		result.addArray(PArray.Boolean("confirms", confirm));
		result.addArray(PArray.Boolean("goBack", goBack));
		result.addArray(PArray.Integer("score", score));
		result.addField(PField.Boolean("showWinner", showWinner));
		return result;
	}
}
