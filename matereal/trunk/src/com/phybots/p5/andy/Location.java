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
package com.phybots.p5.andy;

import com.phybots.service.CoordProvider;

public class Location {
	private com.phybots.utils.Location location;
	private com.phybots.utils.ScreenLocation screenLocation;

	public Location() {
		Andy.getInstance();
		location = new com.phybots.utils.Location();
		screenLocation = new com.phybots.utils.ScreenLocation();
	}

	public Location(double x, double y, double rotation) {
		this();
		setLocation(x, y, rotation);
	}

	public Location(Location l) {
		location = new com.phybots.utils.Location(l.location);
		screenLocation = new com.phybots.utils.ScreenLocation(l.screenLocation);
	}

	public synchronized void setLocation(Location l) {
		location.setLocation(l.location);
		screenLocation.setLocation(l.screenLocation);
	}

	synchronized void setLocation(com.phybots.utils.Location l) {
		location.setLocation(l);
		getCoordProvider().realToScreenOut(location, screenLocation);
	}

	synchronized void setLocation(double x, double y, double rotation) {
		location.setLocation(x, y, rotation);
		getCoordProvider().realToScreenOut(location, screenLocation);
	}

	public synchronized void setX(double x) {
		location.setX(x);
		getCoordProvider().realToScreenOut(location, screenLocation);
	}

	public synchronized void setY(double y) {
		location.setY(y);
		getCoordProvider().realToScreenOut(location, screenLocation);
	}

	public synchronized void setRotation(double rotation) {
		location.setRotation(rotation);
		getCoordProvider().realToScreenOut(location, screenLocation);
	}

	public synchronized void setScrenX(int x) {
		screenLocation.setX(x);
		getCoordProvider().screenToRealOut(screenLocation, location);
	}

	public synchronized void setScreenY(int y) {
		screenLocation.setY(y);
		getCoordProvider().screenToRealOut(screenLocation, location);
	}

	public synchronized void setScreenRotation(double rotation) {
		screenLocation.setRotation(rotation);
		getCoordProvider().screenToRealOut(screenLocation, location);
	}

	public synchronized int getScreenX() {
		return screenLocation.getX();
	}

	public synchronized int getScreenY() {
		return screenLocation.getY();
	}

	public synchronized double getScreenRotation() {
		return screenLocation.getRotation();
	}

	public synchronized double getX() {
		return location.getX();
	}

	public synchronized double getY() {
		return location.getY();
	}

	public synchronized double getRotation() {
		return location.getRotation();
	}

	private CoordProvider getCoordProvider() {
		return Andy.getInstance().getCoordProvider();
	}

}
