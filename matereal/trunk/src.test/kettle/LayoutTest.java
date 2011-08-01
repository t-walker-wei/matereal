package kettle;
import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.gui.workflow.layout.SugiyamaLayouter;
import jp.digitalmuseum.mr.hakoniwa.Hakoniwa;
import jp.digitalmuseum.mr.hakoniwa.HakoniwaCylinder;
import jp.digitalmuseum.mr.hakoniwa.HakoniwaRobot;
import jp.digitalmuseum.mr.task.Push;
import jp.digitalmuseum.mr.workflow.Action;
import jp.digitalmuseum.mr.workflow.Workflow;
import jp.digitalmuseum.mr.workflow.Fork;
import jp.digitalmuseum.mr.workflow.Join;
import jp.digitalmuseum.mr.workflow.Node;
import jp.digitalmuseum.mr.workflow.TimeoutTransition;
import jp.digitalmuseum.mr.workflow.Transition;
import jp.digitalmuseum.utils.Location;
import kettle.task.Boil;
import kettle.task.Pour;
import kettle.task.Stop;

public class LayoutTest {

	public static void main(String[] args) {
		SugiyamaLayouter layout = new SugiyamaLayouter();
		Node node = initGraph();

		layout.doLayout(node);
		Matereal.getInstance().dispose();
	}

	private static Node initGraph() {
		Hakoniwa hakoniwa = new Hakoniwa();
		hakoniwa.start();
		double x = hakoniwa.getRealWidth()/2;
		double y = hakoniwa.getRealHeight()/2;

		Robot robot = new HakoniwaRobot("Servebot", new Location(x, y, -Math.PI*3/4));
		HakoniwaCylinder mug = new HakoniwaCylinder("Mug", x+50, y+50, 16, 0);
		Kettle kettle = new Kettle("Kettle");

		// Entities and marker detector are already initialized.
		Workflow workflow = new Workflow();
		Action push = new Action(robot, new Push(mug, hakoniwa.getPosition(kettle)));
		Action boil = new Action(kettle, new Boil());
		Action boil2 = new Action(kettle, new Boil());
		Action boil3 = new Action(kettle, new Boil());
		Action boil4 = new Action(kettle, new Boil());
		Action pour = new Action(kettle, new Pour());
		Action stop = new Action(kettle, new Stop());
		Action stop2 = new Action(kettle, new Stop());
		Fork fork = new Fork(push, boil);
		Join join = new Join(push, boil4, stop);
		workflow.add(new Node[] {fork, push, boil, boil2, boil3, boil4, join, pour, stop, stop2});
		workflow.addTransition(new TimeoutTransition(push, stop, 10000));
		workflow.addTransition(new Transition(fork, join));
		workflow.addTransition(new Transition(join, pour));
		workflow.addTransition(new Transition(boil, boil2));
		workflow.addTransition(new Transition(boil2, boil3));
		workflow.addTransition(new Transition(boil3, boil4));
		workflow.addTransition(new TimeoutTransition(pour, stop2, 5000));
		workflow.setInitialNode(fork);
		return workflow.getInitialNode();
	}

}
