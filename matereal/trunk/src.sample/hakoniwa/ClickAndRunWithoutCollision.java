package hakoniwa;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.phybots.Phybots;
import com.phybots.entity.Entity;
import com.phybots.entity.Robot;
import com.phybots.gui.*;
import com.phybots.gui.utils.VectorFieldPainter;
import com.phybots.hakoniwa.Hakoniwa;
import com.phybots.hakoniwa.HakoniwaCylinder;
import com.phybots.hakoniwa.HakoniwaRobotWithPen;
import com.phybots.message.Event;
import com.phybots.message.EventListener;
import com.phybots.message.ImageUpdateEvent;
import com.phybots.resource.Wheels;
import com.phybots.resource.WheelsController;
import com.phybots.task.Move;
import com.phybots.task.Task;
import com.phybots.task.VectorFieldTask;
import com.phybots.utils.Location;
import com.phybots.utils.ScreenPosition;
import com.phybots.vectorfield.CollisionAvoidanceField;


/**
 * Click to navigate a robot without collision.
 * Test of assigning multiple vector fields to a robot.
 *
 * @author Jun Kato
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
				Phybots.getInstance().dispose();
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
