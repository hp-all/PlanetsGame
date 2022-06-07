package setWorld;

import java.util.*;

import gameCycle.GamePlay;
import planetServer.ServerClient;

public class ScoreBoard {
	public int gameMode = 0;
	public int playerCount;
	public List<ServerClient> clients = new ArrayList<>();

	public ScoreBoard(List<ServerClient> clients) {
		this.clients = clients;
	}
	public void changePlayerCount(){
		playerCount = clients.size();
	}
	public void inactivePlayer(int inactiveDude) {
		if(clients.get(inactiveDude).score.voidScore)
			clients.get(inactiveDude).score.voidScore();
	}
	public void restartScoreBoard() {
		for(ServerClient c : clients)
			c.score.set();
	}
	public void prepScoreBoard(){
		for(ServerClient c : clients)
			c.score.reset();
	}
	public void addShot(int player) {
		clients.get(player).score.shotsFired++;
	}
	public void addShotConnect(int oj, int sucker, int damage) {
		clients.get(oj).score.shotsConnected++;
		clients.get(oj).score.damageDealt += damage;
		clients.get(sucker).score.damageTaken += damage;
	}
	public void addKill(int oj, int sucker)
	{
		clients.get(oj).score.addKill(sucker);
		clients.get(sucker).score.addDeath(oj);
		System.out.println(sucker + " lives: " + GamePlay.shooterBois[sucker].lives);
		if(GamePlay.shooterBois[sucker].lives == 0)
		{
			System.out.println("setting place for " + sucker);
			clients.get(sucker).score.setPlace();
			System.out.println(clients.get(sucker).score.place);
		}
	}
	public int[] getKills(int player){
		return clients.get(player).score.getKills();
	}
	public int[] getDeaths(int player){
		return clients.get(player).score.getDeaths();
	}
	public void setMainScore(){
		for(int i = 0; i<playerCount; i++) {
			clients.get(i).score.setMainScore();
		}
	}
	public int[] getMainScore(){
		setMainScore();
		int[] score = new int[playerCount];
		for(int i = 0; i<playerCount; i++){
			score[i] = clients.get(i).score.gameScore;
		}
		return score;
	}
	public int[] setMatchScore(){
		int[] score = new int[playerCount];
		for(int i = 0; i<playerCount; i++){ //goes thru players

			int firstPlaceScore = 0;
			switch(playerCount) {
			case 2: firstPlaceScore = 1; break;
			case 3: firstPlaceScore = 3; break;
			case 4: firstPlaceScore = 4; break;
			}
			int place = clients.get(i).score.place;
			switch(place) {
			case -1: score[i] = 0; break;
			case  0: score[i] = firstPlaceScore; break;
			case  3: score[i] = 2; break;
			default: score[i] = place-1; break;
			}

			clients.get(i).score.setGameScore(score[i]);
		}
		return score;
	}
	public int[] getShotData(int i)
	{
		int[] result = new int[4];
		result[0] = clients.get(i).score.shotsFired;
		result[1] = clients.get(i).score.shotsConnected;
		result[2] = clients.get(i).score.damageDealt;
		result[3] = clients.get(i).score.damageTaken;
		return result;
	}
	public int[][] getShotData(){
		int[][] result = new int[clients.size()][4];
		for(int i = 0; i<clients.size(); i++) {
			result[i] = getShotData(i);
		}
		return result;

	}
}
