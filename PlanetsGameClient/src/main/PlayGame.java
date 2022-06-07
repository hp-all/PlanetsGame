package main;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.*;

import javax.swing.JFrame;

import client.Client;
import client.ClientPlayer;
import input.KeyBoard;
import screen.Screen;

/* Runs the Planets Game from the Clients side
 *
 * To Run multiple client programs from the same computer
 * Enter the player number (i.e. 1, 2, 3, or 4) into the IP address 
 * prompt so that you are using different Ports
 */
public class PlayGame extends Canvas implements Runnable{
	private static final long serialVersionUID = 1L;
	public static final int PORT = 4000;

	public static int width = 900;
	public static int height = width / 16 * 9;
	public static int scale = 1;

	private Thread thread;
	public static JFrame frame;
	private KeyBoard key;
	private boolean running = false;

	public static Screen screen; 
	public static Client user;
	public static ConnectScreen connect;

	private BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	private int[] pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();


	//public static MusicComponent mc = new MusicComponent(false);
	//private SocketScreen socScreen = new SocketScreen();

	public PlayGame()
	{
		Dimension size = new Dimension(width*scale, height*scale);
		this.setPreferredSize(size);
		this.setFocusable(true);
		screen = new Screen(width, height);

		frame = new JFrame();
		key = new KeyBoard();
		connect = new ConnectScreen(key, width, height);

		addKeyListener(key);
		addMouseListener(key);
		addMouseMotionListener(key);
	}

	public synchronized void start() {
		running = true;
		thread = new Thread(this, "Main thread");

		thread.start();
		requestFocus();
	}
	public static synchronized void restartConnect(String errorMsg) {
		if(user != null)
			user.close();
		user = null;
		KeyBoard.typing = true;
		connect.restart(errorMsg);
	}
	public synchronized void stop() {
		if(user != null)
			user.sendAFKpacket();
		running = false;
		try { 
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	private void update() {
		requestFocus();
		key.update();
		//key.printInputs();
		
		connect.update();
		if(connect.select == ConnectScreen.Select.HIDE && user == null) {
			KeyBoard.typing = false;
			user = new Client(connect.name, connect.IPAddress, PORT);
			user.connect();
		}
		if(user != null)
			user.sendInput(key.SerializeInput());
	}
	private void render() {
		BufferStrategy bs = getBufferStrategy();
		if(bs == null) {
			createBufferStrategy(3);
			return;
		}
		screen.clear();
		for(int i = 0; i<pixels.length; i++){
			pixels[i] = Screen.pixels[i];
		}		
		Graphics g = bs.getDrawGraphics();
		g.drawImage(image, 0, 0, getWidth(), getHeight(), null);

		connect.render(g);
		screen.render(g);

		g.dispose();
		bs.show();
	}
	@Override
	public void run() {
		long lastTime = System.nanoTime();
		final double ns = 1000000000.0 / 60;
		double delta = 0.0;
		long timer = System.currentTimeMillis();
		int updates = 0;
		int frames = 0;
		requestFocus();

		while(running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while (delta >= 1 ) {
				update();
				updates++;
				delta--;
			}
			render();
			frames++;
			if (System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
				frame.setTitle("PLANETS: " +  updates + " ups, " + frames + " fps");
				updates = 0;
				frames = 0;
			}
		}
	}

	public static void main(String [] args)
	{
		PlayGame game = new PlayGame();
		frame = new JFrame();
		frame.setResizable(false);
		frame.setTitle("PLANETS HOST");
		frame.add(game);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e) {
				game.stop();
			}
		});
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		//	game.frame.getContentPane().add(new SocketScreen());
		//	game.frame.getContentPane().add(new ConnectScreen());
		//	game.mc.execute();

		game.start();
	}
}