package input;

import java.awt.Point;
import java.awt.event.*;

import main.PlayGame;
import screen.Screen;
import serialization.*;

public class KeyBoard implements KeyListener, MouseListener, MouseMotionListener{

	public static boolean typing = true;
	public static final int COOL_DOWN = 3;

	private boolean gotChar = true;
	private char currentChar;
	public boolean[] key = new boolean[128];
	public boolean[] pressedKey = new boolean[9];
	public boolean[] pressedOnceKey = new boolean[9];
	public int[] received = {0, 0, 0, 0, 0, 0, 0, 0};
	public Point mouseV = new Point();

	public KeyBoard() {
		//up, down, left, right, space, back, enter, tab
		for(int i = 0; i<pressedKey.length; i++) {
			pressedKey[i] = false;
			pressedOnceKey[i] = false;
		}
	}
	public void update() {
		for(int i = 0; i<received.length; i++) {
			if(received[i] > 0) {
				if(received[i] > 1 || (received[i] == 1 && !pressedKey[i]))
				{
					pressedOnceKey[i] = false;
					received[i] --;
				}
			} else if(received[i] == 0) {
				if(pressedKey[i])
					received[i] = COOL_DOWN;
				pressedOnceKey[i] = pressedKey[i];
			}
		}
	}
	private boolean isKey(int i) {
		if(typing)
			return pressedOnceKey[i];
		return pressedKey[i];
	}
	public synchronized boolean up() {
		return isKey(0);
	}
	public synchronized boolean down() {
		return isKey(1);
	}
	public synchronized boolean left() {
		return isKey(2);
	}
	public synchronized boolean right() {
		return isKey(3);
	}
	public synchronized boolean space() {
		return isKey(4);
	}
	public synchronized boolean back() {
		return isKey(5);
	}
	public synchronized boolean enter() {
		return isKey(6);
	}
	public synchronized boolean tab() {
		return isKey(7);
	}
	public synchronized boolean ctrl() {
		return pressedKey[8];
	}

	public PObject SerializeInput() {
		PObject result = new PObject("Input");

		boolean[] input = {up(), down(), left(), right(), space() || enter(), back()};
		result.addField(PField.Float("mx", (float)mouseV.getX()/(float)Screen.scale));
		result.addField(PField.Float("my", (float)mouseV.getY()/(float)Screen.scale));
		result.addArray(PArray.Boolean("keys", input));

		return result;
	}
	public void printInputs() {
		System.out.println();
		System.out.println("space: " + space() + " | back: " + back() + " | mouse: (" + mouseV.x + ", " + mouseV.y + ")");
		System.out.println("\t"+up());
		System.out.println(left() + "\t" + down() + "\t" + right());
	}
	public void printChar() {
		Character c = getChar();
		if(c != null && !ctrl() && c != KeyEvent.CHAR_UNDEFINED)
			System.out.println(c);
	}
	public Character getChar() {
		if(!gotChar)
		{
			gotChar = true;
			return currentChar;
		}
		return null;
	}
	public boolean closeWindow() {
		return (ctrl() && key[KeyEvent.VK_W]);
	}
	public boolean askToClose() {
		return key[KeyEvent.VK_ESCAPE];
	}
	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		key[e.getKeyCode()] = true;
		pressedKey[0] = key[KeyEvent.VK_UP]		|| (key[KeyEvent.VK_W] && !typing);
		pressedKey[1] = key[KeyEvent.VK_DOWN] 	|| (key[KeyEvent.VK_S] && !typing);
		pressedKey[2] = key[KeyEvent.VK_LEFT] 	|| (key[KeyEvent.VK_A] && !typing);
		pressedKey[3] = key[KeyEvent.VK_RIGHT] 	|| (key[KeyEvent.VK_D] && !typing);
		pressedKey[4] = key[KeyEvent.VK_SPACE];
		pressedKey[5] = key[KeyEvent.VK_BACK_SPACE];
		pressedKey[6] = key[KeyEvent.VK_ENTER];
		pressedKey[7] = key[KeyEvent.VK_TAB];
		pressedKey[8] = key[KeyEvent.VK_CONTROL];
		
		if(closeWindow() || askToClose())
			PlayGame.frame.dispatchEvent(new WindowEvent(PlayGame.frame, WindowEvent.WINDOW_CLOSING));
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(e.getKeyChar() != KeyEvent.CHAR_UNDEFINED && !e.isControlDown() && e.getKeyCode() != KeyEvent.VK_BACK_SPACE) {
			currentChar = e.getKeyChar();
			gotChar = false;
		}
		key[e.getKeyCode()] = false;

		if(e.getKeyCode() == KeyEvent.VK_UP || (e.getKeyCode() == KeyEvent.VK_W && !typing))
			pressedKey[0] = false;
		if(e.getKeyCode() == KeyEvent.VK_DOWN || (e.getKeyCode() == KeyEvent.VK_S && !typing))
			pressedKey[1] = false;
		if(e.getKeyCode() == KeyEvent.VK_LEFT || (e.getKeyCode() == KeyEvent.VK_A && !typing))
			pressedKey[2] = false;
		if(e.getKeyCode() == KeyEvent.VK_RIGHT || (e.getKeyCode() == KeyEvent.VK_D && !typing))
			pressedKey[3] = false;
		if(e.getKeyCode() == KeyEvent.VK_SPACE)
			pressedKey[4] = false;
		if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
			pressedKey[5] = false;
		if(e.getKeyCode() == KeyEvent.VK_ENTER)
			pressedKey[6] = false;
		if(e.getKeyCode() == KeyEvent.VK_TAB)
			pressedKey[7] = false;
		if(e.getKeyCode() == KeyEvent.VK_CONTROL)
			pressedKey[8] = false;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		mouseV.x = e.getX();
		mouseV.y = e.getY();
	}

	@Override
	public void mouseMoved(MouseEvent e) {	
		mouseV.x = e.getX();
		mouseV.y = e.getY();
	}

	@Override
	public void mouseClicked(MouseEvent e) {		
	}

	@Override
	public void mousePressed(MouseEvent e) {	
		pressedKey[4] = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {	
		pressedKey[4] = false;
	}

	@Override
	public void mouseEntered(MouseEvent e) {		
	}

	@Override
	public void mouseExited(MouseEvent e) {		
	}

}
