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

import jp.digitalmuseum.connector.Connector;
import jp.digitalmuseum.mr.entity.PhysicalResourceAbstractImpl;
import jp.digitalmuseum.mr.entity.PhysicalRobotAbstractImpl;

public abstract class DifferentialWheelsAbstractImpl extends PhysicalResourceAbstractImpl
		implements DifferentialWheelsController {
	private STATUS status;
	private int speed;
	private int rotationSpeed;
	private int innerSpeed;
	private int leftPower = 0;
	private int rightPower = 0;

	public DifferentialWheelsAbstractImpl(PhysicalRobotAbstractImpl robot) {
		super(robot);
		initialize();
	}

	public DifferentialWheelsAbstractImpl(Connector connector) {
		super(connector);
		initialize();
	}

	protected void initialize() {
		speed = getRecommendedSpeed();
		rotationSpeed = getRecommendedRotationSpeed();
		status = STATUS.STOP;
	}

	protected void onFree() {
		stopWheels();
	}

	public STATUS getStatus() {
		return status;
	}

	protected void setStatus(STATUS status) {
		this.status = status;
	}

	public int getSpeed() {
		return speed;
	}

	public boolean setSpeed(int speed) {
		if (speed >= 0 && speed <= 100) {
			this.speed = speed;
			controlWheels();
			return true;
		}
		return false;
	}

	public int getRotationSpeed() {
		return rotationSpeed;
	}

	public boolean setRotationSpeed(int rotationSpeed) {
		if (rotationSpeed >= 0 && speed <= 100) {
			this.rotationSpeed = rotationSpeed;
			controlWheels();
			return true;
		}
		return false;
	}

	public int getLeftWheelPower() {
		return leftPower;
	}

	public int getRightWheelPower() {
		return rightPower;
	}

	public void goForward() {
		status = STATUS.GO_FORWARD;
		controlWheels();
	}

	public void goBackward() {
		status = STATUS.GO_BACKWARD;
		controlWheels();
	}

	public void spin(SPIN spin) {
		status = spin == SPIN.LEFT ? STATUS.SPIN_LEFT :  STATUS.SPIN_RIGHT;
		controlWheels();
	}

	public void spinLeft() {
		status = STATUS.SPIN_LEFT;
		controlWheels();
	}

	public void spinRight() {
		status = STATUS.SPIN_RIGHT;
		controlWheels();
	}

	public void curve(SPIN spin, int innerSpeed) {
		status = spin == SPIN.LEFT ? STATUS.CURVE_LEFT : STATUS.CURVE_RIGHT;
		this.innerSpeed = innerSpeed;
		controlWheels();
	}

	public void curveLeft(int innerSpeed) {
		status = STATUS.CURVE_LEFT;
		this.innerSpeed = innerSpeed;
		controlWheels();
	}

	public void curveRight(int innerSpeed) {
		status = STATUS.CURVE_RIGHT;
		this.innerSpeed = innerSpeed;
		controlWheels();
	}

	public void stopWheels() {
		if (status != STATUS.STOP) {
			doStopWheels();
			leftPower = 0;
			rightPower = 0;
			status = STATUS.STOP;
		}
	}

	public boolean drive(int leftPower, int rightPower) {
		if (leftPower < -100 || rightPower < -100 ||
				leftPower > 100 || rightPower > 100) {
			return false;
		}
		if (leftPower == rightPower) {
			if (leftPower == 0) {
				status = STATUS.STOP;
			} else if (leftPower > 0) {
				status = STATUS.GO_FORWARD;
			} else {
				status = STATUS.GO_BACKWARD;
			}
		} else if (leftPower == -rightPower) {
			if (leftPower > 0) {
				status = STATUS.SPIN_RIGHT;
			} else {
				status = STATUS.SPIN_LEFT;
			}
		} else {
			if (leftPower > rightPower) {
				status = STATUS.CURVE_RIGHT;
			} else {
				status = STATUS.CURVE_LEFT;
			}
		};
		return doDrive_(leftPower, rightPower);
	}

	private void controlWheels() {
		switch (status) {
		case GO_FORWARD:
			doDrive_(speed, speed);
			break;
		case GO_BACKWARD:
			doDrive_(-speed, -speed);
			break;
		case SPIN_LEFT:
			doDrive_(-rotationSpeed, rotationSpeed);
			break;
		case SPIN_RIGHT:
			doDrive_(rotationSpeed, -rotationSpeed);
			break;
		case CURVE_LEFT:
			doDrive_(rotationSpeed*innerSpeed/100, rotationSpeed);
			break;
		case CURVE_RIGHT:
			doDrive_(rotationSpeed, rotationSpeed*innerSpeed/100);
		}
	}

	private boolean doDrive_(int leftPower, int rightPower) {
		if (this.leftPower != leftPower ||
				this.rightPower != rightPower) {
			if (doDrive(leftPower, rightPower)) {
				this.leftPower = leftPower;
				this.rightPower = rightPower;
			} else {
				return false;
			}
		}
		// Output state was already set to what is meant.
		return true;
	}

	protected abstract boolean doDrive(int leftPower, int rightPower);

	protected abstract boolean doStopWheels();

}
