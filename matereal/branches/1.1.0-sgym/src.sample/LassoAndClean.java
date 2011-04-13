import java.util.List;

import jp.digitalmuseum.mr.activity.Action;
import jp.digitalmuseum.mr.activity.ActivityDiagram;
import jp.digitalmuseum.mr.task.EndCleaning;
import jp.digitalmuseum.mr.task.FillPath;
import jp.digitalmuseum.mr.task.Move;
import jp.digitalmuseum.mr.task.MobileCleaningTask;
import jp.digitalmuseum.mr.task.TracePathLoosely;
import jp.digitalmuseum.utils.Position;

/**
 * Lasso path to navigate a robot filling the path with cleaning.<br />
 * 囲んだ領域を舐めるように動いて掃除する
 *
 * @author Jun KATO
 */
public class LassoAndClean extends LassoAndFill {
	private ActivityDiagram ad = null;

	public static void main(String[] args) {
		new LassoAndClean();
	}

	@Override
	protected synchronized void onPathSpecified(List<Position> path) {

		// Stop the activity diagram if running.
		if (ad != null && ad.isStarted()) {
			ad.stop();
		}

		// Construct an activity diagram.
		ad = new ActivityDiagram();

		List<Position> cleaningPath = FillPath.getCleaningPath(path, robot
				.getShape().getBounds().getWidth());
		Position firstCleaningPoint = cleaningPath.remove(0);

		Position firstPoint = path.remove(0);
		path.add(firstPoint);

		Action[] actions = new Action[] {

			// Ensure that the brush is stopped.
			new Action(robot, new EndCleaning()),

			// Move to the starting point of the track.
			new Action(robot, new Move(firstPoint)),

			// Go along the track to clean around the cleaning area.
			new Action(robot, new MobileCleaningTask(new TracePathLoosely(path))),

			// Move to the starting point of the cleaning path.
			new Action(robot, new Move(firstCleaningPoint)),

			// Go along the cleaning path to clean inside the cleaning area.
			new Action(robot, new MobileCleaningTask(new TracePathLoosely(cleaningPath)))
		};
		ad.addInSerial(actions);
		ad.setInitialNode(actions[0]);
		ad.start();
	}
}
