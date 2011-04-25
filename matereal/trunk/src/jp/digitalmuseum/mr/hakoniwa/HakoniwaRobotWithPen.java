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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.ResourceAbstractImpl;
import jp.digitalmuseum.mr.resource.PenController;
import jp.digitalmuseum.mr.resource.Pen.STATUS;
import jp.digitalmuseum.utils.Location;
import jp.digitalmuseum.utils.Position;
import jp.digitalmuseum.utils.ScreenPosition;

public class HakoniwaRobotWithPen extends HakoniwaRobot {
	private static final long serialVersionUID = 3732435694357130474L;
	public static final int DEFAULT_PEN_RADIUS = 1;
	public static final Color DEFAULT_PEN_COLOR = Color.red;

	private transient HakoniwaRobotPen pen;
	private transient ScreenPosition screenPosition;

	public HakoniwaRobotWithPen(String name, Location location) {
		this(name, location.getX(), location.getY(), location.getRotation());
	}

	public HakoniwaRobotWithPen(String name, Position position) {
		this(name, position.getX(), position.getY());
	}

	public HakoniwaRobotWithPen(String name, Location location, Hakoniwa hakoniwa) {
		this(name, location.getX(), location.getY(), location.getRotation(), hakoniwa);
	}

	public HakoniwaRobotWithPen(String name, Position position, Hakoniwa hakoniwa) {
		this(name, position.getX(), position.getY(), 0, hakoniwa);
	}

	public HakoniwaRobotWithPen(String name, double x, double y) {
		this(name, x, y, 0);
	}

	public HakoniwaRobotWithPen(String name, double x, double y, double rotation) {
		this(name, x, y, rotation, Matereal.getInstance().lookForService(Hakoniwa.class));
	}

	public HakoniwaRobotWithPen(String name, double x, double y, double rotation, Hakoniwa hakoniwa) {
		super(name, x, y, rotation, hakoniwa);
		initialize();
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		initialize();
	}

	private void initialize() {
		pen = new HakoniwaRobotPen();
		screenPosition = new ScreenPosition();
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
		if (pen != null && pen.status == STATUS.DOWN) {
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
		private transient STATUS status;
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
			status = STATUS.UP;
		}

		@Override
		protected void onFree() {
			// Do nothing.
		}

		public STATUS getStatus() {
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
			status = STATUS.UP;
		}

		public void putPen() {
			status = STATUS.DOWN;
		}
	}

}
