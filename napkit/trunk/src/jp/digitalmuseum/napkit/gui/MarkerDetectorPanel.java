/*
 * PROJECT: matereal at http://mr.digitalmuseum.jp/
 * ----------------------------------------------------------------------------
 *
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is matereal.
 *
 * The Initial Developer of the Original Code is Jun KATO.
 * Portions created by the Initial Developer are
 * Copyright (C) 2009 Jun KATO. All Rights Reserved.
 *
 * Contributor(s): Jun KATO
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 */
package jp.digitalmuseum.napkit.gui;

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
import java.awt.SystemColor;
import java.awt.image.BufferedImage;
import java.util.Set;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.gui.DisposableComponent;
import jp.digitalmuseum.mr.gui.utils.GUIUtils;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.message.LocationUpdateEvent;
import jp.digitalmuseum.mr.message.ServiceEvent;
import jp.digitalmuseum.mr.message.ServiceUpdateEvent;
import jp.digitalmuseum.mr.message.ServiceEvent.STATUS;
import jp.digitalmuseum.mr.service.ImageProvider;
import jp.digitalmuseum.mr.service.MarkerDetector;
import jp.digitalmuseum.utils.ScreenRectangle;

/**
 * Panel class for configuring MarkerDetector.
 *
 * @author Jun KATO
 * @see jp.digitalmuseum.mr.service.MarkerDetector
 */
public class MarkerDetectorPanel extends JPanel implements DisposableComponent {
	private MarkerDetector markerDetector;  //  @jve:decl-index=0:
	private ImageProvider imageProvider;  //  @jve:decl-index=0:
	private EventListener eventListener;
	private BufferedImage image;
	private boolean showBinarizedImage = true;

	private class JPreviewPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		@Override public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (image != null) {
				final int
					x = (getWidth()-image.getWidth())/2,
					y = (getHeight()-image.getHeight())/2;
				g.drawImage(image, x, y, null);
				final Graphics2D g2 = (Graphics2D) g;
				g2.translate(x, y);
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
		if (this.imageProvider == imageProvider) {
			return;
		}
		if (!imageProvider.equals(markerDetector.getImageProvider())) {
			markerDetector.setImageProvider(imageProvider);
		}
		this.imageProvider = imageProvider;
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
		Set<ImageProvider> providers = Matereal.getInstance().lookForServices(ImageProvider.class);
		getJComboBox().removeAllItems();
		for (ImageProvider provider : providers) {
			getJComboBox().addItem(provider);
		}
		getJComboBox().setSelectedItem(imageProvider);
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
									imageProvider.getImage();
					getJPreviewPanel().repaint();
				} else if (e instanceof ServiceEvent) {
					if (((ServiceEvent) e).getStatus() == STATUS.DISPOSED) {
						dispose();
					}
				} else if (e instanceof ServiceUpdateEvent) {
					if (((ServiceUpdateEvent) e).getParameter() == "threshold") {
						getJSlider().setValue(markerDetector.getThreshold());
					}
				}
			}
		};
		source.addEventListener(eventListener);
	}

	public void dispose() {
		markerDetector.removeEventListener(eventListener);
		setEnabled(false);
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
