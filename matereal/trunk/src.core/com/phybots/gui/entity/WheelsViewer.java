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
package com.phybots.gui.entity;

import java.awt.GridBagLayout;
import javax.swing.JPanel;

import javax.swing.JLabel;

import com.phybots.Phybots;
import com.phybots.gui.DisposableComponent;
import com.phybots.gui.Messages;
import com.phybots.gui.MeterPanel;
import com.phybots.resource.DifferentialWheels;
import com.phybots.resource.Wheels;
import com.phybots.service.Service;
import com.phybots.service.ServiceAbstractImpl;


import java.awt.Font;
import java.awt.GridBagConstraints;

public class WheelsViewer extends JPanel implements DisposableComponent {

	private static final long serialVersionUID = 1L;
	private JLabel jStatusLabel = null;
	private JLabel jPowerLeftLabel = null;
	private MeterPanel jPowerLeftMeter = null;
	private JLabel jPowerRightLabel = null;
	private MeterPanel jPowerRightMeter = null;
	private transient Wheels wheels;
	private transient Service wheelsMonitor;

	/**
	 * This is the default constructor
	 */
	public WheelsViewer(Wheels wheels) {
		super();
		initialize();
		this.wheels = wheels;
		wheelsMonitor = new ServiceAbstractImpl() {
			private static final long serialVersionUID = 8561727051108470192L;
			public String getName() {
				return "Wheels viewer";
			}
			public void run() {
				update();
			}
		};
		update();
		wheelsMonitor.start();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		final Font defaultFont = Phybots.getInstance().getDefaultFont().deriveFont(12);
		GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
		gridBagConstraints7.gridy = 2;
		gridBagConstraints7.gridx = 1;
		gridBagConstraints7.fill = GridBagConstraints.BOTH;
		GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
		gridBagConstraints6.gridy = 2;
		gridBagConstraints6.gridx = 0;
		gridBagConstraints6.anchor = GridBagConstraints.WEST;
		gridBagConstraints6.fill = GridBagConstraints.VERTICAL;
		GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
		gridBagConstraints5.gridy = 1;
		gridBagConstraints5.gridx = 1;
		gridBagConstraints5.weightx = 0.9D;
		gridBagConstraints5.fill = GridBagConstraints.BOTH;
		GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
		gridBagConstraints4.gridy = 1;
		gridBagConstraints4.gridx = 0;
		gridBagConstraints4.weightx = 0.1D;
		gridBagConstraints4.anchor = GridBagConstraints.WEST;
		gridBagConstraints4.fill = GridBagConstraints.VERTICAL;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridy = 1;
		gridBagConstraints1.gridx = 0;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.gridx = 0;
		jPowerRightLabel = new JLabel();
		jPowerRightLabel.setFont(defaultFont);
		jPowerRightLabel.setText(Messages.getString("WheelsViewer.rightWheel"));
		jPowerLeftLabel = new JLabel();
		jPowerLeftLabel.setFont(defaultFont);
		jPowerLeftLabel.setText(Messages.getString("WheelsViewer.leftWheel"));
		jStatusLabel = new JLabel();
		jStatusLabel.setFont(defaultFont.deriveFont(Font.BOLD));
		setLayout(new GridBagLayout());
		this.add(jStatusLabel, gridBagConstraints);
		this.add(jPowerLeftLabel, gridBagConstraints4);
		this.add(getJPowerLeftMeter(), gridBagConstraints5);
		this.add(jPowerRightLabel, gridBagConstraints6);
		this.add(getJPowerRightMeter(), gridBagConstraints7);
	}

	public void dispose() {
		wheelsMonitor.dispose();
	}

	/**
	 * This method initializes jPowerLeftMeter
	 *
	 * @return MeterPanel
	 */
	private MeterPanel getJPowerLeftMeter() {
		if (jPowerLeftMeter == null) {
			jPowerLeftMeter = new MeterPanel();
		}
		return jPowerLeftMeter;
	}

	/**
	 * This method initializes jPowerRightMeter
	 *
	 * @return MeterPanel
	 */
	private MeterPanel getJPowerRightMeter() {
		if (jPowerRightMeter == null) {
			jPowerRightMeter = new MeterPanel();
		}
		return jPowerRightMeter;
	}

	public void update() {
		jStatusLabel.setText(wheels.getStatus().toString());
		int leftWheelPower, rightWheelPower;
		if (wheels instanceof DifferentialWheels) {
			final DifferentialWheels dw = (DifferentialWheels) wheels;
			leftWheelPower = dw.getLeftWheelPower();
			rightWheelPower = dw.getRightWheelPower();
		} else {
			switch (wheels.getStatus()) {
			case GO_FORWARD:
				leftWheelPower = rightWheelPower = 100;
				break;
			case GO_BACKWARD:
				leftWheelPower = rightWheelPower = -100;
				break;
			case SPIN_LEFT:
				leftWheelPower = -100;
				rightWheelPower = 100;
				break;
			case SPIN_RIGHT:
				leftWheelPower = 100;
				rightWheelPower = -100;
				break;
			default:
				leftWheelPower = rightWheelPower = 0;
				break;
			}
		}
		jPowerLeftMeter.setPercentage((leftWheelPower+100)/2);
		jPowerLeftMeter.repaint();
		jPowerRightMeter.setPercentage((rightWheelPower+100)/2);
		jPowerRightMeter.repaint();
	}
}