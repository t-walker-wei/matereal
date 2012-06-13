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

import javax.swing.JTabbedPane;

import javax.swing.JPanel;

import com.phybots.Phybots;

public class MonitorPane extends JTabbedPane implements DisposableComponent {

	private static final long serialVersionUID = 628084706596398147L;
	private WorkflowMonitorPanel graphMonitorPanel = null;
	private EntityMonitorPanel entityMonitorPanel = null;
	private ServiceMonitorPanel serviceMonitorPanel = null;
	private JPanel bluetoothPanel = null;

	/**
	 * This is the default constructor
	 */
	public MonitorPane() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		this.setSize(300, 200);
		this.setFont(Phybots.getInstance().getDefaultFont().deriveFont(12));
		this.addTab(Messages.getString("MonitorPane.entities"), null, getEntityMonitorPanel(), null);
		this.addTab(Messages.getString("MonitorPane.services"), null, getServiceMonitorPanel(), null);
		this.addTab(Messages.getString("MonitorPane.graphs"), null, getGraphMonitorPanel(), null);
		this.addTab(Messages.getString("MonitorPane.bluetooth"), null, getBluetoothPanel(), null);
	}

	public void dispose() {
		getEntityMonitorPanel().dispose();
		getServiceMonitorPanel().dispose();
	}

	/**
	 * This method initializes graphMonitorPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private WorkflowMonitorPanel getGraphMonitorPanel() {
		if (graphMonitorPanel  == null) {
			graphMonitorPanel = new WorkflowMonitorPanel();
		}
		return graphMonitorPanel;
	}

	/**
	 * This method initializes entityMonitorPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private EntityMonitorPanel getEntityMonitorPanel() {
		if (entityMonitorPanel == null) {
			entityMonitorPanel = new EntityMonitorPanel();
		}
		return entityMonitorPanel;
	}

	/**
	 * This method initializes serviceMonitorPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private ServiceMonitorPanel getServiceMonitorPanel() {
		if (serviceMonitorPanel == null) {
			serviceMonitorPanel = new ServiceMonitorPanel();
		}
		return serviceMonitorPanel;
	}

	/**
	 * This method initializes bluetoothPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getBluetoothPanel() {
		if (bluetoothPanel == null) {
			bluetoothPanel = new BluetoothDiscoveryPanel();
		}
		return bluetoothPanel;
	}

}
