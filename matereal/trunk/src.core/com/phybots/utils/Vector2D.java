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
 * 2D vector in the world coordinate.
 *
 * @author Jun Kato
 */
public class Vector2D implements Serializable {
	private static final long serialVersionUID = -350886875168567751L;
	protected double x;
	protected double y;

	public Vector2D() { }
	public Vector2D(double x, double y) { set(x, y); }
	public Vector2D(Vector2D v) { set(v); }

	public void set(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public void set(Vector2D p) {
		x = p.x;
		y = p.y;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getX() {
		return x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getY() {
		return y;
	}

	public double getNorm() {
		return Math.sqrt(x*x+y*y);
	}


	public double getNormSq() {
		return x*x+y*y;
	}

	public double getRelativeDirection(Vector2D v2) {
		return getRelativeDirection(
				v2.getX(),
				v2.getY());
	}

	public double getRelativeDirection(double x, double y) {
		return Math.atan2(
				y - this.y,
				x - this.x);
	}

	public static Vector2D add(Vector2D v1, Vector2D v2) {
		final Vector2D out = new Vector2D();
		addOut(v1, v2, out);
		return out;
	}

	public static void addOut(Vector2D v1, Vector2D v2, Vector2D out) {
		v1.addOut(v2, out);
	}

	public void add(Vector2D v) {
		addOut(v, this);
	}

	public void add(double x, double y) {
		addOut(x, y, this);
	}

	public void addOut(Vector2D v, Vector2D out) {
		addOut(v.x, v.y, out);
	}

	public void addOut(double x, double y, Vector2D out) {
		out.x = this.x + x;
		out.y = this.y + y;
	}

	public static Vector2D sub(Vector2D v1, Vector2D v2) {
		final Vector2D out = new Vector2D();
		subOut(v1, v2, out);
		return out;
	}

	public static void subOut(Vector2D v1, Vector2D v2, Vector2D out) {
		v1.subOut(v2, out);
	}

	public void sub(Vector2D v) {
		subOut(v, this);
	}

	public void sub(double x, double y) {
		subOut(x, y, this);
	}

	public void subOut(Vector2D v, Vector2D out) {
		subOut(v.x, v.y, out);
	}

	public void subOut(double x, double y, Vector2D out) {
		out.x = this.x - x;
		out.y = this.y - y;
	}

	public void mul(double l) {
		mulOut(l, this);
	}

	public void mulOut(double l, Vector2D out) {
		out.x = this.x * l;
		out.y = this.y * l;
	}

	public void div(double l) {
		divOut(l, this);
	}

	public void divOut(double l, Vector2D out) {
		out.x = this.x / l;
		out.y = this.y / l;
	}

	public static double dot(Vector2D v1, Vector2D v2) {
		return v1.dot(v2);
	}

	public double dot(Vector2D v) {
		return dot(v.x, v.y);
	}

	public double dot(double x, double y) {
		return this.x*x + this.y*y;
	}

	public static double cross(Vector2D v1, Vector2D v2) {
		return v1.cross(v2);
	}

	public double cross(Vector2D v) {
		return cross(v.x, v.y);
	}

	public double cross(double x, double y) {
		return this.x*y - this.y*x;
	}

	public void normalize() {
		double norm = getNorm();
		if (norm == 0) {
			return;
		}
		div(norm);
	}

	@Override
	public String toString() {
		return x+", "+y;
	}

}
