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

import com.phybots.utils.ScreenPosition;
import com.phybots.utils.ScreenRectangle;

public interface HomographyCoordProvider extends CoordProvider {

	/**
	 * Set width of the real world coordinate area.
	 */
	public void setRealWidth(double realWidth);

	/**
	 * Set height of the real world coordinate area.
	 */
	public void setRealHeight(double realHeight);

	/**
	 * Set size of the real world coordinate area.
	 */
	public void setRealSize(double realWidth, double realHeight);

	/**
	 * @return Returns a rectangle object which defines screen area. The value is not a copy.
	 */
	public ScreenRectangle getRectangle();

	/**
	 * @see #getRectangle()
	 */
	public void getRectangleOut(ScreenRectangle rectangle);

	public void resetRectangle();

	public void setRectangle(ScreenRectangle rectangle);
	public void setRectangleCorner(int i, ScreenPosition p);
	public void setRectangleCorner(int i, int x, int y);
}
