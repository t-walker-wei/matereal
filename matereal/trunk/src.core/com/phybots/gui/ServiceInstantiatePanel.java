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

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JButton;
import java.awt.Font;


import javax.swing.BorderFactory;

import com.phybots.Phybots;
import com.phybots.gui.service.ServiceTypeComboBox;
import com.phybots.service.Service;

import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServiceInstantiatePanel extends JPanel {

	private static final long serialVersionUID = 8537365036037693120L;
	private JLabel typeLabel = null;
	private ServiceTypeComboBox typeComboBox = null;
	private JPanel buttonPanel = null;
	private JButton okButton = null;
	private JButton cancelButton = null;
	private JLabel entityAddLabel = null;

	private transient ServiceMonitorPanel serviceMonitorPanel;

	/**
	 * This is the default constructor
	 */
	public ServiceInstantiatePanel(ServiceMonitorPanel serviceMonitorPanel) {
		super();
		this.serviceMonitorPanel = serviceMonitorPanel;
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		Font defaultFont = Phybots.getInstance().getDefaultFont();
		GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
		gridBagConstraints21.gridx = 0;
		gridBagConstraints21.anchor = GridBagConstraints.EAST;
		gridBagConstraints21.fill = GridBagConstraints.BOTH;
		gridBagConstraints21.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints21.gridwidth = 2;
		gridBagConstraints21.gridy = 0;
		entityAddLabel = new JLabel();
		entityAddLabel.setText("Add a new service");
		entityAddLabel.setFont(defaultFont.deriveFont(Font.BOLD, 14));
		GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
		gridBagConstraints11.gridx = 0;
		gridBagConstraints11.anchor = GridBagConstraints.SOUTH;
		gridBagConstraints11.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints11.gridwidth = 2;
		gridBagConstraints11.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints11.weightx = 1.0D;
		gridBagConstraints11.weighty = 1.0D;
		gridBagConstraints11.gridy = 3;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.fill = GridBagConstraints.BOTH;
		gridBagConstraints1.gridy = 1;
		gridBagConstraints1.gridx = 1;
		gridBagConstraints1.weighty = 0.0;
		gridBagConstraints1.weightx = 0.7;
		gridBagConstraints1.insets = new Insets(0, 0, 5, 5);
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.weighty = 0.0D;
		gridBagConstraints.weightx = 0.3D;
		gridBagConstraints.insets = new Insets(0, 5, 5, 5);
		typeLabel = new JLabel();
		typeLabel.setFont(defaultFont);
		typeLabel.setText("Service type:");
		this.setSize(300, 200);
		this.setLayout(new GridBagLayout());
		this.add(entityAddLabel, gridBagConstraints21);
		this.add(typeLabel, gridBagConstraints);
		this.add(getTypeComboBox(), gridBagConstraints1);
		this.add(getButtonPanel(), gridBagConstraints11);
	}

	/**
	 * This method initializes typeComboBox
	 *
	 * @return javax.swing.JComboBox
	 */
	private ServiceTypeComboBox getTypeComboBox() {
		if (typeComboBox == null) {
			typeComboBox = new ServiceTypeComboBox();
			typeComboBox.setFont(Phybots.getInstance().getDefaultFont());
		}
		return typeComboBox;
	}

	/**
	 * This method initializes buttonPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 1;
			gridBagConstraints5.anchor = GridBagConstraints.SOUTH;
			gridBagConstraints5.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints5.insets = new Insets(5, 5, 0, 0);
			gridBagConstraints5.gridy = 0;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.anchor = GridBagConstraints.EAST;
			gridBagConstraints4.insets = new Insets(5, 0, 0, 0);
			gridBagConstraints4.weightx = 1.0D;
			gridBagConstraints4.fill = GridBagConstraints.NONE;
			buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridBagLayout());
			buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, SystemColor.controlShadow));
			buttonPanel.add(getOkButton(), gridBagConstraints4);
			buttonPanel.add(getCancelButton(), gridBagConstraints5);
		}
		return buttonPanel;
	}

	/**
	 * This method initializes okButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton();
			okButton.setFont(Phybots.getInstance().getDefaultFont());
			okButton.setText("OK");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Service service = getTypeComboBox().newServiceInstance();
					if (service != null) {
						serviceMonitorPanel.showService(service);
					}
				}
			});
		}
		return okButton;
	}

	/**
	 * This method initializes cancelButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setFont(Phybots.getInstance().getDefaultFont());
			cancelButton.setText("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					serviceMonitorPanel.showService(
							serviceMonitorPanel.getSelectedService());
				}
			});
		}
		return cancelButton;
	}

}
