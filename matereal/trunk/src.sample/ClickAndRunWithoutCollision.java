import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.Entity;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.gui.*;
import jp.digitalmuseum.mr.gui.utils.VectorFieldPainter;
import jp.digitalmuseum.mr.hakoniwa.Hakoniwa;
import jp.digitalmuseum.mr.hakoniwa.HakoniwaCylinder;
import jp.digitalmuseum.mr.hakoniwa.HakoniwaRobotWithPen;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.message.ImageUpdateEvent;
import jp.digitalmuseum.mr.resource.Wheels;
import jp.digitalmuseum.mr.resource.WheelsController;
import jp.digitalmuseum.mr.task.Move;
import jp.digitalmuseum.mr.task.Task;
import jp.digitalmuseum.mr.task.VectorFieldTask;
import jp.digitalmuseum.mr.vectorfield.CollisionAvoidanceField;
import jp.digitalmuseum.utils.Location;
import jp.digitalmuseum.utils.ScreenPosition;

/**
 * Click to navigate a robot without collision.
 * Test of assigning multiple vector fields to a robot.
 *
 * @author Jun KATO
 */
public class ClickAndRunWithoutCollision {
	private Hakoniwa hakoniwa;
	private VectorFieldTask moveWithCollisionAvoidance = null;
	private ScreenPosition goal = null;

	public static void main(String[] args) {
		new ClickAndRunWithoutCollision();
	}

	public ClickAndRunWithoutCollision() {

		// Run hakoniwa.
		hakoniwa = new Hakoniwa();
		hakoniwa.start();

		final Robot robot;
		robot = new HakoniwaRobotWithPen("My robot", new Location(hakoniwa.getRealWidth()/2 - 30, hakoniwa.getRealHeight()/2, -Math.PI*3/4));

		Entity[] entities = new Entity[5];
		final double x = hakoniwa.getRealWidth()/2;
		final double y = hakoniwa.getRealHeight()/2;
		final double r = 240;
		for (int i = 0; i < entities.length; i ++) {
			double theta = Math.PI*i/(entities.length-1);
			entities[i] = new HakoniwaCylinder(
					"Cylinder "+i,
					x + Math.cos(theta) * r,
					y - Math.sin(theta) * r,
					theta,
					20);
		}

		// Make a window for showing captured image.
		final DrawableFrame frame = new DrawableFrame() {
			private static final long serialVersionUID = 1L;
			private transient final VectorFieldPainter vectorFieldPainter =
				new VectorFieldPainter(hakoniwa);
			private transient final Stroke stroke =
				new BasicStroke(5);
			private transient final AlphaComposite alphaComp =
				AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .3f);
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
				g.setComposite(alphaComp);
				g.setColor(Color.red);
				vectorFieldPainter.paint(moveWithCollisionAvoidance, g);
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
						g.drawString("Status: "+task, 10, 30);
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
				int x = e.getX(), y = e.getY();
				goal = new ScreenPosition(x, y);

				Task task = robot.getAssignedTask(Wheels.class);
				if (task != null) task.stop();

				moveWithCollisionAvoidance = new Move(hakoniwa.screenToReal(goal));
				moveWithCollisionAvoidance.add(new CollisionAvoidanceField(robot));
				if (moveWithCollisionAvoidance.assign(robot)) moveWithCollisionAvoidance.start();
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
}
