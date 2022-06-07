package entity;

/*
 * This class is not used in the main program
 */

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

import graphics.Sprite;
import main.SocketScreen;

public class DrawPlayer extends DEntity{
	private int radiusHB = 3;
	private Color color;
	private int maxCharge;
	private Point bodyDim = new Point(26, 20);
	private Point arm = new Point(26, 6);
	private Point screen;

	private double imageScale = .008, walkSpeed = .15;

	public Sprite sprite;
	private String folder = "";
	private BufferedImage head;
	private BufferedImage walkCycle[] = new BufferedImage[8];
	private BufferedImage idle;
	private BufferedImage jump;
	private BufferedImage hurt = toBufferedImage("/Hurt.PNG");

	public DrawPlayer(int i, Color c)
	{
		switch(i)
		{
		case 0: folder = "/blue/"; break;
		case 1: folder = "/red/"; break;
		case 2: folder = "/green/"; break;
		case 3: folder = "/purp/"; break;
		default: folder = "/blue/"; break;
		}

		loadImages();

		color = c;
		screen = SocketScreen.screen;
		initParams();
	}
	private void initParams()
	{
		maxCharge = 20;

		radiusHB *= scale;
		imageScale *= radiusHB;
		x = 0;
		y = 0;
	}
	private void loadImages()
	{
		head = toBufferedImage(folder + "Head.PNG");
		idle = toBufferedImage(folder + "Idle.PNG");
		jump = toBufferedImage(folder + "Jump.PNG");
		for(int i = 0; i<walkCycle.length; i++)
		{
			walkCycle[i] = toBufferedImage(folder + "Walk" + (i+1) + ".PNG");
		}
	}
	public void update(Graphics2D g, float x, float y, boolean isTouchingGround, int walkStep, int hitStep, int health, double bAngle, double hAngle, int c)
	{
		double[] pos = drawIfOffScreen(g, x, y); 
		double s = pos[0];
		x = (float)pos[1];
		y = (float)pos[2];

		//drawHB(g, s, xVal, yVal);
		//drawBox(g, s, xVal, yVal, hitStep, health, bAngle, hAngle, c);
		drawImages(g, s, x, y, isTouchingGround, walkStep, hitStep, health, bAngle, hAngle, c);
	}
	private void drawHB(Graphics2D g, double s, float xVal, float yVal)
	{
		g.setColor(Color.WHITE);
		Ellipse2D.Double e = new Ellipse2D.Double(xVal*scale-radiusHB*s, yVal*scale-radiusHB*s, radiusHB*2*s, radiusHB*2*s);
		g.draw(e);
		g.fill(e);
	}
	private void drawImages(Graphics2D g, double s, float xVal, float yVal, boolean isTouchingGround, int walkStep, int hitStep, int health, double bAngle, double hAngle, int c)
	{
		int offY = 0;
		AffineTransform at = new AffineTransform();
		//slows down the walk cycle
		int ws = (int)Math.abs(walkStep * walkSpeed) % 8;
		//raises the player image depending on where it is in the walk cycle
		if(ws == 0 || ws == 4)
			offY = 0;
		else if(ws == 1 || ws == 5)
			offY = 1;
		else if(ws == 2 || ws == 6)
			offY = 4;
		else if(ws == 3 || ws == 7)
			offY = 3;
		Vec center = new Vec(xVal*scale, yVal*scale);
		Vec up = new Vec((float)Math.cos(bAngle), (float)Math.sin(bAngle)).mul(-offY);
		center.addLocal(up);
		
		double bodyAngle = Math.toDegrees(bAngle);
		double headAngle = Math.toDegrees(hAngle);
		
		headAngle -= bodyAngle - 90;
		if(headAngle > 180)
			headAngle -= 360;
		if(headAngle < -180)
			headAngle += 360;
		//Draw Body
		{
			int width = (int)(idle.getWidth()/2*imageScale*s);
			int height = (int)(idle.getHeight()/2*imageScale*s);
			at.rotate(bAngle-Math.PI/2, center.x, center.y);
						
			if(walkStep > 0 || (walkStep == 0 && headAngle >= -90 && headAngle < 90))
			{
				at.translate(center.x-width, center.y-height);
				at.scale(imageScale*s, imageScale*s);
			}
			else
			{
				at.translate(center.x+width, center.y-height);
				at.scale(-imageScale*s, imageScale*s);
			}
			if(walkStep == 0) {
			    if(isTouchingGround)
			        g.drawImage(idle, at, null);
			    else
			        g.drawImage(jump, at, null);
			} else {
				g.drawImage(walkCycle[ws], at, null);
			}
		} 
		
		//Draw Arm Charge
		g.setColor(Color.ORANGE);
        Rectangle drawArm = new Rectangle(0, 0, (int)(arm.x*s), (int)(arm.y*s));
        at = new AffineTransform();
        at.translate(center.x, center.y-arm.y/2*s);
        at.rotate(hAngle, 0, arm.y/2*s);
        Shape rotatedArm = at.createTransformedShape(drawArm);
        g.draw(rotatedArm);
        g.fill(rotatedArm);

        if(c > 0)
        {
            g.setColor(Color.YELLOW);
            Rectangle charge = new Rectangle(0, 0, (int)map(c, 0, maxCharge, 0, (int)(arm.x*s)), (int)(arm.y*s));
            at = new AffineTransform();
            at.translate(center.x, center.y-arm.y/2*s);
            at.rotate(hAngle, 0, arm.y/2*s);
            Shape rotatedCharge = at.createTransformedShape(charge);
            g.draw(rotatedCharge);
            g.fill(rotatedCharge);
        }
        
		at = new AffineTransform();
		//Draw Head
		{
			int width = (int)(head.getWidth()/2*imageScale*s);
			int height = (int)(head.getHeight()/2*imageScale*s);
			
			at.rotate(hAngle, center.x, center.y);
			if(headAngle >= -90 && headAngle < 90)
			{
				at.translate(center.x - width, center.y - height);
				at.scale(imageScale*s, imageScale*s);
			}
			else
			{
				at.translate(center.x - width, center.y + height);
				at.scale(imageScale*s, -imageScale*s);
			}
			if(hitStep % 7 >= 4 || health <= 0)
			    g.drawImage(hurt, at, null);
			else
			    g.drawImage(head, at, null);
		}
	}
	private void drawBox(Graphics2D g, double s, float xVal, float yVal, int hitStep, int health, double bAngle, double hAngle, int c)
	{
		Rectangle.Double body = new Rectangle.Double(0, 0, bodyDim.x*2, bodyDim.y*2);
		AffineTransform at = new AffineTransform();
		at.translate(xVal*scale-bodyDim.x*s, yVal*scale-bodyDim.y*s);
		at.rotate(bAngle, bodyDim.x*s, bodyDim.y*s);
		at.scale(s, s);
		Shape rotatedBody = at.createTransformedShape(body);
		if(hitStep%8 >= 4 || health <= 0)
			g.setColor(Color.WHITE);
		else
			g.setColor(color);
		g.draw(rotatedBody);
		g.fill(rotatedBody);

		g.setColor(Color.ORANGE);
		Rectangle drawArm = new Rectangle(0, 0, (int)(arm.x*s), (int)(arm.y*s));
		at = new AffineTransform();
		at.translate(xVal*scale, yVal*scale-arm.y/2*s);
		at.rotate(hAngle, 0, arm.y*s);
		Shape rotatedArm = at.createTransformedShape(drawArm);
		g.draw(rotatedArm);
		g.fill(rotatedArm);

		if(c > 0)
		{
			g.setColor(Color.YELLOW);
			Rectangle charge = new Rectangle(0, 0, (int)map(c, 0, maxCharge, 0, (int)(arm.x*s)), (int)(arm.y*s));
			at = new AffineTransform();
			at.translate(xVal*scale, yVal*scale-arm.y/2*s);
			at.rotate(hAngle, 0, arm.y/2*s);
			Shape rotatedCharge = at.createTransformedShape(charge);
			g.draw(rotatedCharge);
			g.fill(rotatedCharge);
		}
	}
	private double[] drawIfOffScreen(Graphics2D g, double x, double y)
	{
		x *= scale;
		y *= scale;
		Point.Double pos = new Point.Double(x, y);
		boolean draw = false;

		double xx = 0;
		double yy = 0;
		int spacex = 40;
		int spacey = 70;
		if(x-bodyDim.x > screen.x || x+bodyDim.x < 0)
		{
			xx = screen.x - spacex;
			if(x+bodyDim.x < 0)
				xx = spacex;

			if(y+bodyDim.x < spacex)
				yy = spacex;
			else if(y - bodyDim.x > screen.y)
				yy = screen.y - spacey;
			else
				yy = y;

			draw = true;
		}
		else if(y+bodyDim.x < 0 || y-bodyDim.x > screen.y)
		{
			yy = spacex;
			if(y-bodyDim.x > screen.y)
				yy = screen.y-spacey;
			xx = x;
			draw = true;
		}
		if(draw && ((xx>screen.x-spacex || xx<spacex) && (yy>screen.y-spacey || yy<spacex)))
		{
			if(xx<spacex)
				xx = spacex;
			else if(xx > screen.x - spacex)
				xx = screen.x-spacex;

			if(yy<spacex)
				yy = spacex;
			else if(yy > screen.y - spacey)
				yy = screen.y - spacey;

			draw = true;
		}
		if(draw)
		{
			Ellipse2D.Double e = new Ellipse2D.Double(xx-35, yy-35, 70, 70);
			g.setColor(Color.WHITE);
			g.draw(e);
			g.fill(e);
			e = new Ellipse2D.Double(xx-32, yy-32, 64, 64);
			g.setColor(Color.BLACK);
			g.draw(e);
			g.fill(e);

			double x2 = Math.pow(xx - pos.x, 2);
			double y2 = Math.pow(yy - pos.y, 2);
			double dist = Math.sqrt(x2 + y2);
			if(dist > 400)
				dist = 400;
			return new double[] {map(dist, 0, 400, 1, .4), xx/scale, yy/scale};
		}
		else
			return new double[] {1, x/scale, y/scale};

	}
	private class Vec
	{
		public float x = 0;
		public float y = 0;
		private Vec(float xx, float yy)
		{
			x = xx;
			y = yy;
		}
		private Vec()
		{
			this(0,0);
		}
		private Vec(double angle)
		{
			x = (float)Math.cos(angle);
			y = (float)Math.sin(angle);
		}
		private void addLocal(Vec a)
		{
			x += a.x;
			y += a.y;
		}
		private Vec mul(float m)
        {
        	return new Vec(x*m, y*m);
        }
        /*private float getMag()
        {
        	return (float)Math.sqrt((double)(x*x) + (double)(y*y));
        }
        private Vec add(Vec a)
        {
        	return new Vec(x + a.x, y + a.y);
        }
        private Vec sub(Vec s)
		{
			return new Vec(x - s.x, y - s.x);
		}
		private void subLocal(Vec s)
		{
			x -= s.x;
			y -= s.y;
		}
		private void mulLocal(float m)
		{
			x *= m;
			y *= m;
		}
		private Vec norm()
		{
			return new Vec(x/getMag(), y/getMag());
		}
		private void normLocal()
		{
			float h = getMag();
			x/=h;
			y/=h;
		}
		private double getAngle()
		{
			return Math.atan2(y, x);
		}
		private void draw(Graphics2D g, int xx, int yy)
		{
			g.drawLine(xx, yy, xx+(int)x, yy+(int)y);
		}//*/
	}
}
