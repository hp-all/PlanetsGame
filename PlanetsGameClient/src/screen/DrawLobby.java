package screen;

import serialization.PObject;
import java.awt.*;

import client.Client;

public class DrawLobby extends Screen{
	
	public static int playerID = 0;
	
	private static int playerControl;
	private static int selected;
	private static int tranStep;
	private static int[] readyStep;
	private static boolean[] playerReady;
	
	private static String[] optionName = {"Map Setting ", "Match Point ", "Match Time ", "Match Lives ", "Start Health "};
	private int midLine = width/16*11;
	private Rectangle optionBox = new Rectangle(midLine - 350/2, 100, 350, 300);
	private int textSize = 24;
	public static int fadeout = 15;
	
	protected static Decoration deco = new Decoration(width, height);
	public DrawLobby()
	{
		playerID = Client.userID;
	}
	public void process(PObject obj) {
		playerID = Client.userID;

		randMap = obj.findField("randomMap").getBoolean();
		matchPoint = obj.findField("matchPoint").getInt();
		matchTime = obj.findField("matchTime").getInt();
		matchLives = obj.findField("matchLives").getInt();
		initHealth = obj.findField("initHealth").getInt();
		playerControl = obj.findField("playerControl").getInt();
		selected = obj.findField("selected").getInt();
		tranStep = obj.findField("tranStep").getInt();
		playerReady = obj.findArray("confirms").boolData;
	}
	public void render(Graphics gg) {
		Graphics2D g = (Graphics2D) gg;
		
		checkSize();
		
		deco.render(g, tranStep);
	    drawPlayers(g);
		drawOptions(g);
	}
	private void checkSize() {
		if(readyStep == null || readyStep.length != playerCount) {
			playerReady = new boolean[playerCount];
			readyStep = new int[playerCount];
			for(int i = 0; i<playerCount; i++) {
				playerReady[i] = false;
				readyStep[i] = 0;
			}
		}
	}
	private void drawPlayers(Graphics2D g)
	{
		checkSize();
		for(int i = 0; i<playerCount; i++)
		{
			if(playerReady[i] && 
					readyStep[i] < 12)
				readyStep[i] ++;
			else if(!playerReady[i] && 
					readyStep[i] > 0)
				readyStep[i] --;
		}
		int interval = Screen.colorBar;
		int leftX = 300;
		int rightX = leftX + 20;
		int halfSize = height/4;
		if(playerCount > 1)
			halfSize = height/playerCount;
		for(int i = 0; i<playerCount; i++) 
		{
			int h = 0;
			if(i < readyStep.length)
				h = (int)(readyStep[i]*4);
			int[] x = {leftX-h*2, rightX-h*2, rightX-h, rightX-h*2, leftX-h*2, leftX-h};
			int[] y = {interval, interval, interval + halfSize/2, interval + halfSize, interval + halfSize, interval + halfSize/2};
			Polygon farBar = new Polygon(x, y, 6);
			x = new int[]{0, leftX-h*2, leftX-h, leftX-h*2, 0};
			y = new int[]{interval, interval, interval + halfSize/2, interval + halfSize, interval + halfSize};
			Polygon shape = new Polygon(x, y, 5);

			g.setColor(new Color(playerColor[i*2].getRed(), playerColor[i*2].getGreen(), playerColor[i*2].getBlue(), 150));
			g.fill(shape);
			g.setColor(playerColor[i*2]);
			g.fill(farBar);


			//Write names and stuff
			g.setColor(Color.WHITE);
			int center = 120 + 24 - h/2;
			drawString(g, players.get(i).getName(), center, interval + halfSize/2-20, 40);
			if(i == playerID && !playerReady[i])
				drawString(g, "Press space to confirm", center, interval + halfSize/2 + 20, 18);
			else if(i < playerReady.length && !playerReady[i])
				drawString(g, "waiting", center, interval + halfSize/2 + 20, 20);
			else
				drawString(g, "ready", center, interval + halfSize/2 + 20, 20);
			if(playerCount > 1)
				interval += height/playerCount;
			else
				interval += height/4;
		}
		if(playerCount < 2) {
			drawString(g, "wait for more players", 140, height/8 * 5, 20);
			for(int i = 0; i<4-playerCount; i++) {
				int h = 0;
				if(tranStep < 1 && -tranStep < leftX)
					h = (int)(leftX + 100*Math.log(Math.abs(tranStep)/(double)fadeout));
				if(h > leftX) {
					h = leftX;
				} else {
					Rectangle shape = new Rectangle(0, interval, leftX-h, height/4);
					g.setColor(new Color(150, 150, 150, 150));
					g.fill(shape);
				}
				Rectangle farBar = new Rectangle(leftX-h, interval, rightX-leftX, height/4);

				g.setColor(new Color(150, 150, 150));
				g.fill(farBar);

				interval += height/4;
			}
		}
	}
	private void drawOptions(Graphics2D g)
	{
		
		int vShift = 0;
		int colorShift = 255;
		if(tranStep < 0)
		{
			vShift = tranStep*-10;
			colorShift = (int)map(-tranStep, 0, fadeout, 0, 255);
			colorShift = 255 - colorShift;
		}
		
		Rectangle backSquare = new Rectangle(optionBox.x, optionBox.y-vShift, optionBox.width, optionBox.height);
		g.setColor(new Color(192, 192, 192, colorShift));
		g.draw(backSquare);
		g.fill(backSquare);
		Rectangle bar = new Rectangle(optionBox.x, optionBox.y-vShift, optionBox.width, 20);
		g.setColor(new Color(64, 64, 64, colorShift));
		g.draw(bar);
		g.fill(bar);
		bar.y = optionBox.y+optionBox.height-20-vShift;
		g.draw(bar);
		g.fill(bar);

		int interval = 47;
		int[] options = {0, matchPoint, 0, matchLives, initHealth};
		for(int i = 0; i<5; i++)
		{			
			int x = midLine + width/16;
			int y = optionBox.y + 48;
			int optionBarShift = optionBox.x/16+7;
			g.setColor(new Color(64, 64, 64, colorShift));
			drawStringL(g, optionName[i], optionBox.x + 15, y + i*interval - vShift, textSize);
			Rectangle optionBar = new Rectangle(
					midLine+optionBarShift, y+i*interval-vShift-9, 
					optionBox.x/12*3, 35);
			g.draw(optionBar);
			g.fill(optionBar);

			g.setColor(new Color(128, 128, 128, colorShift));
			
			int textShiftx = optionBarShift-2;
			
			if(i == 0)
			{
				if(randMap)
					drawString(g, "Random", x+textShiftx, y-vShift+2, 23);
				else
				{
					drawString(g, "Choose", x+textShiftx, y-vShift+2, 23);
				}
			}
			else if(i == 2)
			{
				if(matchTime < 7)
					drawString(g, matchTime + " min", x + textShiftx, y + i*interval - vShift, textSize);
				else
					drawString(g, Character.toString('\u221E'), x + textShiftx, y + i*interval - vShift, textSize);
			}
			else
				drawString(g, options[i]+"", x + textShiftx, y + i*interval - vShift, textSize);
			if(selected == i && tranStep >= 0)
			{
				y -= 9;
				optionBarShift -= 7;
				int[] xx = {x-optionBarShift ,x-optionBarShift-15, x-optionBarShift};
				int[] yy = {y + i*interval, y + i*interval + (int)(35/2), y + i*interval + 35};
				Polygon triangle = new Polygon(xx, yy, 3);
				g.setColor(playerColor[playerControl]);
				g.draw(triangle);
				g.fill(triangle);
				int triangleShift = optionBox.x/12*3 + 11;
				xx = new int[] {xx[0] + triangleShift, xx[1] + triangleShift + 30, xx[2] + triangleShift};
				triangle.xpoints = xx;
				g.draw(triangle);
				g.fill(triangle);
			}
		}
	}
}
