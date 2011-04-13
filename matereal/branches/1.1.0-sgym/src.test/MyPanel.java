

import javax.swing.JPanel;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.UIManager;
import java.awt.FlowLayout;
import javax.swing.JCheckBox;
import javax.swing.BoxLayout;
import java.awt.Font;
import java.awt.Dimension;
import javax.swing.JButton;

public class MyPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JLabel jRobotTypeLabel = null;
	private JComboBox jRobotTypeComboBox = null;
	private JPanel jPanel = null;
	private JCheckBox jTypeAllCheckBox = null;
	private JPanel jPanel1 = null;
	private JLabel jActionTypeLabel = null;
	private JCheckBox jTypeLocomotionCheckBox = null;
	private JPanel jRow1Panel = null;
	private JButton jButton = null;
	private JCheckBox jLocalCheckBox = null;
	private JPanel jPanel2 = null;
	private JLabel jLabel = null;
	private JButton jButton1 = null;
	private JButton jButton2 = null;
	private JPanel jPanel3 = null;
	/**
	 * This method initializes jPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(new BoxLayout(getJPanel(), BoxLayout.Y_AXIS));
			jPanel.add(getJRow1Panel(), null);
			jPanel.add(getJPanel1(), null);
			jPanel.add(jLabel, null);
			jPanel.add(getJPanel3(), null);
		}
		return jPanel;
	}

	/**
	 * This method initializes jTypeAllCheckBox
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJTypeAllCheckBox() {
		if (jTypeAllCheckBox == null) {
			jTypeAllCheckBox = new JCheckBox();
			jTypeAllCheckBox.setText("All");
		}
		return jTypeAllCheckBox;
	}

	/**
	 * This method initializes jPanel1
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			FlowLayout flowLayout1 = new FlowLayout();
			flowLayout1.setVgap(0);
			flowLayout1.setAlignment(FlowLayout.LEFT);
			flowLayout1.setHgap(5);
			jActionTypeLabel = new JLabel();
			jActionTypeLabel.setText("Action type:");
			jActionTypeLabel.setFont(new Font("Dialog", Font.BOLD, 12));
			jPanel1 = new JPanel();
			jPanel1.setLayout(flowLayout1);
			jPanel1.add(jActionTypeLabel, null);
			jPanel1.add(getJPanel2(), null);
		}
		return jPanel1;
	}

	/**
	 * This method initializes jTypeLocomotionCheckBox
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJTypeLocomotionCheckBox() {
		if (jTypeLocomotionCheckBox == null) {
			jTypeLocomotionCheckBox = new JCheckBox();
			jTypeLocomotionCheckBox.setText("Global locomotion");
		}
		return jTypeLocomotionCheckBox;
	}

	/**
	 * This method initializes jRow1Panel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJRow1Panel() {
		if (jRow1Panel == null) {
			FlowLayout flowLayout2 = new FlowLayout();
			flowLayout2.setVgap(0);
			flowLayout2.setAlignment(FlowLayout.LEFT);
			jRow1Panel = new JPanel();
			jRow1Panel.setLayout(flowLayout2);
			jRow1Panel.add(jRobotTypeLabel, null);
			jRow1Panel.add(getJRobotTypeComboBox(), null);
		}
		return jRow1Panel;
	}

	/**
	 * This method initializes jButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("OK");
		}
		return jButton;
	}

	/**
	 * This method initializes jLocalCheckBox
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJLocalCheckBox() {
		if (jLocalCheckBox == null) {
			jLocalCheckBox = new JCheckBox();
			jLocalCheckBox.setText("Local manipulation");
		}
		return jLocalCheckBox;
	}

	/**
	 * This method initializes jPanel2
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			jPanel2 = new JPanel();
			jPanel2.setLayout(new BoxLayout(getJPanel2(), BoxLayout.Y_AXIS));
			jPanel2.add(getJTypeAllCheckBox(), null);
			jPanel2.add(getJTypeLocomotionCheckBox(), null);
			jPanel2.add(getJLocalCheckBox(), null);
		}
		return jPanel2;
	}

	/**
	 * This method initializes jButton1
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setText("Load library");
		}
		return jButton1;
	}

	/**
	 * This method initializes jButton2
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButton2() {
		if (jButton2 == null) {
			jButton2 = new JButton();
			jButton2.setText("Record new action");
		}
		return jButton2;
	}

	/**
	 * This method initializes jPanel3
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel3() {
		if (jPanel3 == null) {
			FlowLayout flowLayout3 = new FlowLayout();
			flowLayout3.setAlignment(FlowLayout.LEFT);
			jPanel3 = new JPanel();
			jPanel3.setLayout(flowLayout3);
			jPanel3.add(getJButton2(), null);
			jPanel3.add(getJButton1(), null);
		}
		return jPanel3;
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		JFrame frame = new JFrame();
		frame.add(new MyPanel());
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * This is the default constructor
	 */
	public MyPanel() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		jLabel = new JLabel();
		jLabel.setText("Available actions:");
		FlowLayout flowLayout = new FlowLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		jRobotTypeLabel = new JLabel();
		jRobotTypeLabel.setText("Robot type:");
		jRobotTypeLabel.setFont(new Font("Dialog", Font.BOLD, 12));
		this.setLayout(flowLayout);
		this.setSize(300, 200);
		this.add(getJPanel(), null);
		this.add(getJButton(), null);
	}

	/**
	 * This method initializes jRobotTypeComboBox
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJRobotTypeComboBox() {
		if (jRobotTypeComboBox == null) {
			jRobotTypeComboBox = new JComboBox();
			jRobotTypeComboBox.setPreferredSize(new Dimension(180, 30));
			jRobotTypeComboBox.addItem("Noopy");
		}
		return jRobotTypeComboBox;
	}

}
