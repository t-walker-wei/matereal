package hakoniwa;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.util.List;

import com.phybots.task.FillPathLoosely;
import com.phybots.utils.Position;


/**
 * Lasso path to navigate a robot filling the path.<br />
 * 囲んだ領域を舐めるように動く
 *
 * @author Jun Kato
 */
public class LassoAndFill extends DrawAndTrace {

	private transient final Stroke stroke =
		new BasicStroke(2);
	private transient final AlphaComposite alphaComp =
		AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .1f);

	private FillPathLoosely fillPath = null;

	public static void main(String[] args) {
		new LassoAndFill();
	}

	protected void onPathSpecified(List<Position> path) {

		// If a task instance already exists, update it.
		if (fillPath != null &&
				fillPath.isStarted()) {
			fillPath.updatePath(path);
		}

		// Otherwise instantiate task and assign it to the robot.
		else {
			fillPath = new FillPathLoosely(path);
			if (fillPath.assign(robot)) {
				fillPath.start();
			}
		}
	}

	protected void drawPath(Graphics2D g, Path2D path2D) {

		// Draw path.
		Stroke s = g.getStroke();
			g.setStroke(stroke);
			g.setColor(Color.black);
			g.draw(path2D);
		g.setStroke(s);

		// Fill the path.
		Composite comp = g.getComposite();
			g.setComposite(alphaComp);
			g.fill(path2D);
		g.setComposite(comp);
	}
}
