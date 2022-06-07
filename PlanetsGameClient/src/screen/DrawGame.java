package screen;

import java.awt.*;
import java.awt.geom.Ellipse2D;

import entity.*;
import serialization.ArrayObj;
import serialization.PObject;

public class DrawGame extends Screen{

	private ShooterBoi[] shooterBois;
	private DrawPlanets drawPlanets;
	public static boolean isMapCompressed = false;

	private static final int READY_COUNT= 35;
	private static final int GO_COUNT = 85;
	private int outroShift = 50;

	private int gameStep;
	private double timer;
	private int gamePhase;

	private Ellipse2D.Double[] shots;
	private int[] shotColor;

	public void prep()
	{
		DEntity.scale = scale;
		isMapCompressed = false;
		shooterBois = new ShooterBoi[playerCount];

		for(int i = 0; i<playerCount; i++)
		{
			shooterBois[i] = new ShooterBoi(i, playerColor[i*2]);
		}
	}
	public void process(PObject obj) {
		gameStep = obj.findField("gameStep").getInt();
		timer = obj.findField("timer").getDouble();
		gamePhase = obj.findField("gamePhase").getInt();
		ArrayObj[] playerData = obj.findArray("Players").objects;
		if(shooterBois == null || playerData.length != shooterBois.length) {
			shooterBois = new ShooterBoi[playerCount];
		}
		for(int i = 0; i<playerCount; i++) {
			if(shooterBois[i] == null)
				shooterBois[i] = new ShooterBoi(i, playerColor[i*2]);
			shooterBois[i].
			update(playerData[i]);
		}

		ArrayObj[] planetData = obj.findArray("Level").objects;
		if(!isMapCompressed) {
			drawPlanets = new DrawPlanets(stageNum);
			drawPlanets.process(planetData);
			isMapCompressed = true;
		}
		ArrayObj[] shotData;
		if(obj.findArray("Shots") != null) {
			shotData = obj.findArray("Shots").objects;
			shots = new Ellipse2D.Double[shotData.length];
			shotColor = new int[shots.length];
			for(int i = 0; i<shots.length; i++) {
				double x  = (double)shotData[i].findField("x").getFloat();
				double y = (double)shotData[i].findField("y").getFloat();
				double radius = (double)shotData[i].findField("radius").getFloat();
				shots[i] = new Ellipse2D.Double(x, y, radius, radius);
				shotColor[i] = shotData[i].findField("color").getInt();
			}
		} else {
			shots = new Ellipse2D.Double[] {};
		}
	}
	public void render(Graphics gg) {	
		Graphics2D g = (Graphics2D) gg;
		if(gameStep > READY_COUNT)
		{
			int gp = gamePhase;
			//gamePhase = Integer.parseInt(data[3]);
			if(gp != gamePhase)
			{
				//PlayGame.mc.nextPhase(gamePhase);
			}
		}	
		if(gameStep >= READY_COUNT || gameStep < 0)
		{

			//drawPlanets(g, planetString);

			if(isMapCompressed)
				drawPlanets.draw(g);

			for(int i = 0; i<playerCount; i++) {
				shooterBois[i].render(g);
			}
			renderShots(g);
			renderHud(g);

			g.setColor(Color.WHITE);
			int size = 120;
			if(gameStep < 0)
			{
				if(gameStep < -outroShift)
					drawCloseGame(g, gameStep + outroShift);
			}
			else if(gameStep == READY_COUNT)
			{
				//PlayGame.mc.fightPhase = 0;
			}
			else if(gameStep < GO_COUNT)
			{
				drawString(g, "READY?", width/2, height/2, size);
			}
			else if(gameStep < GO_COUNT + 25) {
				drawString(g, "GO!", width/2, height/2, size);
			}
		}
		else
		{
			g.setColor(Color.BLACK);
			g.fill(new Rectangle(0, 0, width, height));
		}

	}

	private void renderHud(Graphics2D g) {
		//Draw the time at the top of the screen
		g.setColor(Color.WHITE);
		if(timer != 7*60)
		{
			int min = (int)(timer/60);
			int secs = (int)(timer%60);
			int decs = (int)((timer - (int)(timer))*100);

			String sz = "";
			if(secs < 10)
				sz = "0";
			String dz = "";
			if(decs < 10)
				dz = "0";
			if(min != 0)
				drawString(g, min + ":" + sz + secs, Screen.width/2, 40, 60);
			else
				drawString(g, secs + "." + dz + decs, Screen.width/2, 40, 60);
		}
		else
			drawString(g, Character.toString('\u221E'), Screen.width/2, 40, 60);

		for(int i = 0; i<playerCount; i++)
		{
			String name = Screen.players.get(i).getName();
			int x = 0;
			switch(i) {
			case 0: x = (int)(Screen.width*.3); break;
			case 1: x = (int)(Screen.width*.7); break;
			case 2: x = (int)(Screen.width*.1); break;
			case 3: x = (int)(Screen.width*.9); break;
			}
			
			int maxSize = 150;
			//health outline
			int scaledHealth = (int)map(shooterBois[i].health, 0, initHealth, 0, maxSize);
			g.setColor(playerColor[i*2]);
			drawString(g, name, x, 23, 20);
			Rectangle healthBar = new Rectangle(x-maxSize/2, 40, maxSize, 10);
			g.draw(healthBar);
			//health remaining
			g.setColor(playerColor[i*2]);
			healthBar.width = scaledHealth;
			g.fill(healthBar);
			//empty health
			if(scaledHealth < maxSize)
			{
				g.setColor(new Color(playerColor[i*2].getRed(), playerColor[i*2].getGreen(), playerColor[i*2].getBlue(), 150));
				healthBar.width = maxSize-scaledHealth;
				healthBar.x += scaledHealth;
				g.fill(healthBar);
			}
			//Health number
			g.setColor(Color.WHITE);
			drawStringL(g, shooterBois[i].health + "", x-maxSize/2+2, 43, 12);
			//Lives
			g.setColor(playerColor[i*2]);
			for(int l = 0; l<shooterBois[i].lives; l++)
			{
				Ellipse2D.Double e = new Ellipse2D.Double(x-maxSize/2 + l*9, 55, 7, 7);
				g.draw(e);
				g.fill(e);
			}
		}
	}
	private void renderShots(Graphics2D g) {
		if(shots == null || shots.length == 0)
			return;
		for(int i = 0; i<shots.length; i++)
		{
			if(shots[i] != null) {
				double x = shots[i].x*scale;
				double y = shots[i].y*scale;
				double radius = shots[i].width*scale;
				Ellipse2D.Double outer = new Ellipse2D.Double(x-radius, y-radius, radius*2, radius*2);
				double k = .6;
				Ellipse2D.Double inner = new Ellipse2D.Double(x-radius*k, y-radius*k, radius*2*k, radius*2*k);
				g.setColor(playerColor[shotColor[i]*2]);
				g.draw(outer);
				g.fill(outer);
				g.setColor(playerColor[shotColor[i]*2 + 1]);
				g.draw(inner);
				g.fill(inner);
			}
		}
	}
	private void drawCloseGame(Graphics2D g, double step)
	{
		step = Math.abs(step*1.0);
		int angle = 200;
		int leftH = (int)map(step, 0, outroShift, 0, width+angle*2);
		int[] x = {leftH-angle, 0, 0, leftH};
		int[] y = {0, 0, height, height};
		Polygon whiteFade = new Polygon(x, y, 4);
		g.setColor(Color.WHITE);
		g.draw(whiteFade);
		g.fill(whiteFade);

		int interval = 0;
		int halfSize = height/playerCount;
		int leftX = (width + 68); 
		int rightX = leftX + 20;

		for(int i = 0; i<playerCount; i++)
		{
			leftX = (int)((width + 68) - map(step, 0, 50, 0, width + 20 + 48));
			if(leftX < -68)
				leftX = -68;
			rightX = leftX + 20;
			x = new int[]{leftX, leftX + 48, rightX + 48, rightX, rightX + 48, leftX + 48};
			y = new int[]{interval + halfSize/2, interval, interval, interval + halfSize/2, interval + halfSize, interval + halfSize};
			Polygon farBar = new Polygon(x, y, 6);
			x = new int[]{rightX, rightX + 48, width, width, rightX + 48};
			y = new int[]{interval + halfSize/2, interval, interval, interval + halfSize, interval + halfSize};
			Polygon shape = new Polygon(x, y, 5);

			g.setColor(new Color(playerColor[i*2].getRed(), playerColor[i*2].getGreen(), playerColor[2*i].getBlue(), 150));
			g.fill(shape);
			g.setColor(playerColor[i*2]);
			g.fill(farBar);

			interval += height/playerCount;
			step = (step*1.2);
		}
	}
	private void drawPlanets(Graphics2D g, String[] planetString)
	{
		int planetCount = planetString.length;
		for(int i = 1; i<planetCount; i++)
		{
			String[] planetStatus = planetString[i].split(",,");
			float xPos = Float.parseFloat(planetStatus[0]) * scale;
			float yPos = Float.parseFloat(planetStatus[1]) * scale;
			float radius = Float.parseFloat(planetStatus[2]) * scale;
			Ellipse2D.Double p = new Ellipse2D.Double(xPos-radius, yPos-radius, radius*2, radius*2);
			g.setColor(Color.CYAN);
			g.draw(p);
			g.fill(p);

		}	
	}
}
