package gameCycle;

import java.util.Arrays;

import main.GameEngine;
import planetServer.ServerClient;
import serialization.*;

public class FinishTransition extends CycleBase{
	public int[] score;
	public int[][] kills;
	public int[][] deaths;
	public int[][] shotData;
	public int[] confirm;
	public boolean vis = false;
	public int finishStep = 0;
	public int changeStep = 1;

	boolean sentData = false;

	public void prep()
	{
		sentData = false;
		
		changeStep = 1;
		finishStep = 0;
		
		vis = true;
		finishStep = 0;
		shotData = GameEngine.scoreBoard.getShotData();
		confirm = new int[playerCount];
		kills = new int[playerCount][];
		deaths = new int[playerCount][];
		for(int i = 0; i<playerCount; i++) {
			confirm[i] = 0;
			kills[i] = GameEngine.scoreBoard.getKills(i);
			deaths[i] = GameEngine.scoreBoard.getDeaths(i);
		}
		System.out.println(Arrays.toString(kills[0]) + ",,," + Arrays.toString(kills[1]));
		score = GameEngine.scoreBoard.setMatchScore();
	}

	public void update()
	{
		boolean canStart = playerCount > 1;
		int firstActive = 0;
		for(ServerClient p : players) {
			if(p.isActive()) {
				firstActive = players.indexOf(p);
				break;
			}
		}
		for(ServerClient p : players)
		{
			int i = players.indexOf(p);
			if(p.space() && changeStep != -1 && finishStep > 1)
			{
				confirm[i] ++;
			}
			if(p.back() && changeStep != -1 && finishStep > 1)
				confirm[i] --;

			if(confirm[i] > 2)
				confirm[i] = 2;
			if(confirm[i] < 0)
				confirm[i] = 0;

			if(!p.isActive()) {
				confirm[i] = confirm[firstActive];
			}
			
			if(confirm[i] != 2)
				canStart = false;
		}
		if(canStart && changeStep > 0)
		{
			changeStep = -1;
			finishStep = 0;
		}
		if(finishStep <= -30)
		{
			vis = false;
		}

		finishStep += changeStep;
	}
	public boolean keyIsPressed(boolean before, boolean now)
	{
		boolean bef = before;
		before = now;
		return(!bef && now);
	}
	public PObject serialize() {
		PObject result = new PObject("Finish");
		result.addField(PField.Integer("finishStep", finishStep));

		if(!sentData) {
			ArrayObj[] scoreData = new ArrayObj[GameEngine.playerCount];
			for(int i = 0; i<GameEngine.playerCount; i++) {
				scoreData[i] = new ArrayObj(PField.Integer("score", score[i]));
				scoreData[i].addArray(PArray.Integer("kills", kills[i]));
				scoreData[i].addArray(PArray.Integer("deaths", deaths[i]));
				scoreData[i].addArray(PArray.Integer("shots", shotData[i]));
				scoreData[i].addField(PField.Integer("confirm", confirm[i]));
			}
			result.addArray(PArray.Object("ScoreData", scoreData));
			sentData = true;
		} else {
			result.addArray(PArray.Integer("confirm", confirm));
		}

		return result;
	}
	@Override
	public String toString()
	{
		String scoreString = "";
		for(int i = 0; i<score.length; i++)
			scoreString += score[i] + ",,";
		String shotString = "";
		for(int i = 0; i<shotData.length; i++) {
			for(int j = 0; j<shotData[i].length; j++) {
				shotString += shotData[i][j]+ "--";
			}
			shotString += ",,";
		}
		String confirmString = "";
		for(int i = 0; i<confirm.length; i++)
			confirmString += confirm[i] + ",,";
		//GameEngine.scoT.getKills() + "//" + GameEngine.scoT.getDeaths() + 
		return finishStep + "//" + scoreString + "//" + "//" + shotString + "//" + confirmString;
	}
}
