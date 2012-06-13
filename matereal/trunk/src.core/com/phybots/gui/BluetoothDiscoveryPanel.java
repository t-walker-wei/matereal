package com.phybots.gui;
import java.awt.GridBagLayout;

import javax.bluetooth.RemoteDevice;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import java.awt.GridBagConstraints;
import javax.swing.JProgressBar;
import javax.swing.JButton;
import javax.swing.table.DefaultTableModel;

import com.phybots.Phybots;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Set;

import jp.digitalmuseum.connector.BluetoothConnector;

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

public class BluetoothDiscoveryPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JScrollPane jScrollPane = null;
	private JTable jTable = null;
	private JProgressBar jProgressBar = null;
	private JButton jButton = null;

	/**
	 * This method initializes jScrollPane
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJTable());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jTable
	 *
	 * @return javax.swing.JTable
	 */
	private JTable getJTable() {
		if (jTable == null) {
			jTable = new JTable(new DefaultTableModel(new String[] {
					Messages.getString("BluetoothDiscoveryPanel.address"),
					Messages.getString("BluetoothDiscoveryPanel.identifier"),
					Messages.getString("BluetoothDiscoveryPanel.name")
				}, 0));
			jTable.setFont(Phybots.getInstance().getDefaultFont());
		}
		return jTable;
	}

	/**
	 * This method initializes jProgressBar
	 *
	 * @return javax.swing.JProgressBar
	 */
	private JProgressBar getJProgressBar() {
		if (jProgressBar == null) {
			jProgressBar = new JProgressBar();
			jProgressBar.setMaximum(100);
			jProgressBar.setMinimum(0);
		}
		return jProgressBar;
	}

	/**
	 * This method initializes jButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText(Messages.getString("BluetoothDiscoveryPanel.search"));
			jButton.setFont(Phybots.getInstance().getDefaultFont());
			jButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					discovery();
				}
			});
		}
		return jButton;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame();
				frame.add(new BluetoothDiscoveryPanel());
				frame.setSize(640, 480);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setVisible(true);
			}
		});
	}

	private transient boolean isDiscoveryInProgress = false;

	private void discovery() {
		if (isDiscoveryInProgress) {
			return;
		}
		getJButton().setEnabled(false);
		Phybots.getInstance().submit(new Runnable() {
			public void run() {
				try {
					DefaultTableModel model = (DefaultTableModel) getJTable().getModel();
					while (model.getRowCount() > 0) {
						model.removeRow(0);
					}
					getJProgressBar().setValue(10);
					Set<RemoteDevice> devices = BluetoothConnector.queryDevices();
					getJProgressBar().setValue(30);
					int i = 0;
					for (RemoteDevice remoteDevice : devices) {
						getJProgressBar().setValue(
								30 + 70 * (i++) / devices.size());
						model.addRow(
								new String[] {
							remoteDevice.getBluetoothAddress(),
							"btspp://" + remoteDevice.getBluetoothAddress(),
							"" });
						try {
							model.setValueAt(
									remoteDevice.getFriendlyName(true), i - 1, 2);
						} catch (IOException e) {
							model.setValueAt(
									"(not available)", i - 1, 1);
							continue;
						}
						Thread.sleep(10);
					}
					getJProgressBar().setValue(100);
				} catch (InterruptedException e) {
					// Do nothing.
				}
				getJButton().setEnabled(true);
			}
		});
	}

	/**
	 * This is the default constructor
	 */
	public BluetoothDiscoveryPanel() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridx = 1;
		gridBagConstraints2.weightx = 0.0D;
		gridBagConstraints2.fill = GridBagConstraints.BOTH;
		gridBagConstraints2.insets = new Insets(5, 5, 0, 5);
		gridBagConstraints2.gridy = 0;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.weightx = 1.0D;
		gridBagConstraints1.insets = new Insets(5, 5, 0, 0);
		gridBagConstraints1.fill = GridBagConstraints.BOTH;
		gridBagConstraints1.gridy = 0;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints.gridx = 0;
		this.setSize(300, 200);
		this.setLayout(new GridBagLayout());
		this.add(getJProgressBar(), gridBagConstraints1);
		this.add(getJButton(), gridBagConstraints2);
		this.add(getJScrollPane(), gridBagConstraints);
	}

}
