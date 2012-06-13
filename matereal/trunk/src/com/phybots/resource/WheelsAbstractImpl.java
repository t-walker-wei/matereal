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
package com.phybots.resource;

import java.io.IOException;
import java.io.ObjectInputStream;

import com.phybots.entity.PhysicalResourceAbstractImpl;
import com.phybots.entity.PhysicalRobotAbstractImpl;

import jp.digitalmuseum.connector.Connector;

public abstract class WheelsAbstractImpl extends PhysicalResourceAbstractImpl implements
		WheelsController {
	private static final long serialVersionUID = 7215831625357912590L;
	private WheelsStatus status;

	public WheelsAbstractImpl(PhysicalRobotAbstractImpl robot) {
		super(robot);
		initialize();
	}

	public WheelsAbstractImpl(Connector connector) {
		super(connector);
		initialize();
	}

	private void readObject(ObjectInputStream ois) throws IOException,
			ClassNotFoundException {
		ois.defaultReadObject();
		initialize();
	}

	private void initialize() {
		status = WheelsStatus.STOP;
	}

	@Override
	protected void onFree() {
		stopWheels();
	}

	public void goForward() {
		if (status != WheelsStatus.GO_FORWARD) {
			doGoForward();
			status = WheelsStatus.GO_FORWARD;
		}
	}

	public void goBackward() {
		if (status != WheelsStatus.GO_BACKWARD) {
			doGoBackward();
			status = WheelsStatus.GO_BACKWARD;
		}
	}

	public void spin(Spin direction) {
		if (direction.equals(Spin.LEFT)) {
			spinLeft();
		} else {
			spinRight();
		}
	}

	public void spinLeft() {
		if (status != WheelsStatus.SPIN_LEFT) {
			doSpinLeft();
			status = WheelsStatus.SPIN_LEFT;
		}
	}

	public void spinRight() {
		if (status != WheelsStatus.SPIN_RIGHT) {
			doSpinRight();
			status = WheelsStatus.SPIN_RIGHT;
		}
	}

	public void stopWheels() {
		if (status != WheelsStatus.STOP) {
			doStopWheels();
			status = WheelsStatus.STOP;
		}
	}

	public WheelsStatus getStatus() {
		return status;
	}

	protected abstract void doGoForward();
	protected abstract void doGoBackward();
	protected abstract void doSpinLeft();
	protected abstract void doSpinRight();
	protected abstract void doStopWheels();

}
