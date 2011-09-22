package mw2011.bflags;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.Entity;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.gui.*;
import jp.digitalmuseum.mr.hakoniwa.Hakoniwa;
import jp.digitalmuseum.mr.hakoniwa.HakoniwaCylinder;
import jp.digitalmuseum.mr.hakoniwa.HakoniwaRobotWithPen;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.message.ImageUpdateEvent;
import jp.digitalmuseum.mr.message.ServiceEvent;
import jp.digitalmuseum.mr.message.ServiceStatus;
import jp.digitalmuseum.mr.resource.WheelsController;
import jp.digitalmuseum.mr.task.GoForward;
import jp.digitalmuseum.mr.task.MobilePenTask;
import jp.digitalmuseum.mr.task.Move;
import jp.digitalmuseum.mr.task.Stop;
import jp.digitalmuseum.mr.task.Task;
import jp.digitalmuseum.mr.workflow.Action;
import jp.digitalmuseum.mr.workflow.TimeoutTransition;
import jp.digitalmuseum.mr.workflow.Workflow;
import jp.digitalmuseum.utils.Location;
import jp.digitalmuseum.utils.Position;
import jp.digitalmuseum.utils.ScreenPosition;

public class RunGame implements EventListener {
	private ArrayList<Entity> entities;
	private Hakoniwa hakoniwa;
	private Robot robot;
	private Entity goalEntity;
	private Task move = null;
	private ScreenPosition goal = null;

	public static void main(String[] args) {
		new RunGame();
	}

	public RunGame() {

		// Run hakoniwa.
		hakoniwa = new Hakoniwa();
		hakoniwa.start();

		robot = new HakoniwaRobotWithPen("robot", new Location(hakoniwa.getRealWidth()/2 - 30, hakoniwa.getRealHeight()/2, -Math.PI*3/4));

		entities = new ArrayList<Entity>();
		final double x = hakoniwa.getRealWidth()/2;
		final double y = hakoniwa.getRealHeight()/2;
		final double r = 40;
		int num = 5;
		for (int i = 0; i < num; i ++) {
			double theta = Math.PI*i/(num - 1);
			entities.add(new HakoniwaCylinder(
					"Cylinder "+i,
					x + Math.cos(theta) * r * (i + 1),
					y - Math.sin(theta) * r * (i + 1),
					theta,
					20));
			// System.out.println(hakoniwa.getScreenX(entities[i]));
		}

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
				final Task task = robot.getAssignedTask(WheelsController.class);
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

		frame.getPanel().addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				goNextGoal();
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

	void goNextGoal() {
		goalEntity = getNearest();
		if (goalEntity == null){
			return;
		}
		//int x = e.getX(), y = e.getY();
		//goal = new ScreenPosition(x, y);
		int x = (int)hakoniwa.getScreenX(goalEntity);
		int y = (int)hakoniwa.getScreenY(goalEntity);
		goal = new ScreenPosition(x,y);
		//if (move != null &&
		//		move.isStarted()) {
		//	move.stop();
		//}

		move = new Move(hakoniwa.screenToReal(goal));
		if (move.assign(robot)) {
			move.addEventListener(RunGame.this);
			move.start();
		}
	}

	Entity getNearest() {

		// Set<Entity> entities = Matereal.getInstance().getEntities();
		// Location location = hakoniwa.getLocation(robot);
		// Location loc = hakoniwa.getLocation(entities[0]);
		Position position = hakoniwa.getPosition(robot);
		double distance = Double.MAX_VALUE;
		Entity nearest = null;
		//Entity [] nearest = new Entity[5];
		//double[] dis= new double[entities.length];
		//for(int i=0; i < entities.length;i++){
		//	nearest[i] = null;
		//}
		for (int i = 0; i < entities.size(); i ++) {
			Position p = hakoniwa.getPosition(entities.get(i));
			// dis[i] = position.distance(p);
			double d= position.distance(p);
			if (d < distance) {
				distance = d;
				nearest = entities.get(i);
			}
		}

		// System.out.println(nearest.toString());
		// System.out.println(hakoniwa.getScreenX(nearest));
		return nearest;
	}

	public void eventOccurred(Event e) {
		if (e instanceof ServiceEvent) {
			if (((ServiceEvent) e).getStatus() == ServiceStatus.FINISHED) {
				entities.remove(goalEntity);
				goNextGoal();
			}
		}
	}
}
