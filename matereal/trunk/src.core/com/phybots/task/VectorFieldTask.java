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

import com.phybots.resource.DifferentialWheelsController;
import com.phybots.resource.WheelsController;
import com.phybots.utils.Array;
import com.phybots.utils.MathUtils;
import com.phybots.utils.Position;
import com.phybots.utils.Vector2D;
import com.phybots.utils.VectorField;


/**
 * Abstract task that only needs getVector(Position) method
 * implemented by sub-classes to work.
 *
 * @author Jun Kato
 */
public abstract class VectorFieldTask extends LocationBasedTaskAbstractImpl implements VectorField {
	private static final long serialVersionUID = -7112272585041055060L;
	public static double defaultAllowedDeviationAngle = 0.2; // 17 degrees+
	private final Array<VectorField> optionalFields;
	private transient final Vector2D vector;
	private transient final Position position;
	private double allowedDeviationAngle;

	public VectorFieldTask() {
		optionalFields = new Array<VectorField>();
		vector = new Vector2D();
		position = new Position();
		allowedDeviationAngle = defaultAllowedDeviationAngle;
	}

	public void setAllowedDeviationAngle(double allowedDeviationAngle) {
		this.allowedDeviationAngle = allowedDeviationAngle;
	}

	public double getAllowedDeviationAngle() {
		return allowedDeviationAngle;
	}

	public void run() {
		final WheelsController wheels = getWheels();

		// Get the current position.
		getPositionOut(position);
		if (position.isNotFound()) {
			wheels.stopWheels();
			return;
		}

		// Get the vector.
		getVectorOut(position, vector);

		final double
		curDirection = getRotation(),
		destDirection = Math.atan2(vector.getY(), vector.getX()),
		deviationAngle = MathUtils.roundRadian(destDirection - curDirection),
		absDeviationAngle = Math.abs(deviationAngle);

		// Rotate if needed.
		if (absDeviationAngle > allowedDeviationAngle) {
			if (deviationAngle > 0) {
				wheels.spinLeft();
			} else {
				wheels.spinRight();
			}
		}

		// Otherwise, go forward.
		else {
			if (wheels instanceof DifferentialWheelsController) {
				final int innerSpeed = (int) (Math.cos(absDeviationAngle)*100);
				if (deviationAngle > 0) {
					((DifferentialWheelsController) wheels).curveLeft(innerSpeed);
				} else {
					((DifferentialWheelsController) wheels).curveRight(innerSpeed);
				}
			} else {
				wheels.goForward();
			}
		}
	}

	public void add(VectorField field) {
		optionalFields.push(field);
	}

	public boolean remove(VectorField field) {
		return optionalFields.remove(field);
	}

	public Vector2D getLastVector() {
		return new Vector2D(vector);
	}

	public void getLastVectorOut(Vector2D vector) {
		vector.set(this.vector);
	}

	/**
	 * @param position
	 * @return Returns a vector that specifies in which direction the assigned robot should go.
	 */
	public Vector2D getVector(Position position) {
		final Vector2D vector = new Vector2D();
		getVectorOut(position, vector);
		return vector;
	}

	private Vector2D v = new Vector2D();
	public void getVectorOut(Position position, Vector2D vector) {
		getUniqueVectorOut(position, vector);

		// Add vectors of optional vector fields.
		for (VectorField vf : optionalFields) {
			vf.getVectorOut(position, v);
			vector.add(v);
		}
	}

	public abstract void getUniqueVectorOut(Position position, Vector2D vector);
}
