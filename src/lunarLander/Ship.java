package lunarLander;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

import game.Game;
import game.gameObject.graphics.Paintable;
import game.gameObject.physics.BasicRotatable;
import game.gameObject.physics.Collidable;
import game.gameObject.transform.BoxTransform;
import game.input.keys.KeyListener;
import game.util.math.ColorUtils;
import game.util.math.MathUtils;

public class Ship extends BasicRotatable implements Paintable, Collidable, KeyListener {
	
	private float rotationSpeed = 180f;
	
	private float acceleration = 0.05f;

	private boolean rotatingLeft = false;
	private boolean rotatingRight = false;
	private boolean applyingForce = false;
	private boolean shooting = false;
	
	private float fuel = 5000;
	
	private float thrustFuelDrain = 100;
	
	private float rotationFuelDrain = 10;
	
	private float shootCooldown = 0.2f;
	private float cooldownTimer = 0;
	
	private float angle = 0;
	
	public Ship(float x, float y, Shape shape) {
		super(x, y, shape, 5, 0);
		
		transform = new BoxTransform(x, y, getWidth(), getHeight(), 0.5f, 0.25f);
		
		transform.setRotation(180);
		
		Game.screen.addDebugText(() -> {
			return new String[]{
					"Rotation: " + transform.getRotation(),
					"Dynamic Rotation: " + dr
			};
		});
	}
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		
		cooldownTimer -= deltaTime;
		cooldownTimer = Math.max(cooldownTimer, 0);
		
		if(fuel > 0){
			if(rotatingLeft && !rotatingRight){
				fuel -= rotationFuelDrain * deltaTime;
				dr -= rotationSpeed * deltaTime;
			}
			
			if(rotatingRight && !rotatingLeft){
				fuel -= rotationFuelDrain * deltaTime;
				dr += rotationSpeed * deltaTime;
			}
			
			if(applyingForce){
				fuel -= thrustFuelDrain * deltaTime;
				
				float dirX = -(float) Math.sin(Math.toRadians(transform.getRotation()));
				float dirY = (float) Math.cos(Math.toRadians(transform.getRotation()));
				
				setVelocity(getDX() + (dirX * acceleration), getDY() + (dirY * acceleration));
			}else{
				
			}
			
			//Should shots be another recource
			if(shooting && cooldownTimer <= 0){
				cooldownTimer = shootCooldown;
				Projectile shot = new Projectile(transform.getX(), transform.getY(), new Ellipse2D.Float(-5, -5, 10, 10), transform.getRotation(), 1, 300);
				shot.color = ColorUtils.fromHSV(angle, 1, 1);
				angle += 276;
				Game.gameObjectHandler.addGameObject(shot, "Shot");
			}
		}
		dr = MathUtils.Lerpf(dr, 0, 0.002f);
		
		//TODO: A better system for gravity
		//setDY(getDY() + 0.01f);
	}
	
	@Override
	public void paint(Graphics2D g2d) {
		g2d.setColor(Color.white);
		
		g2d.drawString("Fuel: " + fuel, 150, 20);
		/*
		g2d.setColor(Color.blue);
		
		g2d.draw(getBounds());
		*/
		g2d.setColor(Color.white);
		
		g2d.fill(getTranformedShape());
		/*
		g2d.setColor(Color.WHITE);
		
		g2d.draw(getCollitionShape());
		
		g2d.setColor(Color.green);
		
		g2d.drawLine((int)transform.getX(), (int)transform.getY(), (int)((transform.getX() + Math.cos(transform.getRotationRad() + (Math.PI/2)) * 25)), (int)((transform.getY() + Math.sin(transform.getRotationRad() + (Math.PI/2)) * 25)));
		
		g2d.setColor(Color.magenta);
		
		g2d.drawLine((int)transform.getX(), (int)transform.getY(), (int)((transform.getX() + dx)), (int)((transform.getY() + dy)));
		*/
	}

	@Override
	public BufferedImage getImage() {
		return null;
	}
	
	@Override
	public void hasCollided(Collidable collisionObject) {
		//TODO: Check the angle
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		rotatingRight = Game.keyHandler.isBound("Right", e.getKeyCode()) ? true : rotatingRight;
		rotatingLeft = Game.keyHandler.isBound("Left", e.getKeyCode()) ? true : rotatingLeft;
		applyingForce = Game.keyHandler.isBound("Thrust", e.getKeyCode()) ? true : applyingForce;
		shooting = Game.keyHandler.isBound("Fire", e.getKeyCode()) ? true : shooting;
	}

	@Override
	public void keyReleased(KeyEvent e) {
		rotatingRight = Game.keyHandler.isBound("Right", e.getKeyCode()) ? false : rotatingRight;
		rotatingLeft = Game.keyHandler.isBound("Left", e.getKeyCode()) ? false : rotatingLeft;
		applyingForce = Game.keyHandler.isBound("Thrust", e.getKeyCode()) ? false : applyingForce;
		shooting = Game.keyHandler.isBound("Fire", e.getKeyCode()) ? false : shooting;
	}

	@Override
	public boolean shouldReceiveKeyboardInput() {
		return true;
	}
	
	public boolean isThrusting(){
		return applyingForce && (fuel > 0);
	}
}
