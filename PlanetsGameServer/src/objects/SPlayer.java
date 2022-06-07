package objects;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;

import gameCycle.GamePlay;
import main.GameEngine;
import planetServer.ServerClient;
import serialization.ArrayObj;
import serialization.PField;

public class SPlayer extends DynamicEntity {
	public static int startHealth;
	public static boolean gameStart = false;
	private int MAX_CHARGE = 40;
	private int SHOT_DELAY = 30;
	private static final int radiusHB = 3;
	private static final float density = 1f;

	//the movement forces are multiplied by mass^2
	public float JUMP_FORCE = 150f * radiusHB * radiusHB * density;//(float)Math.pow(radiusHB*radiusHB*Math.PI*density, 2);
	public float SIDE_FORCE = .3f * (float)Math.pow(radiusHB*radiusHB*Math.PI*density, 2);
	public float MAGNET_FORCE = .2f * (float)Math.pow(radiusHB*radiusHB*Math.PI*density, 2);
	public static float MAX_GROUND_SPEED = 17;
	public static float MAX_AIR_SPEED = 35;
	public static float MAX_FALL_SPEED = 40;
	private Vec2 jumpVec = new Vec2();

	private int playerID;
	private ServerClient user;
	private Vec2 spawnPoint = new Vec2();

	public boolean isOnGround;
	public int walkingCheck = 0;
	public boolean walking;
	private int walkStep;
	private int hitStep;
	public int health;
	public int lives;
	private double bodyAngle;
	private double headAngle;
	private int charge;

	public int playerContact = -1;
	
	private int rechargeStep = 0;
	private int respawnStep = 0;
	private int holdJump = 0;
	private int holdJumpLim = 5;
	private boolean isHit;
	public boolean isDead;
	

	public SPlayer(ServerClient client, int id, Vec2 pos, int lives, double angle) {
		//radius 3
		//density .07
		//friction .3
		super("player" + (id+1), pos, radiusHB, density, 1f,  angle, true);
		this.playerID = id;
		this.lives = lives;
		this.spawnPoint = pos;
		this.isHit = false;
		this.user = client;

		CircleShape shape = new CircleShape();
		shape.setRadius(radius + .8f);
		fDef.isSensor = true;
		Fixture planetSensor = center.createFixture(fDef);
		planetSensor.setUserData("sensor" + (playerID + 1));
		
		health = startHealth;
		isDead = false;		
	}

	private void resetObj(Vec2 pos) {
		super.addToWorld(pos);
		
		System.out.println("Resetting");
		CircleShape shape = new CircleShape();
		shape.setRadius(radius + .8f);
		fDef.isSensor = true;
		Fixture planetSensor = center.createFixture(fDef);
		planetSensor.setUserData("sensor" + (playerID + 1));

		health = startHealth;
		isDead = false;
	}

	public void goToSpawn() {

		resetObj(spawnPoint.clone());
		isHit = true;
		respawnStep = 0;
		hitStep = 54;
	}
	public void setOnGround() {
		isOnGround = true;
		walking = true;
		walkingCheck = 3;
	}
	public void update() {
		MAX_AIR_SPEED = 30;
		
		center.m_fixtureList.setFriction(1f);

		Vec2 relativeVert = sumForce.clone();
		relativeVert.normalize();

		Vec2 relativeHorz = new Vec2(-relativeVert.y, relativeVert.x);
		relativeHorz.normalize();

		Vec2 armVec = user.mouseV.sub(getPos());
		armVec.normalize();
		headAngle = Math.atan2(armVec.y, armVec.x);

		if(!user.isActive()) {
			health = 0;
			lives = 0;
			GameEngine.scoreBoard.inactivePlayer(playerID);
		}
		if(gameStart) {
			if(user.up() && (isOnGround || holdJump > 0)) {
				if(holdJump > 0) {
					if(holdJump % 2 == 1) {
						moveBy(jumpVec.mul(-JUMP_FORCE));
					}
					holdJump --;
				} else if(isOnGround) {
					jumpVec = relativeVert.clone();
					holdJump = holdJumpLim;
				}
			} else {
				holdJump = 0;
			}
			if(user.down() && !isOnGround){
				Vec2 relVert = maxForce.clone();
				relVert.normalize();
				moveBy(relVert.mul(MAGNET_FORCE));
			}
			if(user.left() && isOnGround) {
				moveBy(relativeHorz.mul(SIDE_FORCE));
				if(walking) {
					if(walkStep > 0)
						walkStep = 0;
					walkStep -= 1;
				}
			}
			if(user.right() && isOnGround) {
				moveBy(relativeHorz.mul(-SIDE_FORCE));
				if(walking) {
					if(walkStep < 0)
						walkStep = 0;
					walkStep += 1;
				}
			}
			if(!walking || !(user.left() || user.right()))
				walkStep = 0;
		}
		if(!isDead) {
			if(user.space() && rechargeStep <= 0 && charge < MAX_CHARGE) {
				charge ++;
			}
			if(!user.space() && charge > 0) {
				Vec2 pos = getPos().add(armVec.mul(5));
				Shot.prepShot();
				Shot.addShot(pos,charge/2, armVec, playerID);
				charge = 0;
				rechargeStep = SHOT_DELAY;
			}
			if(rechargeStep > 0) {
				rechargeStep --;
			}
		} else {
			JUMP_FORCE = .01f * radiusHB * radiusHB * density;
			SIDE_FORCE = 50 * (float)Math.pow(radiusHB*radiusHB*Math.PI*density, 2);
			MAGNET_FORCE = .1f;
			fixture.setRestitution(1f);
		}
		checkPlayerCollision();
		applyGrav();
		if(isHit) {
			hitStep -= 1;
			if(hitStep <= 0) {
				hitStep = 0;
				isHit = false;
			}
		}
		if(health == 0 && lives > 0) {
			if(respawnStep < 50)
				respawnStep ++;
			if(respawnStep == 20)
			{
				goToSpawn();
			} else if(respawnStep > 20) {	
				System.out.println("Growing a thing: " + (radius + .1f*(20-respawnStep)));
				CircleShape cs = new CircleShape();
				cs.setRadius(radius +.1f*(20-respawnStep));
				fDef.isSensor = false;
				Fixture woosh = center.createFixture(fDef);
				woosh.setUserData((playerID+1)*10);
			} else if(respawnStep > 50) {
				respawnStep = 0;
			}
		}
		if(!isOnGround && walkingCheck > 0) {
			walkingCheck --;
		}
		if(walkingCheck == 0)
			walking = false;
	}
	public void moveBy(Vec2 impulse)
	{
		center.applyLinearImpulse(impulse, center.getWorldCenter(), true);
	}
	private void applyGrav() {
		this.calcGravity();
		int speed = 4;
		if(isOnGround) {
			sumForce = maxForce.clone();
		}
		if(health > 0 && !isDead)
			center.applyForceToCenter(sumForce);

		Vec2 vel = center.getLinearVelocity();

		if(vel.length() > MAX_GROUND_SPEED && isOnGround && !user.up()) {
			vel.normalize();
			center.setLinearVelocity(vel.mulLocal(MAX_GROUND_SPEED));
		} else if(vel.length() > MAX_AIR_SPEED && !user.down()) {
			vel.normalize();
			center.setLinearVelocity(vel.mulLocal(MAX_AIR_SPEED));
		} else if(vel.length() > MAX_FALL_SPEED) {
			vel.normalize();
			center.setLinearVelocity(vel.mulLocal(MAX_FALL_SPEED));
		}
		if(walking) {
			sumForce = maxForce.clone();
			speed = 12;
		}
		sumForceA = Math.atan2(sumForce.y, sumForce.x);
		bodyAngle = moveTowardsAngle(bodyAngle, sumForceA, speed);
	}
	public double moveTowardsAngle(double bodyAngle, double goalAngle, int speed)
	{
		bodyAngle = bodyAngle%(Math.PI*2);
		goalAngle = goalAngle%(Math.PI*2);
		double difference = Math.abs(bodyAngle - goalAngle)%(Math.PI*2);
		double angleChange = Math.toRadians(speed);
		if(difference > angleChange + Math.toRadians(5) && difference < Math.toRadians(360-5)-angleChange){	
			if(difference <= Math.toRadians(180))
				if(bodyAngle >= goalAngle)
					bodyAngle -= angleChange;
				else
					bodyAngle += angleChange;
			else
				if(bodyAngle >= goalAngle)
					bodyAngle += angleChange;
				else
					bodyAngle -= angleChange;
		}
		else
			bodyAngle = goalAngle;
		return bodyAngle;
	}
	public void checkPlayerCollision() {
		if(playerContact >= 0 && !walking) {
			SPlayer p = GamePlay.shooterBois[playerContact];
			
			Vec2 direction = getPos().sub(p.getPos());
			direction.normalize();
			float mag = 100f*center.getMass();
			
			center.applyForceToCenter(direction.mul(mag));
		}
	}
	public void shotCollision(float dmg, int hitterIndex) {
		if(!isHit) {
			isHit = true;
			hitStep = 34;
			int damage = (int)(dmg*7);
			health -= damage;
			GameEngine.scoreBoard.addShotConnect(hitterIndex, playerID, damage);

			if(health <= 0)
			{
				lives --;
				if(health < 0)
					health = 0;
				if(lives == 0)
					isDead = true;
				GameEngine.scoreBoard.addKill(hitterIndex, playerID);
			}
		}
	}
	public ArrayObj serializeToSocket() {
		ArrayObj object = new ArrayObj(PField.Float("x", getPos().x));
		object.addField(PField.Float("y", getPos().y));
		object.addField(PField.Boolean("onGround", walking));
		object.addField(PField.Integer("walkStep", walkStep));
		object.addField(PField.Integer("hitStep", hitStep));
		object.addField(PField.Integer("health", health));
		object.addField(PField.Integer("lives", lives));
		object.addField(PField.Double("bodyAngle", bodyAngle));
		object.addField(PField.Double("headAngle", headAngle));
		object.addField(PField.Integer("charge", charge));
		return object;
	}
}
