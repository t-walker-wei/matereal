package robot;
import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JButton;

import com.phybots.Phybots;
import com.phybots.entity.PhysicalRobot;
import com.phybots.resource.WheelsController;


/**
 * Show a controller GUI for a robot.
 *
 * @author Jun Kato
 */
public class SimpleButtonController extends JFrame {
	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JButton jStopButton = null;
	private JButton jForwardButton = null;
	private JButton jRightButton = null;
	private JButton jLeftButton = null;
	private JButton jBackwardButton = null;

	private transient PhysicalRobot robot;
	private transient WheelsController wheels;

	public static void main(String[] args) {
		new SimpleButtonController();
	}

	/**
	 * This is the default constructor
	 */
	public SimpleButtonController() {
		super();

		Phybots.getInstance().showDebugFrame();

		robot = RobotInfo.getRobot();
		robot.connect();

		wheels = robot.requestResource(WheelsController.class, this);

		initialize();
		setVisible(true);
	}

	public void dispose() {
		super.dispose();
		robot.freeResource(wheels, this);
		Phybots.getInstance().dispose();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		this.setSize(300, 200);
		this.setContentPane(getJContentPane());
		this.setTitle("Controller");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJStopButton(), BorderLayout.CENTER);
			jContentPane.add(getJForwardButton(), BorderLayout.NORTH);
			jContentPane.add(getJRightButton(), BorderLayout.EAST);
			jContentPane.add(getJLeftButton(), BorderLayout.WEST);
			jContentPane.add(getJBackwardButton(), BorderLayout.SOUTH);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jStopButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJStopButton() {
		if (jStopButton == null) {
			jStopButton = new JButton();
			jStopButton.setText("Stop");
			jStopButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("Stop");
					wheels.stopWheels();
				}
			});
		}
		return jStopButton;
	}

	/**
	 * This method initializes jForwardButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJForwardButton() {
		if (jForwardButton == null) {
			jForwardButton = new JButton();
			jForwardButton.setText("Go Forward");
			jForwardButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("Go Forward");
					wheels.goForward();
				}
			});
		}
		return jForwardButton;
	}

	/**
	 * This method initializes jRightButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJRightButton() {
		if (jRightButton == null) {
			jRightButton = new JButton();
			jRightButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("Turn Right");
					wheels.spinRight();
				}
			});
			jRightButton.setText("Turn Right");
		}
		return jRightButton;
	}

	/**
	 * This method initializes jLeftButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJLeftButton() {
		if (jLeftButton == null) {
			jLeftButton = new JButton();
			jLeftButton.setText("Turn Left");
			jLeftButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("Turn Left");
					wheels.spinLeft();
				}
			});
		}
		return jLeftButton;
	}

	/**
	 * This method initializes jBackwardButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJBackwardButton() {
		if (jBackwardButton == null) {
			jBackwardButton = new JButton();
			jBackwardButton.setText("Go Backward");
			jBackwardButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("Go Backward");
					wheels.goBackward();
				}
			});
		}
		return jBackwardButton;
	}
}
