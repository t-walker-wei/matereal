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

import javax.swing.JPanel;

import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;


import javax.swing.JLabel;

import com.phybots.Phybots;
import com.phybots.entity.PhysicalRobot;
import com.phybots.gui.Messages;

import java.awt.Insets;

public class PhysicalRobotPanel extends JPanel {

	private static final long serialVersionUID = -1601581324059426126L;
	private RobotResourcePanel robotResourcePanel = null;
	private transient PhysicalRobot physicalRobot;
	private JLabel resourceLabel = null;
	private JPanel entityPanel = null;
	private JLabel connectorLabel = null;
	private ConnectorPanel connectorPanel = null;

	/**
	 * This method initializes
	 *
	 */
	public PhysicalRobotPanel(PhysicalRobot physicalRobot) {
		super();
		this.physicalRobot = physicalRobot;
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 1.0D;
		gridBagConstraints.weighty = 0.0D;

		GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
		gridBagConstraints11.fill = GridBagConstraints.BOTH;
		gridBagConstraints11.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints11.gridx = 0;
		gridBagConstraints11.gridy = 1;
		gridBagConstraints11.weightx = 1.0D;
		gridBagConstraints11.weighty = 0.0D;
		connectorLabel = new JLabel();
		connectorLabel.setFont(Phybots.getInstance().getDefaultFont().deriveFont(Font.BOLD));
		connectorLabel.setText(Messages.getString("PhysicalRobotPanel.connector"));

		GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
		gridBagConstraints12.fill = GridBagConstraints.BOTH;
		gridBagConstraints12.gridx = 0;
		gridBagConstraints12.gridy = 2;
		gridBagConstraints12.weightx = 1.0D;
		gridBagConstraints12.weighty = 0.0D;

		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.fill = GridBagConstraints.BOTH;
		gridBagConstraints2.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints2.gridx = 0;
		gridBagConstraints2.gridy = 3;
		gridBagConstraints2.weightx = 1.0D;
		gridBagConstraints2.weighty = 0.0D;
		resourceLabel = new JLabel();
		resourceLabel.setFont(Phybots.getInstance().getDefaultFont().deriveFont(Font.BOLD)); //$NON-NLS-1$
		resourceLabel.setText(Messages.getString("RobotPanel.resources"));

		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.fill = GridBagConstraints.BOTH;
		gridBagConstraints1.insets = new Insets(0, 5, 5, 0);
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.gridy = 4;
		gridBagConstraints1.weightx = 1.0D;
		gridBagConstraints1.weighty = 1.0D;

		this.setLayout(new GridBagLayout());
		this.add(getEntityPanel(), gridBagConstraints);
		this.add(connectorLabel, gridBagConstraints11);
		this.add(getConnectorPanel(), gridBagConstraints12);
		this.add(resourceLabel, gridBagConstraints2);
		this.add(getRobotResourcePanel(), gridBagConstraints1);
	}

	/**
	 * This method initializes jPanel1
	 *
	 * @return javax.swing.JPanel
	 */
	private RobotResourcePanel getRobotResourcePanel() {
		if (robotResourcePanel == null) {
			robotResourcePanel = new RobotResourcePanel(physicalRobot);
		}
		return robotResourcePanel;
	}

	/**
	 * This method initializes entityPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getEntityPanel() {
		if (entityPanel == null) {
			entityPanel = new EntityPanel(physicalRobot);
		}
		return entityPanel;
	}

	/**
	 * This method initializes connectorPanel
	 *
	 */
	private ConnectorPanel getConnectorPanel() {
		if (connectorPanel == null) {
			connectorPanel = new ConnectorPanel(physicalRobot);
		}
		return connectorPanel;
	}

}
