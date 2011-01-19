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
package jp.digitalmuseum.mr.task;

import jp.digitalmuseum.mr.resource.WheelsController;
import jp.digitalmuseum.mr.task.LocationBasedTaskAbstractImpl;
import jp.digitalmuseum.utils.MathUtils;
import jp.digitalmuseum.utils.Position;

/**
 * Task: RotateTo<br />
 * Rotate to look towards a certain direction.
 *
 * @author Jun KATO
 */
public class Rotate extends LocationBasedTaskAbstractImpl {
	private final static String TASK_NAME_PREFIX = "Rotate to ";
	private final String nameString;
	private WheelsController wheels;
	private Position destinationPosition;
	private double destination;
	private double allowedDeviationAngle = 0.2;
	private double satisfactoryDeviationAngle = 0.05;

	private double angle;
	private double previousAngle;

	public Rotate(double destination) {
		super();
		this.destination = destination;
		nameString = TASK_NAME_PREFIX + String.format("%.3f", destination);
	}

	public Rotate(Position destinationPosition) {
		this.destinationPosition = destinationPosition;
		nameString = TASK_NAME_PREFIX + "("+destinationPosition+")";
	}

	@Override
	public String getName() {
		return nameString;
	}

	public void setAllowedDeviationAngle(double allowedDeviationAngle) {
		this.allowedDeviationAngle = allowedDeviationAngle;
	}

	public double getAllowedDeviationAngle() {
		return allowedDeviationAngle;
	}

	public void setSatisfactoryDeviationAngle(double satisfactoryDeviationAngle) {
		this.satisfactoryDeviationAngle = satisfactoryDeviationAngle;
	}

	public double getSatisfactoryDeviationAngle() {
		return satisfactoryDeviationAngle;
	}

	@Override
	protected void onStart() {
		super.onStart();
		wheels = (WheelsController) getResourceMap().get(WheelsController.class);
		previousAngle = Double.MAX_VALUE;
	}

	public void run() {
		final double deviationAngle = getCurrentDeviationAngle();
		angle = Math.abs(deviationAngle);
		if (deviationAngle > 0) {
			wheels.spinLeft();
		} else {
			wheels.spinRight();
		}
		previousAngle = angle;

		if (angle < satisfactoryDeviationAngle ||
				(angle < allowedDeviationAngle && angle > previousAngle)) {
			finish();
		}
	}

	public double getCurrentDeviationAngle() {
		return MathUtils.roundRadian(
				(destinationPosition == null ?
						destination : getPosition().getRelativeDirection(destinationPosition))
				- getRotation());
	}
}
