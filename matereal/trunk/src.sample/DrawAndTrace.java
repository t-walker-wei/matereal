import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.gui.*;
import jp.digitalmuseum.mr.hakoniwa.Hakoniwa;
import jp.digitalmuseum.mr.hakoniwa.HakoniwaRobotWithPen;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.message.ImageUpdateEvent;
import jp.digitalmuseum.mr.resource.WheelsController;
import jp.digitalmuseum.mr.task.Task;
import jp.digitalmuseum.mr.task.TracePath;
import jp.digitalmuseum.utils.Location;
import jp.digitalmuseum.utils.Position;
import jp.digitalmuseum.utils.ScreenPosition;

/**
 * Draw path to navigate a robot along the path.
 *
 * @author Jun KATO
 */
public class DrawAndTrace {
	private Hakoniwa hakoniwa;
	private TracePath tracePath = null;
	private List<ScreenPosition> screenPath = new ArrayList<ScreenPosition>();

	public static void main(String[] args) {
		new DrawAndTrace();
	}

	public DrawAndTrace() {

		// Run hakoniwa.
		hakoniwa = new Hakoniwa();
		hakoniwa.start();

		final Robot robot = new HakoniwaRobotWithPen(
				"My robot",
				new Location(
						hakoniwa.getRealWidth()/2 - 30,
						hakoniwa.getRealHeight()/2,
						-Math.PI*3/4));

		// Make a window for showing captured image.
		final DrawableFrame frame = new DrawableFrame() {
			private static final long serialVersionUID = 1L;
			private transient final Stroke stroke =
				new BasicStroke(2);
			private transient final AlphaComposite alphaComp =
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
				g.setColor(Color.black);
				g.fillRect(0, 0, getFrameWidth(), 35);

				// Draw status.
				g.setComposite(comp);
				g.drawLine(0, 35, getFrameWidth(), 35);
				g.setColor(Color.white);
				if (screenPath.isEmpty()) {
					g.drawString("Draw path.", 10, 30);
				} else {
					if (task != null) {
						g.drawString("Status: "+task, 10, 30);
					} else {
						g.drawString("Status: Stopped", 10, 30);
					}
					if (!screenPath.isEmpty()) {
						Stroke s = g.getStroke();
						g.setStroke(stroke);
						g.setColor(Color.black);
						final Iterator<ScreenPosition> it = screenPath.iterator();
						ScreenPosition currentPosition = it.next(), nextPosition = null;
						while(it.hasNext()) {
							nextPosition = it.next();
							g.drawLine(currentPosition.getX(), currentPosition.getY(),
									nextPosition.getX(), nextPosition.getY());
							currentPosition = nextPosition;
						}
						g.setStroke(s);
					}
				}
				hakoniwa.drawImage(g);
			}
		};
		frame.setResizable(false);
		frame.setFrameSize(hakoniwa.getWidth(), hakoniwa.getHeight());

		// Bring the clicked object to the location in front of the user.
		frame.getPanel().addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				screenPath.clear();
				screenPath.add(new ScreenPosition(e.getX(), e.getY()));
			}
			public void mouseReleased(MouseEvent e) {
				screenPath.add(new ScreenPosition(e.getX(), e.getY()));

				synchronized (robot) {
					resample(20);
					List<Position> path = new ArrayList<Position>();
					for (ScreenPosition sp : screenPath) {
						path.add(hakoniwa.screenToReal(sp));
					}
					if (tracePath != null &&
							tracePath.isStarted()) {
						tracePath.updatePath(path);
					} else {
						tracePath = new TracePath(path);
						if (tracePath.assign(robot)) {
							tracePath.start();
						} else {
							tracePath = null;
						}
					}
				}
			}
		});
		frame.getPanel().addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				screenPath.add(new ScreenPosition(e.getX(), e.getY()));
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

	private void resample(double unitLength) {
		if (screenPath.isEmpty()) {
			return;
		}

		List<ScreenPosition> newPath = new ArrayList<ScreenPosition>();
		final Iterator<ScreenPosition> it = screenPath.iterator();
		ScreenPosition currentPosition = it.next(), nextPosition = null;
		while(it.hasNext()) {
			nextPosition = it.next();
			double distance;
			while ((distance = currentPosition.distance(nextPosition)) > unitLength) {
				newPath.add(currentPosition);
				currentPosition = new ScreenPosition(
						(int)((nextPosition.getX()*unitLength +
								currentPosition.getX()*(distance-unitLength))/distance),
						(int)((nextPosition.getY()*unitLength +
								currentPosition.getY()*(distance-unitLength))/distance));
			}
		}

		if (nextPosition != null) {
			newPath.add(nextPosition);
			screenPath = newPath;
		}
	}
}
