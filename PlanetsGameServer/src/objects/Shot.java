package objects;

import java.util.*;

import org.jbox2d.common.Vec2;

import main.GameEngine;
import serialization.ArrayObj;
import serialization.PField;

public class Shot extends DynamicEntity{
	protected static final int MAX_SHOT_COUNT = 99;
	private static int shotCount = 0;

	private static float MINIMUM_SIZE = .1f;
	private static float BOUNCE_REDUCE = .5f;
	private static float ROLL_REDUCE = .5f;

	public static List<Shot> shots = new ArrayList<Shot>();
	
	public boolean alreadyOnGround = false;
	private int freshCount = 1;

	private Vec2 initMove = new Vec2();
	private int playerID;
	private int shotID;
	
	public Shot(Vec2 pos, float charge, Vec2 armVec, int ID) {
		//Radius, density, friction
		super("shot," + ID + "," + shotCount, pos, 
				(float)(1+(charge-1)*.05), 2f, .2f, 0, false);
		fixture.setRestitution(.5f);
		initMove = armVec.clone();
		playerID = ID;
		shotID = shotCount;
		shotCount ++;
	
		gravityMultiplier = 3f;
		//initMove = speedInit;
	}
	public static void addShot(Vec2 pos, float charge, Vec2 armVec, int ID) {
		shots.add(new Shot(pos, charge, armVec, ID));
		GameEngine.scoreBoard.addShot(ID);
	}
	public static Shot getShot(int i) {
		if(i < shots.size())
			return shots.get(i);
		return null;
	}
	public void move()
	{
		MINIMUM_SIZE = .9f;
		BOUNCE_REDUCE = .5f;
		ROLL_REDUCE = .5f;
		
		if(freshCount > 0)
		{
			gravityMultiplier = 30/(center.getMass()*.5f);

			initMove.normalize();
			initMove.mulLocal(50000*center.getMass());
			center.applyForceToCenter(initMove);
			freshCount --;
		}
		else 
		{
			calcGravity();
			center.applyForceToCenter(sumForce);
			
			if(getPos().x < 0 || getPos().x > GameEngine.width || getPos().y < 0 || getPos().y > GameEngine.height) {
				if(sumForce.length() < 1.5f)
					awaitDeath();
			}
		}
	}
	public void roll()
	{
		if(alreadyOnGround) {
			radius -= ROLL_REDUCE;
			setRadius();
		} else alreadyOnGround = true;
	}
	public void bounce() {
		alreadyOnGround = false;
		radius -= BOUNCE_REDUCE;
		setRadius();
	}
	private void setRadius() {
		if(radius <= MINIMUM_SIZE)
			setShotToDead(shotID);
		else {
			center.getFixtureList().m_shape.m_radius = radius;
		}
	}
	public static void prepShot() {
		if(shotCount >= Shot.MAX_SHOT_COUNT)
			setShotToDead(MAX_SHOT_COUNT);
	}
	public static void setShotToDead(Shot s)
	{
		setShotToDead(s.shotID);
	}
	public void resetID(int i)
	{
		fixture.setUserData("shot," + playerID + "," + i);
		shotID = i;
	}
	public static void setShotToDead(int i)
	{
		getShot(i).awaitDeath();
		shotCount--;
		shots.remove(i);
		for(int j = 0; j<shots.size(); j++) {
			getShot(j).resetID(j);
		}
	}
	public ArrayObj serializeToSocket() {
		ArrayObj object = new ArrayObj(PField.Float("x", getPos().x));
		object.addField(PField.Float("y", getPos().y));
		object.addField(PField.Float("radius", radius));
		object.addField(PField.Integer("color", playerID));
		return object;
	}
	@Override
	public String toString()
	{
		/* send to socket
		 * float x position
		 * float y position
		 * float radius
		 * int userData
		 */
		Vec2 pos = center.getPosition();
		return  pos.x + ",," + pos.y + ",," + radius + ",," + ((Integer)(center.getUserData())-100)/100;
	}
}
