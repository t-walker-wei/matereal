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
package jp.digitalmuseum.mr.gui.entity;

import javax.swing.JPanel;

import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.PhysicalRobot;
import jp.digitalmuseum.mr.gui.Messages;

import javax.swing.JLabel;
import java.awt.Insets;

public class PhysicalRobotPanel extends JPanel {

	private static final long serialVersionUID = -1601581324059426126L;
	private RobotResourcePanel robotResourcePanel = null;
	private transient PhysicalRobot robot;
	private JLabel resourceLabel = null;
	private JPanel entityPanel = null;
	private JLabel connectorLabel = null;

	/**
	 * This method initializes
	 *
	 */
	public PhysicalRobotPanel(PhysicalRobot robot) {
		super();
		this.robot = robot;
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
        connectorLabel = new JLabel();
        connectorLabel.setFont(Matereal.getInstance().getDefaultFont().deriveFont(Font.BOLD));
        connectorLabel.setText(Messages.getString("PhysicalRobotPanel.connector"));
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0D;
        gridBagConstraints.weighty = 0.0D;
        gridBagConstraints.gridx = 0;
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.fill = GridBagConstraints.BOTH;
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 1;
        gridBagConstraints2.insets = new Insets(5, 5, 5, 5);
        gridBagConstraints2.weightx = 1.0D;
        gridBagConstraints2.weighty = 0.0D;
        resourceLabel = new JLabel();
		resourceLabel.setFont(Matereal.getInstance().getDefaultFont().deriveFont(Font.BOLD)); //$NON-NLS-1$
        resourceLabel.setText(Messages.getString("RobotPanel.resources"));
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.insets = new Insets(0, 5, 5, 0);
        gridBagConstraints1.weighty = 1.0D;
        gridBagConstraints1.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
        this.setLayout(new GridBagLayout());
        this.add(getEntityPanel(), gridBagConstraints);
        this.add(connectorLabel, new GridBagConstraints());
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
			robotResourcePanel = new RobotResourcePanel(robot);
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
			entityPanel = new EntityPanel(robot);
		}
		return entityPanel;
	}

}
