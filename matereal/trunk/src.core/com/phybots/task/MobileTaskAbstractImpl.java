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
package com.phybots.task;

import java.util.List;

import com.phybots.entity.Resource;
import com.phybots.resource.DifferentialWheelsController;
import com.phybots.resource.WheelsController;


/**
 * Abstract implementation of MobileTask.
 *
 * @author Jun Kato
 */
public abstract class MobileTaskAbstractImpl extends TaskAbstractImpl implements
		MobileTask {
	private static final long serialVersionUID = -9218644881209179648L;
	private transient WheelsController w;
	private Integer rotationSpeed = null;
	private Integer speed = null;

	/**
	 * @return Returns a set of required interfaces (including Wheels) for this
	 *         task.
	 */
	@Override
	public List<Class<? extends Resource>> getRequirements() {
		List<Class<? extends Resource>> requirements = super.getRequirements();
		requirements.add(WheelsController.class);
		return requirements;
	}

	@Override
	protected void onAssigned() {
		w = (WheelsController) getResourceMap().get(WheelsController.class);
		if (w instanceof DifferentialWheelsController) {
			if (speed != null) {
				((DifferentialWheelsController) w).setSpeed(speed);
			}
			if (rotationSpeed != null) {
				((DifferentialWheelsController) w).setRotationSpeed(rotationSpeed);
			}
		}
	}

	@Override
	protected void onPause() {
		getWheels().stopWheels();
	}

	protected WheelsController getWheels() {
		return w;
	}

	public int getRecommendedSpeed() {
		if (getWheels() instanceof DifferentialWheelsController) {
			return ((DifferentialWheelsController) getWheels())
					.getRecommendedSpeed();
		}
		return -1;
	}

	public int getRecommendedRotationSpeed() {
		if (getWheels() instanceof DifferentialWheelsController) {
			return ((DifferentialWheelsController) getWheels())
					.getRecommendedRotationSpeed();
		}
		return -1;
	}

	public boolean setSpeed(int speed) {
		if (getWheels() instanceof DifferentialWheelsController) {
			return ((DifferentialWheelsController) getWheels())
					.setSpeed(speed);
		}
		this.speed = speed;
		return true;
	}

	public int getSpeed() {
		if (getWheels() instanceof DifferentialWheelsController) {
			return ((DifferentialWheelsController) getWheels())
					.getSpeed();
		}
		return -1;
	}

	public boolean setRotationSpeed(int speed) {
		if (getWheels() instanceof DifferentialWheelsController) {
			return ((DifferentialWheelsController) getWheels())
					.setRotationSpeed(speed);
		}
		this.rotationSpeed = speed;
		return true;
	}

	public int getRotationSpeed() {
		if (getWheels() instanceof DifferentialWheelsController) {
			return ((DifferentialWheelsController) getWheels())
					.getRotationSpeed();
		}
		return -1;
	}
}
