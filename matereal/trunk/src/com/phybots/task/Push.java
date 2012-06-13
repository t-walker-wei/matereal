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
import com.phybots.task.VectorFieldTask;
import com.phybots.utils.Position;
import com.phybots.utils.Vector2D;
import com.phybots.vectorfield.PushField;


/**
 * Task: Push<br />
 * Push an object to a certain position.
 * (<a href="http://designinterface.jp/projects/push/">A Dipole Field for Object Delivery by Pushing on a Flat Surface</a>)
 *
 * @author Jun Kato
 */
public class Push extends VectorFieldTask {
	private static final long serialVersionUID = -8873781987217990435L;
	private double allowedDistance = 7.0;
	private double previousDistance;
	private double distance;
	private PushField pushField;

	/**
	 * Push the specified entity to the specified position.
	 *
	 * @param entity
	 * @param x
	 * @param y
	 */
	public Push(Entity entity, double x, double y) {
		pushField = new PushField(entity, x, y);
	}

	/**
	 * Push the specified entity to the specified position.
	 *
	 * @param entity
	 * @param destination
	 */
	public Push(Entity entity, Position destination) {
		this(entity, destination.getX(), destination.getY());
	}

	@Override
	public String getName() {
		return pushField.getName();
	}

	public Position getDestination() {
		return pushField.getDestination();
	}

	public void getDestinationOut(Position position) {
		pushField.getDestinationOut(position);
	}

	public void setAllowedDistance(double allowedNorm) {
		this.allowedDistance = allowedNorm;
	}

	public double getAllowedDistance() {
		return allowedDistance;
	}

	@Override
	protected void onStart() {
		super.onStart();
		distance = Double.MAX_VALUE;
		previousDistance = Double.MAX_VALUE;
	}

	@Override
	public void run() {
		super.run();
		distance = pushField.getLastDistance();
		if (distance < allowedDistance &&
				distance > previousDistance) {
			finish();
		}
		previousDistance = distance;
	}

	@Override
	public void getUniqueVectorOut(Position position, Vector2D vector) {
		pushField.getVectorOut(position, vector);
	}
}