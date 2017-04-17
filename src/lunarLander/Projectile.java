package lunarLander;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import game.Game;
import game.gameObject.GameObject;
import game.gameObject.graphics.Paintable;
import game.gameObject.physics.BasicRotatable;
import game.gameObject.physics.Collidable;
import game.util.math.vector.Vector2D;

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
	
	protected float homing = 0.1f;
	protected float homingRadius = 100;
	
	protected float maxVel = 200;
	
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
		
		setVelocity(Vector2D.mult(
				Vector2D.normalize(
						new Vector2D((float)-(speed * Math.sin(Math.toRadians(rotation))),
								(float)(speed * Math.cos(Math.toRadians(rotation))))),
				maxVel));
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
		
		Collidable[] collidables = Game.physicsEngine.overlapShape(new Ellipse2D.Float(transform.getX() - homingRadius, transform.getY() - homingRadius, homingRadius * 2, homingRadius * 2), 0, 1, 2, 3, 4, 5);
		
		for (Collidable collidable : collidables) {
			if (collidable instanceof Enemy) {
				setVelocity(getDX() + ((collidable.getX() - getX()) * homing), getDY() + ((collidable.getY() - getY()) * homing));				
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
