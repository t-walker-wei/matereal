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

import java.io.Serializable;

/**
 * Location information. (X, Y) [px] and direction [rad] in a screen coordinate.
 *
 * @author Jun Kato
 */
public class ScreenLocation implements Serializable {
	private static final long serialVersionUID = 129802525260500361L;
	private ScreenPosition position;
	private double rotation;

	public ScreenLocation() {
		position = new ScreenPosition();
	}
	public ScreenLocation(ScreenLocation location) {
		position = new ScreenPosition(location.position);
		rotation = location.rotation;
	}
	public ScreenLocation(ScreenPosition position, double rotation) {
		this.position = new ScreenPosition(position);
		this.rotation = rotation;
	}
	public ScreenLocation(int x, int y, double rotation) {
		position = new ScreenPosition(x, y);
		this.rotation = rotation;
	}

	public void setLocation(int x, int y, double rotation) {
		this.position.set(x, y);
		this.rotation = rotation;
	}
	public void setLocation(ScreenPosition position, double rotation) {
		this.position.set(position);
		this.rotation = rotation;
	}
	public void setLocation(ScreenLocation location) {
		position.set(location.position);
		rotation = location.rotation;
	}

	public void setPosition(int x, int y) {
		position.set(x, y);
	}
	public void setPosition(ScreenPosition position) {
		this.position.set(position);
	}

	public void setX(int x) {
		position.setX(x);
	}

	public void setY(int y) {
		position.setY(y);
	}

	public void setRotation(double rotation) {
		this.rotation = rotation;
		position.setNotFound(false);
	}

	public ScreenPosition getPosition() {
		return new ScreenPosition(position);
	}

	public void getPositionOut(ScreenPosition screenPosition) {
		screenPosition.set(position);
	}

	public int getX() {
		return position.getX();
	}

	public int getY() {
		return position.getY();
	}

	public double getRotation() {
		return rotation;
	}

	@Override
	public String toString() {
		return position.toString()+", "+rotation;
	}

	public boolean isNotFound() {
		return position.isNotFound();
	}

	public boolean isFound() {
		return position.isFound();
	}

	public void setNotFound(boolean isNotFound) {
		position.setNotFound(isNotFound);
	}
}
