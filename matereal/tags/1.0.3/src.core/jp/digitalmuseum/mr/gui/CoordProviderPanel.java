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
package jp.digitalmuseum.mr.gui;

import java.awt.Graphics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import jp.digitalmuseum.mr.gui.utils.GUIUtils;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.message.ImageUpdateEvent;
import jp.digitalmuseum.mr.message.ServiceEvent;
import jp.digitalmuseum.mr.message.ServiceUpdateEvent;
import jp.digitalmuseum.mr.message.ServiceEvent.STATUS;
import jp.digitalmuseum.mr.service.CoordProvider;
import jp.digitalmuseum.mr.service.HomographyCoordProvider;
import jp.digitalmuseum.utils.ScreenPosition;
import jp.digitalmuseum.utils.ScreenRectangle;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.JLabel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Stroke;

import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.Font;

/**
 * Panel class for configuring CoordProvider.
 *
 * @author Jun KATO
 * @see jp.digitalmuseum.mr.service.CoordProvider
 */
public class CoordProviderPanel extends JPanel implements DisposableComponent {
	private static final long serialVersionUID = 1L;
	private static final int RADIUS = 6;
	private final transient CoordProvider source;
	private final transient EventListener eventListener;
	private final transient Stroke stroke;
	private transient BufferedImage image;
	private final ScreenRectangle rectangle = new ScreenRectangle();
	private int dragging = -1;

	protected JPanel jPanel = null;
	private JPanel jInputPanel = null;
	private JTextField jRealXField = null;
	private JLabel jRealXLabel = null;
	private JLabel jRealXPostfixLabel = null;
	private JPanel jRealXPanel = null;
	private JPanel jRealYPanel = null;
	private JLabel jRealYLabel = null;
	private JTextField jRealYField = null;
	private JLabel jRealYPostfixLabel = null;
	private JPanel jCommandPanel = null;
	protected JButton jResetButton = null;
	private JButton jApplyButton = null;
	private JPanel jApplyPanel = null;
	private JPanel jResetPanel = null;

	public CoordProviderPanel(CoordProvider coordProvider) {
		source = coordProvider;
		stroke = new BasicStroke(2);

		// Add event listener to the source.
		eventListener = new EventListener() {
			public void eventOccurred(Event e) {
				if (e instanceof ImageUpdateEvent) {
					image = source.getImage();
					repaint();
				} else if (e instanceof ServiceEvent) {
					if (((ServiceEvent) e).getStatus() == STATUS.DISPOSED) {
						dispose();
					}
				} else if (e instanceof ServiceUpdateEvent) {
					updateRealSize();
				}
			}
		};
		source.addEventListener(eventListener);
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
        this.setLayout(new BorderLayout());
        this.add(getJPanel(), BorderLayout.CENTER);
        this.add(getJInputPanel(), BorderLayout.SOUTH);

	}

	public void dispose() {
		source.removeEventListener(eventListener);
		setEnabled(false);
	}

	public CoordProvider getSource() {
		return source;
	}

	private int getOffsetX() {
		return (getJPanel().getWidth() -
				(image == null ? 0 : image.getWidth()))/2;
	}

	private int getOffsetY() {
		return (getJPanel().getHeight() -
				(image == null ? 0 : image.getHeight()))/2;
	}

	private void updateRealSize() {
		getJRealXField().setText(String.valueOf(source.getRealWidth()));
		getJRealYField().setText(String.valueOf(source.getRealHeight()));
	}

	/**
	 * This method initializes jPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel() {
				private static final long serialVersionUID = 1L;
				@Override public void paintComponent(Graphics g) {
					super.paintComponent(g);
					g.translate(getOffsetX(), getOffsetY());
					if (image != null) {
						g.setColor(Color.black);
						g.drawRect(-1, -1, image.getWidth()+2, image.getHeight()+2);
						g.setColor(Color.white);
						g.fillRect(0, 0, image.getWidth(), image.getHeight());
						g.drawImage(image, 0, 0, null);
					}
					if (source instanceof HomographyCoordProvider) {
						((HomographyCoordProvider) source).getRectangleOut(rectangle);
						final Graphics2D g2 = (Graphics2D) g;
						g2.setColor(Color.orange);
						g2.setStroke(stroke);
						rectangle.draw(g2);
						for (int i = 0; i < 4; i ++) {
							final ScreenPosition p = rectangle.get(i);
							String real,
									x = getJRealXField().getText(),
									y = getJRealYField().getText();
							switch (i) {
							case ScreenRectangle.LEFT_BOTTOM:
								real = "0, 0"; break;
							case ScreenRectangle.LEFT_TOP:
								real = "0, "+y; break;
							case ScreenRectangle.RIGHT_BOTTOM:
								real = x+", 0"; break;
							default:
								real = x+", "+y; break;
							}
							g2.setColor(i == dragging ? Color.red : Color.orange);
							g2.drawOval(p.getX()-RADIUS, p.getY()-RADIUS, RADIUS*2, RADIUS*2);
							g2.drawString("("+p.toString()+")[px] = ("+real+")[cm]", p.getX()+RADIUS*3/2, p.getY());
						}
					}
				}
			};
			if (source instanceof HomographyCoordProvider) {
				jPanel.addMouseMotionListener(new MouseMotionListener() {
					public void mouseDragged(MouseEvent e) {
						final int
								x = e.getX() - getOffsetX(),
								y = e.getY() - getOffsetY();
						((HomographyCoordProvider) source).getRectangleOut(rectangle);
						dragging = rectangle.getRectangleCornerIndexNear(x, y);
						((HomographyCoordProvider) source).setRectangleCorner(dragging, x, y);
					}
					public void mouseMoved(MouseEvent e) {
						dragging = -1;
					}
				});
			}
		}
		return jPanel;
	}

	/**
	 * This method initializes jInputPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJInputPanel() {
		if (jInputPanel == null) {
			jInputPanel = new JPanel();
			jInputPanel.setLayout(new BoxLayout(getJInputPanel(), BoxLayout.Y_AXIS));
			jInputPanel.add(getJRealXPanel(), null);
			jInputPanel.add(getJRealYPanel(), null);
			jInputPanel.add(getJCommandPanel(), null);
		}
		return jInputPanel;
	}

	/**
	 * This method initializes jRealXPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJRealXPanel() {
		if (jRealXPanel == null) {
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setVgap(0);
			jRealXPostfixLabel = new JLabel();
			jRealXPostfixLabel.setText(Messages.getString("CoordProviderPanel.centimeter")); //$NON-NLS-1$
			jRealXPostfixLabel.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			jRealXLabel = new JLabel();
			jRealXLabel.setText(Messages.getString("CoordProviderPanel.width")); //$NON-NLS-1$
			jRealXLabel.setPreferredSize(new Dimension(180, 16));
			jRealXPanel = new JPanel();
			jRealXPanel.setLayout(flowLayout);
			jRealXPanel.add(jRealXLabel, null);
			jRealXPanel.add(getJRealXField(), null);
			jRealXPanel.add(jRealXPostfixLabel, null);
			updateRealSize();
		}
		return jRealXPanel;
	}

	/**
	 * This method initializes jRealXField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJRealXField() {
		if (jRealXField == null) {
			jRealXField = new JTextField();
			jRealXField.setPreferredSize(new Dimension(48, 20));
		}
		return jRealXField;
	}

	/**
	 * This method initializes jRealYPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJRealYPanel() {
		if (jRealYPanel == null) {
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setVgap(0);
			jRealYPostfixLabel = new JLabel();
			jRealYPostfixLabel.setText(Messages.getString("CoordProviderPanel.centimeter")); //$NON-NLS-1$
			jRealYPostfixLabel.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			jRealYLabel = new JLabel();
			jRealYLabel.setText(Messages.getString("CoordProviderPanel.height")); //$NON-NLS-1$
			jRealYLabel.setPreferredSize(new Dimension(180, 16));
			jRealYPanel = new JPanel();
			jRealYPanel.setLayout(flowLayout);
			jRealYPanel.add(jRealYLabel, null);
			jRealYPanel.add(getJRealYField(), null);
			jRealYPanel.add(jRealYPostfixLabel, null);
		}
		return jRealYPanel;
	}

	/**
	 * This method initializes jRealYField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJRealYField() {
		if (jRealYField == null) {
			jRealYField = new JTextField();
			jRealYField.setPreferredSize(new Dimension(48, 20));
		}
		return jRealYField;
	}

	/**
	 * This method initializes jCommandPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJCommandPanel() {
		if (jCommandPanel == null) {
			jCommandPanel = new JPanel();
			jCommandPanel.setLayout(new BoxLayout(getJCommandPanel(), BoxLayout.Y_AXIS));
			jCommandPanel.add(getJApplyPanel(), null);
			jCommandPanel.add(getJResetPanel(), null);
		}
		return jCommandPanel;
	}

	/**
	 * This method initializes jResetButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJResetButton() {
		if (jResetButton == null) {
			jResetButton = new JButton();
			jResetButton.setText(Messages.getString("CoordProviderPanel.reset")); //$NON-NLS-1$
			jResetButton.setName("jResetButton"); //$NON-NLS-1$
			jResetButton.setHorizontalAlignment(SwingConstants.RIGHT);
			if (source instanceof HomographyCoordProvider) {
				jResetButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						((HomographyCoordProvider) source).resetRectangle();
					}
				});
			} else {
				jResetButton.setEnabled(false);
			}
		}
		return jResetButton;
	}

	/**
	 * This method initializes jApplyButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJApplyButton() {
		if (jApplyButton == null) {
			jApplyButton = new JButton();
			jApplyButton.setHorizontalAlignment(SwingConstants.RIGHT);
			jApplyButton.setName("jApplyButton"); //$NON-NLS-1$
			jApplyButton.setText(Messages.getString("CoordProviderPanel.apply")); //$NON-NLS-1$
				if (source instanceof HomographyCoordProvider) {
				jApplyButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						double
							realWidth = source.getRealWidth(),
							realHeight = source.getRealHeight();
						try {
							realWidth = Double.parseDouble(jRealXField.getText());
						} catch (NumberFormatException nfe) {
							GUIUtils.errorDialog(CoordProviderPanel.this,
									Messages.getString("CoordProviderPanel.errorDescWidth"), Messages.getString("CoordProviderPanel.errorTitle")); //$NON-NLS-1$ //$NON-NLS-2$
							return;
						}
						((HomographyCoordProvider) source).setRealWidth(realWidth);
						try {
							realHeight = Double.parseDouble(jRealXField.getText());
						} catch (NumberFormatException nfe) {
							GUIUtils.errorDialog(CoordProviderPanel.this,
									Messages.getString("CoordProviderPanel.errorDescHeight"), Messages.getString("CoordProviderPanel.errorTitle")); //$NON-NLS-1$ //$NON-NLS-2$
							return;
						}
						((HomographyCoordProvider) source).setRealHeight(realHeight);
					}
				});
			} else {
				jApplyButton.setEnabled(false);
			}
		}
		return jApplyButton;
	}

	/**
	 * This method initializes jApplyPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJApplyPanel() {
		if (jApplyPanel == null) {
			FlowLayout flowLayout1 = new FlowLayout();
			flowLayout1.setAlignment(FlowLayout.RIGHT);
			jApplyPanel = new JPanel();
			jApplyPanel.setLayout(flowLayout1);
			jApplyPanel.add(getJApplyButton(), null);
		}
		return jApplyPanel;
	}

	/**
	 * This method initializes jResetPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJResetPanel() {
		if (jResetPanel == null) {
			FlowLayout flowLayout2 = new FlowLayout();
			flowLayout2.setAlignment(FlowLayout.RIGHT);
			jResetPanel = new JPanel();
			jResetPanel.setLayout(flowLayout2);
			jResetPanel.add(getJResetButton(), null);
		}
		return jResetPanel;
	}

}
