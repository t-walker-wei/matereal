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
package com.phybots.service;

import java.awt.Shape;
import java.util.Set;

import com.phybots.Phybots;
import com.phybots.entity.Entity;
import com.phybots.utils.Location;
import com.phybots.utils.Position;


/**
 * Abstract implementation of LocationProvider.
 *
 * @author Jun Kato
 * @see LocationProvider
 */
public abstract class LocationProviderAbstractImpl extends ServiceAbstractImpl implements LocationProvider {
	private static final long serialVersionUID = 3860237767370741601L;
	final Location location = new Location();

	public Location getLocation(Entity e) {
		getLocationOut(e, location);
		return new Location(location);
	}

	public abstract void getLocationOut(Entity e, Location location);

	public Position getPosition(Entity e) {
		getLocationOut(e, location);
		return location.getPosition();
	}

	public void getPositionOut(Entity e, Position position) {
		getLocationOut(e, location);
		position.set(location.getX(), location.getY());
	}

	public double getX(Entity e) {
		getLocationOut(e, location);
		return location.getX();
	}

	public double getY(Entity e) {
		getLocationOut(e, location);
		return location.getY();
	}

	public double getRotation(Entity e) {
		getLocationOut(e, location);
		return location.getRotation();
	}

	public boolean contains(Entity e, Position position) {
		getLocationOut(e, location);
		Shape shape = e.getShape();
		return shape != null &&
				shape.contains(position.getX() - location.getX(), position.getY() - location.getY());
	}

	/**
	 * Find a proper location provider for an entity.
	 */
	public static LocationProvider findProperLocationProvider(Entity entity) {
		final Set<LocationProvider> providers =
			Phybots.getInstance().lookForServices(LocationProvider.class);
		LocationProvider provider =
			providers.iterator().next();
		for (LocationProvider providerCandidate : providers) {
			if (providerCandidate.getLocation(entity) != null) {
				provider = providerCandidate;
				break;
			}
		}
		return provider;
	}
}
