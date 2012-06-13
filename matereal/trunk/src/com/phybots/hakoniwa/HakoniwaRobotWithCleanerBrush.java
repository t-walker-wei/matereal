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
import com.phybots.resource.CleanerBrushController;
import com.phybots.utils.Location;
import com.phybots.utils.Position;
import com.phybots.utils.ScreenPosition;


public class HakoniwaRobotWithCleanerBrush extends HakoniwaRobot {
	private static final long serialVersionUID = -2267090199931536404L;
	public static final int DEFAULT_PEN_RADIUS = 20;
	public static final Color DEFAULT_PEN_COLOR = Color.cyan;

	private transient HakoniwaRobotCleanerBrush cleanerBrush;
	private transient ScreenPosition screenPosition;

	public HakoniwaRobotWithCleanerBrush() {
		super();
	}

	public HakoniwaRobotWithCleanerBrush(Hakoniwa hakoniwa) {
		super(hakoniwa);
	}

	public HakoniwaRobotWithCleanerBrush(Location location) {
		super(location);
	}

	public HakoniwaRobotWithCleanerBrush(Position position) {
		super(position);
	}

	public HakoniwaRobotWithCleanerBrush(Location location, Hakoniwa hakoniwa) {
		super(location, hakoniwa);
	}

	public HakoniwaRobotWithCleanerBrush(Position position, Hakoniwa hakoniwa) {
		super(position, hakoniwa);
	}

	public HakoniwaRobotWithCleanerBrush(double x, double y) {
		super(x, y);
	}

	public HakoniwaRobotWithCleanerBrush(double x, double y, Hakoniwa hakoniwa) {
		super(x, y, hakoniwa);
	}

	public HakoniwaRobotWithCleanerBrush(double x, double y, double rotation) {
		super(x, y, rotation);
	}

	public HakoniwaRobotWithCleanerBrush(double x, double y, double rotation, Hakoniwa hakoniwa) {
		super(x, y, rotation, hakoniwa);
	}

	public HakoniwaRobotWithCleanerBrush(String name) {
		super(name);
	}

	public HakoniwaRobotWithCleanerBrush(String name, Hakoniwa hakoniwa) {
		super(name, hakoniwa);
	}

	public HakoniwaRobotWithCleanerBrush(String name, Location location) {
		super(name, location);
	}

	public HakoniwaRobotWithCleanerBrush(String name, Position position) {
		super(name, position);
	}

	public HakoniwaRobotWithCleanerBrush(String name, Location location, Hakoniwa hakoniwa) {
		super(name, location, hakoniwa);
	}

	public HakoniwaRobotWithCleanerBrush(String name, Position position, Hakoniwa hakoniwa) {
		super(name, position, hakoniwa);
	}

	public HakoniwaRobotWithCleanerBrush(String name, double x, double y) {
		super(name, x, y);
	}

	public HakoniwaRobotWithCleanerBrush(String name, double x, double y, Hakoniwa hakoniwa) {
		super(name, x, y, hakoniwa);
	}

	public HakoniwaRobotWithCleanerBrush(String name, double x, double y, double rotation) {
		super(name, x, y, rotation);
	}

	public HakoniwaRobotWithCleanerBrush(String name, double x, double y, double rotation, Hakoniwa hakoniwa) {
		super(name, x, y, rotation, hakoniwa);
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		initialize();
	}

	protected void initialize() {
		cleanerBrush = new HakoniwaRobotCleanerBrush();
		screenPosition  = new ScreenPosition();
		super.initialize();
	}

	@Override
	protected List<ResourceAbstractImpl> getResources() {
		List<ResourceAbstractImpl> rs = super.getResources();
		rs.add(cleanerBrush);
		return rs;
	}

	public void setBrushRadius(int penRadius) {
		this.cleanerBrush.setRadius(penRadius);
	}

	public int getBrushRadius() {
		return cleanerBrush.getRadius();
	}

	public void setBrushColor(Color penColor) {
		cleanerBrush.setColor(penColor);
	}

	public Color getBrushColor() {
		return cleanerBrush.getColor();
	}

	@Override
	public void preStep() {
		super.preStep();
		if (cleanerBrush != null && cleanerBrush.isWorking()) {
			final Hakoniwa hakoniwa = getHakoniwa();
			Graphics2D g2 = hakoniwa.createBackgroundGraphics();
			g2.setRenderingHint(
					RenderingHints.KEY_ANTIALIASING,
					hakoniwa.isAntialiased() ?
							RenderingHints.VALUE_ANTIALIAS_ON :
							RenderingHints.VALUE_ANTIALIAS_OFF);
			hakoniwa.getScreenPositionOut(this, screenPosition);
			g2.setColor(cleanerBrush.getColor());
			g2.fillOval(
					screenPosition.getX()-getBrushRadius(),
					screenPosition.getY()-getBrushRadius(),
					getBrushRadius()*2, getBrushRadius()*2);
			g2.dispose();
		}
	}

	protected static class HakoniwaRobotCleanerBrush extends ResourceAbstractImpl implements CleanerBrushController {
		private static final long serialVersionUID = 4941075903840939231L;
		private transient boolean isWorking;
		private Color color;
		private int radius;

		private HakoniwaRobotCleanerBrush() {
			setColor(DEFAULT_PEN_COLOR);
			setRadius(DEFAULT_PEN_RADIUS);
			initialize();
		}

		private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
			ois.defaultReadObject();
			initialize();
		}

		private void initialize() {
			isWorking = false;
		}

		@Override
		protected void onFree() {
			// Do nothing.
		}

		public void startCleaning() {
			isWorking = true;
		}

		public void endCleaning() {
			isWorking = false;
		}

		public boolean isWorking() {
			return isWorking;
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
	}

}
