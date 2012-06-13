/*
 * PROJECT: Phybots at http://phybots.com/
 * ----------------------------------------------------------------------------
 *
 * This file is part of Phybots.
 * Phybots is a Java/Processing toolkit for making robotic things.
 *
 * ----------------------------------------------------------------------------
 *
 * License version: MPL 1.1/GPL 2.0/LGPL 2.1
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
package com.phybots.utils;

/**
 * Position information. (X, Y) [cm] in the world coordinate.
 *
 * @author Jun Kato
 */
public class Position extends Vector2D {
	private static final long serialVersionUID = 8401243578082968254L;
	private boolean isNotFound = false;

	public Position() {
		isNotFound = true;
	}
	public Position(double x, double y) {
		set(x, y);
	}
	public Position(Vector2D position) {
		if (position instanceof Position &&
				((Position) position).isNotFound) {
			isNotFound = true;
		} else {
			set(position.x, position.y);
		}
	}

	@Override
	public void set(double x, double y) {
		super.set(x, y);
		isNotFound = false;
	}

	@Override
	public void set(Vector2D p) {
		super.set(p);
		isNotFound = false;
	}

	@Override
	public void setX(double x) {
		super.setX(x);
		isNotFound = false;
	}

	@Override
	public void setY(double y) {
		super.setY(y);
		isNotFound = false;
	}

	public double getRelativeDirection(Position destination) {
		if (isNotFound || destination.isNotFound) {
			return 0;
		}
		return super.getRelativeDirection(destination);
	}

	public double distanceSq(Position p) {
		if (isNotFound || p.isNotFound) {
			return 0;
		}
		return distanceSq(p.getX(), p.getY());
	}
	public double distanceSq(double x, double y) {
		final double
			dx = x - this.x,
			dy = y - this.y;
		return dx*dx + dy*dy;
	}

	public double distance(Position p) {
		if (isNotFound || p.isNotFound) {
			return 0;
		}
		return distance(p.getX(), p.getY());
	}
	public double distance(double x, double y) {
		return Math.sqrt(distanceSq(x, y));
	}

	public boolean isNotFound() {
		return isNotFound;
	}

	public boolean isFound() {
		return !isNotFound;
	}

	public void setNotFound(boolean isNotFound) {
		this.isNotFound = isNotFound;
		if (isNotFound) {
			x = 0; y = 0;
		}
	}

	@Override
	public String toString() {
		return isNotFound ? "Not found" : x+", "+y;
	}
}
