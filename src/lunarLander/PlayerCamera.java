package lunarLander;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import game.gameObject.graphics.Camera;
import game.screen.ScreenRect;

public class PlayerCamera extends Camera {

	//private Ship ship;
	
	public PlayerCamera(Rectangle2D.Float rect, ScreenRect screenRect, Color bgColor) {
		super(rect, screenRect, bgColor);
	}
	
	public void setPlayer(Ship ship){
		//this.ship = ship;
		
		//TODO: Fix camera rendering and child transforms
		transform.setParent(ship.getTransform());
		//transform.setRotation(180);
		//transform.setPosition(getWidth()/2, getHeight()/2);
	}
}
