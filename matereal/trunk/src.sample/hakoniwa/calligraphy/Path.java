package hakoniwa.calligraphy;

import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.phybots.utils.ScreenPosition;


public class Path implements Iterable<ScreenPosition> {
	private PointList points;
	private double length;

	public Path() {
		points = new PointList();
	}

	public synchronized void add(int x, int y) {
		points.add(x, y);
		calcLength();
	}

	public int size() {
		return points.size();
	}

	public double getLength() {
		return length;
	}

	public synchronized void resample(int unitLength) {
		final PointList newPoints = new PointList();
		final Iterator<ScreenPosition> it = points.iterator();
		ScreenPosition currentPosition = it.next(), nextPosition = null;
		while(it.hasNext()) {
			nextPosition = it.next();
			double distance;
			while ((distance = currentPosition.distance(nextPosition)) > unitLength) {
				newPoints.add(currentPosition);
				currentPosition = new ScreenPosition(
						(int) ((nextPosition.getX()*unitLength +
								currentPosition.getX()*(distance-unitLength))/distance),
						(int) ((nextPosition.getY()*unitLength +
								currentPosition.getY()*(distance-unitLength))/distance));
			}
		}
		if (nextPosition != null) {
			newPoints.add(nextPosition);
			points = newPoints;
		}
	}

	public synchronized void paint(Graphics2D g) {
		points.paint(g);
	}

	private void calcLength() {
		length = 0;
		ScreenPosition p_ = points.get(0);
		for (ScreenPosition p : points) {
			length += p_.distance(p);
			p_ = p;
		}
	}

	public Iterator<ScreenPosition> iterator() {
		return points.iterator();
	}

	private static class PointList implements Iterable<ScreenPosition> {
		private List<ScreenPosition> points;
		private GeneralPath path;
		public PointList() {
			points = new ArrayList<ScreenPosition>();
			path = new GeneralPath();
			path = new GeneralPath();
		}

		public void add(ScreenPosition p) {
			add(p.getX(), p.getY());
		}

		public void add(int x, int y) {
			points.add(new ScreenPosition(x, y));
			if (points.size() > 1) {
				path.lineTo(x, y);
			} else {
				path.moveTo(x, y);
			}
		}

		public ScreenPosition get(int index) {
			return points.get(index);
		}

		public int size() {
			return points.size();
		}

		public void paint(Graphics2D g) {
			g.draw(path);
		}

		public Iterator<ScreenPosition> iterator() {
			return points.iterator();
		}
	}

}