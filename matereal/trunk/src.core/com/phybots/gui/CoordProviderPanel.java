/*
 * PROJECT: Phybots at http://phybots.com/
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
 * The Original Code is Phybots.
 *
 * The Initial Developer of the Original Code is Jun Kato.
 * Portions created by the Initial Developer are
 * Copyright (C) 2009 Jun Kato. All Rights Reserved.
 *
 * Contributor(s): Jun Kato
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
package com.phybots.gui;

import java.awt.Graphics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;


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

import com.phybots.Phybots;
import com.phybots.gui.utils.GUIUtils;
import com.phybots.message.Event;
import com.phybots.message.EventListener;
import com.phybots.message.ImageUpdateEvent;
import com.phybots.message.ServiceEvent;
import com.phybots.message.ServiceStatus;
import com.phybots.message.ServiceUpdateEvent;
import com.phybots.service.CoordProvider;
import com.phybots.service.HomographyCoordProvider;
import com.phybots.utils.ScreenPosition;
import com.phybots.utils.ScreenRectangle;

/**
 * Panel class for configuring CoordProvider.
 *
 * @author Jun Kato
 * @see com.phybots.service.CoordProvider
 */
public class CoordProviderPanel extends JPanel implements DisposableComponent {
	private static final long serialVersionUID = 1L;
	private static final int RADIUS = 6;
	private transient CoordProvider source;
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
	protected JButton jResetButton = null;
	private JButton jApplyButton = null;
	private JPanel jCommandPanel = null;
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
					if (((ServiceEvent) e).getStatus() == ServiceStatus.DISPOSED) {
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
		setEnabled(false);
		if (source != null) {
			source.removeEventListener(eventListener);
			source = null;
		}
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
									x = String.format("%.1f", source.getRealWidth()),
									y = String.format("%.1f", source.getRealHeight());
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
			jRealXPostfixLabel.setFont(Phybots.getInstance().getDefaultFont());
			jRealXPostfixLabel.setText(Messages.getString("CoordProviderPanel.centimeter")); //$NON-NLS-1$
			jRealXLabel = new JLabel();
			jRealXLabel.setFont(Phybots.getInstance().getDefaultFont());
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
			jRealXField.setFont(Phybots.getInstance().getDefaultFont());
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
			jRealYPostfixLabel.setFont(Phybots.getInstance().getDefaultFont());
			jRealYPostfixLabel.setText(Messages.getString("CoordProviderPanel.centimeter")); //$NON-NLS-1$
			jRealYLabel = new JLabel();
			jRealYLabel.setFont(Phybots.getInstance().getDefaultFont());
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
			jRealYField.setFont(Phybots.getInstance().getDefaultFont());
			jRealYField.setPreferredSize(new Dimension(48, 20));
		}
		return jRealYField;
	}

	/**
	 * This method initializes jResetButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJResetButton() {
		if (jResetButton == null) {
			jResetButton = new JButton();
			jResetButton.setFont(Phybots.getInstance().getDefaultFont());
			jResetButton.setText(Messages.getString("CoordProviderPanel.reset")); //$NON-NLS-1$
			jResetButton.setHorizontalAlignment(SwingConstants.RIGHT);
			if (source instanceof HomographyCoordProvider) {
				jResetButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						((HomographyCoordProvider) source).resetRectangle();
						updateRealSize();
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
			jApplyButton.setFont(Phybots.getInstance().getDefaultFont());
			jApplyButton.setText(Messages.getString("CoordProviderPanel.apply")); //$NON-NLS-1$
			if (source instanceof HomographyCoordProvider) {
				jApplyButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						double realWidth, realHeight;
						try {
							realWidth = Double.parseDouble(getJRealXField().getText());
						} catch (NumberFormatException nfe) {
							GUIUtils.errorDialog(CoordProviderPanel.this,
									Messages.getString("CoordProviderPanel.errorDescWidth"), Messages.getString("CoordProviderPanel.errorTitle")); //$NON-NLS-1$ //$NON-NLS-2$
							return;
						}
						try {
							realHeight = Double.parseDouble(getJRealYField().getText());
						} catch (NumberFormatException nfe) {
							GUIUtils.errorDialog(CoordProviderPanel.this,
									Messages.getString("CoordProviderPanel.errorDescHeight"), Messages.getString("CoordProviderPanel.errorTitle")); //$NON-NLS-1$ //$NON-NLS-2$
							return;
						}
						((HomographyCoordProvider) source).setRealWidth(realWidth);
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
	 * This method initializes jCommandPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJCommandPanel() {
		if (jCommandPanel == null) {
			FlowLayout flowLayout1 = new FlowLayout();
			flowLayout1.setAlignment(FlowLayout.RIGHT);
			jCommandPanel = new JPanel();
			jCommandPanel.setLayout(flowLayout1);
			jCommandPanel.add(getJResetButton(), null);
			jCommandPanel.add(getJApplyButton(), null);
		}
		return jCommandPanel;
	}

}
