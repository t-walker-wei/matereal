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
package jp.digitalmuseum.mr.gui;

import java.awt.Panel;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import java.awt.GridLayout;

public class RobotSelectorPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private JLabel jRobotTypeLabel = null;
	private JComboBox jRobotTypeComboBox = null;
	private JLabel jConnectionLabel = null;
	private JTextField jConnectionTextField = null;
	private JLabel jConnectionTypeLabel = null;
	private JComboBox jConnectionTypeComboBox = null;

	/**
	 * This is the default constructor
	 */
	public RobotSelectorPanel() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		jConnectionTypeLabel = new JLabel();
		jConnectionTypeLabel.setText("Connection type");
		GridLayout gridLayout = new GridLayout();
		gridLayout.setRows(3);
		gridLayout.setColumns(2);
		gridLayout.setVgap(5);
		gridLayout.setHgap(5);
		jConnectionLabel = new JLabel();
		jConnectionLabel.setText("Connection address");
		jRobotTypeLabel = new JLabel();
		jRobotTypeLabel.setText("Type of a robot");
		jRobotTypeLabel.setName("jLabel");
		this.setLayout(gridLayout);
		this.setSize(300, 200);
		this.add(jRobotTypeLabel, null);
		this.add(getJRobotTypeComboBox(), null);
		this.add(jConnectionTypeLabel, null);
		this.add(getJConnectionTypeComboBox(), null);
		this.add(jConnectionLabel, null);
		this.add(getJConnectionTextField(), null);
	}

	/**
	 * This method initializes jRobotTypeComboBox
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJRobotTypeComboBox() {
		if (jRobotTypeComboBox == null) {
			jRobotTypeComboBox = new JComboBox();
			jRobotTypeComboBox.addItem("Roomba");
		}
		return jRobotTypeComboBox;
	}

	/**
	 * This method initializes jConnectionTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJConnectionTextField() {
		if (jConnectionTextField == null) {
			jConnectionTextField = new JTextField();
		}
		return jConnectionTextField;
	}

	/**
	 * This method initializes jConnectionTypeComboBox
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJConnectionTypeComboBox() {
		if (jConnectionTypeComboBox == null) {
			jConnectionTypeComboBox = new JComboBox();
		}
		return jConnectionTypeComboBox;
	}

}
