package lunarLander;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import game.Game;
import game.GameInitializer;
import game.GameSettings;
import game.GameSystem;
import game.IO.IOHandler;
import game.IO.load.LoadRequest;
import game.controller.event.engineEvents.GameQuitEvent;
import game.controller.event.engineEvents.SceneLoadEvent;
import game.debug.log.Log.LogImportance;
import game.gameObject.BasicGameObject;
import game.gameObject.GameObject;
import game.gameObject.graphics.Camera;
import game.gameObject.graphics.Paintable;
import game.gameObject.handler.event.GameObjectCreatedEvent;
import game.gameObject.handler.event.GameObjectDestroyedEvent;
import game.gameObject.particles.Particle;
import game.gameObject.particles.ParticleEffector;
import game.gameObject.particles.ParticleEmitter;
import game.gameObject.particles.ParticleSystem;
import game.gameObject.transform.BoxTransform;
import game.gameObject.transform.Transform;
import game.input.mouse.MouseListener;
import game.settings.SettingsUtil;
import game.util.math.ColorUtils;
import game.util.math.MathUtils;

/**
 * @author Julius Häger
 *
 */
public class LunarLander implements GameInitializer{

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Game.log.setLogConsumer((message) -> { System.out.printf("%-50.50s %15.15s: at %s\n", message.getMessage(), message.getImportance(), message.getLogCallSite()); });
		
		GameSettings settings = SettingsUtil.load("./res/Settings.set");
		
		Game game = new Game(settings);
		
		game.run();
		
		/*
		GameSettings settings = GameSettings.createDefaultGameSettings();
		
		settings.putSetting("OnScreenDebug", false);
		
		settings.putSetting("GameInit", new LunarLander());
		
		settings.putSetting("ScreenMode", Screen.Mode.NORMAL);
		
		settings.putSetting("OnScreenDebug", true);
		
		settings.putSetting("DebugID", true);
		
		settings.putSetting("DebugGameSystem", true);
		
		Game game = new Game(settings);
		
		game.run();
		*/
	}
	
	@Override
	public void initialize(Game game, GameSettings settings) {
		settings.getSettingAs("MainCamera", Camera.class).setBackgroundColor(Color.black);
		
		Game.log.setAcceptLevel(LogImportance.DEBUG);
		
		Game.keyHandler.addKeyBinding("Thrust", KeyEvent.VK_W, KeyEvent.VK_UP);
		Game.keyHandler.addKeyBinding("Left", KeyEvent.VK_A, KeyEvent.VK_LEFT);
		Game.keyHandler.addKeyBinding("Right", KeyEvent.VK_D, KeyEvent.VK_RIGHT);
		Game.keyHandler.addKeyBinding("Fire", KeyEvent.VK_SPACE);
		Game.keyHandler.addKeyBinding("Up", KeyEvent.VK_W);
		Game.keyHandler.addKeyBinding("Down", KeyEvent.VK_S);
		
		float width = 20;
		float height = 20;
		
		int[] xpoints = new int[]{
				(int)(0),
				(int)((width/2)),
				(int)(width)
		};
		
		int[] ypoints = new int[]{
				(int)(0),
				(int)(height),
				(int)(0)
		};
		
		Polygon poly = new Polygon(xpoints, ypoints, xpoints.length);
		
		/*
		float landHeight = 400;
		
		Land land = Land.generateLand(0, Game.screen.getHeight() - landHeight - 10, Game.screen.getWidth(), landHeight, 150);
		
		Game.gameObjectHandler.addGameObject(land, "Land");
		*/
		
		Ship ship = new Ship(200, 200, poly, settings.getSettingAs("MainCamera", Camera.class).getBounds());
		
		Game.gameObjectHandler.addGameObject(ship, "Player ship");
		
		//FIXME: Never pass null to the transform constructor!
		BoxTransform<GameObject> transform = new BoxTransform<GameObject>(null, 0, 0, Game.screen.getWidth(), Game.screen.getHeight());
		
		ParticleSystem system = new ParticleSystem(transform, 4, 2000, (p) -> { p.setLifetime(1.5f); p.color = Color.red; });
		
		ParticleEmitter emitter = new ParticleEmitter((system.getWidth()/2)-(width/2), 0, 10, 10, 200);
		
		float exaustSpeed = 150;
		
		emitter.customizer = (p) -> { p.image = 0; p.setLifetime(1.5f); p.setVelocity((float)-(exaustSpeed * -Math.sin(ship.getRotationRad())), (float)-(exaustSpeed * Math.cos(ship.getRotationRad()))); };
		
		emitter.enabled = false;
		
		system.addEmitter(emitter);
		
		system.addEffector(ParticleEffector.createScaleOverLifetimeEffector(ParticleEffector.ACCEPT_ALL, (r) -> { return Math.max(1.2f * r, 0.2f); })); 
		
		float shiftPoint = 0.92f;
		
		system.addEffector(ParticleEffector.createColorOverLifetimeEffector((p, d) -> {
			return p.image == 0;
		}, (r) -> { 
			return r <= shiftPoint ?
					ColorUtils.Lerp(Color.yellow, Color.red, MathUtils.map(r, 0f, shiftPoint, 0, 1)) :
					ColorUtils.Lerp(Color.red, Color.blue, MathUtils.map(r, shiftPoint, 1f, 0, 1));
		}));
		
		system.addEffector(new ParticleEffector() {
			
			@Override
			public void effect(Particle particle, float deltaTime) {
				particle.accX -= particle.accX * (1f * deltaTime);
				particle.accY -= particle.accY * (1f * deltaTime);

				particle.dx -= particle.dx * (1f * deltaTime);
				particle.dy -= particle.dy * (1f * deltaTime);
			}
		});
		
		Game.gameObjectHandler.addGameObject(system, "Exaust");
		
		BufferedImage particleImage = null;
		
		try {
			particleImage = IOHandler.load(new LoadRequest<BufferedImage>("StandardParticle", new File("./res/particles/StandardParticle_10.png"), BufferedImage.class)).result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		system.addImage(0, particleImage);
		system.addImage(1, particleImage);
		
		//NOTE: Is this a good practice?
		new GameSystem("Exaust Updater") {
			
			CopyOnWriteArrayList<ParticleEmitter> projectileEmitters;
			
			CopyOnWriteArrayList<Projectile> projectiles = new CopyOnWriteArrayList<>();
			
			{
				Game.eventMachine.addEventListener(GameQuitEvent.class, (event) -> { destroy(); });
				Game.eventMachine.addEventListener(SceneLoadEvent.class, (event) -> { destroy(); });
				Game.eventMachine.addEventListener(GameObjectCreatedEvent.class, (event) -> { 
					if(event.object instanceof Projectile) {
						projectiles.add((Projectile) event.object);
					}
				});
				Game.eventMachine.addEventListener(GameObjectDestroyedEvent.class, (event) -> {
					if (event.object instanceof Projectile) {
						projectiles.remove(event.object);
					}
				});
			}
			
			@Override
			public void earlyUpdate(float deltaTime) {
				if(projectileEmitters == null){
					projectileEmitters = new CopyOnWriteArrayList<>();
					for (int i = 0; i < 50; i++) {
						ParticleEmitter emitter = new ParticleEmitter(0, 0, 5, 100);
						
						system.addEmitter(emitter);
						
						projectileEmitters.add(emitter);
					}
				}
				
				Game.log.logDebug("Number of projectiles: " + projectiles.size());
				
				int i = 0;
				for (ParticleEmitter emitter : projectileEmitters) {
					if(i < projectiles.size()){
						Projectile p = projectiles.get(i);
						
						emitter.x = p.getX();
						emitter.y = p.getY();

						//FIXME: Find a better way to color the particles!
						emitter.customizer = (particle) -> { particle.image = 1; particle.setLifetime(0.5f); particle.color = p.color; };
						
						emitter.enabled = true;
					}else{
						emitter.enabled = false;
					}
					i++;
				}
				
				emitter.x = ship.getX() - (emitter.width/2);
				emitter.y = ship.getY() - (emitter.height/2);
			}
			
			@Override
			public void lateUpdate(float deltaTime) {
				emitter.enabled = ship.isThrusting();
			}
		};
		
		//Enemy enemy = new Enemy(new Transform(), poly, 5, ship);
		
		//Game.gameObjectHandler.addGameObject(enemy, "Enemy");
		
		new GameSystem("Enemy Spawner") {
			
			float intervalMax = 10;
			float intervalMin = 2;
			
			float timer = 0;
			
			Random rand = new Random();
			
			{
				Game.eventMachine.addEventListener(GameQuitEvent.class, (event) -> { destroy(); });
				Game.eventMachine.addEventListener(SceneLoadEvent.class, (event) -> { destroy(); });
			}
			
			@Override
			public void earlyUpdate(float deltaTime) {
				
				timer -= deltaTime;
				
				if(timer <= 0){
					
					//Spawn enemy
					
					Game.gameObjectHandler.addGameObject(new Enemy(new Transform<GameObject>(null), poly, 5, ship), "Enemy");
					
					timer = intervalMin + (rand.nextFloat() * (intervalMax - intervalMin));
				}
				
			}
			
			@Override
			public void lateUpdate(float deltaTime) {
				
			}
		};
		
		//TODO: Remove or find a better place for this code
		
		/*
		BufferedImage particleImage = null;
		
		try {
			particleImage = IOHandler.load(new LoadRequest<BufferedImage>("StandardParticle", new File("./res/particles/StandardParticle_10.png"), BufferedImage.class)).result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Random rand = new Random();
		
		float outerPadding = 200;
		
		ParticleSystem system = new ParticleSystem(-outerPadding, -outerPadding, new Rectangle2D.Float(0, 0, Game.screen.getWidth() + (outerPadding * 2), Game.screen.getHeight() + (outerPadding * 2)), 10, 1000, (p) -> { 
			
			p.x = rand.nextFloat() * 200;
			p.y = rand.nextFloat() * 200;
			
			//p.dx = (rand.nextFloat() - 0.5f) * 100;
			//p.dy = (rand.nextFloat() - 0.5f) * 100;
			
			p.lifetime = p.currLifetime = 10 + (rand.nextFloat() * 10);
			
			p.color = rand.nextBoolean() ? Color.blue : Color.red;
		});
		
		system.addImage(0, particleImage);
		
		system.edgeAction = (p) -> {};
		
		float emission = 50f;
		
		float padding = outerPadding + 200;
		
		ParticleCustomizer customizer = (p) -> {
			p.color = rand.nextBoolean() ? Color.blue : Color.red;
			p.setVelocity((rand.nextFloat() - 0.5f) * 100, (rand.nextFloat() - 0.5f) * 100);
			
			p.lifetime = p.currLifetime = 100;
		};
		
		ParticleEmitter emitter = new ParticleEmitter(padding, padding, 50, 50, emission + rand.nextFloat());
		
		emitter.customizer = customizer;
		
		system.addEmitter(emitter);
		
		emitter = new ParticleEmitter(system.getWidth() - padding, padding, 50, 50, emission + rand.nextFloat());

		emitter.customizer = customizer;
		
		system.addEmitter(emitter);
		
		emitter = new ParticleEmitter(padding, system.getHeight() - padding, 50, 50, emission + rand.nextFloat());

		emitter.customizer = customizer;
		
		system.addEmitter(emitter);
		
		emitter = new ParticleEmitter(system.getWidth() - padding, system.getHeight() - padding, 50, 50, emission + rand.nextFloat());		

		emitter.customizer = customizer;
		
		system.addEmitter(emitter);
		
		float force = 100;
		
		Point2D.Float point = new Point2D.Float(75, 75);
		
		system.addEffector(new ParticleEffector() {
			float timer = 0;
			
			@Override
			public void effect(Particle particle, float deltaTime) {
				particle.dx += ((particle.x - (point.x + 30))) * -force * deltaTime;
				particle.dy += ((particle.y - (point.y + 30))) * -force * deltaTime;
				
				timer += deltaTime;
				point.x = (float) (75 + (Math.sin(timer) * 10));
				point.y = (float) (75 + (Math.sin(timer) * 10));
			}
		});
		
		system.addEffector(new ParticleEffector() {
			float timer = 0;
			
			@Override
			public void effect(Particle particle, float deltaTime) {
				timer += deltaTime / 100;
				
				particle.dx += rand.nextFloat() * Math.sin(timer) * force * deltaTime;
				particle.dy += rand.nextFloat() * Math.cos(timer) * force * deltaTime;
			}
		});
		
		system.addEffector(new ParticleEffector() {
			
			@Override
			public void effect(Particle particle, float deltaTime) {
				//particle.setVelocity(MathUtils.Lerpf(0, particle.dx, 0.999f), MathUtils.Lerpf(0, particle.dy, 0.999f));
			}
		}); 
		
		system.addEffector(ParticleEffector.createColorOverLifetimeEffector(
				(p, delta) -> {
					return p.color.getRed() < p.color.getBlue(); 
				},
				(ratio) -> {
					return ColorUtils.Lerp(Color.CYAN, Color.blue, ratio);
				}
		));
		
		system.addEffector(ParticleEffector.createColorOverLifetimeEffector(
				(p, delta) -> {
					return p.color.getRed() > p.color.getBlue(); 
				},
				(ratio) -> {
					return ColorUtils.Lerp(Color.orange, Color.red, ratio);
				}
		));
		
		Game.gameObjectHandler.addGameObject(system, "Particle test");
		
		Test t = new Test(0, 0, 100, 100, 5, system);
		
		Game.gameObjectHandler.addGameObject(t ," test");
		
		*/
	}
	
	/**
	 * @author Julius Häger
	 *
	 */
	public class Test extends BasicGameObject implements MouseListener, Paintable {

		Point2D point = new Point2D.Float();
		
		float force = 1000f;
		
		ParticleSystem system;
		
		/**
		 * @param x
		 * @param y
		 * @param width
		 * @param height
		 * @param zOrder
		 * @param system
		 */
		public Test(float x, float y, float width, float height, int zOrder, ParticleSystem system) {
			super(x, y, width, height, zOrder);
			
			this.system = system;
			
			system.addEffector(new ParticleEffector() {
				
				@Override
				public void effect(Particle particle, float deltaTime) {
					float dist = (float) point.distance(new Point2D.Float((system.getX() + particle.x), (system.getY() + particle.y)));
					
					particle.dx += 1/(dist * dist) * ((system.getX() + particle.x) - point.getX()) * -force * deltaTime;
					particle.dy += 1/(dist * dist) * ((system.getY() + particle.y) - point.getY()) * -force * deltaTime;
				}
			});
		}
		
		@Override
		public void paint(Graphics2D g2d) {
			g2d.setColor(Color.white);
			g2d.drawRect((int)point.getX(), (int)point.getY(), 5, 5);
		}
		
		@Override
		public BufferedImage getImage() {
			return null;
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			point = e.getPoint();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			point = e.getPoint();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			point = e.getPoint();
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			point = e.getPoint();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			point = e.getPoint();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			point = e.getPoint();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			point = e.getPoint();
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			point = e.getPoint();
		}

		@Override
		public boolean absorb() {
			return false;
		}

		@Override
		public boolean souldReceiveMouseInput() {
			return true;
		}

		@Override
		public Rectangle2D getBounds() {
			return super.getBounds();
		}
	}
}
