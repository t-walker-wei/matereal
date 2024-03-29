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
package jp.digitalmuseum.mr.vectorfield;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.Entity;
import jp.digitalmuseum.mr.service.LocationProvider;
import jp.digitalmuseum.mr.task.VectorFieldTask;
import jp.digitalmuseum.utils.Position;
import jp.digitalmuseum.utils.Vector2D;
import jp.digitalmuseum.utils.VectorField;

public class CollisionAvoidance implements VectorField {
	private LocationProvider locationProvider;
	private Entity entity;

	public CollisionAvoidance(Entity entity) {
		this.entity = entity;
	}

	public Vector2D getVector(Position position) {
		Vector2D vector = new Vector2D();
		getVectorOut(position, vector);
		return vector;
	}

	private static final double MAX_DISTANCE = 100;
	Position p = new Position();
	public void getVectorOut(Position position, Vector2D vector) {
		checkLocationProvider();
		vector.set(0, 0);
		for (Entity e : locationProvider.getEntities()) {
			if (e == entity) continue;
			locationProvider.getPositionOut(e, p);
			if (!p.isNotFound()) {
				p.sub(position);
				double distance = p.getNorm();
				if (distance <= 0.0) {
					// Collision detected. What to do...?
				} else if (distance < MAX_DISTANCE) {
					p.mul((distance - 2*MAX_DISTANCE) / MAX_DISTANCE *
							VectorFieldTask.MINIMAL_NORM / MAX_DISTANCE);
					vector.add(p);
				}
			}
		}
	}

	private void checkLocationProvider() {
		if (locationProvider == null) {
			getLocationProvider();
		}
	}

	private void getLocationProvider() {
		for (LocationProvider locationProvider :
				Matereal.getInstance().lookForServices(LocationProvider.class)) {
			if (locationProvider.contains(entity)) {
				this.locationProvider = locationProvider;
			}
		}
	}
}
