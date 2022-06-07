package graphics;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class SpriteSheet {
    private String path;
    protected BufferedImage image;
    public final int WIDTH, HEIGHT;
    public final int PER_ROW, PER_COL;
    public int[] pixels;
    
    public static SpriteSheet blueSheet;
    public static SpriteSheet redSheet;
    public static SpriteSheet greenSheet;
    public static SpriteSheet purpSheet;
    public static SpriteSheet hurt;
    
    public SpriteSheet(String path, int size, int perRow) {
        this.path = path;
        WIDTH = size;
        HEIGHT = size;
        PER_ROW = perRow;
        PER_COL = perRow;
        pixels = new int[WIDTH * HEIGHT];
        load();
    }
    public SpriteSheet(String path, int w, int h, int perRow, int perCol) {
        this.path = path;
        WIDTH = w;
        HEIGHT = h;
        PER_ROW = perRow;
        PER_COL = perCol;
        pixels = new int[WIDTH * HEIGHT];
        load();
    }
    public static void loadAll() {
        blueSheet	= new SpriteSheet("/BlueSheet.PNG", 1388, 1098, 4, 3);
        redSheet 	= new SpriteSheet("/RedSheet.PNG", 1388, 1098, 4, 3);
        greenSheet 	= new SpriteSheet("/GreenSheet.PNG", 1388, 1098, 4, 3);
        purpSheet 	= new SpriteSheet("/PurpSheet.PNG", 1388, 1098, 4, 3);
        hurt		= new SpriteSheet("/Hurt.PNG", 347, 366, 1, 1);
    }
    private void load() {
        try {
            image = ImageIO.read(SpriteSheet.class.getResource(path));
            int w = image.getWidth();
            int h = image.getHeight();
            image.getRGB(0, 0, w, h, pixels, 0, w);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}