package setWorld;

import java.util.ArrayList;
import java.util.List;

public class Score {
	protected int gameScore;
	protected int matchScore;
	protected List<Integer> kills = new ArrayList<>();
	protected List<Integer> deaths = new ArrayList<>();
	
	protected int shotsFired;
	protected int shotsConnected;
	protected int damageTaken;
	protected int damageDealt;
	
	protected static int placeCounter = 0;
	protected int place;
	
	public boolean hasPlaced = false;
	public boolean voidScore = false;
	
	public Score() {
		set();
	}
	protected void set() {
		gameScore = 0;
		reset();
	}
	protected void reset() {
		placeCounter = 0;
		matchScore = 0;
		kills.clear();
		deaths.clear();
		shotsFired = 0;
		shotsConnected = 0;
		damageTaken = 0;
		damageDealt = 0;
		place = 0;
		hasPlaced = false;
		voidScore = false;
	}
	protected void setPlace() {
		placeCounter ++;
		place = placeCounter;
		hasPlaced = true;
	}
	/**
	 * if the player disconnects, set the place to -1
	 * if place was already zero (i.e. player hasn't lost yet) increase the placeCounter
	 */
	public void voidScore() {
		if(place == 0)
			placeCounter ++;
		place = -1;
		voidScore = true;
	}
	protected void addKill(int sucker) {	
		kills.add(sucker);
	}
	protected void addDeath(int oj) {	
		deaths.add(oj);
	}
	protected int[] getKills() {
		int[] result = new int[kills.size()];
		for(int i = 0; i<kills.size(); i++) {
			result[i] = kills.get(i);
		}
		return result;
	}
	protected int[] getDeaths() {
		int[] result = new int[deaths.size()];
		for(int i = 0; i<result.length; i++) {
			result[i] = deaths.get(i);
		}
		return result;
	}
	protected void setMainScore() {
		gameScore += matchScore;
		matchScore = 0;
		kills.clear();
		deaths.clear();
	}
	protected void setGameScore(int points) {
		gameScore += points;
		reset();
	}
	
}
