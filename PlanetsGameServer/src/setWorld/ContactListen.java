package setWorld;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.contacts.Contact;

import gameCycle.GamePlay;
import main.GameEngine;
import objects.Shot;

public class ContactListen implements ContactListener{
	private int playerOnGround = 0;
	private int player = 0;
	private int player2 = 0;
	private boolean planet = false;
	private int shotPlayerNum = 0;
	private int shotNum = -1;
	private int shotNum2 = -1;

	@Override
	public void beginContact(Contact contact)
	{
		reset();

		checkContact(contact.getFixtureA());
		checkContact(contact.getFixtureB());

		if(player != 0 && player2 != 0) {
			GamePlay.shooterBois[player-1].playerContact = player2-1;
			GamePlay.shooterBois[player2-1].playerContact = player-1;
		}
		if(planet && playerOnGround != 0) {
			GamePlay.shooterBois[playerOnGround-1].setOnGround();
		}
		if(shotNum >= 0)
		{
			if(Shot.shots.size() > 0)
			{
				if(player-1 == shotPlayerNum || planet)
				{
					if(shotNum < Shot.shots.size())
						Shot.getShot(shotNum).roll();
				}
				else if(player-1 != shotPlayerNum && player > 0 && shotNum < Shot.shots.size())
				{
					float d = Shot.getShot(shotNum).getRadius();
					GamePlay.shooterBois[player-1].shotCollision(d, shotPlayerNum);
					Shot.setShotToDead(shotNum);
				}
			}
		}
	}
	@Override
	public void endContact(Contact contact) {
		reset();

		checkContact(contact.getFixtureA());
		checkContact(contact.getFixtureB());

		if(player != 0 && player2 != 0) {
			GamePlay.shooterBois[player-1].playerContact = -1;
			GamePlay.shooterBois[player2-1].playerContact = -1;
		}
		if(planet) {
			if(playerOnGround != 0)
				GamePlay.shooterBois[playerOnGround-1].isOnGround = false;
		}
		if(shotNum >= 0 && Shot.shots.size() > 0) {
			if(player-1 == shotPlayerNum || planet)
			{
				if(shotNum < Shot.shots.size())
					Shot.getShot(shotNum).bounce();
			}
		}
	}
	private void checkContact(Fixture fix)
	{
		Object fixtureUserData = fix.getUserData();
		if ( fixtureUserData != null)
		{
			String stringData = fixtureUserData.toString();
			if(stringData.contains("player")) {
				if(player == 0)
					player = Integer.parseInt(stringData.substring(6));
				else
					player2 = Integer.parseInt(stringData.substring(6));
			} else if(stringData.contains("sensor")) {
				playerOnGround = Integer.parseInt(stringData.substring(6));
			} else if(stringData.contains("planet")) {
				planet = true;
			} else {
				if(stringData.contains("shot")) {
					if(shotNum == -1) {
						String[] shotData = ((String)stringData).split(",");
						shotPlayerNum = Integer.parseInt(shotData[1]);
						shotNum = Integer.parseInt(shotData[2]);
					} else {
						String[] shotData = ((String)stringData).split(",");
						shotNum2 = Integer.parseInt(shotData[2]);
					}
				}
			}
		}
	}
	public void reset()
	{
		playerOnGround = 0;
		player = 0;
		player2 = 0;
		planet = false;
		shotPlayerNum = 0;
		shotNum = -1;
		shotNum2 = -1;
	}
	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		// TODO Auto-generated method stub

	}
	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		// TODO Auto-generated method stub

	}

}
