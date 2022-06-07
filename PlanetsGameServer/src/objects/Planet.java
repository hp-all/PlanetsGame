package objects;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyType;
import serialization.ArrayObj;
import serialization.PField;

public class Planet extends SEntity{
	//public float mass = 0;
	public int type = 0;
	public Planet(int type, Vec2 pos, float radius, float density, float friction)
	{
		//userID is always 10
		super("planet", pos, radius, density, friction, 0, false, BodyType.STATIC);
		this.type = type;
		center.m_mass = density * (float)Math.PI*radius*radius;
		//System.out.println("Planet density: " + density + "Planet mass: " + center.getMass());
	}
	public ArrayObj serializeToSocket() {
		ArrayObj object = new ArrayObj(PField.Float("x", getPos().x));
		object.addField(PField.Float("y", getPos().y));
		object.addField(PField.Float("radius", radius));
		object.addField(PField.Integer("type", type));
		return object;
	}
	public void update(float density, float friction) {
		this.density = density;
		center.m_mass = density * (float)Math.PI*radius*radius;
		center.m_fixtureList.m_friction = friction;
	}
	@Override
	public String toString()
	{
		return getPos().x + ",," + getPos().y + ",," + radius + ",," + type;
	}
}
