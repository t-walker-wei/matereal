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
package jp.digitalmuseum.mr.resource;

import java.io.IOException;
import java.io.ObjectInputStream;

import jp.digitalmuseum.connector.Connector;
import jp.digitalmuseum.mr.entity.PhysicalResourceAbstractImpl;
import jp.digitalmuseum.mr.entity.PhysicalRobotAbstractImpl;

public abstract class WheelsAbstractImpl extends PhysicalResourceAbstractImpl implements
		WheelsController {
	private static final long serialVersionUID = 7215831625357912590L;
	private STATUS status;

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
		status = STATUS.STOP;
	}

	@Override
	protected void onFree() {
		stopWheels();
	}

	public void goForward() {
		if (status != STATUS.GO_FORWARD) {
			doGoForward();
			status = STATUS.GO_FORWARD;
		}
	}

	public void goBackward() {
		if (status != STATUS.GO_BACKWARD) {
			doGoBackward();
			status = STATUS.GO_BACKWARD;
		}
	}

	public void spin(SPIN direction) {
		if (direction.equals(SPIN.LEFT)) {
			spinLeft();
		} else {
			spinRight();
		}
	}

	public void spinLeft() {
		if (status != STATUS.SPIN_LEFT) {
			doSpinLeft();
			status = STATUS.SPIN_LEFT;
		}
	}

	public void spinRight() {
		if (status != STATUS.SPIN_RIGHT) {
			doSpinRight();
			status = STATUS.SPIN_RIGHT;
		}
	}

	public void stopWheels() {
		if (status != STATUS.STOP) {
			doStopWheels();
			status = STATUS.STOP;
		}
	}

	public STATUS getStatus() {
		return status;
	}

	protected abstract void doGoForward();
	protected abstract void doGoBackward();
	protected abstract void doSpinLeft();
	protected abstract void doSpinRight();
	protected abstract void doStopWheels();

}
