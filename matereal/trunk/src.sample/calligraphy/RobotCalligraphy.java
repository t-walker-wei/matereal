package calligraphy;

import java.awt.BasicStroke;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.hakoniwa.Hakoniwa;
import jp.digitalmuseum.mr.hakoniwa.HakoniwaRobotWithPen;
import jp.digitalmuseum.utils.Position;

public class RobotCalligraphy {
	private Robot[] robots = new Robot[5];

	public static void main(String[] args) {
		new RobotCalligraphy();
	}

	public RobotCalligraphy() {

		// Run hakoniwa.
		Hakoniwa hakoniwa = new Hakoniwa();
		hakoniwa.setAntialiased(true);
		hakoniwa.setBackgroundTransparent(false);
		hakoniwa.setViewportSize(640, 480);
		hakoniwa.start();

		// Instantiate a robot.
		for(int i = 0; i < robots.length; i++) {
			robots[i] = new HakoniwaRobotWithPen("Cali-"+(i+1), new Position(
					hakoniwa.getRealWidth()/2+(i-2)*50,
					hakoniwa.getRealHeight()/2));
		}

		// Initialize wizard components.
		StrokePainterPanel strokePainterPanel = new StrokePainterPanel(hakoniwa);
		strokePainterPanel.setStroke(new BasicStroke(3));
		StrokePlayerPanel strokePlayerPanel = new StrokePlayerPanel(hakoniwa);
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
		wizardFrame.setVisible(true);
		wizardFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				Matereal.getInstance().dispose();
			}
		});
		wizardFrame.setTitle("Robot Caligraphy");
	}
}
