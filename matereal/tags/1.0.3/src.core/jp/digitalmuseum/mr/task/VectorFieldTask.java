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

import jp.digitalmuseum.mr.resource.DifferentialWheelsController;
import jp.digitalmuseum.mr.resource.WheelsController;
import jp.digitalmuseum.utils.Array;
import jp.digitalmuseum.utils.MathUtils;
import jp.digitalmuseum.utils.Position;
import jp.digitalmuseum.utils.Vector2D;
import jp.digitalmuseum.utils.VectorField;

/**
 * Abstract task that only needs getVector(Position) method
 * implemented by sub-classes to work.
 *
 * @author Jun KATO
 */
public abstract class VectorFieldTask extends LocationBasedTaskAbstractImpl implements VectorField {
	public final static double MINIMAL_NORM = 100.0;
	private final Array<VectorField> optionalFields = new Array<VectorField>();
	private final Vector2D vector = new Vector2D();
	private final Position position = new Position();
	private double allowedDeviationAngle = 0.2; // 17degrees+

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

	public Vector2D getCurrentVector() {
		return new Vector2D(vector);
	}

	public void getCurrentVectorOut(Vector2D vector) {
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

	protected double getMinimalNorm() {
		return MINIMAL_NORM;
	}

	protected double getMinimalNormSq() {
		return MINIMAL_NORM * MINIMAL_NORM;
	}
}
