package levelPack;

import java.io.*;
import java.util.ArrayList;

import org.jbox2d.common.Vec2;

import objects.Planet;
import serialization.*;

//Planets LevelSelect
public class LevelSelect {
	public static int levelCount;
	private ArrayList<Level> maps = new ArrayList<Level>();
	public ArrayList<Planet> currentMap = new ArrayList<Planet>();
	private int currentLvl = 0;
	public Vec2[] currentSpawnPoints;
	public double[] currentSpawnAngles;
	
	private PDatabase mapDatabase = new PDatabase("maps");
	private static final String FILE_PATH = "levels.plnt";
	public LevelSelect()
	{		
		mapOne();
		mapTwo();
		mapThree();
		mapFour();
		levelCount = 4;
		
		byte[] stream = new byte[mapDatabase.getSize()];
		mapDatabase.setBytes(stream, 0);
		saveToFile(FILE_PATH, stream);
		//mapDatabase.SerializeToFile(filePath);
		System.out.println("wrote to file " + FILE_PATH);
	}
	protected static Level loadFromFile(int i) {
		PDatabase database = PDatabase.DeserializeFromFile(FILE_PATH);
		if(database == null) return null;
		//System.out.println(database);
		return Level.Deserialize(database.findObject("map" + i));
	}
	public void update() {
		maps.set(currentLvl-1, loadFromFile(currentLvl));
		setMap(currentLvl);
	}
	public void setMap(int n)
	{
		if(n < 1)
			n = 1;
		else if(n > maps.size())
			n = maps.size();
		boolean updateMap = currentLvl == n;
	    currentLvl = n;
		n--;
		int planetsSize = 0;
		for(int i = 0; i<maps.get(n).getPlanets().size(); i++) {
			if(i < currentMap.size() && currentMap.get(i) != null) {
				if(updateMap) {
					maps.get(n).updatePlanet(i, currentMap.get(i));
				} else {
					currentMap.get(i).awaitDeath();
					currentMap.set(i, maps.get(n).createPlanet(i));
				}
			} else currentMap.add(maps.get(n).createPlanet(i));
			planetsSize = i + 1;
		}
		while(planetsSize < currentMap.size()) {
			currentMap.get(planetsSize).awaitDeath();
			currentMap.remove(planetsSize);
		}
		currentSpawnPoints = maps.get(n).getSpawnPoints();
		currentSpawnAngles = maps.get(n).getSpawnAngles();
	}
	public void clearMap()
	{
		for(Planet p : currentMap)
		{
			p.destroy();
		}
		currentMap.clear();
	}
	private void mapOne()
	{
	    Level map1 = new Level();
	    float density = .5f;
	    float friction = 1.4f;
		map1.addPlanet(0, new Vec2(22.5f, 37.5f), 16, friction, density);
		map1.addPlanet(1, new Vec2(71, 12.5f), 6, friction, .8f);
		map1.addPlanet(3, new Vec2(117.5f, 93.5f), 60, friction, density*.5f);
		
		map1.setSpawnPoint(0, new Vec2(39, 49), -150);
		map1.setSpawnPoint(2, new Vec2(40, 25.5f), 120);
		map1.setSpawnPoint(1, new Vec2(70.5f, 21.5f), 270);
		map1.setSpawnPoint(3, new Vec2(76.5f, 47.5f), 30);

		Level mapFromFile = loadFromFile(1);
		if(mapFromFile != null) {
			maps.add(mapFromFile);
			System.out.println("Got map from file");
		}
		else
			maps.add(map1);
		
		PObject mapData = new PObject("map1");
		maps.get(0).serializeToFile(mapData);
		mapDatabase.addObject(mapData);
	}
	private void mapTwo()
	{
	    Level map2 = new Level();
		float density = .6f;
		float friction =.2f;
		map2.addPlanet(0, new Vec2(23, 23), 16, friction, density);
		map2.addPlanet(1, new Vec2(96.2f, 42.1f), 16, friction, density);
		
		map2.setSpawnPoint(0, new Vec2(23.7f, 39), 45);
		map2.setSpawnPoint(2, new Vec2(34, 6.8f), 45+90);
		map2.setSpawnPoint(1, new Vec2(89, 20.5f), 45+180);
		map2.setSpawnPoint(3, new Vec2(78.9f, 52), 45+270);
		
		Level mapFromFile = loadFromFile(2);
		if(mapFromFile != null)
			maps.add(mapFromFile);
		else
			maps.add(map2);
		
		PObject mapData = new PObject("map2");
		maps.get(1).serializeToFile(mapData);
		mapDatabase.addObject(mapData);
	}
	private void mapThree()
	{
	    Level map3 = new Level();
	    float friction = .1f;
	    float density = .8f;
        map3.addPlanet(2, new Vec2(60, 34), 25, friction, density);
        map3.addPlanet(1, new Vec2(105, 19), 7, friction*3, density);
        map3.addPlanet(2, new Vec2(11, 34), 3, friction, density*3);
        
        map3.setSpawnPoint(0,  new Vec2(28, 31), 30);
        map3.setSpawnPoint(1, new Vec2(88, 31), 30);
        map3.setSpawnPoint(2, new Vec2(69.8f, 55.5f), 30);
        map3.setSpawnPoint(3, new Vec2(44, 4.5f), 30);
        
        Level mapFromFile = loadFromFile(3);
		if(mapFromFile != null)
			maps.add(mapFromFile);
		else
			maps.add(map3);
		
		PObject mapData = new PObject("map3");
		maps.get(2).serializeToFile(mapData);
		mapDatabase.addObject(mapData);
	}
	private void mapFour() {
	    Level map = new Level();
		float density = 1.5f;
		float friction = .5f;
        double theta = Math.toRadians(60);
        int r = 30;
        map.addPlanet(4, new Vec2(60, 33), 15, 
        		.01f, density/3);
        map.addPlanet(0, new Vec2(60+r, 33), 3, friction, density);
        map.addPlanet(0, new Vec2(60-r, 33), 3, friction, density);
        map.addPlanet(0, new Vec2(60+r*(float)Math.cos(theta), 33+r*(float)Math.sin(theta)), 3, friction, density);
        map.addPlanet(0, new Vec2(60-r*(float)Math.cos(theta), 33+r*(float)Math.sin(theta)), 3, friction, density);
        map.addPlanet(0, new Vec2(60+r*(float)Math.cos(theta), 33-r*(float)Math.sin(theta)), 3, friction, density);
        map.addPlanet(0, new Vec2(60-r*(float)Math.cos(theta), 33-r*(float)Math.sin(theta)), 3, friction, density);
        
        map.setSpawnPoint(0, new Vec2(42.8f, 17.5f), 45);
        map.setSpawnPoint(1, new Vec2(71.3f, 42.2f), 45+90);
        map.setSpawnPoint(2, new Vec2(70.8f, 17.7f), 45+180);
        map.setSpawnPoint(3, new Vec2(43.6f, 42.7f), 45+270);


        Level mapFromFile = loadFromFile(4);
		if(mapFromFile != null)
			maps.add(mapFromFile);
		else
			maps.add(map);
		
		PObject mapData = new PObject("map4");
		maps.get(3).serializeToFile(mapData);
		mapDatabase.addObject(mapData);
	}
	public PArray serializeToSocket() {
		
		ArrayObj[] planetData = new ArrayObj[currentMap.size()];
		for(int i = 0; i<currentMap.size(); i++) {
			planetData[i] = currentMap.get(i).serializeToSocket();
		}
		PArray result = PArray.Object("Level", planetData);
		
		return result;
	}
	public static void saveToFile(String path, byte[] data) {
		try {
			BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(path));
			stream.write(data);
			stream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public String toString()
	{
		String planetList = "";
		for(Planet p : currentMap)
		{
			planetList += p + "//";
		}
		return currentLvl + "//" + planetList;
	}
	
}
