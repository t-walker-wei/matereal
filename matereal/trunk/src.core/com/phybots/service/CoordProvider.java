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

import com.phybots.utils.Location;
import com.phybots.utils.Position;
import com.phybots.utils.ScreenLocation;
import com.phybots.utils.ScreenPosition;

/**
 * Implementation class of this interface provides a mapping from a screen coordinate to a world coordinate.
 *
 * @author Jun Kato
 * @see LocationProvider
 * @see HomographyCoordProvider
 */
public interface CoordProvider extends ImageProvider {

	/**
	 * Get width of the real world coordinate area.
	 * @return
	 */
	public double getRealWidth();

	/**
	 * Get height of the real world coordinate area.
	 * @return
	 */
	public double getRealHeight();

	/**
	 * @param screenPosition
	 * @return
	 * @see #screenToRealOut(ScreenPosition, Position)
	 */
	public Position screenToReal(ScreenPosition screenPosition) throws IllegalArgumentException;

	/**
	 * @param screenLocation
	 * @return
	 * @see #screenToRealOut(ScreenLocation, Location)
	 */
	public Location screenToReal(ScreenLocation screenLocation) throws IllegalArgumentException;

	/**
	 * Convert screen position to real position.
	 * @param screenPosition
	 * @param realPosition
	 */
	public void screenToRealOut(ScreenPosition screenPosition, Position realPosition) throws IllegalArgumentException;

	/**
	 * Convert screen location to real location.
	 * @param screenLocation
	 * @param realLocation
	 */
	public void screenToRealOut(ScreenLocation screenLocation, Location realLocation) throws IllegalArgumentException;

	/**
	 * @param realPosition
	 * @return
	 * @see #realToScreenOut(Position, ScreenPosition)
	 */
	public ScreenPosition realToScreen(Position realPosition);

	/**
	 * @param realLocation
	 * @return
	 * @see #realToScreenOut(Location, ScreenLocation)
	 */
	public ScreenLocation realToScreen(Location realLocation);

	/**
	 * Convert real position to screen position.
	 * @param realPosition
	 * @param screenPosition
	 */
	public void realToScreenOut(Position realPosition, ScreenPosition screenPosition);

	/**
	 * Convert real location to screen location.
	 * @param realLocation
	 * @param screenLocation
	 */
	public void realToScreenOut(Location realLocation, ScreenLocation screenLocation);
}
