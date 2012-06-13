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
package com.phybots.hakoniwa;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import com.phybots.entity.ResourceAbstractImpl;
import com.phybots.resource.PenController;
import com.phybots.resource.Pen.PenStatus;
import com.phybots.utils.Location;
import com.phybots.utils.Position;
import com.phybots.utils.ScreenPosition;


public class HakoniwaRobotWithPen extends HakoniwaRobot {
	private static final long serialVersionUID = 3732435694357130474L;
	public static final int DEFAULT_PEN_RADIUS = 1;
	public static final Color DEFAULT_PEN_COLOR = Color.red;

	private transient HakoniwaRobotPen pen;
	private transient ScreenPosition screenPosition;


	public HakoniwaRobotWithPen() {
		super();
	}

	public HakoniwaRobotWithPen(Hakoniwa hakoniwa) {
		super(hakoniwa);
	}

	public HakoniwaRobotWithPen(Location location) {
		super(location);
	}

	public HakoniwaRobotWithPen(Position position) {
		super(position);
	}

	public HakoniwaRobotWithPen(Location location, Hakoniwa hakoniwa) {
		super(location, hakoniwa);
	}

	public HakoniwaRobotWithPen(Position position, Hakoniwa hakoniwa) {
		super(position, hakoniwa);
	}

	public HakoniwaRobotWithPen(double x, double y) {
		super(x, y);
	}

	public HakoniwaRobotWithPen(double x, double y, Hakoniwa hakoniwa) {
		super(x, y, hakoniwa);
	}

	public HakoniwaRobotWithPen(double x, double y, double rotation) {
		super(x, y, rotation);
	}

	public HakoniwaRobotWithPen(double x, double y, double rotation, Hakoniwa hakoniwa) {
		super(x, y, rotation, hakoniwa);
	}

	public HakoniwaRobotWithPen(String name) {
		super(name);
	}

	public HakoniwaRobotWithPen(String name, Hakoniwa hakoniwa) {
		super(name, hakoniwa);
	}

	public HakoniwaRobotWithPen(String name, Location location) {
		super(name, location);
	}

	public HakoniwaRobotWithPen(String name, Position position) {
		super(name, position);
	}

	public HakoniwaRobotWithPen(String name, Location location, Hakoniwa hakoniwa) {
		super(name, location, hakoniwa);
	}

	public HakoniwaRobotWithPen(String name, Position position, Hakoniwa hakoniwa) {
		super(name, position, hakoniwa);
	}

	public HakoniwaRobotWithPen(String name, double x, double y) {
		super(name, x, y);
	}

	public HakoniwaRobotWithPen(String name, double x, double y, Hakoniwa hakoniwa) {
		super(name, x, y, hakoniwa);
	}

	public HakoniwaRobotWithPen(String name, double x, double y, double rotation) {
		super(name, x, y, rotation);
	}

	public HakoniwaRobotWithPen(String name, double x, double y, double rotation, Hakoniwa hakoniwa) {
		super(name, x, y, rotation, hakoniwa);
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		initialize();
	}

	protected void initialize() {
		pen = new HakoniwaRobotPen();
		screenPosition = new ScreenPosition();
		super.initialize();
	}

	@Override
	protected List<ResourceAbstractImpl> getResources() {
		List<ResourceAbstractImpl> rs = super.getResources();
		rs.add(pen);
		return rs;
	}

	public void setPenRadius(int penRadius) {
		this.pen.setRadius(penRadius);
	}

	public int getPenRadius() {
		return pen.getRadius();
	}

	public void setPenColor(Color penColor) {
		pen.setColor(penColor);
	}

	public Color getPenColor() {
		return pen.getColor();
	}

	@Override
	public void preStep() {
		super.preStep();
		if (pen != null && pen.status == PenStatus.DOWN) {
			final Hakoniwa hakoniwa = getHakoniwa();
			Graphics2D g2 = hakoniwa.createBackgroundGraphics();
			g2.setRenderingHint(
					RenderingHints.KEY_ANTIALIASING,
					hakoniwa.isAntialiased() ?
							RenderingHints.VALUE_ANTIALIAS_ON :
							RenderingHints.VALUE_ANTIALIAS_OFF);
			hakoniwa.getScreenPositionOut(this, screenPosition);
			g2.setColor(pen.getColor());
			g2.fillOval(
					screenPosition.getX()-getPenRadius(),
					screenPosition.getY()-getPenRadius(),
					getPenRadius()*2, getPenRadius()*2);
			g2.dispose();
		}
	}

	protected static class HakoniwaRobotPen extends ResourceAbstractImpl implements PenController {
		private static final long serialVersionUID = 7309774293086967156L;
		private transient PenStatus status;
		private Color color;
		private int radius;

		private HakoniwaRobotPen() {
			setColor(DEFAULT_PEN_COLOR);
			setRadius(DEFAULT_PEN_RADIUS);
			initialize();
		}

		private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
			ois.defaultReadObject();
			initialize();
		}

		private void initialize() {
			status = PenStatus.UP;
		}

		@Override
		protected void onFree() {
			// Do nothing.
		}

		public PenStatus getStatus() {
			return status;
		}

		public void setColor(Color color) {
			this.color = color;
		}

		public Color getColor() {
			return color;
		}

		public void setRadius(int radius) {
			this.radius = radius;
		}

		public int getRadius() {
			return radius;
		}

		public void endPen() {
			status = PenStatus.UP;
		}

		public void putPen() {
			status = PenStatus.DOWN;
		}
	}

}
