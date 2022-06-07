package main;

import static main.ConnectScreen.Select.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import input.KeyBoard;

public class ConnectScreen extends JPanel{
	protected enum Select{
		HOW_TO_PLAY, TYPE_NAME, TYPE_IP, READY, START, HIDE, ERROR;
	}

	private int width, height;

	public Select select = Select.TYPE_NAME;
	public boolean start = false;

	private int bgCount = 0;
	private int tCount = 30;
	private int fadeCount = 0;
	private int fadeCutoff = 40;

	public String name = "", IPAddress = "";
	public String errorMsg = "";
	public static final int MAX_NAME_SIZE = 10;

	private Ellipse2D.Double[] choices = new Ellipse2D.Double[3];
	private Ellipse2D.Double STARt = new Ellipse2D.Double(width*.7, height *.7, 100, 100);
	private Ellipse2D.Double[] stars = new Ellipse2D.Double[200];

	private KeyBoard keys;
	//private int[] pixels;

	public ConnectScreen(KeyBoard keys, int width, int height)
	{
		this.keys = keys;
		this.width = width;
		this.height = height;
		//pixels = new int[width*height];

		setOpaque(false);

		for(int i = 0; i<3; i++)
		{
			choices[i] = new Ellipse2D.Double(0, 0, 10, 10);
		}
		for(int i = 0; i<stars.length; i++)
		{
			int r = (int)(Math.random()*2)+1;
			stars[i] = new Ellipse2D.Double(Math.random()*width-r, Math.random()*height-r, r*2, r*2);
		}
	}
	public void restart(String errMsg)
	{
		bgCount = 0;
		tCount = 40;
		fadeCount = 0;
		select = Select.TYPE_IP;
		start = false;
		IPAddress = "";
		errorMsg = errMsg;
		select = Select.ERROR;
	}

	public void update() {
		if(bgCount == 0)
			this.requestFocus();
		int tRange = (int)Math.toDegrees(Math.PI/7);
		if(select == Select.TYPE_IP && tCount < tRange)
			tCount ++;
		else if(select == Select.HOW_TO_PLAY && tCount > -tRange)
			tCount--;
		else if(select == Select.TYPE_NAME) {
			if(tCount > 0)
				tCount --;
			else if(tCount < 0)
				tCount ++;
		}
		moveStars();
		bgCount += 1;
		if(start)
			fadeCount += 1;
		if(fadeCount > fadeCutoff)
		{
			select = Select.HIDE;
		}

		if(select != Select.HIDE) {
			if(select == Select.ERROR) {
				if(keys.space() || keys.enter() || keys.tab()) {
					select = TYPE_NAME;
					errorMsg = "";
				}
			} else {
				if(select == Select.START)
					start = true;
				else {
					checkMouse();		
					checkKeys();
				}
			}
		}

		repaint();
	}
	public void checkKeys() {
		if(select == TYPE_NAME) {
			name = writeOutString(name, MAX_NAME_SIZE);
		} else if(select == TYPE_IP) {
			IPAddress = writeOutString(IPAddress, 20);
		}

		if(keys.enter() || keys.tab()) {
			if(name != "" && IPAddress == "") {
				select = TYPE_IP;
			}
			else if(name == "" && IPAddress != "") {
				select = TYPE_NAME;
			}
			else if(name != "" && IPAddress != "" && select == READY) {
				select = START;
			} else if(name != "" && IPAddress != "") 
				select = READY;
		}

		if(keys.up() || keys.left()) {
			if(select == TYPE_NAME) {
				select = HOW_TO_PLAY;
			}
			else if(select == TYPE_IP) {
				select = TYPE_NAME;
			}
			else if(select != HOW_TO_PLAY)
				select = TYPE_NAME;
		}
		if(keys.down() || keys.right())
		{
			if(select == TYPE_NAME)
			{
				select = TYPE_IP;
			}
			else if(select == HOW_TO_PLAY)
			{
				select = TYPE_NAME;
			}
			else
				select = TYPE_IP;
		}
	}
	public void checkMouse() {
		if(keys.space()) {
			if(choices[0].contains(keys.mouseV))
			{
				select = HOW_TO_PLAY;
			}
			if(choices[1].contains(keys.mouseV))
			{
				select = TYPE_NAME;
			}
			if(choices[2].contains(keys.mouseV))
			{
				select = TYPE_IP;
			}
			if(STARt.contains(keys.mouseV) && select == READY)
			{
				select = START;
			}
		}
	}
	private String writeOutString(String s, int maxSize)
	{
		Character letter = keys.getChar();
		String addOn = "";

		if(s.length() > maxSize)
			s = s.substring(0, maxSize);
		else if(keys.back() && s.length() > 0) {
			if(keys.ctrl())
				s = "";
			else
				if(s.length() == 1)
					s = "";
				else
					s = s.substring(0, s.length()-1);
		}
		else if (letter != null && letter != KeyEvent.CHAR_UNDEFINED && letter != '\n')
			addOn += letter;

		return s + addOn;
	}
	private void moveStars()
	{
		for(int i = 0; i<stars.length; i++)
		{
			if(i<stars.length/4)
				stars[i].x += .3;
			else if(i<stars.length/2)
				stars[i].x += .225;
			else if(i<stars.length/4*3)
				stars[i].x += .15;
			else
				stars[i].x += .075;
			
			if(stars[i].x > width)
				stars[i].x = 0;
			stars[i].y -= .01;
			if(stars[i].y < 0)
				stars[i].y = height;
		}
	}
	public void render(Graphics gg)
	{
		super.paintComponent(gg);
		Graphics2D g = (Graphics2D) gg;
		//Create the stars in the background
		g.setColor(Color.WHITE);
		for(int i = 0; i<stars.length; i++)
		{
			g.draw(stars[i]);
			g.fill(stars[i]);
		}

		if(select != Select.HIDE)
		{
			//Create Title and planet behind it
			int radius = 800;
			Ellipse2D.Double largePlanet = new Ellipse2D.Double(width/2-radius, -radius*.74-radius, radius*2, radius*2);
			g.setColor(new Color(0, 220, 220)); 
			g.draw(largePlanet);
			g.fill(largePlanet);
			g.setColor(Color.WHITE);
			drawString(g, "Planets", width/2, 50, 120);

			//Create planet and spheres in bottom left corner
			radius = 250;
			Ellipse2D.Double smallPlanet = new Ellipse2D.Double(-radius, height-radius, radius*2, radius*2);
			g.setColor(new Color(0, 190, 190));
			g.draw(smallPlanet);
			g.fill(smallPlanet);

			//creates the small planet options
			int boxWidth = 210;
			//Name
			Color unselected = new Color(100, 100, 100);
			Color selected = new Color(150, 150, 150);
			int distanceFromCenter = radius + 70;
			double position = Math.toRadians(tCount) + Math.PI/4;
			if(select == Select.TYPE_NAME)
			{
				radius = 40;
				g.setColor(selected);
			}
			else
			{
				radius = 30;
				g.setColor(unselected);
			}
			choices[1] = new Ellipse2D.Double(distanceFromCenter*Math.cos(position)-radius, 
					height-distanceFromCenter*Math.sin(position)-radius, radius*2, radius*2);	
			g.draw(choices[1]);
			g.fill(choices[1]);
			g.setColor(new Color(100, 100, 100, 100));
			Rectangle r = new Rectangle((int)choices[1].x+radius+50, (int)choices[1].y+radius-10, boxWidth, 20);
			g.draw(r);
			g.fill(r);

			g.setColor(Color.WHITE);
			drawString(g, "Name", (int)choices[1].x+radius, (int)choices[1].y+radius-5, 20);
			drawStringL(g, name, (int)choices[1].x+radius+55, (int)choices[1].y+radius-5, 20);

			//How to play
			if(select == Select.HOW_TO_PLAY)
			{
				radius = 40;
				g.setColor(selected);
			}
			else
			{
				radius = 30;
				g.setColor(unselected);
			}
			position += Math.PI/7;
			choices[0] = new Ellipse2D.Double(distanceFromCenter*Math.cos(position)-radius, 
					height-distanceFromCenter*Math.sin(position)-radius, radius*2, radius*2);	
			g.draw(choices[0]);
			g.fill(choices[0]);
			g.setColor(Color.WHITE);
			drawString(g, "How", (int)choices[0].x+radius, (int)choices[0].y+radius-15, 15);
			drawString(g, "to", (int)choices[0].x+radius, (int)choices[0].y+radius-2, 10);
			drawString(g, "Play", (int)choices[0].x+radius, (int)choices[0].y+radius+10, 15);

			//IP address
			if(select == Select.TYPE_IP)
			{
				radius = 40;
				g.setColor(selected);
			}
			else
			{
				radius = 30;
				g.setColor(unselected);
			}
			position = position - 2*Math.PI/7;
			choices[2] = new Ellipse2D.Double(distanceFromCenter*Math.cos(position)-radius, 
					height-distanceFromCenter*Math.sin(position)-radius, radius*2, radius*2);	
			g.draw(choices[2]);
			g.fill(choices[2]);
			g.setColor(new Color(100, 100, 100, 100));
			r = new Rectangle((int)choices[2].x+radius+50, (int)choices[2].y+radius-10, boxWidth, 20);
			g.draw(r);
			g.fill(r);
			g.setColor(Color.WHITE);
			drawString(g, "IP", (int)choices[2].x+radius, (int)choices[2].y+radius-15, 20);
			drawString(g, "Address", (int)choices[2].x+radius, (int)choices[2].y+radius+5, 13);		
			drawStringL(g, IPAddress, (int)choices[2].x+radius+55, (int)choices[2].y+radius-5, 20);

			//draws the start button
			if(name != "" && IPAddress != "" && select == Select.READY)
				g.setColor(Color.WHITE);
			else
				g.setColor(new Color(255, 255, 255, 100));
			double startR = 50 + Math.exp((fadeCount)*.18);
			STARt = new Ellipse2D.Double(width*.8-startR, height*.8-startR, startR*2, startR*2);
			g.fill(STARt);
			double k = fadeCount*.5;
			if(k > 55)
				k = 55;
			if(name != "" && IPAddress != "" && select == Select.READY)
				g.setColor(new Color((int)(200+k), (int)(200+k), (int)(200+k)));
			drawString(g, "Start", (int)(width*.8), (int)(height*.8)-8, 30);
		}
		else
		{
			Rectangle fadeIn = new Rectangle(0, 0, width, height);
			int k = (int)(255 - Math.pow((fadeCount-fadeCutoff), 1.7));
			if(k < 0)
				k = 0;
			g.setColor(new Color(255, 255, 255, k));
			g.draw(fadeIn);
			g.fill(fadeIn);

			setFocusable(false);
		}
		//Creates Error Message
		if(errorMsg != "")
		{
			int errHeight = 40;
			Rectangle errBar = new Rectangle(width/2-width/2, height/2-errHeight/2, width, errHeight);
			g.setColor(new Color(200, 200, 200));
			g.draw(errBar);
			g.setColor(new Color(200, 200, 200, 150));
			g.fill(errBar);
			g.setColor(Color.WHITE);
			drawString(g, errorMsg, width/2, height/2-5, 20);
		}
	}
	private void drawString(Graphics2D g, String s, int x, int y, int size)
	{
		Font f = new Font("Verdana", 1, size);
		g.setFont(f);
		FontRenderContext frc = g.getFontRenderContext();
		Rectangle2D r = f.getStringBounds(s, frc);
		r = new Rectangle(x-(int)r.getWidth()/2, y-(int)r.getHeight()/2, (int)r.getWidth(), (int)r.getHeight());
		g.drawString(s, (int)(x-r.getWidth()/2), (int)(y+r.getHeight()/2));
	}
	private void drawStringL(Graphics2D g, String s, int x, int y, int size)
	{
		Font f = new Font("Verdana", 1, size);
		g.setFont(f);
		FontRenderContext frc = g.getFontRenderContext();
		Rectangle2D r = f.getStringBounds(s, frc);
		r = new Rectangle(x-(int)r.getWidth()/2, y-(int)r.getHeight()/2, (int)r.getWidth(), (int)r.getHeight());
		g.drawString(s, (int)(x), (int)(y+r.getHeight()/2));
	}
}
