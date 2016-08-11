package lunarLander;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import game.gameObject.graphics.Paintable;
import game.gameObject.physics.BasicCollidable;
import game.gameObject.physics.Collidable;
import game.util.math.MathUtils;

public class Land extends BasicCollidable implements Paintable {
	
	public static float landingSpotLength = 0.02f;
	
	private static float[] smoothingKernel = new float[]{ 1/44f, 3/44f, 5/44f, 8/44f, 10/44f, 8/44f, 5/44f, 3/44f, 1/44f };
	
	private float[] heights;
	private float spacing;
	
	private boolean[] colliding;
	
	public static Land generateLand(float x, float y, float width, float height, int res){
		float spacing = width / res;
		
		Random rand = new Random();
		
		Path2D.Float path2d = new Path2D.Float();
		
		int[] landingSpots = new int[4];
		
		float[] heights = new float[res + 1];
		
		for (int i = 0; i < landingSpots.length; i++) {
			landingSpots[i] = rand.nextInt((res) - (int)(res * landingSpotLength)) + 1;
		}
		
		for (int i = 0; i < heights.length; i++) {
			heights[i] = MathUtils.clamp(rand.nextFloat() * height, 0, height);
		}
		
		//Filter
		for(int i = 0; i < heights.length; i++) {
			float average = 0;
			for (int j = 0; j < smoothingKernel.length; j++) {
				average += MathUtils.isOutside(i - (smoothingKernel.length/2) + j, 0, heights.length - 1) == 0 ? (heights[i - (smoothingKernel.length/2) + j]) * (smoothingKernel[j]) : 0;
			}
			average /= smoothingKernel.length;
			
			heights[i] = average;
		}
		
		for (int i = 0; i < landingSpots.length; i++) {
			for (int j = 0; j <= res * landingSpotLength; j++) {
				heights[landingSpots[i] + j] = heights[landingSpots[i] + j - 1];
			}
		}
		
		path2d.moveTo(0, height);
		for (int i = 0; i < heights.length; i++) {
			path2d.lineTo(i * spacing, heights[i]);
		}
		path2d.lineTo(width, height);
		path2d.closePath();
		
		return new Land(x, (int)((y)), path2d, heights, spacing);
	}
	
	private Land(float x, float y, Path2D.Float path, float[] heights, float spacing) {
		super(x, y, path, 5);
		this.heights = heights;
		this.spacing = spacing;
		colliding = new boolean[heights.length];
	}

	@Override
	public void paint(Graphics2D g2d) {
		g2d.setColor(Color.gray);
		
		g2d.fill(getTranformedShape());
		
		/*g2d.setColor(Color.blue);
		
		g2d.draw(getBounds());
		
		g2d.setColor(Color.CYAN);
		
		g2d.draw(getTranformedShape());
		
		g2d.setColor(Color.gray);
		
		g2d.draw(getCollitionShape());
		*/
		
		for (int i = 0; i < heights.length; i++) {
			g2d.setColor(colliding[i] ? Color.yellow : Color.MAGENTA);
			
			g2d.fillRect((int)(transform.getX() + (i * spacing)), (int)(transform.getY() + heights[i]), colliding[i] ? 4 : 2, colliding[i] ? 4 : 2);
		}
	}

	@Override
	public BufferedImage getImage() {
		return null;
	}
	
	@Override
	public void hasCollided(Collidable collisionObject) {
		super.hasCollided(collisionObject);
		
		if(collisionObject instanceof Ship){
			Shape collitionShape = collisionObject.getCollitionShape();
			for (int i = 0; i < heights.length; i++) {
				colliding[i] = collitionShape.contains(transform.getX() + (i * spacing), transform.getY() + heights[i]);
			}
		}
	}

}
