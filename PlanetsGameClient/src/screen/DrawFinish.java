package screen;

import java.awt.*;
import java.awt.geom.Ellipse2D;

import serialization.ArrayObj;
import serialization.PObject;

public class DrawFinish extends Screen{
	private int finStep;
	private static int[] score;
	private int[][] kills;
	private int[][] deaths;
	private int[][] shotData;
	private int[] confirm;
	private int[] readyStep;

	protected boolean receivedData = false;

	public DrawFinish()
	{
	}
	public void prep()
	{
		readyStep = new int[playerCount];
		for(int i = 0; i<playerCount; i++)
		{
			readyStep[i] = 0;
		}
		System.out.println("preping");
	}
	public void process(PObject obj) {
		finStep = obj.findField("finishStep").getInt();
		if(!receivedData) {
			System.out.println("updating Score");
			ArrayObj[] scoreData = obj.findArray("ScoreData").objects;
			score = new int[scoreData.length];
			kills = new int[scoreData.length][];
			deaths = new int[scoreData.length][];
			shotData = new int[scoreData.length][4];
			confirm = new int[scoreData.length];
			for(int i = 0; i<scoreData.length; i++) {
				score[i] = scoreData[i].findField("score").getInt();
				kills[i] = scoreData[i].findArray("kills").intData;
				deaths[i] = scoreData[i].findArray("deaths").intData;
				shotData[i] = scoreData[i].findArray("shots").intData;
				confirm[i] = scoreData[i].findField("confirm").getInt();
			}
			receivedData = true;
		} else {
			confirm = obj.findArray("confirm").intData;
		}
	}
	public void render(Graphics gg) {	
		Graphics2D g = (Graphics2D) gg;
		renderArrows(g);
		renderInfo(g);
	}
	private void renderArrows(Graphics2D g) {
		int interval = Screen.colorBar;
		int halfSize = height/playerCount;

		for(int i = 0; i<playerCount; i++)
		{
			Color color = playerColor[i*2];
			if(!players.get(i).active) {
				color = playerColor[8];
			}
			if(finStep > 25)
				finStep = 25;
			double max = map(5-score[i], 0, 5, 100, 200);
			int leftX = (int)((width) - map(finStep, 0, 25, 0, max));
			if(finStep < 0) {
				leftX = (int)map(finStep, 0, -30, (width)-max, 302);
			}
			int rightX = leftX + 20;


			int[] x = {leftX, rightX, rightX + 48, rightX, leftX, leftX + 48};
			int[] y = {interval, interval, interval + halfSize/2, interval + halfSize, interval + halfSize, interval + halfSize/2};
			Polygon farBar = new Polygon(x, y, 6);
			x = new int[]{leftX, leftX + 48, leftX, 0, 0};
			y = new int[]{interval, interval + halfSize/2, interval + halfSize, interval + halfSize, interval};
			Polygon shape = new Polygon(x, y, 5);

			g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
			g.fill(shape);
			g.setColor(color);
			g.fill(farBar);

			//Draw Names
			g.setColor(Color.WHITE);
			int center = 180;

			drawString(g, players.get(i).getName(), center, interval + halfSize/2-10, 40);
			//Draw Score
			drawString(g, "+" + score[i], rightX - 20, interval + halfSize/2-10, 40);

			interval += height/playerCount;
		}
	}
	private void renderInfo(Graphics2D g) {
		int interval = 10;
		int halfSize = height/playerCount;

		boolean all2 = true;

		for(int i = 0; i<playerCount; i++)
		{
			double max = map(5-score[i], 0, 5, 400, 500);

			if(confirm[i] == 0)
			{
				all2 = false;
				//Draw Kills
				g.setColor(Color.WHITE);

				drawStringL(g, "Kills", 260, interval + halfSize/2 - 35, 15);
				drawStringL(g, "Deaths", 260, interval + halfSize/2 + 30, 15);
				g.setColor(playerColor[i]);
				g.drawLine(260, interval + halfSize/2, width - 50 -(int)max, interval + halfSize/2);

				int rad = 20;
				int height = 30;
				int spacing = 50;
				int reduceJ = 0;
				for(int j = 0; j<kills[i].length; j++) {
					if(kills[i] != null) {
						g.setColor(playerColor[(kills[i][j - reduceJ])*2]);
						g.fill(new Ellipse2D.Double(325 + (j - reduceJ)*spacing, interval + halfSize/2-rad-height, rad*2, rad*2));
					}
					else
						reduceJ --;
				}
				reduceJ = 0;
				for(int j = 0; j<deaths[i].length; j++)
				{
					if(deaths[i] != null) {
						g.setColor(playerColor[(deaths[i][j - reduceJ])*2]);
						g.fill(new Ellipse2D.Double(320 + (j - reduceJ)*spacing, interval + halfSize/2-rad+height, rad*2, rad*2));
					}
					else
						reduceJ --;
				}
			} else if(confirm[i] == 1) {
				all2 = false;
				//draw shotData
				int fired = shotData[i][0];
				int hits = shotData[i][1];
				int damageDealt = shotData[i][2];
				int damageTaken = shotData[i][3];
				double accuracy = (fired == 0) ? 0:((int)10000.*hits/fired)/100.;
				double dmgRatio = (damageTaken == 0) ? 1:((int)100.*damageDealt/damageTaken)/100.;
				String line1a = "Shots Fired " + fired;
				String line2a = "Shots Hit " + hits;
				String line3a = "Accuracy " + accuracy + "%";
				String line1b = "\t Damage Dealt " + damageDealt;
				String line2b = "\t Damage Received " + damageTaken;
				String line3b = "\t Damage Ratio " + dmgRatio;
				drawStringL(g, line1a, 280, interval + halfSize/2 - 35, 17);
				drawStringL(g, line2a, 280, interval + halfSize/2 - 2, 17);
				drawStringL(g, line3a, 280, interval + halfSize/2 + 30, 17);
				drawStringL(g, line1b, 450, interval + halfSize/2 - 35, 17);
				drawStringL(g, line2b, 450, interval + halfSize/2 - 2, 17);
				drawStringL(g, line3b, 450, interval + halfSize/2 + 30, 17);

			} else if(confirm[i] == 2) {
				g.setColor(Color.WHITE);
				drawStringL(g, "Ready", 400, interval + halfSize/2-10, 40);
			}
			for(int j = 0; j<playerCount; j++) {
				g.setColor(Color.WHITE);
				drawString(g, confirm[i]+"", 10, interval + halfSize/2-5, 20);
			}
			interval += height/playerCount;
		}
	}
}