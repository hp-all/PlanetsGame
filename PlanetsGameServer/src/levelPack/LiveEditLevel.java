package levelPack;

import java.io.*;

import serialization.*;

public class LiveEditLevel {
	private static Level level;
	private static final String FILE_PATH = "levels.plnt";
	private static PDatabase mapDatabase = new PDatabase("maps");

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
	public static void main(String[] args) {
		int levelNum = 2;
		PDatabase database = PDatabase.DeserializeFromFile(FILE_PATH);
		System.out.println(database);
		PObject levelData = database.findObject("map" + levelNum);

		float density = .6f;
		float friction = .5f;
		
		ArrayObj planet = getPlanet(levelData, 0);
		planet.setField("density", PField.Float("d", density));
		
		planet = getPlanet(levelData, 1);
		planet.setField("density", PField.Float("d", density));
		
		System.out.println("Test: " + getPlanet(database.findObject("map" + levelNum), 0).findField("type"));
		
		byte[] stream = new byte[database.getSize()];
		database.setBytes(stream, 0);
		saveToFile(FILE_PATH, stream);
	}
	private static ArrayObj getPlanet(PObject level, int i) {
		ArrayObj[] planets = level.findArray("planets").objects;
		if(i < planets.length)
			return planets[i];
		return null;
	}
	private static ArrayObj getSpawnPoint(PObject level, int i) {
		ArrayObj[] spawnPs = level.findArray("spawnPoints").objects;
		if(i < spawnPs.length)
			return spawnPs[i];
		return null;
	}
}
