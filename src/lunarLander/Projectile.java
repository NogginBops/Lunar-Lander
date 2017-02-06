package lunarLander;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import game.Game;
import game.gameObject.GameObject;
import game.gameObject.graphics.Paintable;
import game.gameObject.physics.BasicRotatable;
import game.gameObject.physics.Collidable;

/**
 * @author Julius Häger
 *
 */
public class Projectile extends BasicRotatable implements Collidable, Paintable{
	
	protected float damage;
	
	protected Color color = Color.white;
	
	protected float speed;
	
	protected float lifetime = 5;
	protected float lifetimeTimer = lifetime;
	
	protected GameObject shooter;
	
	private Rectangle2D outerBounds;
	
	/**
	 * @param x
	 * @param y
	 * @param shape
	 * @param rotation
	 * @param damage
	 * @param speed
	 * @param shooter 
	 * @param outerBounds 
	 */
	public Projectile(float x, float y, Shape shape, float rotation, float damage, float speed, GameObject shooter, Rectangle2D outerBounds) {
		super(x, y, shape, 5, rotation);
		this.damage = damage;
		
		this.speed = speed;
		
		this.shooter = shooter;
		
		this.outerBounds = outerBounds;
		
		setVelocity((float)-(speed * Math.sin(Math.toRadians(rotation))), (float)(speed * Math.cos(Math.toRadians(rotation))));
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		
		lifetimeTimer -= deltaTime;
		
		if(lifetimeTimer <= 0){
			Game.gameObjectHandler.removeGameObject(this);
		}
		
		if(outerBounds.contains(getBounds()) == false){
			Rectangle2D bounds = getBounds();
			
			if(outerBounds.getMinX() > bounds.getMinX() || outerBounds.getMaxX() < bounds.getMaxX()){
				setDX(-getDX());
			}
			
			if(outerBounds.getMinY() > bounds.getMinY() || outerBounds.getMaxY() < bounds.getMaxY()){
				setDY(-getDY());
			}
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
		if(collisionObject == shooter){
			return;
		}
		
		if(collisionObject instanceof Destroyable){
			((Destroyable) collisionObject).damage(damage);
			
			Game.gameObjectHandler.removeGameObject(this);
		}
	}
}
