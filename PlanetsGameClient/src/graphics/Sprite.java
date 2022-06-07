package graphics;

import java.awt.image.BufferedImage;

public class Sprite {
    public final int SIZE;
    public final int WIDTH;
    public final int HEIGHT;
    
    private int x, y;
    public int[] pixels;
    protected BufferedImage image;
    protected SpriteSheet sheet;


    protected Sprite(int color) {
    	SIZE = SpriteSheet.blueSheet.WIDTH/SpriteSheet.blueSheet.PER_ROW;
    	WIDTH = SIZE;
    	HEIGHT = SpriteSheet.blueSheet.HEIGHT/SpriteSheet.blueSheet.PER_COL;
    	switch(color) {
    	case 0: sheet = SpriteSheet.blueSheet; break;
    	case 1: sheet = SpriteSheet.redSheet; break;
    	case 2: sheet = SpriteSheet.greenSheet; break;
    	default: sheet = SpriteSheet.purpSheet; break;
    	}
    }
    public Sprite(int x, int y, SpriteSheet sheet) {
    	SIZE = sheet.WIDTH/sheet.PER_ROW;
    	WIDTH = SIZE;
    	HEIGHT = sheet.HEIGHT/sheet.PER_COL;
    	this.x = x*WIDTH;
    	this.y = y*HEIGHT;
    	this.sheet = sheet;
    	pixels = new int[WIDTH*HEIGHT];
    	load();
    }    
    public Sprite(int size, int x, int y, SpriteSheet sheet) {
        SIZE = size;
        WIDTH = size;
        HEIGHT = size;
        this.x = x * size;
        this.y = y * size;
        this.sheet = sheet;
        pixels = new int[SIZE * SIZE];
        load();
    }
    public Sprite(int width, int height, int x, int y, SpriteSheet sheet) {
        SIZE = width;
        WIDTH = width;
        HEIGHT = height;
        this.x = x * width;
        this.y = y * height;
        this.sheet = sheet;
        pixels = new int[width * height];
        load();
    }
    
    public static void loadAll() {
    	
    }
    private void load() {
        for(int y = 0; y < HEIGHT; y++) {
        	if(y > sheet.HEIGHT) continue;
            for(int x = 0; x<WIDTH; x++) {
            	if(x > sheet.WIDTH) continue;
                pixels[x+y*WIDTH] = sheet.pixels[(this.x + x) + (this.y + y) * sheet.WIDTH];
            }
        }
        image = sheet.image.getSubimage(x, y, WIDTH, HEIGHT);
    }
    protected static Sprite[] loadSpriteArray(int size, int first, int upTo, SpriteSheet sheet) {
        Sprite[] result = new Sprite[upTo - first];
        for(int i = first; i<upTo; i++) {
            result[i-first] = new Sprite(sheet.WIDTH/sheet.PER_ROW, sheet.HEIGHT/sheet.PER_COL, i % sheet.PER_ROW, i/sheet.PER_ROW, sheet);
        }
        return result;
    }
    public int getSize() {
        return SIZE;
    }
}