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

import com.phybots.entity.Entity;
import com.phybots.service.LocationProvider;
import com.phybots.service.LocationProviderAbstractImpl;
import com.phybots.utils.Location;
import com.phybots.utils.Position;


/**
 * Abstract implementation of LocationBasedTask.
 * LocationBasedTask implementation classes are recommended to extend this class for convenience.
 *
 * @author Jun Kato
 */
public abstract class LocationBasedTaskAbstractImpl extends MobileTaskAbstractImpl implements LocationBasedTask {
	private static final long serialVersionUID = 8609358749504948317L;
	private LocationProvider provider;

	/**
	 * Try to find and bind a proper location provider with this task on starting.
	 * Subclasses should call super.onStart() when overriding this method.
	 *
	 * @see LocationProviderAbstractImpl#findProperLocationProvider(com.phybots.entity.Entity)
	 */
	@Override
	protected void onStart() {
		super.onStart();
		if (provider == null) {
			provider = LocationProviderAbstractImpl
					.findProperLocationProvider(getAssignedRobot());
		}
	}

	public void setLocationProvider(LocationProvider provider) {
		this.provider = provider;
	}

	public LocationProvider getLocationProvider() {
		return provider;
	}

	public double getX() {
		return getX(getAssignedRobot());
	}

	public double getY() {
		return getY(getAssignedRobot());
	}

	public double getRotation() {
		return getRotation(getAssignedRobot());
	}

	public Position getPosition() {
		return getPosition(getAssignedRobot());
	}

	public void getPositionOut(Position position) {
		getPositionOut(getAssignedRobot(), position);
	}

	public Location getLocation() {
		return getLocation(getAssignedRobot());
	}

	public void getLocationOut(Location location) {
		getLocationOut(getAssignedRobot(), location);
	}

	public double getX(Entity entity) {
		checkLocationProvider();
		return provider.getX(entity);
	}

	public double getY(Entity entity) {
		checkLocationProvider();
		return provider.getY(entity);
	}

	public double getRotation(Entity entity) {
		checkLocationProvider();
		return provider.getRotation(entity);
	}

	public Position getPosition(Entity entity) {
		checkLocationProvider();
		return provider.getPosition(entity);
	}

	public void getPositionOut(Entity entity, Position position) {
		checkLocationProvider();
		provider.getPositionOut(entity, position);
	}

	public Location getLocation(Entity entity) {
		checkLocationProvider();
		return provider.getLocation(entity);
	}

	public void getLocationOut(Entity entity, Location location) {
		checkLocationProvider();
		provider.getLocationOut(entity, location);
	}

	/**
	 * Check if valid location provider is assigned to this location-based task.
	 * If not, try to assign one.
	 */
	private void checkLocationProvider() {
		if (provider == null) {
			provider = LocationProviderAbstractImpl
					.findProperLocationProvider(getAssignedRobot());
			if (provider == null) {
				throw new IllegalStateException("No LocationProvider is assigned to this robot.");
			}
		}
	}
}
