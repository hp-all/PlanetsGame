package graphics;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import entity.ShooterBoi;
import screen.Screen;

public class PlayerSprite extends Sprite{
	private static float gameScale;
	
	public ShooterBoi player;
	public double imageScale;
	
	private static Sprite hurt;
	private Sprite head;
	private Sprite idle;
	private Sprite jump;
	private Sprite[] walk;
	
	private boolean direction = true; //true = right
	
		
	public PlayerSprite(int color, ShooterBoi player) {
		super(color);
		Sprite[] stills = loadSpriteArray(WIDTH, 0, 3, sheet);
		head = stills[0];
		idle = stills[1];
		jump = stills[2];
		walk = loadSpriteArray(WIDTH, 3, 11, sheet);
		hurt = new Sprite(0, 0, SpriteSheet.hurt);
		gameScale = Screen.scale;
		this.player = player;
		this.imageScale = player.netImageScale;
	}
	public void renderHead(Graphics2D g, double x, double y, double angle) {
		renderHead(g, head.image, x, y, angle);
	}
	public void renderHurtHead(Graphics2D g, double x, double y, double angle) {
		renderHead(g, hurt.image, x, y, angle);
	}
	private void renderHead(Graphics2D g, BufferedImage image, double x, double y, double angle) {
		this.imageScale = player.netImageScale;

		AffineTransform at = new AffineTransform();
		int width = (int)(image.getWidth()/2*imageScale*(double)gameScale);
		int height = (int)(image.getHeight()/2*imageScale*(double)gameScale);
		at.rotate(angle, x, y);
		int dVal = (direction)?-1:1;
		at.translate(x-width, y-dVal*height);
		at.scale(imageScale*(double)gameScale, dVal*imageScale*(double)gameScale);
		
		g.drawImage(image, at, null);
	}
	public void renderIdle(Graphics2D g, double x, double y, double angle) {
		renderBody(g, idle.image, x, y, angle);
	}
	public void renderJump(Graphics2D g, double x, double y, double angle) {
		renderBody(g, jump.image, x, y, angle);
	}
	public void renderWalk(Graphics2D g, int i, double x, double y, double angle) {
		renderBody(g, walk[i].image, x, y, angle);
	}
	private void renderBody(Graphics2D g, BufferedImage image, double x, double y, double angle) {
		this.imageScale = player.netImageScale;

		AffineTransform at = new AffineTransform();
		int width = (int)(image.getWidth()/2*imageScale*(double)gameScale);
		int height = (int)(image.getHeight()/2*imageScale*(double)gameScale);
		at.rotate(angle, x, y);
		int dVal = (direction)?-1:1;
		at.translate(x-dVal*width, y-height);
		at.scale(dVal*imageScale*(double)gameScale, imageScale*(double)gameScale);
		
		g.drawImage(image, at, null);
	}
	public void faceLeft() {
		direction = false;
	}
	public void faceRight() {
		direction = true;
	}
}
