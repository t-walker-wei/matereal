import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Path2D;
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
import jp.digitalmuseum.mr.task.FillPathLoosely;
import jp.digitalmuseum.mr.task.Task;
import jp.digitalmuseum.utils.Location;
import jp.digitalmuseum.utils.Position;
import jp.digitalmuseum.utils.ScreenPosition;

/**
 * Lasso path to navigate a robot filling the path.
 *
 * @author Jun KATO
 */
public class LassoAndClean {
	private Hakoniwa hakoniwa;
	private FillPathLoosely fillPath = null;
	private List<ScreenPosition> screenPath = new ArrayList<ScreenPosition>();
	private Path2D path2D;

	public static void main(String[] args) {
		new LassoAndClean();
	}

	public LassoAndClean() {

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
				AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .1f);
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
				if (screenPath.isEmpty()) {
					g.drawString("Draw path.", 10, 30);
				} else {
					if (task != null) {
						g.drawString("Status: "+task, 10, 30);
					} else {
						g.drawString("Status: Stopped", 10, 30);
					}
					if (path2D != null) {
						Stroke s = g.getStroke();
						g.setStroke(stroke);
						g.setColor(Color.black);
						g.draw(path2D);
						g.setStroke(s);
						g.setComposite(alphaComp);
						g.fill(path2D);
						g.setComposite(comp);
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
				int x = e.getX(), y = e.getY();
				screenPath.clear();
				screenPath.add(new ScreenPosition(x, y));
				path2D = new Path2D.Float();
				path2D.moveTo(x, y);
			}
			public void mouseReleased(MouseEvent e) {
				screenPath.add(new ScreenPosition(e.getX(), e.getY()));

				synchronized (robot) {
					resample(20);
					List<Position> path = new ArrayList<Position>();
					path2D.reset();
					boolean isFirst = true;
					for (ScreenPosition sp : screenPath) {
						path.add(hakoniwa.screenToReal(sp));
						if (isFirst) {
							path2D.moveTo(sp.getX(), sp.getY());
							isFirst = false;
						} else {
							path2D.lineTo(sp.getX(), sp.getY());
						}
					}
					path2D.closePath();
					if (fillPath != null &&
							fillPath.isStarted()) {
						fillPath.updatePath(path);
					} else {
						fillPath = new FillPathLoosely(path);
						if (fillPath.assign(robot)) {
							fillPath.start();
						} else {
							fillPath = null;
						}
					}
				}
			}
		});
		frame.getPanel().addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				int x = e.getX(), y = e.getY();
				screenPath.add(new ScreenPosition(x, y));
				path2D.lineTo(x, y);
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
