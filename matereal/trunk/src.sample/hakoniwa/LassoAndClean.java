package hakoniwa;
import java.util.List;

import com.phybots.task.EndCleaning;
import com.phybots.task.FillPath;
import com.phybots.task.MobileCleaningTask;
import com.phybots.task.Move;
import com.phybots.task.TracePathLoosely;
import com.phybots.utils.Position;
import com.phybots.workflow.Action;
import com.phybots.workflow.Workflow;


/**
 * Lasso path to navigate a robot filling the path with cleaning.<br />
 * 囲んだ領域を舐めるように動いて掃除する
 *
 * @author Jun Kato
 */
public class LassoAndClean extends LassoAndFill {
	private Workflow workflow = null;

	public static void main(String[] args) {
		new LassoAndClean();
	}

	@Override
	protected synchronized void onPathSpecified(List<Position> path) {

		// Stop the workflow graph if running.
		if (workflow != null && workflow.isStarted()) {
			workflow.stop();
		}

		// Construct a workflow graph.
		workflow = new Workflow();

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
		workflow.addInSerial(actions);
		workflow.setInitialNode(actions[0]);
		workflow.start();
	}
}
