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

import jp.digitalmuseum.connector.Connector;
import jp.digitalmuseum.connector.ConnectorFactory;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import javax.swing.JButton;

import com.phybots.entity.PhysicalRobot;
import com.phybots.gui.DisposableComponent;
import com.phybots.gui.Messages;
import com.phybots.message.EntityEvent;
import com.phybots.message.EntityStatus;
import com.phybots.message.EntityUpdateEvent;
import com.phybots.message.Event;
import com.phybots.message.EventListener;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConnectorPanel extends JPanel implements DisposableComponent {

	private static final long serialVersionUID = -2319312498174759887L;
	private transient PhysicalRobot physicalRobot;
	private JTextField connectionStringField = null;
	private JButton editButton = null;

	/**
	 * This is the default constructor
	 */
	public ConnectorPanel(PhysicalRobot physicalRobot) {
		super();
		this.physicalRobot = physicalRobot;
		physicalRobot.addEventListener(new EventListener() {
			public void eventOccurred(Event e) {
				if (e instanceof EntityUpdateEvent) {
					if ("connector".equals(((EntityUpdateEvent) e).getParameter())) {
						setEnables(false);
					}
				} else if (e instanceof EntityEvent) {
					if (((EntityEvent) e).getStatus() == EntityStatus.DISPOSED) {
						dispose();
					}
				}
			}
		});
		initialize();
		setEnables(physicalRobot.getConnector() == null);
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.fill = GridBagConstraints.BOTH;
		gridBagConstraints1.gridx = 1;
		gridBagConstraints1.gridy = 0;
		gridBagConstraints1.weightx = 0.0D;
		gridBagConstraints1.weighty = 1.0D;
		gridBagConstraints1.insets = new Insets(5, 0, 5, 5);
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0D;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints.gridx = 0;
		this.setSize(300, 200);
		this.setLayout(new GridBagLayout());
		this.add(getConnectionStringField(), gridBagConstraints);
		this.add(getEditButton(), gridBagConstraints1);
	}

	public void dispose() {
		setEnabled(false);
		physicalRobot = null;
	}

	/**
	 * This method initializes connectionStringField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getConnectionStringField() {
		if (connectionStringField == null) {
			connectionStringField = new JTextField();
		}
		return connectionStringField;
	}

	/**
	 * This method initializes editButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getEditButton() {
		if (editButton == null) {
			editButton = new JButton();
			editButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JTextField field = getConnectionStringField();
					if (field.isEditable()) {
						Connector oldConnector = physicalRobot.getConnector();
						if (oldConnector != null) {
							oldConnector.disconnect();
						}
						Connector connector = ConnectorFactory.makeConnector(
								field.getText());
						if (connector != null
								&& connector.connect()) {
							physicalRobot.setConnector(connector);
							setEnables(false);
						}
					} else {
						setEnables(true);
					}
				}
			});
		}
		return editButton;
	}

	private void setEnables(boolean isEnable) {
		JTextField field = getConnectionStringField();
		JButton button = getEditButton();
		if (isEnable) {
			field.setEditable(true);
			button.setText(Messages.getString("ConnectorPanel.ok"));
		} else {
			field.setEditable(false);
			if (physicalRobot.getConnector() != null) {
				field.setText(
						physicalRobot.getConnector().getConnectionString());
			}
			button.setText(Messages.getString("ConnectorPanel.change"));
		}
	}
}
