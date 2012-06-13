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
package com.phybots.entity;

import javax.swing.JComponent;

import com.phybots.gui.entity.PhysicalRobotPanel;
import com.phybots.message.RobotUpdateEvent;

import jp.digitalmuseum.connector.Connector;
import jp.digitalmuseum.connector.ConnectorFactory;

public abstract class PhysicalRobotAbstractImpl extends RobotAbstractImpl implements PhysicalRobot {
	private static final long serialVersionUID = 2914683592930955216L;
	public static boolean isAutoConnectEnabled = true;

	public static void setAutoConnect(boolean isAutoConnectEnabled) {
		PhysicalRobotAbstractImpl.isAutoConnectEnabled = isAutoConnectEnabled;
	}

	public static boolean isAutoConnectEnabled() {
		return isAutoConnectEnabled;
	}

	/** Connector for this robot */
	private Connector connector;

	public PhysicalRobotAbstractImpl() {
		super();
	}

	public PhysicalRobotAbstractImpl(String connectionString) {
		this(ConnectorFactory.makeConnector(connectionString));
	}

	public PhysicalRobotAbstractImpl(Connector conenctor) {
		super();
		setConnector(conenctor);
	}

	public PhysicalRobotAbstractImpl(String connectionString, String name) {
		this(ConnectorFactory.makeConnector(connectionString), name);
	}

	public PhysicalRobotAbstractImpl(Connector conenctor, String name) {
		this(conenctor);
		setName(name);
	}

	@Override
	public void dispose() {
		super.dispose();
		if (connector != null) {
			connector.disconnect();
			connector = null;
		}
	}

	/**
	 * Bind a connector to this robot.
	 */
	final public void setConnector(Connector connector) {
		if (this.connector != connector) {
			this.connector = connector;
			if (PhysicalRobotAbstractImpl.isAutoConnectEnabled) {
				connector.connect();
			}
			distributeEvent(new RobotUpdateEvent(this, "connector", connector));
		}
	}

	/**
	 * Get the connector bound with this robot.
	 */
	final public Connector getConnector() {
		return connector;
	}

	final public boolean isConnected() {
		return connector != null
				&& connector.isConnected();
	}

	final public boolean connect() {
		return connector != null
				&& connector.connect();
	}

	final public void disconnect() {
		if (connector != null) {
			connector.disconnect();
		}
	}

	@Override
	public JComponent getConfigurationComponent() {
		return new PhysicalRobotPanel(this);
	}
}
