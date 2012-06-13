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

import java.awt.Point;

/**
 * Position information. (X, Y) [px] in a screen coordinate.
 *
 * @author Jun Kato
 */
public class ScreenPosition extends ScreenVector2D {
	private static final long serialVersionUID = 7724280665928559617L;
	private boolean isNotFound = false;

	public ScreenPosition() {
		isNotFound = true;
	}

	public ScreenPosition(int x, int y) {
		set(x, y);
	}

	public ScreenPosition(Point p) {
		this(p.x, p.y);
	}

	public ScreenPosition(ScreenVector2D position) {
		if (position instanceof ScreenPosition
				&& ((ScreenPosition) position).isNotFound) {
			isNotFound = true;
		} else {
			set(position.x, position.y);
		}
	}

	@Override
	public void set(int x, int y) {
		super.set(x, y);
		isNotFound = false;
	}

	@Override
	public void set(ScreenVector2D p) {
		super.set(p);
		isNotFound = false;
	}

	@Override
	public void setX(int x) {
		super.setX(x);
		isNotFound = false;
	}

	@Override
	public void setY(int y) {
		super.setY(y);
		isNotFound = false;
	}

	public int distanceSq(ScreenPosition p) {
		if (isNotFound || p.isNotFound) {
			return 0;
		}
		return distanceSq(p.getX(), p.getY());
	}
	public int distanceSq(int x, int y) {
		final int
			dx = x - this.x,
			dy = y - this.y;
		return dx*dx + dy*dy;
	}

	public double distance(ScreenPosition p) {
		if (isNotFound || p.isNotFound) {
			return 0;
		}
		return distance(p.getX(), p.getY());
	}
	public double distance(int x, int y) {
		return Math.sqrt(distanceSq(x, y));
	}

	public double getRelativeDirection(ScreenPosition destination) {
		if (isNotFound || destination.isNotFound) {
			return 0;
		}
		return getRelativeDirection(
				destination.getX(),
				destination.getY());
	}
	public double getRelativeDirection(int x, int y) {
		return Math.atan2(
				y - this.y,
				x - this.x);
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
