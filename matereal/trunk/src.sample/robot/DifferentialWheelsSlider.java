package robot;
import javax.swing.SwingUtilities;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JSlider;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


import javax.swing.JLabel;
import javax.swing.JButton;

import com.phybots.Phybots;
import com.phybots.entity.PhysicalRobot;
import com.phybots.resource.DifferentialWheelsController;
import com.phybots.service.ServiceAbstractImpl;

import java.awt.Insets;

public class DifferentialWheelsSlider extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JSlider leftSlider = null;
	private JSlider rightSlider = null;
	private JLabel leftPowerLabel = null;
	private JLabel rightPowerLabel = null;
	private JButton leftStopButton = null;
	private JButton rightStopButton = null;
	private JLabel leftLabel = null;
	private JLabel rightLabel = null;

	/**
	 * This method initializes leftSlider
	 *
	 * @return javax.swing.JSlider
	 */
	private JSlider getLeftSlider() {
		if (leftSlider == null) {
			leftSlider = new JSlider();
			leftSlider.setMaximum(100);
			leftSlider.setMinimum(-100);
			leftSlider.setValue(0);
		}
		return leftSlider;
	}

	/**
	 * This method initializes rightSlider
	 *
	 * @return javax.swing.JSlider
	 */
	private JSlider getRightSlider() {
		if (rightSlider == null) {
			rightSlider = new JSlider();
			rightSlider.setMaximum(100);
			rightSlider.setMinimum(-100);
			rightSlider.setValue(0);
		}
		return rightSlider;
	}

	/**
	 * This method initializes leftPowerLabel
	 *
	 * @return javax.swing.JLabel
	 */
	private JLabel getLeftPowerLabel() {
		if (leftPowerLabel == null) {
			leftPowerLabel = new JLabel();
			leftPowerLabel.setText("0");
		}
		return leftPowerLabel;
	}

	/**
	 * This method initializes leftLabel
	 *
	 * @return javax.swing.JLabel
	 */
	private JLabel getRightPowerLabel() {
		if (rightPowerLabel == null) {
			rightPowerLabel = new JLabel();
			rightPowerLabel.setText("0");
		}
		return rightPowerLabel;
	}

	/**
	 * This method initializes leftStopButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getLeftStopButton() {
		if (leftStopButton == null) {
			leftStopButton = new JButton();
			leftStopButton.setText("Stop");
		}
		return leftStopButton;
	}

	/**
	 * This method initializes rightStopButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getRightStopButton() {
		if (rightStopButton == null) {
			rightStopButton = new JButton();
			rightStopButton.setText("Stop");
		}
		return rightStopButton;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		final PhysicalRobot robot = RobotInfo.getRobot();
		final DifferentialWheelsController wheels =
				robot.requestResource(DifferentialWheelsController.class, null);
		robot.connect();

		Phybots.getInstance().showDebugFrame();

		SwingUtilities.invokeLater(new Runnable() {
			private DifferentialWheelsSlider thisClass;
			public void run() {
				thisClass = new DifferentialWheelsSlider();
				thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				thisClass.setVisible(true);
				thisClass.setTitle("Differential wheels test");

				thisClass.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						wheels.stopWheels();
						Phybots.getInstance().dispose();
					}
				});

				new ServiceAbstractImpl() {
					private static final long serialVersionUID = 1L;
					private int leftPower = 0;
					private int rightPower = 0;

					public void run() {
						int leftPower = thisClass.getLeftSlider().getValue();
						int rightPower = thisClass.getRightSlider().getValue();
						if (this.leftPower != leftPower
								|| this.rightPower != rightPower) {
							thisClass.getLeftPowerLabel().setText(String.valueOf(leftPower));
							thisClass.getRightPowerLabel().setText(String.valueOf(rightPower));
							this.leftPower = leftPower;
							this.rightPower = rightPower;
							wheels.drive(leftPower, rightPower);
							System.out.println(String.format(
									"%03d %03d", leftPower, rightPower));
						}
					}
				}.start();

				thisClass.getLeftStopButton().addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						thisClass.getLeftSlider().setValue(0);
						wheels.drive(
								0,
								thisClass.getRightSlider().getValue());
					}
				});

				thisClass.getRightStopButton().addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						thisClass.getRightSlider().setValue(0);
						wheels.drive(
								thisClass.getLeftSlider().getValue(),
								0);
					}
				});
			}
		});
	}

	/**
	 * This is the default constructor
	 */
	public DifferentialWheelsSlider() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		this.setSize(300, 200);
		this.setContentPane(getJContentPane());
		this.setTitle("JFrame");
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints00 = new GridBagConstraints();
			gridBagConstraints00.fill = GridBagConstraints.BOTH;
			gridBagConstraints00.insets = new Insets(10, 10, 10, 5);
			gridBagConstraints00.gridx = 0;
			gridBagConstraints00.gridy = 0;
			leftLabel = new JLabel();
			leftLabel.setText("Left motor");
			GridBagConstraints gridBagConstraints01 = new GridBagConstraints();
			gridBagConstraints01.fill = GridBagConstraints.BOTH;
			gridBagConstraints01.insets = new Insets(10, 10, 10, 5);
			gridBagConstraints01.gridx = 0;
			gridBagConstraints01.gridy = 1;
			rightLabel = new JLabel();
			rightLabel.setText("Right motor");
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.fill = GridBagConstraints.BOTH;
			gridBagConstraints10.insets = new Insets(10, 5, 10, 5);
			gridBagConstraints10.gridx = 1;
			gridBagConstraints10.gridy = 0;
			gridBagConstraints10.weightx = 1.0;
			gridBagConstraints10.weighty = 0.5D;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.fill = GridBagConstraints.BOTH;
			gridBagConstraints11.insets = new Insets(10, 5, 10, 5);
			gridBagConstraints11.gridx = 1;
			gridBagConstraints11.gridy = 1;
			gridBagConstraints11.weightx = 1.0;
			gridBagConstraints11.weighty = 0.5D;
			GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
			gridBagConstraints20.fill = GridBagConstraints.BOTH;
			gridBagConstraints20.insets = new Insets(10, 5, 10, 5);
			gridBagConstraints20.gridx = 2;
			gridBagConstraints20.gridy = 0;
			gridBagConstraints20.ipadx = 5;
			gridBagConstraints20.ipady = 5;
			gridBagConstraints20.weightx = 0.3D;
			gridBagConstraints20.weighty = 0.5D;
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.fill = GridBagConstraints.BOTH;
			gridBagConstraints21.insets = new Insets(10, 5, 10, 5);
			gridBagConstraints21.gridx = 2;
			gridBagConstraints21.gridy = 1;
			GridBagConstraints gridBagConstraints30 = new GridBagConstraints();
			gridBagConstraints30.fill = GridBagConstraints.BOTH;
			gridBagConstraints30.insets = new Insets(10, 5, 5, 10);
			gridBagConstraints30.gridx = 3;
			gridBagConstraints30.gridy = 0;
			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			gridBagConstraints31.fill = GridBagConstraints.BOTH;
			gridBagConstraints31.insets = new Insets(5, 5, 10, 10);
			gridBagConstraints31.gridx = 3;
			gridBagConstraints31.gridy = 1;
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(leftLabel, gridBagConstraints00);
			jContentPane.add(rightLabel, gridBagConstraints01);
			jContentPane.add(getLeftSlider(), gridBagConstraints10);
			jContentPane.add(getRightSlider(), gridBagConstraints11);
			jContentPane.add(getLeftPowerLabel(), gridBagConstraints20);
			jContentPane.add(getRightPowerLabel(), gridBagConstraints21);
			jContentPane.add(getLeftStopButton(), gridBagConstraints30);
			jContentPane.add(getRightStopButton(), gridBagConstraints31);
		}
		return jContentPane;
	}

}
