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

import com.phybots.message.ServiceUpdateEvent;
import com.phybots.utils.Location;
import com.phybots.utils.Position;
import com.phybots.utils.ScreenLocation;
import com.phybots.utils.ScreenPosition;
import com.phybots.utils.ScreenRectangle;


/**
 * Abstract implementation of CoordProvider.
 * This class calculates linear mapping between coordinates using homography.
 *
 * @author Jun Kato
 * @see CoordProvider
 */
public abstract class HomographyCoordProviderAbstractImpl extends ServiceAbstractImpl implements HomographyCoordProvider {
	private static final long serialVersionUID = -7516458060912262010L;
	final public static double DEFAULT_REAL_WIDTH = 800.0;
	final public static double DEFAULT_REAL_HEIGHT = 600.0;

	/** Rectangle used for mapping screen coordinate to real world coordinate. */
	private ScreenRectangle rectangle;
	/** Real world coordinate width and height. */
	private double realWidth, realHeight;

	private ThreadLocal<ScreenPosition> screenPosition = new ThreadLocal<ScreenPosition>(){
		public ScreenPosition initialValue() {
			return new ScreenPosition();
		}
	};

	private ThreadLocal<Position> position = new ThreadLocal<Position>(){
		public Position initialValue() {
			return new Position();
		}
	};

	public HomographyCoordProviderAbstractImpl() {
		super();
		rectangle = new ScreenRectangle();
		realWidth = DEFAULT_REAL_WIDTH;
		realHeight = DEFAULT_REAL_HEIGHT;
	}

	public double getRealWidth() {
		return realWidth;
	}

	public double getRealHeight() {
		return realHeight;
	}

	public void setRealSize(double realWidth, double realHeight) {
		this.realWidth = realWidth;
		this.realHeight = realHeight;
		distributeEvent(new ServiceUpdateEvent(this,
				"real size", new double[] { realWidth, realHeight }));
	}

	public void setRealWidth(double realWidth) {
		this.realWidth = realWidth;
		distributeEvent(new ServiceUpdateEvent(this, "real width", realWidth));
	}

	public void setRealHeight(double realHeight) {
		this.realHeight = realHeight;
		distributeEvent(new ServiceUpdateEvent(this, "real height", realHeight));
	}

	public ScreenRectangle getRectangle() {
		return new ScreenRectangle(rectangle);
	}

	public void getRectangleOut(ScreenRectangle rectangle) {
		rectangle.set(this.rectangle);
	}

	public void setRectangle(ScreenRectangle rectangle) {
		this.rectangle.set(rectangle);
		distributeEvent(new ServiceUpdateEvent(this,
				"rectangle", new ScreenRectangle(rectangle)));
	}

	public void setRectangleCorner(int i, ScreenPosition p) {
		rectangle.set(i, p);
		distributeEvent(new ServiceUpdateEvent(this,
				"rectangle", new ScreenRectangle(rectangle)));
	}

	public void setRectangleCorner(int i, int x, int y) {
		rectangle.set(i, x, y);
		distributeEvent(new ServiceUpdateEvent(this,
				"rectangle", new ScreenRectangle(rectangle)));
	}

	public void resetRectangle() {
		rectangle.setLeftTop(0, 0);
		rectangle.setRightTop(getWidth(), 0);
		rectangle.setRightBottom(getWidth(), getHeight());
		rectangle.setLeftBottom(0, getHeight());
		distributeEvent(new ServiceUpdateEvent(this,
				"rectangle", new ScreenRectangle(rectangle)));
	}

	public ScreenLocation realToScreen(Location location) {
		final ScreenLocation screenLocation = new ScreenLocation();
		realToScreenOut(location, screenLocation);
		return screenLocation;
	}

	public ScreenPosition realToScreen(Position position) {
		final ScreenPosition screenPosition = new ScreenPosition();
		realToScreenOut(position, screenPosition);
		return screenPosition;
	}

	public void realToScreenOut(Location location, ScreenLocation screenLocation) {
		location.getPositionOut(position.get());
		realToScreenOut(position.get(), screenPosition.get());
		screenLocation.setPosition(screenPosition.get());
		screenLocation.setRotation(location.getRotation());
	}

	public void realToScreenOut(Position position, ScreenPosition screenPosition) {
		final double
		a = position.getX()/realWidth,
		b = 1-position.getY()/realHeight;

		// Vector e, f, g, h
		final double
		ex =
			  (1-b)*rectangle.getRightTop().getX()
			+ b*rectangle.getRightBottom().getX(),
		fx =
			  a*rectangle.getRightBottom().getX()
			+ (1-a)*rectangle.getLeftBottom().getX(),
		gx =
			  b*rectangle.getLeftBottom().getX()
			+ (1-b)*rectangle.getLeftTop().getX(),
		hx =
			  (1-a)*rectangle.getLeftTop().getX()
			+ a*rectangle.getRightTop().getX();
		final double
		ey =
			  (1-b)*rectangle.getRightTop().getY()
			+ b*rectangle.getRightBottom().getY(),
		fy =
			  a*rectangle.getRightBottom().getY()
			+ (1-a)*rectangle.getLeftBottom().getY(),
		gy =
			  b*rectangle.getLeftBottom().getY()
			+ (1-b)*rectangle.getLeftTop().getY(),
		hy =
			  (1-a)*rectangle.getLeftTop().getY()
			+ a*rectangle.getRightTop().getY();

		// relative vector seen from g
		final double
		ix = ex - gx, iy = ey - gy,
		jx = fx - gx, jy = fy - gy,
		kx = hx - gx, ky = hy - gy;

		// cross product ∝ distance
		final double
		d1 = Math.abs(ix*jy - jx*iy),
		d2 = Math.abs(ix*ky - kx*iy);

		final double t = d1 / (d1+d2);
		screenPosition.set(
				(int) ((1-t)*fx+t*hx),
				(int) ((1-t)*fy+t*hy));
	}

	public Location screenToReal(ScreenLocation screenLocation) {
		final Location realLocation = new Location();
		screenToRealOut(screenLocation, realLocation);
		return realLocation;
	}

	public Position screenToReal(ScreenPosition screenPosition) {
		final Position realPosition = new Position();
		screenToRealOut(screenPosition, realPosition);
		return realPosition;
	}

	public void screenToRealOut(ScreenLocation screenLocation, Location location) {
		screenLocation.getPositionOut(screenPosition.get());
		screenToRealOut(screenPosition.get(), position.get());
		location.setPosition(position.get());
		location.setRotation(screenLocation.getRotation());
	}

	public void screenToRealOut(ScreenPosition screenPosition, Position position) {
		// Vector a, b, c, d
		final int
		ax =
			  rectangle.getLeftTop().getX()
			- rectangle.getRightTop().getX()
			- rectangle.getLeftBottom().getX()
			+ rectangle.getRightBottom().getX(),
		bx =
			- rectangle.getLeftTop().getX()
			+ rectangle.getRightTop().getX(),
		cx =
			- rectangle.getLeftTop().getX()
			+ rectangle.getLeftBottom().getX(),
		dx =
			  rectangle.getLeftTop().getX()
			- screenPosition.getX();
		final int
		ay =
			  rectangle.getLeftTop().getY()
			- rectangle.getRightTop().getY()
			- rectangle.getLeftBottom().getY()
			+ rectangle.getRightBottom().getY(),
		by =
			- rectangle.getLeftTop().getY()
			+ rectangle.getRightTop().getY(),
		cy =
			- rectangle.getLeftTop().getY()
			+ rectangle.getLeftBottom().getY(),
		dy =
			  rectangle.getLeftTop().getY()
			- screenPosition.getY();
		// ax*uv + bx*u + cx*v + dx = 0
		// ay*uv + by*u + cy*v + dy = 0

		/*
		 * case: parallelogram
		 * get u and v
		 * that satisfies (x, y) = (1-u-v)*LeftTop + u*RightTop + v*LeftBottom
		 */
		if (ax == 0 && ay == 0) {

			/** outer product */
			final int
				/** b*c */ e1 = bx*cy - cx*by,
				/** c*d */ e2 = cx*dy - dx*cy,
				/** d*b */ e3 = dx*by - bx*dy;

			// calculating dimension ratio of parallelograms
			position.set((double)e2/e1*realWidth, (1-(double)e3/e1)*realHeight);
			return;
		}

		/*
		 * default:
		 * get u and v
		 * that satisfies (x, y) = (1-u)(1-v)*points[0] + u(1-v)*points[1] + (1-u)v*points[3] + uv*points[2]
		 */

		/** outer product */
		int
			/** b*a */ e1 = bx*ay - ax*by,
			/** c*a */ e2 = cx*ay - ax*cy,
			/** d*a */ e3 = dx*ay - ax*dy;

		// express v with u by e1*u + e2*v + e3 = 0.
		// Coefficient alpha, beta, gamma
		final double
			alpha = (double) -ax * e1 / e2,
			beta  = (double) -ax * e3 / e2 + bx - (double) cx * e1 / e2,
			gamma = (double) -cx * e3 / e2 + dx;
		// alpha*u^2 + beta*u + gamma = 0

		final double
			plusminus = Math.sqrt(beta*beta - 4*alpha*gamma),
			uPlus  = (-beta + plusminus) / (2 * alpha),
			uMinus = (-beta - plusminus) / (2 * alpha);

		final double u = (0 <= uPlus && uPlus <= 1) ? uPlus : uMinus;
		/*
		if (u < 0 || 1 < u) {
			throw new IllegalArgumentException();
		}
		*/
		final double v = (double) -e1 / e2 * u - (double) e3 / e2;
		/*
		if (v < 0 || 1 < v) {
			throw new IllegalArgumentException();
		}
		*/
		position.set(u*realWidth, (1-v)*realHeight);
	}
}
