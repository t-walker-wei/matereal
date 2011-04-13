/*
 * PROJECT: matereal at http://mr.digitalmuseum.jp/
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
 * The Original Code is matereal.
 *
 * The Initial Developer of the Original Code is Jun KATO.
 * Portions created by the Initial Developer are
 * Copyright (C) 2009 Jun KATO. All Rights Reserved.
 *
 * Contributor(s): Jun KATO
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
package jp.digitalmuseum.mr.hakoniwa;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.ResourceAbstractImpl;
import jp.digitalmuseum.mr.resource.CleanerBrushController;
import jp.digitalmuseum.utils.Location;
import jp.digitalmuseum.utils.Position;
import jp.digitalmuseum.utils.ScreenPosition;

public class HakoniwaRobotWithCleanerBrush extends HakoniwaRobot {
	public static final int DEFAULT_PEN_RADIUS = 20;
	public static final Color DEFAULT_PEN_COLOR = Color.cyan;
	private HakoniwaRobotCleanerBrush cleanerBrush;
	final private ScreenPosition screenPosition =
			new ScreenPosition();

	public HakoniwaRobotWithCleanerBrush(String name, Location location) {
		this(name, location.getX(), location.getY(), location.getRotation());
	}

	public HakoniwaRobotWithCleanerBrush(String name, Position position) {
		this(name, position.getX(), position.getY());
	}

	public HakoniwaRobotWithCleanerBrush(String name, Location location, Hakoniwa hakoniwa) {
		this(name, location.getX(), location.getY(), location.getRotation(), hakoniwa);
	}

	public HakoniwaRobotWithCleanerBrush(String name, Position position, Hakoniwa hakoniwa) {
		this(name, position.getX(), position.getY(), 0, hakoniwa);
	}

	public HakoniwaRobotWithCleanerBrush(String name, double x, double y) {
		this(name, x, y, 0);
	}

	public HakoniwaRobotWithCleanerBrush(String name, double x, double y, double rotation) {
		this(name, x, y, rotation, Matereal.getInstance().lookForService(Hakoniwa.class));
	}

	public HakoniwaRobotWithCleanerBrush(String name, double x, double y, double rotation, Hakoniwa hakoniwa) {
		super(name, x, y, rotation, hakoniwa);
		this.cleanerBrush = new HakoniwaRobotCleanerBrush();
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
		private Color color;
		private int radius;
		private boolean isWorking;

		private HakoniwaRobotCleanerBrush() {
			setColor(DEFAULT_PEN_COLOR);
			setRadius(DEFAULT_PEN_RADIUS);
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
