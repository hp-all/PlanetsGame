package screen;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import entity.DrawPlayer;
import main.PlayGame;
import serialization.PObject;

public class DrawTransition extends Screen{
	private static int playerID;
	private int tranStep;
	private int playerControl;
	private boolean[] confirms;
	private boolean[] goBack;
	private int[] score;
	private boolean showWinner;
	
	private static int[] readyStep;
	
	private Decoration deco;
	private int midLine = width/16*11;
	private Rectangle optionBox = new Rectangle(midLine - 350/2, 0, 350, 230);

	
	private BufferedImage[] levels = new BufferedImage[4];
	private BufferedImage[] victories = new BufferedImage[4];
	public DrawTransition()
	{
		optionBox.y = height/2 - optionBox.height/2;
		
	    for(int i = 0; i<levels.length; i++) {
	        levels[i] = DrawPlayer.toBufferedImage("/levels/Level" + (i+1) + ".PNG");
	    }
	    victories[0] = DrawPlayer.toBufferedImage("/victory/BlueWin.PNG");
	    victories[1] = DrawPlayer.toBufferedImage("/victory/RedWin.PNG");
	    victories[2] = DrawPlayer.toBufferedImage("/victory/BlueWin.PNG");
	    victories[3] = DrawPlayer.toBufferedImage("/victory/BlueWin.PNG");
	    
		deco = DrawLobby.deco;
	}
	public void prep()
	{
		confirms = new boolean[playerCount];
		goBack = new boolean[playerCount];
		score = new int[playerCount];
		readyStep = new int[playerCount];
		playerID = DrawLobby.playerID;
		for(int i = 0; i<playerCount; i++)
		{
			confirms[i] = false;
			goBack[i] = false;
			readyStep[i] = 0;
			score[i] = 0;
		}
		deco = DrawLobby.deco;
	}
	public void process(PObject obj) {
		tranStep = obj.findField("tranStep").getInt();
		stageNum = obj.findField("stageNum").getInt();
		randMap = obj.findField("randMap").getBoolean();
		playerControl = obj.findField("playerControl").getInt();
		confirms = obj.findArray("confirms").boolData;
		goBack = obj.findArray("goBack").boolData;
		score = obj.findArray("score").intData;
		showWinner = obj.findField("showWinner").getBoolean();	
		
		if(playerCount == 0) {
			playerCount = confirms.length;
		}
	}
	public void render(Graphics gg)
	{
		Graphics2D g = (Graphics2D) gg;

		if(!showWinner){
			deco.render(g, tranStep);
			drawMatchInfo(g);
			drawPlayers(g);
		}
		else
			drawWinner(g);
		if(tranStep < 0)
		{
			Rectangle blackout = new Rectangle(0, 0, width, height);
			int k = (int)map(Math.abs(tranStep), 0, 20, 0, 255);
			if(k > 255)
				k = 255;
			if(k < 0)
				k = 0;
			g.setColor(new Color(0, 0, 0, k));
			g.draw(blackout);
			g.fill(blackout);
		}

	}
	public void drawPlayers(Graphics2D g)
	{
		int interval = Screen.colorBar;
		int leftX = 300-46*2;
		int rightX = leftX + 20;
		int halfSize = height/playerCount;

		for(int i = 0; i<playerCount; i++)
		{
			Color color = playerColor[i*2];
			if(!PlayGame.user.isPlayerActive(i))
				color = playerColor[4*2];
			
			if(readyStep[i] < 20 && readyStep[i] != 0)
				readyStep[i] ++;
			if(confirms[i] && readyStep[i] == 0)
				readyStep[i] = 1;
			else if(!confirms[i] && readyStep[i] == 20)
				readyStep[i] = -20;
			int h = 0;
			if(readyStep[i] >= 0)
				h = (int)(Math.exp((readyStep[i]-20)/5.0)*(width+100))-21;
			else
				h = readyStep[i]*15;

			int[] x = {leftX+h, rightX+h, rightX+48+h, rightX+h, leftX+h, leftX+48+h};
			int[] y = {interval, interval, interval + halfSize/2, interval + halfSize, interval + halfSize, interval + halfSize/2};
			Polygon farBar = new Polygon(x, y, 6);
			x = new int[]{h-48, leftX+h, leftX+48+h, leftX+h, h-48, h};
			y = new int[]{interval, interval, interval + halfSize/2, interval + halfSize, interval + halfSize, interval + halfSize/2};
			Polygon shape = new Polygon(x, y, 6);

			g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
			g.fill(shape);
			g.setColor(color);
			g.fill(farBar);

			if(confirms[i])
			{
				int w = rightX - leftX;
				if(h > 48)
					h = 48 + w;
				x = new int[]{h-w - 48, h-48, h, h-48, h-w-48, h-w};
				y = new int[]{interval, interval, interval + halfSize/2, interval + halfSize, interval + halfSize, interval + halfSize/2};
				farBar = new Polygon(x, y, 6);

				x = new int[]{h-48-w, h-w, h-48-w};
				y = new int[]{interval, interval + halfSize/2, interval + halfSize};
				shape = new Polygon(x, y, 3);

				g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
				g.fill(shape);
				g.setColor(color);
				g.fill(farBar);
			}


			//Draw Names and Stuff
			g.setColor(Color.WHITE);
			int center = 120;
			drawString(g, players.get(i).getName(), center, interval + halfSize/2-20, 40);
			if(i == playerID && !confirms[i])
				drawString(g, "Press space to ready up", center, interval + halfSize/2 + 20, 15);
			else if(!PlayGame.user.isPlayerActive(i))
				drawString(g, "player is inactive", center, interval + halfSize/2 + 20, 20);
			else if(!confirms[i])
				drawString(g, "waiting", center, interval + halfSize/2 + 20, 20);
			else
				drawString(g, "ready", center, interval + halfSize/2 + 20, 20);
			if(!confirms[i])
				drawString(g, score[i] + "", 232+h, interval + halfSize/2-6, 30);
			else
				drawString(g, score[i] + "", -44+h, interval + halfSize/2-6, 30);
			interval += height/playerCount;

			//draw go back bars
			Rectangle thisBar = new Rectangle(width/16*12+25 + 200/playerCount*i, 27, 200/playerCount, 10);
			g.setColor(Color.WHITE);
			g.draw(thisBar);
			if(goBack[i])
			{
				g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 200));
				g.fill(thisBar);
			}
			else
			{
				g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
				g.fill(thisBar);
			}
		}
		g.setColor(Color.WHITE);
		drawString(g, "Press backspace to go back", width/16*12+25 + 100, 15, 12);
		g.draw(new Rectangle(970, 50, 200, 10));
	}
	private void drawWinner(Graphics2D g)
	{
		int winner = 0;
		int maxScore = 0;
		for(int i = 0; i<score.length; i++){
			if(score[i] > maxScore){
				maxScore = score[i];
				winner = i;
			}
		}
		g.setColor(playerColor[winner]);
        double s =1.2;
        int width = (int)(victories[winner].getWidth()*s);
		drawString(g, players.get(winner).getName() + " Wins!", width-width/2, height/2-30, 70);

		AffineTransform at = new AffineTransform();
        at.scale(s, s);
        at.translate(width-width/s*1.51, 0);
        g.drawImage(victories[winner], at, null);
        
		for(int i = 0; i<playerCount; i++){
			if(confirms[i])
				g.setColor(playerColor[i*2]);
			else
				g.setColor(Color.WHITE);
			Ellipse2D.Double e = new Ellipse2D.Double(100 + 400/playerCount*i, 600, 50, 50);
			g.draw(e);
			g.fill(e);
		}

	}
	private void drawMatchInfo(Graphics2D g) {
		int vShift = 0;
		int colorShift = 255;
		if(tranStep < 15 && tranStep >= 0)
		{
			vShift = (15-tranStep)*-10;
			colorShift = (int)map(tranStep, 0, 15, 0, 255);
		}
		int middle = 788;
		int width = 350;
		Rectangle backSquare = new Rectangle(optionBox.x, optionBox.y-vShift, 
				optionBox.width, optionBox.height);
		g.setColor(new Color(192, 192, 192, colorShift));
		g.draw(backSquare);
		g.fill(backSquare);
		Rectangle bar = new Rectangle(optionBox.x, optionBox.y-vShift, 
				optionBox.width, 20);
		g.setColor(new Color(64, 64, 64, colorShift));
		g.draw(bar);
		g.fill(bar);
		bar.y = optionBox.y+optionBox.height-20-vShift;
		g.draw(bar);
		g.fill(bar);


		vShift += 70;
		g.setColor(new Color(64, 64, 64, colorShift));
		Rectangle optionBar = new Rectangle(midLine-25, 257 - vShift, 50, 20);
		g.draw(optionBar);
		g.fill(optionBar);
		//g.setColor(new Color(128, 128, 128, k));
		g.setColor(new Color(192, 192, 192, colorShift));
		drawString(g, stageNum + "", midLine, 262 - vShift, 20);
		g.setColor(new Color(64, 64, 64, colorShift));
		drawString(g, "Stage", midLine, 240 - vShift, 20);
		
		AffineTransform at = new AffineTransform();
		at.scale(.13, .13);
		BufferedImage b = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR).filter(levels[stageNum-1],  null);
		at = new AffineTransform();
		at.translate(midLine-b.getWidth()/2, 346-b.getHeight()/2-vShift);
		g.drawImage(new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR).filter(b, null), 0, 0, null);
		
		if(!randMap) 
		{
			int space = 40;
			vShift += 16;
			int[] xx = {midLine-space, midLine-space-15, midLine-space};
			int[] yy = {257 - vShift, 257 + (int)(35/2) - vShift, 257 + 35 - vShift};
			Polygon triangle = new Polygon(xx, yy, 3);
			g.setColor(playerColor[playerControl]);
			g.draw(triangle);
			g.fill(triangle);
			space += 0;
			xx = new int[] {xx[0] + space*2, xx[1] + space*2 + 30, xx[2] + space*2};
			triangle.xpoints = xx;
			g.draw(triangle);
			g.fill(triangle);
		}
	}
}
