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
package jp.digitalmuseum.mr.entity;

import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.util.List;


import jp.digitalmuseum.connector.Connector;
import jp.digitalmuseum.mr.entity.PhysicalRobotAbstractImpl;
import jp.digitalmuseum.mr.entity.ResourceAbstractImpl;
import jp.digitalmuseum.mr.resource.DifferentialWheelsAbstractImpl;

/**
 * Mini, a prototype robot developed at JST ERATO Igarashi Design Interface Project.
 * http://designinterface.jp/
 *
 * @author Jun KATO
 */
public class Mini extends PhysicalRobotAbstractImpl {
	final public static double WIDTH = 8;
	final public static double HEIGHT = 6;
	private MiniWheels w;
	private Shape shape;

	public Mini(String name) {
		super();
		initialize(name);
	}

	public Mini(String name, String connectionString) {
		super(connectionString);
		initialize(name);
	}

	public Mini(String name, Connector connector) {
		super(connector);
		initialize(name);
	}

	@Override
	public void dispose() {
		w.stopWheels();
		super.dispose();
	}


	private void initialize(String name) {
		setName(name);
		setTypeName("Mini");
		w = new MiniWheels(this);
		shape = new RoundRectangle2D.Double(
				-WIDTH/2 , -HEIGHT/2,
				WIDTH, HEIGHT,
				3, 3);
	}

	@Override
	protected List<ResourceAbstractImpl> getResources() {
		List<ResourceAbstractImpl> rs = super.getResources();
		rs.add(w);
		return rs;
	}

	public Shape getShape() {
		return shape;
	}

	/**
	 * Wheels of Mini.
	 *
	 * @author Jun KATO
	 * @see Mini
	 */
	public static class MiniWheels extends DifferentialWheelsAbstractImpl {
		public static final int DEFAULT_SPEED = 70; // 11=too slow, 13=too fast
		public static final int MAX_STEP = 50000;
		public static final int MAX_ROTATION_SPEED = 8000;
		public static final int MIN_COMMAND_INTERVAL = 100;
		private long time = 0;

		public MiniWheels(Mini mini) {
			super(mini);
			initialize();
		}

		public MiniWheels(Connector connector) {
			super(connector);
			initialize();
		}

		public int getRecommendedRotationSpeed() {
			return DEFAULT_SPEED;
		}

		public int getRecommendedSpeed() {
			return DEFAULT_SPEED;
		}

		@Override
		protected synchronized boolean doDrive(int leftPower, int rightPower) {
			final int l = getPower(leftPower);
			final int r = getPower(rightPower);
			final String cmd = String.format("%d,%04d,%05d,%d,%04d,%05d",
					l>0?0:1, l>0?l:-l, MAX_STEP,
					r>0?0:1, r>0?r:-r, MAX_STEP);

			// Do not send command if enough time has not passed since the last command.
			final long currentTime = System.currentTimeMillis();
			if (currentTime - time < MIN_COMMAND_INTERVAL) {
				return false;
			}
			time = currentTime;
			return	getConnector().write(cmd) &&
					getConnector().write(0x0d);
		}

		@Override
		protected boolean doStopWheels() {

			// Wait to stop the robot with certainty.
			final long currentTime = System.currentTimeMillis();
			if (currentTime - time < MIN_COMMAND_INTERVAL) {
				try {
					Thread.sleep(currentTime + MIN_COMMAND_INTERVAL - time);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			time = currentTime;
			return getConnector().write(0x0d);
		}

		private int getPower(int percentage) {
			return percentage * MAX_ROTATION_SPEED / 100;
		}
	}
}