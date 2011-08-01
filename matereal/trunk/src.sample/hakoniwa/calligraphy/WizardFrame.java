package hakoniwa.calligraphy;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JButton;

import java.awt.FlowLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import java.awt.Font;

public class WizardFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JPanel jNavigationPanel = null;
	private JButton jNextButton = null;
	private JPanel jConfigurationPanel = null;
	private transient CardLayout cardLayout;
	private transient final JComponent[] components;
	private transient int index = 0;
	private JPanel jTitlePanel = null;
	private JLabel jTitleLabel = null;

	/**
	 * This is the default constructor
	 */
	public WizardFrame(JComponent[] components) {
		super();
		initialize();
		this.components = components;
		for (JComponent card : components) {
			getJConfigurationPanel().add(card, card.getName());
		}
		loadWizardComponent();
	}

	public void next() {
		if (index + 1 >= components.length) {
			return;
		} else if (index + 2 >= components.length) {
			getJNextButton().setEnabled(false);
		}
		unloadWizardComponent();
		cardLayout.next(getJConfigurationPanel());
		index = (index + 1) % components.length;
		loadWizardComponent();
	}

	private void unloadWizardComponent() {
		if (components[index] instanceof WizardComponent) {
			((WizardComponent)components[index]).onUnload();
		}
	}

	private void loadWizardComponent() {
		getJTitleLabel().setText("Step."+(index + 1)+" "+components[index].getName());
		if (components[index] instanceof WizardComponent) {
			((WizardComponent)components[index]).onLoad();
		}
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
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJTitlePanel(), BorderLayout.NORTH);
			jContentPane.add(getJConfigurationPanel(), BorderLayout.CENTER);
			jContentPane.add(getJNavigationPanel(), BorderLayout.SOUTH);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jNavigationPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJNavigationPanel() {
		if (jNavigationPanel == null) {
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setAlignment(FlowLayout.RIGHT);
			jNavigationPanel = new JPanel();
			jNavigationPanel.setLayout(flowLayout);
			jNavigationPanel.add(getJNextButton(), null);
		}
		return jNavigationPanel;
	}

	/**
	 * This method initializes jNextButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJNextButton() {
		if (jNextButton == null) {
			jNextButton = new JButton();
			jNextButton.setText("Next");
			jNextButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) { next(); }
			});
		}
		return jNextButton;
	}

	/**
	 * This method initializes jConfigurationPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJConfigurationPanel() {
		if (jConfigurationPanel == null) {
			cardLayout = new CardLayout();
			cardLayout.setHgap(5);
			cardLayout.setVgap(5);
			jConfigurationPanel = new JPanel();
			jConfigurationPanel.setLayout(cardLayout);
		}
		return jConfigurationPanel;
	}

	/**
	 * This method initializes jTitlePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJTitlePanel() {
		if (jTitlePanel == null) {
			FlowLayout flowLayout1 = new FlowLayout();
			flowLayout1.setAlignment(FlowLayout.LEFT);
			jTitlePanel = new JPanel();
			jTitlePanel.setLayout(flowLayout1);
			jTitlePanel.add(getJTitleLabel(), null);
		}
		return jTitlePanel;
	}

	private JLabel getJTitleLabel() {
		if (jTitleLabel == null) {
			jTitleLabel = new JLabel();
			jTitleLabel.setText("JLabel");
			jTitleLabel.setFont(new Font("Dialog", Font.BOLD, 24));
		}
		return jTitleLabel;
	}

}
