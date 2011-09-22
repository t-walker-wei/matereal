package mw2011.bflags;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.Entity;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.gui.*;
import jp.digitalmuseum.mr.gui.utils.VectorFieldPainter;
import jp.digitalmuseum.mr.hakoniwa.Hakoniwa;
import jp.digitalmuseum.mr.hakoniwa.HakoniwaCylinder;
import jp.digitalmuseum.mr.hakoniwa.HakoniwaEntity;
import jp.digitalmuseum.mr.hakoniwa.HakoniwaRobotWithPen;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.message.ImageUpdateEvent;
import jp.digitalmuseum.mr.message.ServiceEvent;
import jp.digitalmuseum.mr.message.ServiceStatus;
import jp.digitalmuseum.mr.resource.WheelsController;
import jp.digitalmuseum.mr.service.LocationProvider;
import jp.digitalmuseum.mr.task.GoForward;
import jp.digitalmuseum.mr.task.MobilePenTask;
import jp.digitalmuseum.mr.task.Move;
import jp.digitalmuseum.mr.task.Stop;
import jp.digitalmuseum.mr.task.Task;
import jp.digitalmuseum.mr.task.VectorFieldTask;
import jp.digitalmuseum.mr.workflow.Action;
import jp.digitalmuseum.mr.workflow.TimeoutTransition;
import jp.digitalmuseum.mr.workflow.Workflow;
import jp.digitalmuseum.utils.Location;
import jp.digitalmuseum.utils.Position;
import jp.digitalmuseum.utils.ScreenPosition;
import jp.digitalmuseum.utils.Vector2D;

import jp.digitalmuseum.mr.vectorfield.CollisionAvoidanceField;
import jp.digitalmuseum.mr.vectorfield.VectorFieldAbstractImpl;

public class RunGame3 {
	private ArrayList<Entity> entities;
	private ArrayList<Entity> object;
	private Hakoniwa hakoniwa;
	private Robot robot1;
	private Robot robot2;
	// private Entity goalEntity;
	// private HashMap<Robot, Entity> goalEntities;
	private Move move = null;
	private ScreenPosition goal = null;

	public static void main(String[] args) {
		new RunGame3();
	}

	public RunGame3() {

		// Run hakoniwa.
		hakoniwa = new Hakoniwa();
		hakoniwa.start();

		robot1 = new HakoniwaRobotWithPen("robot", new Location(hakoniwa.getRealWidth()*3/4, hakoniwa.getRealHeight()*3/4, -Math.PI*3/4));
		robot2 = new HakoniwaRobotWithPen("robot", new Location(hakoniwa.getRealWidth()/4,hakoniwa.getRealHeight()/4,-Math.PI*3/4));

		// goalEntities = new HashMap<Robot, Entity>();

		entities = new ArrayList<Entity>();
		double x = hakoniwa.getRealWidth()/2;
		double y = hakoniwa.getRealHeight()/2;
		double r = 200;
		int num = 6;
		for (int i = 0; i < num; i ++) {
			double theta = Math.PI*i/(num - 1);
			entities.add(new HakoniwaCylinder(
					"Cylinder "+i,
					x + Math.cos(theta) * r,
					y - Math.sin(theta) * r,
					theta,
					20));
		}

		object = new ArrayList<Entity>();
		x = hakoniwa.getRealWidth()/2;
		y = hakoniwa.getRealHeight()/2;
		r = 40;
		num = 4;
		for (int i = 0; i < num; i ++) {
			double theta = Math.PI*i/(num - 1);
			object.add(new HakoniwaCylinder(
					"Cylinder "+i,
					x + Math.cos(theta) * r * (i + 1),
					y - Math.sin(theta) * r * (i + 1),
					theta,
					20));
		}

		// Make a window for showing captured image.
		final DrawableFrame frame = new DrawableFrame() {
			private static final long serialVersionUID = 1L;
			private transient final Stroke stroke =
				new BasicStroke(5);
			private transient final AlphaComposite alphaComp2 =
				AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .7f);
			private transient final VectorFieldPainter vectorFieldPainter =
					new VectorFieldPainter(hakoniwa);

			@Override public void dispose() {
				super.dispose();
				Matereal.getInstance().dispose();
			}

			@Override
			public void paint2D(Graphics2D g) {
				final Task task = robot1.getAssignedTask(WheelsController.class);
				g.setColor(Color.white);
				g.fillRect(0, 0, getFrameWidth(), getFrameHeight());
				final Composite comp = g.getComposite();

				// Draw vectors.
				g.setComposite(alphaComp2);
				g.setColor(Color.black);
				g.fillRect(0, 0, getFrameWidth(), 35);

				// Draw status.
				g.setComposite(comp);
				g.drawLine(0, 35, getFrameWidth(), 35);
				g.setColor(Color.white);
				if (goal == null) {
					g.drawString("Click to set the destination.", 10, 30);
				} else {
					if (task != null) {
						g.drawString("Status: "+task.toString(), 10, 30);
						if (task instanceof VectorFieldTask) {
							g.setColor(Color.red);
							vectorFieldPainter.paint((VectorFieldTask) task, g);
						}
					} else {
						g.drawString("Status: Stopped", 10, 30);
					}
					Stroke s = g.getStroke();
					g.setStroke(stroke);
					g.setColor(Color.black);
					g.drawLine(
							goal.getX()-5,
							goal.getY()-5,
							goal.getX()+5,
							goal.getY()+5);
					g.drawLine(
							goal.getX()-5,
							goal.getY()+5,
							goal.getX()+5,
							goal.getY()-5);
					g.setStroke(s);
				}

				hakoniwa.drawImage(g);
			}
		};
		frame.setResizable(false);
		frame.setFrameSize(hakoniwa.getWidth(), hakoniwa.getHeight());

		// Bring the clicked object to the location in front of the user.
		frame.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					goNextGoal(robot1);
					goNextGoal(robot2);
				}
			}
		});

		frame.getPanel().addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
//				goNextGoal(robot1);
//				goNextGoal(robot2);
				Position mousePosition = hakoniwa.screenToReal(
						new ScreenPosition(e.getX(), e.getY()));
				HakoniwaEntity entity = null;
				double distance = Double.MAX_VALUE;
				for (int i = 0; i < object.size(); i ++) {
					Entity en = object.get(i);
					double d = hakoniwa.getPosition(en).distance(mousePosition);
					System.out.println(d);
					if (d < distance) {
						entity = (HakoniwaEntity) en;
						distance = d;
					}
				}
				entity.setPosition(mousePosition);
			}
		});

		// Repaint the window every time the image is updated.
		hakoniwa.addEventListener(new EventListener() {
			public void eventOccurred(Event e) {
				if (e instanceof ImageUpdateEvent) {
					frame.repaint();
				}
			}
		});
	}

	void goNextGoal(Robot robot) {
		Entity goalEntity = getNearest(robot);
		if (goalEntity == null){
			return;
		}

		// goalEntities.put(robot, goalEntity);

		int x = (int)hakoniwa.getScreenX(goalEntity);
		int y = (int)hakoniwa.getScreenY(goalEntity);
		goal = new ScreenPosition(x,y);

		move = new Move(hakoniwa.screenToReal(goal));
		move.add(new MyField());
		if (move.assign(robot)) {
			final Robot r = robot;
			final Entity g = goalEntity;
			move.addEventListener(new EventListener() {

				public void eventOccurred(Event e) {

					if (e instanceof ServiceEvent) {
						if (((ServiceEvent) e).getStatus() == ServiceStatus.FINISHED) {
							// Entity goalEntity = goalEntities.get(r);
							// entities.remove(goalEntity);
							if(entities.remove(g)){
								// ;
							}
							goNextGoal(r);
						}
					}

				}
			});
			move.start();
		}
	}

	Entity getNearest(Robot robot) {

		Position position1 = hakoniwa.getPosition(robot);

		double distance = Double.MAX_VALUE;
		Entity nearest = null;
		for (int i = 0; i < entities.size(); i ++) {
			Position p = hakoniwa.getPosition(entities.get(i));
			double d= position1.distance(p);
			if (d < distance) {
				distance = d;
				nearest = entities.get(i);
			}
		}
		return nearest;
	}

	private class MyField extends VectorFieldAbstractImpl {
		private static final double MAX_DISTANCE = 100;
		// private Entity entity;
		private Position p;

		public MyField(/* Entity entity */) {
			// this.entity = entity;
			p = new Position();
		}

		public synchronized void getVectorOut(Position position, Vector2D vector) {
			LocationProvider locationProvider = getLocationProvider(); // hakoniwa
			vector.set(0, 0);
			for (Entity e : object) {
			// for (Entity e : locationProvider.getEntities()) {
				// if (e != entity) {
					locationProvider.getPositionOut(e, p);
					if (p.isFound()) {
						p.sub(position);
						double distance = p.getNorm();
						if (distance <= 0.0) {
							// Collision detected. What to do...?
						} else if (distance < MAX_DISTANCE) {
							p.mul((distance - 2*MAX_DISTANCE) / MAX_DISTANCE / MAX_DISTANCE);
							vector.add(p);
						}
					}
				// }
			}
		}
	}
}
