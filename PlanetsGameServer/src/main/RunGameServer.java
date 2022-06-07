package main;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.OverlayLayout;


//TODO Runs the Planets Host Server
public class RunGameServer extends Canvas implements Runnable{
    private static final long serialVersionUID = 1L;
    
    private Thread thread;
    private static JFrame frame;
    private boolean running = false;
    
    private GameEngine gameEng;

	public RunGameServer()
	{
		this.setSize(400, 200);
		this.setFocusable(true);
		this.addKeyListener(new KeyKey());
	}
	
	public synchronized void start() {
		running = true;
		thread = new Thread(this, "Main thread");
		
		gameEng = new GameEngine();
		
		thread.start();		
		requestFocus();
	}
	
	public synchronized void stop() {
		GameEngine.server.sendErrorToAll((byte)0x01);
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	private void update() {
		requestFocus();
		gameEng.update();
	}
	private void render() {
		Graphics g = this.getGraphics();
		g.setColor(Color.BLACK);
        g.drawString("Connected Players: " + Arrays.toString(GameEngine.server.getNames()).replace("[", " ").replace("]", " "), 5, 80);
        g.dispose();
	}
	@Override
	public void run() {
		long lastTime = System.nanoTime();
		final double ns = 1000000000.0 / 60;
		double delta = 0.0;
		long timer = System.currentTimeMillis();
		int updates = 0;
		int frames = 0;
		
		Graphics g = this.getGraphics();
		g.drawString("Hosting Server", 5, 20);
        try {
			g.drawString("IP address: " + InetAddress.getLocalHost(), 5, 40);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
        g.dispose();//*/
        
		while(running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			if (delta >= 1.0) {
				update();
				updates++;
				delta--;
			}
			render();
			frames++;
			if (System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
				frame.setTitle("PLANETS HOST: " +  updates + " ups, " + frames + " fps");
				updates = 0;
				frames = 0;
			}
		}
	}

	public static void main(String [] args)
	{
		RunGameServer game = new RunGameServer();
		frame = new JFrame();
		frame.setResizable(false);
		frame.setTitle("PLANETS HOST");
		frame.add(game);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				game.stop();
			}
		});
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		game.start();
	}
	private class KeyKey implements KeyListener{

		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_ESCAPE)
				RunGameServer.frame.dispatchEvent(new WindowEvent(RunGameServer.frame, WindowEvent.WINDOW_CLOSING));
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}
	}
}

