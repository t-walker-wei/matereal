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

import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Rectangle in the screen coordinate.
 *
 * @author Jun Kato
 */
public class ScreenRectangle implements Iterable<ScreenPosition>, Serializable {
	private static final long serialVersionUID = 8793281047504948273L;
	public static final int RIGHT_TOP = 0;
	public static final int RIGHT_BOTTOM = 1;
	public static final int LEFT_BOTTOM = 2;
	public static final int LEFT_TOP = 3;
	private ScreenPosition[] corners;

	public ScreenRectangle() {
		corners = new ScreenPosition[4];
		for (int i = 0; i < 4; i ++) {
			corners[i] = new ScreenPosition();
		}
	}

	public ScreenRectangle(ScreenRectangle rectangle) {
		corners = new ScreenPosition[4];
		for (int i = 0; i < 4; i ++) {
			corners[i] = new ScreenPosition(rectangle.corners[i]);
		}
	}

	public ScreenRectangle(ScreenPosition rt, ScreenPosition rb, ScreenPosition lb, ScreenPosition lt) {
		corners = new ScreenPosition[] { rt, rb, lb, lt };
	}

	public ScreenRectangle(ScreenPosition[] corners) {
		if (corners == null ||
				corners.length != 4) {
			throw new IllegalArgumentException("Rectangle has 4 corners.");
		}
	}

	public synchronized ScreenPosition get(int i) {
		return corners[i];
	}
	public synchronized ScreenPosition getRightTop() {
		return corners[RIGHT_TOP];
	}
	public synchronized ScreenPosition getRightBottom() {
		return corners[RIGHT_BOTTOM];
	}
	public synchronized ScreenPosition getLeftBottom() {
		return corners[LEFT_BOTTOM];
	}
	public synchronized ScreenPosition getLeftTop() {
		return corners[LEFT_TOP];
	}

	public synchronized void set(ScreenRectangle rectangle) {
		for (int i = 0; i < 4; i ++) {
			corners[i].set(rectangle.corners[i]);
		}
	}
	public synchronized void set(int i, ScreenPosition p) {
		corners[i] = p;
	}
	public synchronized void set(int i, int x, int y) {
		corners[i].set(x, y);
	}
	public synchronized void setRightTop(ScreenPosition p) {
		corners[RIGHT_TOP] = p;
	}
	public synchronized void setRightTop(int x, int y) {
		corners[RIGHT_TOP].set(x, y);
	}
	public synchronized void setRightBottom(ScreenPosition p) {
		corners[RIGHT_BOTTOM] = p;
	}
	public synchronized void setRightBottom(int x, int y) {
		corners[RIGHT_BOTTOM].set(x, y);
	}
	public synchronized void setLeftBottom(ScreenPosition p) {
		corners[LEFT_BOTTOM] = p;
	}
	public synchronized void setLeftBottom(int x, int y) {
		corners[LEFT_BOTTOM].set(x, y);
	}
	public synchronized void setLeftTop(ScreenPosition p) {
		corners[LEFT_TOP] = p;
	}
	public synchronized void setLeftTop(int x, int y) {
		corners[LEFT_TOP].set(x, y);
	}

	public synchronized int getRectangleCornerIndexNear(int x, int y) {
		int distanceSq = Integer.MAX_VALUE;
		int nearestIndex = -1;
		for (int i = 0; i < 4; i ++) {
			final ScreenPosition p = get(i);
			final int curDistanceSq = p.distanceSq(x, y);
			if (curDistanceSq < distanceSq) {
				nearestIndex = i;
				distanceSq = curDistanceSq;
			}
		}
		return nearestIndex;
	}

	public synchronized double getRotation() {
		return Math.atan2(
				  getRightTop().getY()
				+ getLeftTop().getY()
				- getRightBottom().getY()
				- getLeftBottom().getY(),
				  getRightTop().getX()
				+ getLeftTop().getX()
				- getRightBottom().getX()
				- getLeftBottom().getX());
	}

	public synchronized Iterator<ScreenPosition> iterator() {
		return new RectangleIterator();
	}

	private class RectangleIterator implements Iterator<ScreenPosition> {
		private int counter = 0;
		public boolean hasNext() { return counter < 4; }

		public ScreenPosition next() {
			synchronized (ScreenRectangle.this) {
				if (counter > 3) {
					throw new NoSuchElementException();
				}
				return corners[counter ++];
			}
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public synchronized void draw(Graphics2D g) {
		ScreenPosition previousPoint = corners[3];
		for (ScreenPosition point : corners) {
			g.drawLine(previousPoint.getX(), previousPoint.getY(),
					point.getX(), point.getY());
			previousPoint = point;
		}
	}

	public synchronized void draw(Graphics2D g, boolean withDescription) {
		draw(g);
		if (!withDescription) { return; }
		drawString(g, "RT", getRightTop());
		drawString(g, "RB", getRightBottom());
		drawString(g, "LB", getLeftBottom());
		drawString(g, "LT", getLeftTop());
	}

	private void drawString(Graphics2D g, String text, ScreenPosition p) {
		g.drawString(text, p.getX(), p.getY());
	}
}
