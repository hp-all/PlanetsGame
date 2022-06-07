package objects;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyType;

import levelPack.LevelSelect;
import main.GameEngine;

public class DynamicEntity extends SEntity{

	protected Vec2 sumForce = new Vec2();
	protected Vec2 maxForce = new Vec2();
	protected double sumForceA = 0;
		
	public DynamicEntity(String userData, Vec2 pos, float radius, float density, float friction, double angle, boolean fixedR) {
		super(userData, pos, radius, density, friction, angle, fixedR, BodyType.DYNAMIC);
	}
	
	public void update() {
		
	}

	public void calcGravity() {
		sumForce = new Vec2();
		maxForce = new Vec2();
		//finds the sum of all gravity vectors, or finds the greatest one if touching a planet
		for(Planet p: GameEngine.levelSel.currentMap)
		{
			sumForce.addLocal(p.getGravity(this));
			if(maxForce.length() < p.getGravity(this).length())
				maxForce = p.getGravity(this).clone();
			//System.out.println("SumForce: " + sumForce);
		}
	}
}
