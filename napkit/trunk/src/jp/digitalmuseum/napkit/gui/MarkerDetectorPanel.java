/*
 * PROJECT: napkit at http://mr.digitalmuseum.jp/
 * ----------------------------------------------------------------------------
 *
 * This file is part of NyARToolkit Application Toolkit.
 *
 * NyARToolkit Application Toolkit, or simply "napkit",
 * is a simple wrapper library for NyARToolkit.
 *
 * ----------------------------------------------------------------------------
 *
 * License version: GPL 3.0
 *
 * napkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * napkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with napkit. If not, see <http://www.gnu.org/licenses/>.
 */
package jp.digitalmuseum.napkit.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JCheckBox;
import java.awt.Dimension;
import javax.swing.JTextPane;

import com.phybots.Phybots;
import com.phybots.gui.DisposableComponent;
import com.phybots.gui.utils.GUIUtils;
import com.phybots.message.Event;
import com.phybots.message.EventListener;
import com.phybots.message.LocationUpdateEvent;
import com.phybots.message.ServiceEvent;
import com.phybots.message.ServiceStatus;
import com.phybots.message.ServiceUpdateEvent;
import com.phybots.service.ImageProvider;
import com.phybots.service.MarkerDetector;
import com.phybots.utils.ScreenRectangle;

import java.awt.SystemColor;
import java.awt.image.BufferedImage;
import java.util.Set;


/**
 * Panel class for configuring MarkerDetector.
 *
 * @author Jun KATO
 * @see com.phybots.service.MarkerDetector
 */
public class MarkerDetectorPanel extends JPanel implements DisposableComponent {
	private MarkerDetector markerDetector;  //  @jve:decl-index=0:
	private EventListener eventListener;
	private BufferedImage image;
	private boolean showBinarizedImage = true;

	private class JPreviewPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private final BasicStroke stroke = new BasicStroke(3);
		@Override public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (image != null) {
				final int
					x = (getWidth()-image.getWidth())/2,
					y = (getHeight()-image.getHeight())/2;
				g.drawImage(image, x, y, null);
				final Graphics2D g2 = (Graphics2D) g;
				g2.translate(x, y);
				g2.setStroke(stroke);
				g2.setColor(Color.orange);
				for (ScreenRectangle rectangle : markerDetector.getSquares()) {
					rectangle.draw(g2);
				}
			}
		}
	}

	private void setMarkerDetector(MarkerDetector markerDetector) {
		this.markerDetector = markerDetector;
		setImageProvider(markerDetector.getImageProvider());
		getJSlider().setValue(markerDetector.getThreshold());
		updateJSlider();
	}

	private void setImageProvider(ImageProvider imageProvider) {
		if (markerDetector.getImageProvider() == imageProvider) {
			return;
		}
		if (!imageProvider.equals(markerDetector.getImageProvider())) {
			markerDetector.setImageProvider(imageProvider);
		}
		jLabel.setText(imageProvider.getName() +
				", (" + Messages.getString("MarkerDetectorPanel.width") + imageProvider.getWidth() +
				", "  + Messages.getString("MarkerDetectorPanel.height") + imageProvider.getHeight() +
				"), " + Messages.getString("MarkerDetectorPanel.fps") + (1000/imageProvider.getInterval()));
		updateJComboBox();
	}

	private void setThreshold(int threshold) {
		if (markerDetector != null) {
			markerDetector.setThreshold(threshold);
		}
		updateJSlider();
	}

	private void showBinarizedImage(boolean showBinarizedImage) {
		this.showBinarizedImage = showBinarizedImage;
	}

	private void updateJComboBox() {
		Set<ImageProvider> providers = Phybots.getInstance().lookForServices(ImageProvider.class);
		getJComboBox().removeAllItems();
		for (ImageProvider provider : providers) {
			getJComboBox().addItem(provider);
		}
		getJComboBox().setSelectedItem(markerDetector.getImageProvider());
	}

	private void updateJSlider() {
		getJSlider().setToolTipText(Messages.getString("MarkerDetectorPanel.threshold")+(
				markerDetector == null ?
						"-" :
						markerDetector.getThreshold()));
		updateJTextPaneContents();
	}

	private void updateJTextPaneContents() {
		if (markerDetector == null) {
			getJTextPane().setText(
					Messages.getString("MarkerDetectorPanel.slideToDetectRobustlly"));
		}
		else {
			getJTextPane().setText(
					Messages.getString("MarkerDetectorPanel.slideToDetectRobustlly") + " " +
					Messages.getString("MarkerDetectorPanel.threshold") +
					markerDetector.getThreshold());
		}
	}

// Visual Editor generated codes below.

	private static final long serialVersionUID = 1L;
	private JPanel jPanel = null;
	private JComboBox jComboBox = null;
	private JLabel jLabel = null;
	private JPreviewPanel jPreviewPanel = null;
	private JPanel jPreviewOptionPanel = null;
	private JSlider jSlider = null;
	private JCheckBox jCheckBox = null;
	private JTextPane jTextPane = null;
	/**
	 * This is the default constructor
	 */
	public MarkerDetectorPanel(final MarkerDetector source) {
		super();
		initialize();
		setMarkerDetector(source);

		// Add event listener to the source.
		eventListener = new EventListener() {

			public void eventOccurred(Event e) {
				if (e instanceof LocationUpdateEvent) {
					image = showBinarizedImage ?
							MarkerDetectorPanel.this.
									markerDetector.getBinarizedImage() :
							MarkerDetectorPanel.this.
									markerDetector.getImageProvider().getImage();
					getJPreviewPanel().repaint();
				} else if (e instanceof ServiceEvent) {
					if (((ServiceEvent) e).getStatus() == ServiceStatus.DISPOSED) {
						dispose();
					}
				} else if (e instanceof ServiceUpdateEvent) {
					String paramName = ((ServiceUpdateEvent) e).getParameter();
					if (paramName == "threshold") {
						getJSlider().setValue(markerDetector.getThreshold());
					} else if (paramName == "image provider" || paramName == "coord provider") {
						updateJComboBox();
					}
				}
			}
		};
		source.addEventListener(eventListener);
	}

	public void dispose() {
		setEnabled(false);
		if (markerDetector != null) {
			markerDetector.removeEventListener(eventListener);
			markerDetector = null;
		}
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		this.setSize(320, 280);
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(320, 280));
		this.add(getJPreviewOptionPanel(), BorderLayout.SOUTH);
		this.add(getJPanel(), BorderLayout.NORTH);
		this.add(getJPreviewPanel(), BorderLayout.CENTER);
	}

	/**
	 * This method initializes jPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			BorderLayout borderLayout = new BorderLayout();
			borderLayout.setHgap(5);
			borderLayout.setVgap(5);
			jLabel = new JLabel();
			jLabel.setText(Messages.getString("MarkerDetectorPanel.descriptionHere"));
			jLabel.setToolTipText(Messages.getString("MarkerDetectorPanel.descriptionHere"));
			GUIUtils.setBorder(jLabel, 5);
			jPanel = new JPanel();
			jPanel.setLayout(borderLayout);
			jPanel.add(getJComboBox(), BorderLayout.NORTH);
			jPanel.add(jLabel, BorderLayout.CENTER);
		}
		return jPanel;
	}

	/**
	 * This method initializes jComboBox
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBox() {
		if (jComboBox == null) {
			jComboBox = new JComboBox();
			jComboBox.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					final Object item = ((JComboBox)e.getSource()).getSelectedItem();
					if (item instanceof ImageProvider) {
						setImageProvider(ImageProvider.class.cast(item));
					}
				}
			});
		}
		return jComboBox;
	}

	/**
	 * This method initializes jPreviewPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPreviewPanel getJPreviewPanel() {
		if (jPreviewPanel == null) {
			jPreviewPanel = new JPreviewPanel();
			jPreviewPanel.setLayout(new BorderLayout());
		}
		return jPreviewPanel;
	}

	/**
	 * This method initializes jPreviewOptionPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPreviewOptionPanel() {
		if (jPreviewOptionPanel == null) {
			jPreviewOptionPanel = new JPanel();
			jPreviewOptionPanel.setLayout(new BorderLayout());
			jPreviewOptionPanel.add(getJSlider(), BorderLayout.CENTER);
			jPreviewOptionPanel.add(getJCheckBox(), BorderLayout.EAST);
			jPreviewOptionPanel.add(getJTextPane(), BorderLayout.SOUTH);
		}
		return jPreviewOptionPanel;
	}

	/**
	 * This method initializes jSlider
	 *
	 * @return javax.swing.JSlider
	 */
	private JSlider getJSlider() {
		if (jSlider == null) {
			jSlider = new JSlider();
			jSlider.setMinimum(MarkerDetector.THRESHOLD_MIN);
			jSlider.setMaximum(MarkerDetector.THRESHOLD_MAX);
			jSlider.addChangeListener(new javax.swing.event.ChangeListener() {
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					final JSlider slider = (JSlider) e.getSource();
					setThreshold(slider.getValue());
				}
			});
		}
		return jSlider;
	}

	/**
	 * This method initializes jCheckBox
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBox() {
		if (jCheckBox == null) {
			jCheckBox = new JCheckBox();
			jCheckBox.setText(Messages.getString("MarkerDetectorPanel.showBinarizedImage"));
			jCheckBox.setSelected(showBinarizedImage);
			jCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					showBinarizedImage(((JCheckBox) e.getSource()).isSelected());
				}
			});
		}
		return jCheckBox;
	}

	/**
	 * This method initializes jTextPane
	 *
	 * @return javax.swing.JTextPane
	 */
	private JTextPane getJTextPane() {
		if (jTextPane == null) {
			jTextPane = new JTextPane();
			jTextPane.setBackground(SystemColor.control);
			updateJTextPaneContents();
			GUIUtils.setBorder(jTextPane, 5);
		}
		return jTextPane;
	}

}  //  @jve:decl-index=0:visual-constraint="125,58"
