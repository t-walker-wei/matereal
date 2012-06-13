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
 * Location information. (X, Y) [cm] and direction [rad] in the world coordinate.
 *
 * @author Jun Kato
 */
public class Location implements Serializable {
	private static final long serialVersionUID = 370618063374236950L;
	private Position position;
	private double rotation;

	public Location() {
		position = new Position();
	}
	public Location(Location location) {
		position = new Position(location.position);
		rotation = location.rotation;
	}
	public Location(Position position, double rotation) {
		this.position = new Position(position);
		this.rotation = rotation;
	}
	public Location(double x, double y, double rotation) {
		position = new Position(x, y);
		this.rotation = rotation;
	}

	public void setLocation(double x, double y, double rotation) {
		this.position.set(x, y);
		this.rotation = rotation;
	}
	public void setLocation(Position position, double rotation) {
		this.position.set(position);
		this.rotation = rotation;
	}
	public void setLocation(Location location) {
		position.set(location.position);
		rotation = location.rotation;
	}

	public void setPosition(double x, double y) {
		position.set(x, y);
	}
	public void setPosition(Position position) {
		this.position.set(position);
	}

	public void setX(double x) {
		position.setX(x);
	}

	public void setY(double y) {
		position.setY(y);
	}

	public void setRotation(double rotation) {
		this.rotation = rotation;
		position.setNotFound(false);
	}

	public Position getRelativePosition(Position relativePosition) {
		final Position position = new Position();
		getRelativePositionOut(relativePosition, position);
		return position;
	}
	public Position getRelativePosition(double x, double y) {
		final Position position = new Position();
		getRelativePositionOut(x, y, position);
		return position;
	}
	public void getRelativePositionOut(Position relativePosition, Position position) {
		getRelativePositionOut(relativePosition.getX(),
				relativePosition.getY(),
				position);
	}
	public void getRelativePositionOut(double rx, double ry, Position position) {
		final double
			cos = Math.sin(rotation),
			sin = Math.cos(rotation);
		position.set(
			getX() +cos*rx+sin*ry,
			getY() -sin*rx+cos*ry);
	}

	public void getRelativeLocation(Location relativeLocation) {
		final Location location = new Location();
		getRelativeLocationOut(relativeLocation, location);
	}
	public void getRelativeLocationOut(Location relativeLocation, Location location) {
		getRelativePositionOut(
				relativeLocation.getX(), relativeLocation.getY(),
				location.position);
		location.setRotation(
				location.getRotation()+rotation);
	}

	public Position getPosition() {
		return new Position(position);
	}

	public void getPositionOut(Position position) {
		position.set(this.position);
	}

	public double getX() {
		return position.getX();
	}

	public double getY() {
		return position.getY();
	}

	public double getRotation() {
		return rotation;
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

	@Override
	public String toString() {
		return isNotFound() ? position.toString() : position.toString()+", "+rotation;
	}
}
