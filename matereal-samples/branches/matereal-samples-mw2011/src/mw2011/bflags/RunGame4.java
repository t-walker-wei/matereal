package mw2011.bflags;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

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
import jp.digitalmuseum.mr.message.LocationUpdateEvent;
import jp.digitalmuseum.mr.resource.WheelsController;
import jp.digitalmuseum.mr.service.LocationProvider;
import jp.digitalmuseum.mr.task.Move;
import jp.digitalmuseum.mr.task.Task;
import jp.digitalmuseum.mr.task.VectorFieldTask;
import jp.digitalmuseum.utils.Location;
import jp.digitalmuseum.utils.Position;
import jp.digitalmuseum.utils.ScreenPosition;
import jp.digitalmuseum.utils.Vector2D;

import jp.digitalmuseum.mr.vectorfield.VectorFieldAbstractImpl;

public class RunGame4 {
	private ArrayList<Entity> objects;
	private Hakoniwa hakoniwa;
	private Robot robot1;
	private Robot robot2;
	private Robot robot3;
	private Robot robot4;
	private boolean finished = false;
	private int finishedrobot;
	private int goalSize = 40;

	// private Entity goalEntity;
	// private HashMap<Robot, Entity> goalEntities;
	private Move move = null;
	private ScreenPosition goal = null;

	public static void main(String[] args) {
		new RunGame4();
	}

	public RunGame4() {

		// Run hakoniwa.
		hakoniwa = new Hakoniwa();
		hakoniwa.start();

		robot1 = new HakoniwaRobotWithPen("robot", new Location(0, 0, -Math.PI*3/4));
		robot2 = new HakoniwaRobotWithPen("robot", new Location(hakoniwa.getRealWidth(), 0 ,-Math.PI*3/4));
		robot3 = new HakoniwaRobotWithPen("robot", new Location(hakoniwa.getRealWidth(),hakoniwa.getRealHeight(),0 ));
		robot4 = new HakoniwaRobotWithPen("robot", new Location(0,hakoniwa.getRealHeight(),0));

		// goalEntities = new HashMap<Robot, Entity>();

		/*
//		entities = new ArrayList<Entity>();
	//	double x = hakoniwa.getRealWidth()/2;
		//double y = hakoniwa.getRealHeight()/2;
	//	double r = 200;
		//int num = 6;
		for (int i = 0; i < num; i ++) {
			double theta = Math.PI*i/(num - 1);
			entities.add(new HakoniwaCylinder(
					"Cylinder "+i,
					x + Math.cos(theta) * r,
					y - Math.sin(theta) * r,
					theta,
					20));
		}
		*/

		double x, y, r;
		int num;

		objects = new ArrayList<Entity>();
		x = hakoniwa.getRealWidth()/2;
		y = hakoniwa.getRealHeight()/2;
		r = 40;
		num = 4;
		for (int i = 0; i < num; i ++) {
			double theta = Math.PI*i/(num - 1);
			objects.add(new HakoniwaCylinder(
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

				ScreenPosition sp = hakoniwa.getScreenPosition(robot1);
				if (sp.isFound()) {
					g.setColor(Color.cyan);
					g.drawOval(sp.getX()-20, sp.getY()-20, 40, 40);
				}
				sp = hakoniwa.getScreenPosition(robot3);
				if(sp.isFound()){
					g.setColor(Color.cyan);
					g.drawOval(sp.getX()-20,sp.getY()-20,40,40);
				}
				sp = hakoniwa.getScreenPosition(robot2);
				if(sp.isFound()){
					g.setColor(Color.green);
					g.drawOval(sp.getX()-20,sp.getY()-20,40,40);
				}
				sp = hakoniwa.getScreenPosition(robot4);
				if(sp.isFound()){
					g.setColor(Color.green);
					g.drawOval(sp.getX()-20,sp.getY()-20,40,40);
				}

				Stroke s = g.getStroke();
				g.setStroke(stroke);
				g.setColor(Color.blue);
				g.drawOval(
						hakoniwa.getWidth()/2 - goalSize, hakoniwa.getHeight()/2 - goalSize,
						goalSize*2, goalSize*2);
				g.setStroke(s);

				if (finished) {
					Font font = g.getFont().deriveFont(Font.BOLD, 64f);//Font.getFont("Arial");
					g.setColor(Color.red);
					g.setFont(font);
					String goal = "";
					if(finishedrobot == 1){
						goal = "GOAL!CeanPlayer!!";
					}
					if(finishedrobot == 2){
						goal = "GOAL!GreenPlayer!!";
					}
					if(finishedrobot == 3){
						goal = "GOAL!CeanPlayer!!";
					}
					if(finishedrobot == 4){
						goal = "GOAL!GreenPlayer!!";
					}
					int w = g.getFontMetrics().stringWidth(goal);
					g.drawString(goal, (hakoniwa.getWidth()-w)/2, hakoniwa.getHeight()/2);
				}
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
					goNextGoal(robot3);
					goNextGoal(robot4);
				}
			}
		});

		frame.getPanel().addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				Position mousePosition = hakoniwa.screenToReal(
						new ScreenPosition(e.getX(), e.getY()));
				HakoniwaEntity entity = null;
				double distance = Double.MAX_VALUE;
				for (int i = 0; i < objects.size(); i ++) {
					Entity en = objects.get(i);
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
				if (e instanceof LocationUpdateEvent) {
					/*
					Position p = hakoniwa.screenToReal(new ScreenPosition(
							hakoniwa.getWidth()/2, hakoniwa.getHeight()/2));
					*/
					ScreenPosition sp = new ScreenPosition(hakoniwa.getWidth()/2, hakoniwa.getHeight()/2);
					ScreenPosition rp;
					double minmum = Double.MAX_VALUE;
					int robotnumber = 0;
					rp = hakoniwa.getScreenPosition(robot1);
					double distanceSq1 = (rp.isNotFound() ? Double.MAX_VALUE : rp.distanceSq(sp));
					if(distanceSq1 < minmum){
						minmum = distanceSq1;
						robotnumber = 1;
					}
					rp = hakoniwa.getScreenPosition(robot2);
					double distanceSq2 = (rp.isNotFound() ? Double.MAX_VALUE : rp.distanceSq(sp));
					if(distanceSq2 < minmum){
						minmum = distanceSq2;
						robotnumber = 2;
					}
					rp = hakoniwa.getScreenPosition(robot3);
					double distanceSq3 = (rp.isNotFound() ? Double.MAX_VALUE : rp.distanceSq(sp));
					if(distanceSq3 < minmum){
						minmum = distanceSq3;
						robotnumber = 3;
					}
					rp = hakoniwa.getScreenPosition(robot4);
					double distanceSq4 = (rp.isNotFound() ? Double.MAX_VALUE : rp.distanceSq(sp));
					if(distanceSq4 < minmum){
						minmum = distanceSq4;
						robotnumber = 4;
					}
					double goalSizeSq = goalSize * goalSize;
					if (distanceSq1 < goalSizeSq ||
							distanceSq2 < goalSizeSq || distanceSq3 < goalSizeSq || distanceSq4 < goalSizeSq){
							finished = true;
							finishedrobot = robotnumber;

							Task task;
							task = robot1.getAssignedTask(WheelsController.class);
							if (task != null) {
								task.stop();
							}
							task = robot2.getAssignedTask(WheelsController.class);
							if (task != null) {
								task.stop();
							}
							task = robot3.getAssignedTask(WheelsController.class);
							if (task != null) {
								task.stop();
							}
							task = robot4.getAssignedTask(WheelsController.class);
							if (task != null) {
								task.stop();
							}

					}
				}
			}
		});
	}

	void goNextGoal(Robot robot) {
		goal = new ScreenPosition(hakoniwa.getWidth()/2, hakoniwa.getHeight()/2);
		move = new Move(hakoniwa.screenToReal(goal));
		move.add(new MyField());
		if (move.assign(robot)) {
			move.start();
		}
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
			for (Entity e : objects) {
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
