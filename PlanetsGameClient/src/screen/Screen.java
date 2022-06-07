package screen;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import client.Client;
import client.ClientPlayer;
import graphics.SpriteSheet;
import serialization.PObject;

public class Screen {
	public enum Phase{
		CONNECTING, LOBBY, TRANSITION, GAME, FINISH;
	}
	public static int width;
	public static int height;
	public static float scale = 7.5f;
	public static int[] pixels;
	protected static final Color[] playerColor = {Color.BLUE, new Color(51, 153, 255),
			Color.RED, new Color(255, 0, 183), 
			Color.GREEN, new Color(153, 255, 51), 
			Color.YELLOW, new Color(255, 223, 0),
			new Color(100, 100, 100), new Color(200, 200, 200)};
	public static int colorBar = 10;

	public static Phase phase = Phase.CONNECTING;
	public static int playerCount = 0;

	public static boolean randMap;
	public static int matchPoint;
	public static int matchTime;
	public static int matchLives;
	public static int initHealth;
	
	public static int stageNum;

	public static List<ClientPlayer> players = new ArrayList<>();

	public static Screen lobby;
	public static Screen transition;
	public static Screen game;
	public static Screen finish;

	public int xOffset, yOffset;

	protected Screen() {
	}
	public Screen(int width, int height) {
		Screen.width = width;
		Screen.height = height;
		pixels = new int[width*height];

		lobby = new DrawLobby();
		transition = new DrawTransition();
		game =new DrawGame();
		finish = new DrawFinish();
		
		SpriteSheet.loadAll();
	}
	public static void setPlayers(List<ClientPlayer> pp) {
		players = pp;
		playerCount = players.size();
	}
	public void process(PObject obj) {
		switch(obj.getName()) {
		case "Lobby": 
			lobby.process(obj); 
			if(phase != Phase.LOBBY) {
				phase = Phase.LOBBY; 
			}
			break;
		case "Transition": 
			transition.process(obj); 
			if(phase != Phase.TRANSITION) {
				phase = Phase.TRANSITION; 
				((DrawTransition)transition).prep();
			}
			break;
		case "Game": 
			game.process(obj); 
			if(phase != Phase.GAME) {
				phase = Phase.GAME; 
				((DrawGame)game).prep();
			}			
			break;
		case "Finish":
			if(phase != Phase.FINISH)
				((DrawFinish)finish).receivedData = false;
			finish.process(obj); 
			if(phase != Phase.FINISH) {
				phase = Phase.FINISH; 
				((DrawFinish)finish).prep();
			}			
			break;
		}
		setOffset(0, 0);
	}
	public void clear() {
		for(int i = 0; i<pixels.length; i++) {
			pixels[i] = 0;
		}
	}
	public void update() {
	}
	public void render(Graphics g) {
		if(phase != Phase.CONNECTING) {
			g.setColor(playerColor[Client.userID * 2]);
			g.drawRect(0, 0, width, colorBar);
			g.fillRect(0, 0, width, colorBar);
		}

		switch(phase) {
		case LOBBY: lobby.render(g); break;
		case TRANSITION: transition.render(g); break;
		case GAME: game.render(g); break;
		case FINISH: finish.render(g); break;
		default: break;
		}
	}
	public void setOffset(int xOffset, int yOffset) {
		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}
	protected void drawString(Graphics2D g, String s, int x, int y, int size)
	{
		Font f = new Font("Verdana", 1, size);
		g.setFont(f);
		FontRenderContext frc = g.getFontRenderContext();
		Rectangle2D r = f.getStringBounds(s, frc);
		r = new Rectangle(x-(int)r.getWidth()/2, y-(int)r.getHeight()/2, (int)r.getWidth(), (int)r.getHeight());
		g.drawString(s, (int)(x-r.getWidth()/2), (int)(y+r.getHeight()/2));
	}
	protected void drawStringL(Graphics2D g, String s, int x, int y, int size)
	{
		Font f = new Font("Verdana", 1, size);
		g.setFont(f);
		FontRenderContext frc = g.getFontRenderContext();
		Rectangle2D r = f.getStringBounds(s, frc);
		r = new Rectangle(x-(int)r.getWidth()/2, y-(int)r.getHeight()/2, (int)r.getWidth(), (int)r.getHeight());
		g.drawString(s, (int)(x), (int)(y+r.getHeight()/2));
	}
	protected double map(double x, double aMin, double aMax, double bMin, double bMax)
	{
		return (x-aMin)/(aMax-aMin)*(bMax-bMin)+bMin;
	}
}
