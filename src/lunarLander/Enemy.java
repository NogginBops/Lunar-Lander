package lunarLander;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import game.Game;
import game.gameObject.GameObject;
import game.gameObject.graphics.Paintable;
import game.gameObject.physics.BasicRotatable;
import game.gameObject.physics.Collidable;
import game.gameObject.transform.Transform;
import game.util.math.vector.Vector2D;

/**
 * @author Julius Häger
 *
 */
public class Enemy extends BasicRotatable implements Paintable, Collidable, Destroyable{

	protected float health = 1;
	
	protected float speed = 40;
	
	protected Color color = Color.white;
	
	protected Ship target;
	
	/**
	 * @param transform
	 * @param shape
	 * @param zOrder
	 * @param target
	 */
	public Enemy(Transform<GameObject> transform, Shape shape, int zOrder, Ship target) {
		super(transform, shape, zOrder, 0);
		
		this.target = target;
	}
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		
		setVelocity(Vector2D.mult(Vector2D.normalize(new Vector2D( target.getX() - getX(), target.getY() - getY() )), speed));
		
		transform.setRotation(Vector2D.angle(Vector2D.UP, getVelocity()));
	}
	
	@Override
	public BufferedImage getImage() {
		return null;
	}
	
	@Override
	public void paint(Graphics2D g2d) {
		g2d.setColor(color);
		
		g2d.fill(getTranformedShape());
	}

	@Override
	public void hasCollided(Collidable collisionObject) {
		if(collisionObject instanceof Ship){
			((Ship)collisionObject).damage(1);
		}
	}
	
	@Override
	public float getHealth() {
		return health;
	}

	@Override
	public void setHealth(float health) {
		this.health = health;
	}

	@Override
	public void damage(float damage) {
		health -= damage;
		if(health < 0){
			destroy();
		}
	}

	@Override
	public void destroy() {
		Game.gameObjectHandler.removeGameObject(this);
	}
}
