package mw2011.bflags;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.Entity;
import jp.digitalmuseum.mr.entity.EntityImpl;
import jp.digitalmuseum.mr.entity.Noopy2;
import jp.digitalmuseum.mr.entity.PhysicalRobot;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.gui.*;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.message.ImageUpdateEvent;
import jp.digitalmuseum.mr.message.LocationUpdateEvent;
import jp.digitalmuseum.mr.resource.WheelsController;
import jp.digitalmuseum.mr.service.Camera;
import jp.digitalmuseum.mr.service.LocationProvider;
import jp.digitalmuseum.mr.service.MarkerDetector;
import jp.digitalmuseum.mr.task.Move;
import jp.digitalmuseum.mr.task.Task;
import jp.digitalmuseum.utils.Position;
import jp.digitalmuseum.utils.ScreenPosition;
import jp.digitalmuseum.utils.Vector2D;

import jp.digitalmuseum.mr.vectorfield.VectorFieldAbstractImpl;
import jp.digitalmuseum.napkit.NapDetectionResult;
import jp.digitalmuseum.napkit.NapMarker;

public class RunGameReal {
	private ArrayList<Entity> objects;
	private Camera camera;
	private MarkerDetector detector;
	private PhysicalRobot robot1;
	private PhysicalRobot robot2;
	private PhysicalRobot robot3;
	private PhysicalRobot robot4;
	private boolean finished = false;
	private int finishedrobot;
	private int goalSize = 40;

	private Move move = null;
	private ScreenPosition goal = null;

	public static void main(String[] args) {
		new RunGameReal();
	}

	public RunGameReal() {

		Matereal.getInstance().showDebugFrame();

		robot1 = new Noopy2("btspp://646E6C00DCAA");
		robot2 = new Noopy2("btspp://646E6C00DCB2");
		robot3 = new Noopy2("btspp://646E6C00DCAC");
		robot4 = new Noopy2("btspp://646E6C00A8E2");

		int num;
		objects = new ArrayList<Entity>();
		num = 4;
		for (int i = 0; i < num; i ++) {
			objects.add(new EntityImpl());
		}

		// Run a camera.
		// Let users select a device to capture images.
		String identifier = (String) JOptionPane.showInputDialog(null,
				"Select a device to capture images.", "Device list",
				JOptionPane.QUESTION_MESSAGE, null, Camera.queryIdentifiers(), null);
		camera = new Camera(identifier);
		camera.start();

		// Run a marker detector.
		detector = new MarkerDetector();
		detector.addMarker(new NapMarker("markers/4x4_907.patt", 5.5), robot1);
		detector.addMarker(new NapMarker("markers/4x4_112.patt", 5.5), robot2);
		detector.addMarker(new NapMarker("markers/4x4_78.patt", 5.5), robot3);
		detector.addMarker(new NapMarker("markers/4x4_71.patt", 5.5), robot4);
		detector.addMarker(new NapMarker("markers/4x4_35.patt", 5.5), objects.get(0));
		detector.addMarker(new NapMarker("markers/4x4_91.patt", 5.5), objects.get(1));
		detector.addMarker(new NapMarker("markers/4x4_642.patt", 5.5), objects.get(2));
		detector.addMarker(new NapMarker("markers/4x4_190.patt", 5.5), objects.get(3));
		detector.setImageProvider(camera);
		detector.start();

		// Make a window for showing captured image.
		final DrawableFrame frame = new DrawableFrame() {
			private static final long serialVersionUID = 1L;
			private transient final Stroke stroke =
				new BasicStroke(5);
			private transient final AlphaComposite alphaComp2 =
				AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .7f);

			@Override public void dispose() {
				super.dispose();
				Matereal.getInstance().dispose();
			}

			@Override
			public void paint2D(Graphics2D g) {
				final Composite comp = g.getComposite();
				g.setRenderingHint(
						RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);

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

				camera.drawImage(g);

				Stroke s = g.getStroke();
				g.setStroke(stroke);
				ScreenPosition sp = detector.getScreenPosition(robot1);
				if (sp.isFound()) {
					g.setColor(Color.black);
					g.drawOval(sp.getX()-20, sp.getY()-20, 40, 40);
				}
				sp = detector.getScreenPosition(robot3);
				if(sp.isFound()){
					g.setColor(Color.black);
					g.drawOval(sp.getX()-20,sp.getY()-20,40,40);
				}
				sp = detector.getScreenPosition(robot2);
				if(sp.isFound()){
					g.setColor(Color.green);
					g.drawOval(sp.getX()-20,sp.getY()-20,40,40);
				}
				sp = detector.getScreenPosition(robot4);
				if(sp.isFound()){
					g.setColor(Color.green);
					g.drawOval(sp.getX()-20,sp.getY()-20,40,40);
				}

				g.setColor(Color.red);
				for (Entity e : objects) {
					NapDetectionResult result = detector.getResult(e);
					if (result != null) {
						result.getSquare().draw(g, true);
					}
				}

				g.setColor(Color.blue);
				g.drawOval(
						camera.getWidth()/2 - goalSize, camera.getHeight()/2 - goalSize,
						goalSize*2, goalSize*2);
				g.setStroke(s);

				if (finished) {
					Font font = g.getFont().deriveFont(Font.BOLD, 64f);//Font.getFont("Arial");
					g.setColor(Color.red);
					g.setFont(font);
					String goal = "";
					if(finishedrobot == 1){
						goal = "GOAL! White Player Won!!";
					}
					if(finishedrobot == 2){
						goal = "GOAL! Green Player Won!!";
					}
					if(finishedrobot == 3){
						goal = "GOAL! White Player Won!!";
					}
					if(finishedrobot == 4){
						goal = "GOAL! Green Player Won!!";
					}
					int w = g.getFontMetrics().stringWidth(goal);
					g.drawString(goal, (camera.getWidth()-w)/2, camera.getHeight()/2);
				}
			}
		};
		frame.setResizable(false);
		frame.setFrameSize(camera.getWidth(), camera.getHeight());

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

		// Repaint the window every time the image is updated.
		camera.addEventListener(new EventListener() {
			public void eventOccurred(Event e) {
				if (e instanceof ImageUpdateEvent) {
					frame.repaint();
				}
			}
		});


		detector.addEventListener(new EventListener() {
			public void eventOccurred(Event e) {

				if (e instanceof LocationUpdateEvent) {
					ScreenPosition sp = new ScreenPosition(camera.getWidth()/2, camera.getHeight()/2);
					ScreenPosition rp;
					double minmum = Double.MAX_VALUE;
					int robotnumber = 0;
					rp = detector.getScreenPosition(robot1);
					double distanceSq1 = (rp.isNotFound() ? Double.MAX_VALUE : rp.distanceSq(sp));
					if(distanceSq1 < minmum){
						minmum = distanceSq1;
						robotnumber = 1;
					}
					rp = detector.getScreenPosition(robot2);
					double distanceSq2 = (rp.isNotFound() ? Double.MAX_VALUE : rp.distanceSq(sp));
					if(distanceSq2 < minmum){
						minmum = distanceSq2;
						robotnumber = 2;
					}
					rp = detector.getScreenPosition(robot3);
					double distanceSq3 = (rp.isNotFound() ? Double.MAX_VALUE : rp.distanceSq(sp));
					if(distanceSq3 < minmum){
						minmum = distanceSq3;
						robotnumber = 3;
					}
					rp = detector.getScreenPosition(robot4);
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
		goal = new ScreenPosition(camera.getWidth()/2, camera.getHeight()/2);
		move = new Move(camera.screenToReal(goal));
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
			LocationProvider locationProvider = getLocationProvider(); // camera
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
