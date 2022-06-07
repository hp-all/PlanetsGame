package entity;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import graphics.PlayerSprite;
import graphics.Sprite;
import screen.Screen;
import serialization.ArrayObj;

public class ShooterBoi extends DEntity{

	private Sprite sprite;
	private Color color;
	private int ID;

	private boolean isOnGround;
	private int walkStep;
	private int hitStep;
	public int health;
	public int lives;
	private double bodyAngle;
	private double headAngle;
	private int charge;

	private static final int MAX_CHARGE = 40;
	private final double radiusHB = (double)Screen.scale * 3;
	private final double imageScale = .001 * radiusHB;
	public double netImageScale = imageScale;
	private double WALK_SPEED = .2;

	private PlayerSprite looks;

	private final Point.Double bodyRec = new Point.Double(2.6*Screen.scale, 2*Screen.scale);
	private final Point.Double armRec = new Point.Double(2.6*Screen.scale, .6*Screen.scale);

	public ShooterBoi(int i, Color c) {
		ID = i;
		color = c;
		looks = new PlayerSprite(i, this);
	}

	public void update(ArrayObj data) {
		scale = Screen.scale;
		x = data.findField("x").getFloat()*scale;
		y = data.findField("y").getFloat()*scale;
		isOnGround = data.findField("onGround").getBoolean();
		walkStep = data.findField("walkStep").getInt();
		hitStep = data.findField("hitStep").getInt();
		health = data.findField("health").getInt();
		lives = data.findField("lives").getInt();
		bodyAngle = data.findField("bodyAngle").getDouble();
		headAngle = data.findField("headAngle").getDouble();
		charge = data.findField("charge").getInt();
	}

	public void render(Graphics gg) {
		Graphics2D g = (Graphics2D) gg;
		float[] positionData = drawIfOffScreen(g, x, y);
		netImageScale = imageScale * positionData[0];
		x = positionData[1];
		y = positionData[2];

		//renderHB(g);
		renderBoi(g);
	}
	public void renderBoi(Graphics2D g) {
		int walkHeight = 0; //raises the player image depending on where it is in the walk cycle
		//walkStep * walkSpeed slows down the walk cycle
		int ws = (int)Math.abs(walkStep * WALK_SPEED) % 8;
		switch(ws) {
		case 0: walkHeight = 0; break;
		case 1: walkHeight = 1; break;
		case 2: walkHeight = 2; break;
		case 3: walkHeight = 3; break;
		case 4: walkHeight = 0; break;
		case 5: walkHeight = 1; break;
		case 6: walkHeight = 2; break;
		case 7: walkHeight = 3; break;//*/
		default: walkHeight = 0; break;
		}
		Vec center = new Vec(x, y);
		Vec walkHeightV = new Vec((float)Math.cos(bodyAngle), (float)Math.sin(bodyAngle)).mul(-walkHeight);
		center.addLocal(walkHeightV);
		double bAngle = Math.toDegrees(bodyAngle);
		double hAngle = Math.toDegrees(headAngle);

		hAngle -= bAngle - 90;
		if(hAngle > 180)
			hAngle -= 360;
		if(hAngle < -180)
			hAngle += 360;//*/
		//Draw Body
		{
			if(walkStep > 0 || (walkStep == 0 && hAngle >= -90 && hAngle < 90))
				looks.faceLeft();
			else looks.faceRight();

			double ba = bodyAngle - Math.PI/2;
			if(walkStep == 0) {
				if(isOnGround)
					looks.renderIdle(g, center.x, center.y, ba);
				else
					looks.renderJump(g, center.x, center.y, ba);
			} else looks.renderWalk(g, ws, center.x, center.y, ba);
		} 

		//Draw Arm Charge
		renderArm(g, center.x, center.y, headAngle);

		//Draw Head
		{
			if(hAngle >= -90 && hAngle < 90)
				looks.faceLeft();
			else
				looks.faceRight();
			if(hitStep % 7 >= 4 || health <= 0)
				looks.renderHurtHead(g, center.x, center.y, headAngle);
			else
				looks.renderHead(g, center.x, center.y, headAngle);
		}
	}
	private float[] drawIfOffScreen(Graphics2D g, float x, float y)
	{
		Point.Double pos = new Point.Double(x, y);
		boolean draw = false;

		float newX = 0;
		float newY = 0;

		Rectangle.Double screenBox = new Rectangle.Double(-radiusHB, -radiusHB, Screen.width+radiusHB, Screen.height+radiusHB);

		int borderSpace = 40;
		int topSpace = 70;

		if(x < screenBox.x || x > screenBox.width) {
			newX = Screen.width - borderSpace;
			if(x+bodyRec.x < 0)
				newX = borderSpace;
			screenBox.y = topSpace;
			screenBox.height = Screen.height-borderSpace;

			draw = true;
		} else {
			newX = x;
		}
		if(y < screenBox.y || y > screenBox.height) {
			newY = Screen.height - borderSpace;
			if(y+bodyRec.y < 0)
				newY = topSpace;
			if(!draw) {
				screenBox.x = borderSpace;
				screenBox.width = Screen.width-borderSpace;

				if(x < screenBox.x || x > screenBox.width) {
					newX = Screen.width - borderSpace;
					if(x+bodyRec.x < 0)
						newX = borderSpace;
				} else {
					newX = x;
				}
				draw = true;
			}
		} else {
			newY = y;
		}

		if(draw)
		{
			Ellipse2D.Double e = new Ellipse2D.Double(newX-35, newY-35, 70, 70);
			g.setColor(Color.WHITE);
			g.draw(e);
			g.fill(e);
			e = new Ellipse2D.Double(newX-32, newY-32, 64, 64);
			g.setColor(Color.BLACK);
			g.draw(e);
			g.fill(e);

			double x2 = Math.pow(newX - pos.x, 2);
			double y2 = Math.pow(newY - pos.y, 2);
			double dist = Math.sqrt(x2 + y2);
			if(dist > 400)
				dist = 400;
			float scale = (float)map(dist, 0, 400, 1, .4);
			return new float[] {scale, newX, newY};
		}
		else
			return new float[] {1, x, y};

	}
	private void renderHB(Graphics2D g)
	{
		/*g.setColor(Color.GRAY);
		double detectorSize = .8*scale;
		Ellipse2D.Double e = new Ellipse2D.Double(x-radiusHB-detectorSize, y-radiusHB-detectorSize, 
				radiusHB*2+detectorSize*2, radiusHB*2+detectorSize*2);
		g.draw(e);
		g.fill(e);//*/
		g.setColor(Color.WHITE);
		Ellipse2D.Double physicsHB = new Ellipse2D.Double(x-radiusHB, y-radiusHB, radiusHB*2, radiusHB*2);
		g.draw(physicsHB);
		g.fill(physicsHB);
	}
	private void renderBox(Graphics2D g) {
		Rectangle.Double body = new Rectangle.Double(0, 0, bodyRec.x*2, bodyRec.y*2);
		AffineTransform at = new AffineTransform();
		at.translate(x-bodyRec.x, y-bodyRec.y);
		at.rotate(bodyAngle, bodyRec.x, bodyRec.y);
		//at.scale(scale, scale);
		Shape rotatedBody = at.createTransformedShape(body);
		if(hitStep%8 >= 4 || health <= 0)
			g.setColor(Color.WHITE);
		else
			g.setColor(color);
		g.draw(rotatedBody);
		g.fill(rotatedBody);

		renderArm(g, x, y, headAngle);
	}
	private void renderArm(Graphics2D g, float x, float y, double angle) {
		g.setColor(Color.ORANGE);
		Rectangle drawArm = new Rectangle(0, 0, (int)(armRec.x), (int)(armRec.y));
		AffineTransform at = new AffineTransform();
		at.translate(x, y-armRec.y/2);
		at.rotate(angle, 0, armRec.y/2);
		Shape rotatedArm = at.createTransformedShape(drawArm);
		g.draw(rotatedArm);
		g.fill(rotatedArm);

		if(charge > 0)
		{
			g.setColor(Color.YELLOW);
			Rectangle chargeRec = new Rectangle(0, 0, (int)map(charge, 0, MAX_CHARGE, 0, (int)(armRec.x)), (int)(armRec.y));
			at = new AffineTransform();
			at.translate(x, y-armRec.y/2);
			at.rotate(angle, 0, armRec.y/2);
			Shape rotatedCharge = at.createTransformedShape(chargeRec);
			g.draw(rotatedCharge);
			g.fill(rotatedCharge);
		}
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
	}
}
