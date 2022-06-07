package objects;

import java.util.ArrayList;
import java.util.List;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;

import main.GameEngine;

public abstract class SEntity {
	
    public static float G_CONST = 50f;
	protected float gravityMultiplier = 1;
    
	protected float radius; 
	protected float density;
	protected BodyType bodyType;
	protected boolean fixedRotation;
	protected float friction;
	protected double angle0;
	protected String userData;
	
	private boolean dead = false;
	public static List<SEntity> deadObjects = new ArrayList<>();
	
	protected Body center;
	protected BodyDef bDef;
	protected FixtureDef fDef;
	protected Fixture fixture;
	
	public SEntity(String userData, Vec2 pos, float radius, float density, float friction, double angle, boolean fixedR, BodyType bodyType) {
		this.radius = radius;
		this.density = density;
		this.friction = friction;
		this.angle0 = angle;
		this.fixedRotation = fixedR;
		this.bodyType = bodyType;
		this.userData = userData;

		addToWorld(pos.clone());
	}
	public void addToWorld(Vec2 pos) {
		bDef = new BodyDef();
		fDef = new FixtureDef();
		CircleShape shape = new CircleShape();
		
		bDef.type = bodyType;
		bDef.setPosition(pos);
		bDef.setFixedRotation(fixedRotation);
		bDef.angle = (float)angle0;
		
		fDef.friction = friction;
		fDef.density = density;
		
		shape.setRadius(radius);
		fDef.shape = shape;
		center = GameEngine.world.createBody(bDef);
		fixture = center.createFixture(fDef);
		fixture.setUserData(userData);
	}
	public Body getCenter()
	{
		return center;
	}
	public Vec2 getPos() {
		return center.getPosition();
	}
	public Vec2 getGravity(SEntity entity)
	{
		Body b = entity.center;
		Vec2 direction = getPos().sub(b.getPosition());
		float mag = G_CONST*b.getMass()*center.getMass()/(float)Math.pow(direction.length(),2);
		mag *= gravityMultiplier * entity.gravityMultiplier;
		direction.normalize();
		direction.mulLocal(mag);
		return direction;
		
	}
	public float getRadius() {
		return radius;
	}
	public double getAngle()
	{
		return (double)center.getAngle();
	}
	public void awaitDeath() {
		deadObjects.add(this);
	}
	public static void destroyDeathList() {
		for(int i = 0; i<deadObjects.size(); i++) {
			deadObjects.get(i).destroy();
			deadObjects.remove(i);
			i --;
		}
	}
	public void destroy()
	{
		GameEngine.world.destroyBody(center);
	}
}
