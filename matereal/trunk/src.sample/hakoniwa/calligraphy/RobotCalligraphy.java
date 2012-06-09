package hakoniwa.calligraphy;

import java.awt.BasicStroke;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.phybots.Phybots;
import com.phybots.entity.Robot;
import com.phybots.hakoniwa.Hakoniwa;
import com.phybots.hakoniwa.HakoniwaRobotWithPen;
import com.phybots.message.Event;
import com.phybots.message.EventListener;
import com.phybots.message.ImageUpdateEvent;
import com.phybots.message.LocationUpdateEvent;
import com.phybots.service.ServiceGroup;
import com.phybots.utils.Position;
import com.phybots.workflow.Workflow;


public class RobotCalligraphy {

	public static void main(String[] args) {
		new RobotCalligraphy();
	}

	public RobotCalligraphy() {

		Phybots.getInstance().addEventListener(new EventListener() {

			public void eventOccurred(Event e) {
				if (e instanceof ImageUpdateEvent ||
						e instanceof LocationUpdateEvent) {
					return;
				}
				System.out.println(e);
			}
		});

		// Run hakoniwa.
		final Hakoniwa hakoniwa = new Hakoniwa();
		hakoniwa.setAntialiased(true);
		hakoniwa.setBackgroundTransparent(false);
		hakoniwa.setViewportSize(640, 480);
		hakoniwa.start(new ServiceGroup());

		// Instantiate a robot.
		final Robot[] robots = new Robot[5];
		for(int i = 0; i < robots.length; i++) {
			robots[i] = new HakoniwaRobotWithPen("Cali-"+(i+1), new Position(
					hakoniwa.getRealWidth()/2+(i-2)*50,
					hakoniwa.getRealHeight()/2));
		}

		// Prepare a workflow graph.
		final Workflow workflow = new Workflow();

		// Show the main window.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				// Initialize wizard components.
				StrokePainterPanel strokePainterPanel = new StrokePainterPanel(hakoniwa);
				strokePainterPanel.setStroke(new BasicStroke(3));
				StrokePlayerPanel strokePlayerPanel = new StrokePlayerPanel(hakoniwa, workflow);
				strokePlayerPanel.setPathsProvider(strokePainterPanel);
				strokePlayerPanel.setStroke(strokePainterPanel.getStroke());
				strokePlayerPanel.setRobots(robots);
				JComponent[] components = new JComponent[] {
					strokePainterPanel,
					strokePlayerPanel
				};
				components[0].setName("Draw exemplar paths");
				components[1].setName("Let the robots play");

				// Show a wizard.
				final WizardFrame wizardFrame = new WizardFrame(components);
				wizardFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				wizardFrame.setSize(740, 680);
				wizardFrame.setLocationRelativeTo(null);
				wizardFrame.setVisible(true);
				wizardFrame.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent e) {
						Phybots.getInstance().dispose();
					}
				});
				wizardFrame.setTitle("Robot Caligraphy");

				Phybots.getInstance().showDebugFrame();
			}
		});
	}
}
