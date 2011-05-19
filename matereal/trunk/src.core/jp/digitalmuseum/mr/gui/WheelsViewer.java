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

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.GridLayout;

import javax.swing.JLabel;

import jp.digitalmuseum.mr.resource.DifferentialWheels;
import jp.digitalmuseum.mr.resource.Wheels;
import jp.digitalmuseum.mr.service.Service;
import jp.digitalmuseum.mr.service.ServiceAbstractImpl;

import java.awt.GridBagConstraints;
import java.awt.Insets;

public class WheelsViewer extends JPanel implements DisposableComponent {

	private static final long serialVersionUID = 1L;
	private JLabel jStatusLabel = null;
	private JPanel jStatusDetailPanel = null;
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
		wheelsMonitor.start();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		GridLayout gridLayout2 = new GridLayout();
		gridLayout2.setRows(2);
		gridLayout2.setVgap(5);
		gridLayout2.setColumns(1);
		setLayout(gridLayout2);
		jStatusLabel = new JLabel();
		jStatusLabel.setText("JLabel");
		add(jStatusLabel, null);
		add(getJStatusDetailPanel(), null);
	}

	public void dispose() {
		wheelsMonitor.stop();
	}

	/**
	 * This method initializes jStatusDetailPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJStatusDetailPanel() {
		if (jStatusDetailPanel == null) {
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.insets = new Insets(3, 3, 0, 0);
			gridBagConstraints7.gridy = 1;
			gridBagConstraints7.ipadx = 59;
			gridBagConstraints7.ipady = 6;
			gridBagConstraints7.fill = GridBagConstraints.BOTH;
			gridBagConstraints7.gridx = 1;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.insets = new Insets(3, 0, 0, 2);
			gridBagConstraints6.gridy = 1;
			gridBagConstraints6.gridx = 0;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.insets = new Insets(0, 3, 2, 0);
			gridBagConstraints5.gridy = 0;
			gridBagConstraints5.ipadx = 59;
			gridBagConstraints5.ipady = 6;
			gridBagConstraints5.weightx = 0.9D;
			gridBagConstraints5.fill = GridBagConstraints.BOTH;
			gridBagConstraints5.gridx = 1;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.insets = new Insets(0, 0, 2, 2);
			gridBagConstraints4.gridy = 0;
			gridBagConstraints4.ipadx = 7;
			gridBagConstraints4.weightx = 0.1D;
			gridBagConstraints4.gridx = 0;
			jPowerRightLabel = new JLabel();
			jPowerRightLabel.setText("Right wheel:");
			jPowerLeftLabel = new JLabel();
			jPowerLeftLabel.setText("Left wheel:");
			jStatusDetailPanel = new JPanel();
			jStatusDetailPanel.setLayout(new GridBagLayout());
			jStatusDetailPanel.add(jPowerLeftLabel, gridBagConstraints4);
			jStatusDetailPanel.add(getJPowerLeftMeter(), gridBagConstraints5);
			jStatusDetailPanel.add(jPowerRightLabel, gridBagConstraints6);
			jStatusDetailPanel.add(getJPowerRightMeter(), gridBagConstraints7);
		}
		return jStatusDetailPanel;
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