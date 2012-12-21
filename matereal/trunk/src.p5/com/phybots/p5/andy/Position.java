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

public class Position {
	private com.phybots.utils.Position position;
	private com.phybots.utils.ScreenPosition screenPosition;

	public Position() {
		Andy.getInstance();
		position = new com.phybots.utils.Position();
		screenPosition = new com.phybots.utils.ScreenPosition();
	}

	public Position(int x, int y) {
		this();
		setScreen(x, y);
	}

	public Position(double x, double y) {
		this();
		set(x, y);
	}

	public Position(Position p) {
		this();
		set(p);
	}

	public void set(Position p) {
		this.position.set(p.position);
		this.screenPosition.set(p.screenPosition);
	}

	public void set(com.phybots.utils.Position p) {
		this.position.set(p);
		getCoordProvider().realToScreenOut(position, screenPosition);
	}

	public synchronized void setScreen(int x, int y) {
		screenPosition.set(x, y);
		getCoordProvider().screenToRealOut(screenPosition, position);
	}

	public synchronized void setScreenX(int x) {
		screenPosition.setX(x);
		getCoordProvider().screenToRealOut(screenPosition, position);
	}

	public synchronized void setScreenY(int y) {
		screenPosition.setY(y);
		getCoordProvider().screenToRealOut(screenPosition, position);
	}

	public synchronized void set(double x, double y) {
		position.set(x, y);
		getCoordProvider().realToScreenOut(position, screenPosition);
	}

	public synchronized void setX(double x) {
		position.setX(x);
		getCoordProvider().realToScreenOut(position, screenPosition);
	}

	public synchronized void setY(double y) {
		position.setY(y);
		getCoordProvider().realToScreenOut(position, screenPosition);
	}

	public synchronized int getScreenX() {
		return screenPosition.getX();
	}

	public synchronized int getScreenY() {
		return screenPosition.getY();
	}

	public synchronized double getX() {
		return position.getX();
	}

	public synchronized double getY() {
		return position.getY();
	}

	private CoordProvider getCoordProvider() {
		return Andy.getInstance().getCoordProvider();
	}
}
