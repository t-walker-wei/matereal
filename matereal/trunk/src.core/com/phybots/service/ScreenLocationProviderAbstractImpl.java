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


import com.phybots.entity.Entity;
import com.phybots.utils.ScreenLocation;
import com.phybots.utils.ScreenPosition;



/**
 * Abstract implementation of LocationProvider.
 *
 * @author Jun Kato
 * @see LocationProvider
 */
public abstract class ScreenLocationProviderAbstractImpl extends ServiceAbstractImpl implements ScreenLocationProvider {
	private static final long serialVersionUID = -2104247148124396683L;
	final ScreenLocation screenLocation = new ScreenLocation();

	public ScreenLocation getScreenLocation(Entity e) {
		getScreenLocationOut(e, screenLocation);
		return new ScreenLocation(screenLocation);
	}

	/**
	 * Get screen location of the entity.<br />
	 *
	 * @param e Entity to get screen location.
	 * @param screenLocation Screen location object.
	 */
	public abstract void getScreenLocationOut(Entity e, ScreenLocation screenLocation);

	public ScreenPosition getScreenPosition(Entity e) {
		getScreenLocationOut(e, screenLocation);
		return screenLocation.getPosition();
	}

	public void getScreenPositionOut(Entity e, ScreenPosition screenPosition) {
		getScreenLocationOut(e, screenLocation);
		screenPosition.set(screenLocation.getX(), screenLocation.getY());
	}

	public int getScreenX(Entity e) {
		getScreenLocationOut(e, screenLocation);
		return screenLocation.getX();
	}

	public int getScreenY(Entity e) {
		getScreenLocationOut(e, screenLocation);
		return screenLocation.getY();
	}

	public double getScreenRotation(Entity e) {
		getScreenLocationOut(e, screenLocation);
		return screenLocation.getRotation();
	}

}
