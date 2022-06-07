package levelPack;

import java.util.ArrayList;

import org.jbox2d.common.Vec2;

import objects.Planet;
import serialization.*;

public class Level {
	private ArrayList<float[]> planetList = new ArrayList<float[]>();
	private Vec2[] spawnPoints = {new Vec2(1, 1), new Vec2(110, 1), new Vec2(110, 67), new Vec2(1, 67)};
	private double[] spawnAngles = {0, 0, 0, 0};
	public void setSpawnPoint(int i, Vec2 p, double a)
	{
		spawnPoints[i] = p;
		spawnAngles[i] = Math.toRadians(a);
	}
	public void addPlanet(int type, Vec2 pos, float rad, float fric, float density)
	{
		planetList.add(new float[] {type, pos.x, pos.y, rad, fric, density});
	}
	public ArrayList<float[]> getPlanets()
	{
		return planetList;
	}
	public Planet createPlanet(int i) {
		float[] planetData = planetList.get(i);
		float type = planetData[0];
		Vec2 pos = new Vec2(planetData[1], planetData[2]);
		float radius = planetData[3];
		float friction = planetData[4];
		float density = planetData[5];
		return new Planet((int)type, pos, radius, density, friction);
	}
	public void updatePlanet(int i, Planet p) {
		float[] planetData = planetList.get(i);
		float friction = planetData[4];
		float density = planetData[5];
		p.update(density, friction);
	}
	public void serializeToFile(PObject levObj) {
		ArrayObj[] planets = new ArrayObj[planetList.size()];
		for(int i = 0; i<planetList.size(); i++) {
			planets[i] = serializePlanet(i);
		}
		PArray planetArr = PArray.Object("planets", planets);
		
		ArrayObj[] spawnP = new ArrayObj[4];
		for(int i = 0; i<4; i++) {
			spawnP[i] = new ArrayObj(PField.Float("x", spawnPoints[i].x));
			spawnP[i].addField(PField.Float("y", spawnPoints[i].y));
			spawnP[i].addField(PField.Double("angle", spawnAngles[i]));
		}
		PArray spawnArr = PArray.Object("spawnPoints", spawnP);
		
		levObj.addArray(planetArr);
		levObj.addArray(spawnArr);
	}
	public ArrayObj serializePlanet(int i) {
		float[] planetData = planetList.get(i);
		ArrayObj planet = new ArrayObj(PField.Integer("type", (int)planetData[0]));
		planet.addField(PField.Float("x", planetData[1]));
		planet.addField(PField.Float("y", planetData[2]));
		planet.addField(PField.Float("radius", planetData[3]));
		planet.addField(PField.Float("friction", planetData[4]));
		planet.addField(PField.Float("density", planetData[5]));
		return planet;
	}
	public static Level Deserialize(PObject levelObj) {
		Level result = new Level();
		ArrayObj[] planets = levelObj.findArray("planets").objects;
		for(int i = 0; i<planets.length; i++) {
			float[] planetData = new float[6];
			planetData[0] = (int)planets[i].findField("type").getInt();
			planetData[1] = planets[i].findField("x").getFloat();
			planetData[2] = planets[i].findField("y").getFloat();
			planetData[3] = planets[i].findField("radius").getFloat();
			planetData[4] = planets[i].findField("friction").getFloat();
			planetData[5] = planets[i].findField("density").getFloat();
			
			result.planetList.add(planetData);
		}
		
		ArrayObj[] spawnP = levelObj.findArray("spawnPoints").objects;
		for(int i = 0; i<4; i++) {
			result.spawnPoints[i].x = spawnP[i].findField("x").getFloat();
			result.spawnPoints[i].y = spawnP[i].findField("y").getFloat();
			result.spawnAngles[i] = spawnP[i].findField("angle").getDouble();
		}
		
		return result;
	}
	public Vec2[] getSpawnPoints()
	{
		return spawnPoints;
	}
	public double[] getSpawnAngles()
	{
	    return spawnAngles;
	}
	
}
