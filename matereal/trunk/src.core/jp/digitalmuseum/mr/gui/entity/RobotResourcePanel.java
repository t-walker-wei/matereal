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

import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import java.awt.GridBagConstraints;

import jp.digitalmuseum.mr.entity.Resource;
import jp.digitalmuseum.mr.entity.Robot;

import java.awt.Insets;
import java.awt.CardLayout;
import java.util.List;

public class RobotResourcePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JComboBox jComboBox = null;
	private JPanel resourcePanel = null;

	private transient Robot robot;

	/**
	 * This is the default constructor
	 */
	public RobotResourcePanel(Robot robot) {
		super();
		this.robot = robot;
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.insets = new Insets(0, 5, 5, 5);
		gridBagConstraints1.weighty = 1.0D;
		gridBagConstraints1.weightx = 1.0D;
		gridBagConstraints1.fill = GridBagConstraints.BOTH;
		gridBagConstraints1.gridy = 1;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints.weighty = 0.0D;
		gridBagConstraints.weightx = 1.0;
		this.setSize(300, 200);
		this.setLayout(new GridBagLayout());
		this.add(getJComboBox(), gridBagConstraints);
		this.add(getResourcePanel(), gridBagConstraints1);
	}

	/**
	 * This method initializes jComboBox
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBox() {
		if (jComboBox == null) {
			jComboBox = new JComboBox();
			List<Class<? extends Resource>> resourceTypes = robot.getResourceTypes();
			List<JComponent> resourceComponents = robot.getResourceComponents(resourceTypes);
			// TODO implement this.
		}
		return jComboBox;
	}

	/**
	 * This method initializes resourcePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getResourcePanel() {
		if (resourcePanel == null) {
			resourcePanel = new JPanel();
			resourcePanel.setLayout(new CardLayout());
		}
		return resourcePanel;
	}

}
