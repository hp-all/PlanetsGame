package entity;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import graphics.*;
import screen.Screen;

public abstract class DEntity {
	public boolean removed = false;
	public float x, y;
	

	public static float scale;

	public void update() {
	}
	public void render(Screen screen) {
	}
	public void remove() {
		//Remove entity from the game
		removed = true;
	}
	protected double map(double x, double aMin, double aMax, double bMin, double bMax)
	{
		return (x-aMin)/(aMax-aMin)*(bMax-bMin)+bMin;
	}
	public static BufferedImage toBufferedImage(String s)
	{
		try {
			BufferedImage bif = ImageIO.read(DEntity.class.getResource(s));
			return bif;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
