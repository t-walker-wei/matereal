package hakoniwa;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.gui.*;
import jp.digitalmuseum.mr.gui.utils.VectorFieldPainter;
import jp.digitalmuseum.mr.hakoniwa.Hakoniwa;
import jp.digitalmuseum.mr.hakoniwa.HakoniwaRobotWithPen;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.message.ImageUpdateEvent;
import jp.digitalmuseum.mr.resource.Wheels;
import jp.digitalmuseum.mr.resource.WheelsController;
import jp.digitalmuseum.mr.task.Follow;
import jp.digitalmuseum.mr.task.Move;
import jp.digitalmuseum.mr.task.Task;
import jp.digitalmuseum.mr.task.VectorFieldTask;
import jp.digitalmuseum.utils.Location;
import jp.digitalmuseum.utils.ScreenPosition;

/**
 * Click to navigate one robot. The other robot will follow him.
 * Test of assigning tasks to robots.
 *
 * @author Jun KATO
 */
public class ClickAndFollow {
	private Hakoniwa hakoniwa;
	private VectorFieldTask follow;
	private ScreenPosition goal = null;

	public static void main(String[] args) {
		new ClickAndFollow();
	}

	public ClickAndFollow() {

		// Run hakoniwa.
		hakoniwa = new Hakoniwa();
		hakoniwa.start();

		final Robot followee;
		final Robot follower;
		followee = new HakoniwaRobotWithPen("followee", new Location(hakoniwa.getRealWidth()/2 - 30, hakoniwa.getRealHeight()/2, -Math.PI*3/4));
		follower = new HakoniwaRobotWithPen("follower", new Location(hakoniwa.getRealWidth()/2 + 30, hakoniwa.getRealHeight()/2, -Math.PI*3/4));

		follow = new Follow(followee);
		follow.assign(follower);
		follow.start();

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
				final Task task = followee.getAssignedTask(WheelsController.class);
				g.setColor(Color.white);
				g.fillRect(0, 0, getFrameWidth(), getFrameHeight());
				final Composite comp = g.getComposite();

				// Draw vectors.
				g.setComposite(alphaComp);
				g.setColor(Color.red);
				vectorFieldPainter.paint(follow, g);
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

				Task task = followee.getAssignedTask(Wheels.class);
				if (task != null) task.stop();

				Move move = new Move(hakoniwa.screenToReal(goal));
				if (move.assign(followee)) move.start();
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
