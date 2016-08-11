package lunarLander;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import game.Game;
import game.gameObject.graphics.Paintable;
import game.gameObject.physics.BasicRotatable;
import game.gameObject.physics.Collidable;

public class Projectile extends BasicRotatable implements Collidable, Paintable{
	
	protected float damage;
	
	protected Color color = Color.white;
	
	protected float speed;
	
	protected float lifetime = 5;
	protected float lifetimeTimer = lifetime;
	
	public Projectile(float x, float y, Shape shape, float rotation, float damage, float speed) {
		super(x, y, shape, 4, rotation);
		this.damage = damage;
		
		this.speed = speed;
		
		setVelocity((float)-(speed * Math.sin(Math.toRadians(rotation))), (float)(speed * Math.cos(Math.toRadians(rotation))));
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		
		lifetimeTimer -= deltaTime;
		
		if(lifetimeTimer <= 0){
			Game.gameObjectHandler.removeGameObject(this);
		}
	}
	
	@Override
	public void paint(Graphics2D g2d) {
		g2d.setColor(color);
		
		g2d.fill(getTranformedShape());
	}

	@Override
	public BufferedImage getImage() {
		return null;
	}

	@Override
	public void hasCollided(Collidable collisionObject) {
		if(collisionObject instanceof Destroyable){
			((Destroyable) collisionObject).damage(damage);
		}
	}
}
