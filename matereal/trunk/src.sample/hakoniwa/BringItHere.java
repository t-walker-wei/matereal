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
import com.phybots.hakoniwa.HakoniwaBox;
import com.phybots.hakoniwa.HakoniwaRobot;
import com.phybots.message.Event;
import com.phybots.message.EventListener;
import com.phybots.message.ImageUpdateEvent;
import com.phybots.resource.WheelsController;
import com.phybots.task.Push;
import com.phybots.task.Task;
import com.phybots.task.VectorFieldTask;
import com.phybots.utils.Location;
import com.phybots.utils.Position;
import com.phybots.utils.ScreenPosition;


/**
 * Bring it here!
 * Test of assigning a task to a robot.
 *
 * @author Jun Kato
 */
public class BringItHere {
	private Hakoniwa hakoniwa;
	private VectorFieldTask push;
	private ScreenPosition goal = null;

	public static void main(String[] args) {
		new BringItHere();
	}

	public BringItHere() {

		// Run hakoniwa.
		hakoniwa = new Hakoniwa();
		hakoniwa.start();

		final Robot robot;
		final Entity[] entities;
		robot = new HakoniwaRobot("My robot", new Location(hakoniwa.getRealWidth()/2 - 30, hakoniwa.getRealHeight()/2, -Math.PI*3/4));
		entities = new Entity[5];
		final double x = hakoniwa.getRealWidth()/2;
		final double y = hakoniwa.getRealHeight()/2;
		final double r = 200;
		for (int i = 0; i < entities.length; i ++) {
			double theta = Math.PI*i/(entities.length-1);
			entities[i] = new HakoniwaBox(
					"Box "+i,
					x + Math.cos(theta) * r,
					y - Math.sin(theta) * r,
					theta,
					20*(i+1),
					15*(i+1));
		}

		// Show status of hakoniwa.
		final DrawableFrame frame = new DrawableFrame() {
			private static final long serialVersionUID = 1L;
			private transient final VectorFieldPainter vectorFieldPainter =
					new VectorFieldPainter(hakoniwa);
			private transient final Stroke stroke =
					new BasicStroke(5);
			private transient final AlphaComposite alphaComp = AlphaComposite
					.getInstance(AlphaComposite.SRC_OVER, .3f);
			private transient final AlphaComposite alphaComp2 = AlphaComposite
					.getInstance(AlphaComposite.SRC_OVER, .7f);

			@Override public void dispose() {
				super.dispose();
				Phybots.getInstance().dispose();
			}

			@Override
			public void paint2D(Graphics2D g2) {
				final Task task = robot.getAssignedTask(WheelsController.class);
				g2.setColor(Color.white);
				g2.fillRect(0, 0, getFrameWidth(), getFrameHeight());
				final Composite comp = g2.getComposite();

				// Draw vectors.
				g2.setComposite(alphaComp);
				g2.setColor(Color.red);
				vectorFieldPainter.paint(push, g2);
				g2.setComposite(alphaComp2);
				g2.setColor(Color.black);
				g2.fillRect(0, 0, getFrameWidth(), 35);

				// Draw status.
				g2.setComposite(comp);
				g2.drawLine(0, 35, getFrameWidth(), 35);
				g2.setColor(Color.white);
				if (goal == null) {
					g2.drawString("Click to set the destination.", 10, 30);
				} else {
					if (task != null) {
						g2.drawString("Status: "+task, 10, 30);
					} else {
						g2.drawString("Status: Stopped", 10, 30);
					}
					Stroke s = g2.getStroke();
					g2.setStroke(stroke);
					g2.setColor(Color.black);
					g2.drawLine(
							goal.getX()-5,
							goal.getY()-5,
							goal.getX()+5,
							goal.getY()+5);
					g2.drawLine(
							goal.getX()-5,
							goal.getY()+5,
							goal.getX()+5,
							goal.getY()-5);
					g2.setStroke(s);
				}
				hakoniwa.drawImage(g2);
			}
		};
		frame.setResizable(false);
		frame.setFrameSize(hakoniwa.getWidth(), hakoniwa.getHeight());

		// Bring the clicked object to the location in front of the user.
		frame.getPanel().addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				int x = e.getX(), y = e.getY();
				if (goal == null) {
					goal = new ScreenPosition(x, y);
					return;
				}
				Entity entity = getClickedEntity(x, y);
				if (entity != null && entity != robot) {
					if (push != null) {
						push.stop();
					}
					push = new Push(entity, hakoniwa.screenToReal(goal));
					if (push.assign(robot)) push.start();
				}
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

	/**
	 * Get clicked entity.
	 * @param x
	 * @param y
	 * @return Clicked entity
	 */
	private Entity getClickedEntity(int x, int y) {
		Position p = hakoniwa.screenToReal(new ScreenPosition(x, y));
		for (Entity e : Phybots.getInstance().getEntities()) {
			if (hakoniwa.contains(e, p)) {
				return e;
			}
		}
		return null;
	}
}
