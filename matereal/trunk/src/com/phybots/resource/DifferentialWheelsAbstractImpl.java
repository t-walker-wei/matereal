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

public abstract class DifferentialWheelsAbstractImpl extends PhysicalResourceAbstractImpl
		implements DifferentialWheelsController {
	private static final long serialVersionUID = 4104568454137232520L;
	private WheelsStatus status;
	private int speed;
	private int rotationSpeed;
	private int innerSpeed;
	private transient int leftPower = 0;
	private transient int rightPower = 0;

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
		status = WheelsStatus.STOP;
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		initialize();
	}

	protected void onFree() {
		stopWheels();
	}

	public WheelsStatus getStatus() {
		return status;
	}

	protected void setStatus(WheelsStatus status) {
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
		status = WheelsStatus.GO_FORWARD;
		controlWheels();
	}

	public void goBackward() {
		status = WheelsStatus.GO_BACKWARD;
		controlWheels();
	}

	public void spin(Spin spin) {
		status = spin == Spin.LEFT ? WheelsStatus.SPIN_LEFT :  WheelsStatus.SPIN_RIGHT;
		controlWheels();
	}

	public void spinLeft() {
		status = WheelsStatus.SPIN_LEFT;
		controlWheels();
	}

	public void spinRight() {
		status = WheelsStatus.SPIN_RIGHT;
		controlWheels();
	}

	public void curve(Spin spin, int innerSpeed) {
		status = spin == Spin.LEFT ? WheelsStatus.CURVE_LEFT : WheelsStatus.CURVE_RIGHT;
		this.innerSpeed = innerSpeed;
		controlWheels();
	}

	public void curveLeft(int innerSpeed) {
		status = WheelsStatus.CURVE_LEFT;
		this.innerSpeed = innerSpeed;
		controlWheels();
	}

	public void curveRight(int innerSpeed) {
		status = WheelsStatus.CURVE_RIGHT;
		this.innerSpeed = innerSpeed;
		controlWheels();
	}

	public void stopWheels() {
		if (status != WheelsStatus.STOP) {
			doStopWheels();
			leftPower = 0;
			rightPower = 0;
			status = WheelsStatus.STOP;
		}
	}

	public boolean drive(int leftPower, int rightPower) {
		if (leftPower < -100 || rightPower < -100 ||
				leftPower > 100 || rightPower > 100) {
			return false;
		}
		if (leftPower == rightPower) {
			if (leftPower == 0) {
				status = WheelsStatus.STOP;
			} else if (leftPower > 0) {
				status = WheelsStatus.GO_FORWARD;
			} else {
				status = WheelsStatus.GO_BACKWARD;
			}
		} else if (leftPower == -rightPower) {
			if (leftPower > 0) {
				status = WheelsStatus.SPIN_RIGHT;
			} else {
				status = WheelsStatus.SPIN_LEFT;
			}
		} else {
			if (leftPower > rightPower) {
				status = WheelsStatus.CURVE_RIGHT;
			} else {
				status = WheelsStatus.CURVE_LEFT;
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
		if (this.leftPower != leftPower
				|| this.rightPower != rightPower) {
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
