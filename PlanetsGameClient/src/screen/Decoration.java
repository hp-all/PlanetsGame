package screen;

import java.awt.*;

public class Decoration {
	private static int width, height;
	private int midLine;

	private int barWidth = 300;
	private int barHeight = 88;
	private int barTilt = 50;
	private int spacing = 40;
	private int totalSpace;
	public static Polygon[] deco;
	
	public Decoration(int w, int h) {
		width = w;
		height = h;
		this.midLine = width/16*11;
		this.totalSpace = height + barTilt + barHeight;
		
		deco = new Polygon[totalSpace/(barHeight + spacing)];
		
		int vShift = barHeight + spacing;
		for(int i = 0; i<deco.length; i++)
		{
			// bottom right, top right, top left, bottom left
			int[] x = {midLine + barWidth/2, midLine + barWidth/2, midLine - barWidth/2, midLine - barWidth/2};
			int[] y = {vShift*i, vShift*i - barHeight, vShift*i - barHeight - barTilt, vShift*i - barTilt};
			deco[i] = new Polygon(x, y, 4);
		}
	}
	public void render(Graphics2D g, int tranStep) {
		if(tranStep != 0)
			tranStep = (int)(tranStep/Math.abs(tranStep));
		if(tranStep < 0)
			tranStep = -4;
		g.setColor(new Color(100, 100, 100));
		g.fill(new Rectangle(midLine - barWidth/2, 0, barWidth, height));
		g.setColor(new Color(200, 200, 200));

		for(int i = 0; i< deco.length; i++)
		{
			int[] y = deco[i].ypoints;
			for(int j = 0; j<y.length; j++)
				deco[i].ypoints[j] += tranStep;

			if(y[0] > height + barTilt + barHeight)
				deco[i].ypoints = new int[] {0, -barHeight, -barHeight-barTilt, -barTilt};
			if(y[0] < 0)
				deco[i].ypoints = new int[] {height+barHeight+barTilt, height + barTilt, height, height + barHeight};

			g.fill(deco[i]);
		}
		
		
		
	}
}
