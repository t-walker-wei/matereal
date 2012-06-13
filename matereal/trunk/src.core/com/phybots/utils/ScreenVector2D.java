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
import java.io.Serializable;

/**
 * 2D vector in a screen coordinate.
 *
 * @author Jun Kato
 */
public class ScreenVector2D implements Serializable {
	private static final long serialVersionUID = -8961098699551120179L;
	protected int x;
	protected int y;

	public ScreenVector2D() {
		// Do nothing.
	}

	public ScreenVector2D(int x, int y) {
		set(x, y);
	}

	public ScreenVector2D(Point p) {
		set(p.x, p.y);
	}

	public ScreenVector2D(ScreenVector2D v) {
		set(v);
	}

	public void set(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void set(ScreenVector2D p) {
		x = p.x;
		y = p.y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getX() {
		return x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getY() {
		return y;
	}

	public void add(ScreenVector2D v) {
		addOut(v, this);
	}

	public void add(int x, int y) {
		addOut(x, y, this);
	}

	public void addOut(ScreenVector2D v, ScreenVector2D out) {
		addOut(v.x, v.y, out);
	}

	public void addOut(int x, int y, ScreenVector2D out) {
		out.x = this.x + x;
		out.y = this.y + y;
	}

	public void sub(ScreenVector2D v) {
		subOut(v, this);
	}

	public void sub(int x, int y) {
		subOut(x, y, this);
	}

	public void subOut(ScreenVector2D v, ScreenVector2D out) {
		subOut(v.x, v.y, out);
	}

	public void subOut(int x, int y, ScreenVector2D out) {
		out.x = this.x - x;
		out.y = this.y - y;
	}

	public void mul(double l) {
		mulOut(l, this);
	}

	public void mulOut(double l, ScreenVector2D out) {
		out.x = (int) (this.x * l);
		out.y = (int) (this.y * l);
	}

	public void div(double l) {
		divOut(l, this);
	}

	public void divOut(double l, ScreenVector2D out) {
		out.x = (int) (this.x / l);
		out.y = (int) (this.y / l);
	}

	public int dot(ScreenVector2D v) {
		return dot(v.x, v.y);
	}

	public int dot(int x, int y) {
		return this.x*x + this.y*y;
	}

	@Override
	public String toString() {
		return x+", "+y;
	}

}
