package lunarLander;

import game.gameObject.GameObject;

/**
 * @author Julius Häger
 *
 */
public interface Destroyable extends GameObject {
	
	//JAVADOC: Destroyable
	
	//TODO: Move to game engine?
	
	//TODO: Invincibility
	
	/**
	 * @return
	 */
	public float getHealth();
	
	/**
	 * @param health
	 */
	public void setHealth(float health);
	
	/**
	 * @param damage
	 */
	public void damage(float damage);
	
	/**
	 * 
	 */
	public void destroy();
	
}
