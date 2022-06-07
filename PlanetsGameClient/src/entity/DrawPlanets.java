package entity;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import graphics.Sprite;
import serialization.ArrayObj;

public class DrawPlanets extends DEntity{
    private static BufferedImage levelImage;
    private static BufferedImage[] planet;
    private static double IMAGE_SCALE = .0059;
    private int levelNum;
    private Sprite sprite;
    
    public DrawPlanets(int levelNum) {
    	this.levelNum = levelNum;
        loadImages();
    }
    private static void loadImages() {
        planet = new BufferedImage[5];
        String folder = "/planets/";
        planet[0] = toBufferedImage(folder + "Grass1.PNG");
        planet[1] = toBufferedImage(folder + "Grass2.PNG");
        planet[2] = toBufferedImage(folder + "Rock1.PNG");
        planet[3] = toBufferedImage(folder + "Rock2.PNG");
        planet[4] = toBufferedImage(folder + "Ice1.PNG");
    }
    public void process(ArrayObj[] planetData) {       
        int maxWidth = 0;
        int maxHeight = 0;
        BufferedImage[] planetImgs = new BufferedImage[planetData.length];
        for(int i = 0; i<planetData.length; i++)
        {
            float x = planetData[i].findField("x").getFloat()*scale;
            float y = planetData[i].findField("y").getFloat()*scale;
            float r = planetData[i].findField("radius").getFloat()*scale;
            int type = planetData[i].findField("type").getInt();
            System.out.println(type);
            planetImgs[i] = resize(type, x, y, r);
            maxWidth = Math.max(planetImgs[i].getWidth(), maxWidth);
            maxHeight = Math.max(planetImgs[i].getHeight(), maxHeight);
        }
        levelImage = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics g = levelImage.getGraphics();
        for(BufferedImage bi : planetImgs)
        {
            g.drawImage(bi, 0, 0, null);
        }
        int boundX = (levelImage.getWidth() >= 1200) ? 1200:levelImage.getWidth();
        int boundY = (levelImage.getHeight() >= 650) ? 650:levelImage.getHeight();
        levelImage = levelImage.getSubimage(0, 0, boundX, boundY);
        g.dispose();
    }
    
    public void draw(Graphics2D g) {
        g.drawImage(levelImage, 0, 0, null);
    }
    public static void clearLevel() {
        levelImage = null;
    }
    private BufferedImage resize(int type, float x, float y, float r) {
        AffineTransform at = new AffineTransform();
        at.scale(IMAGE_SCALE*(double)r, IMAGE_SCALE*(double)r);
        BufferedImage b = (new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR)).filter(planet[type], null);

        at = new AffineTransform();
        at.translate((double)x-b.getWidth()/2, (double)y-b.getHeight()/2);
        at.rotate(Math.random()*Math.PI*2, b.getWidth()/2, b.getHeight()/2);
        
        return (new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR)).filter(b, null);
    }
}
