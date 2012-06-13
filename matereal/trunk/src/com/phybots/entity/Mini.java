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
package com.phybots.entity;

import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

import com.phybots.entity.PhysicalRobotAbstractImpl;
import com.phybots.entity.ResourceAbstractImpl;
import com.phybots.resource.DifferentialWheelsAbstractImpl;


import jp.digitalmuseum.connector.Connector;

/**
 * Mini, a prototype robot developed at JST ERATO Igarashi Design Interface Project.
 * http://designinterface.jp/
 *
 * @author Jun Kato
 */
public class Mini extends PhysicalRobotAbstractImpl {
	private static final long serialVersionUID = 3586580600456610155L;
	public static final double WIDTH = 8;
	public static final double HEIGHT = 6;
	private static int instances = 0;
	private MiniWheels w;
	private Shape shape;

	public Mini() {
		super();
	}

	public Mini(String connectionString) {
		super(connectionString);
	}

	public Mini(String connectionString, String name) {
		super(connectionString, name);
	}

	public Mini(Connector connector) {
		super(connector);
	}

	public Mini(Connector connector, String name) {
		super(connector, name);
	}

	@Override
	protected void initialize() {
		setTypeName("Mini");
		instances ++;
		if (getName() == null) {
			setName(getTypeName()+" ("+instances+")");
		}
		w = new MiniWheels(this);
		shape = new RoundRectangle2D.Double(
				-WIDTH/2 , -HEIGHT/2,
				WIDTH, HEIGHT,
				3, 3);
	}

	@Override
	public void dispose() {
		w.stopWheels();
		super.dispose();
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
	 * @author Jun Kato
	 * @see Mini
	 */
	public static class MiniWheels extends DifferentialWheelsAbstractImpl {
		private static final long serialVersionUID = 8894011744025618265L;
		public static final int DEFAULT_SPEED = 70; // 11=too slow, 13=too fast
		public static final int MAX_STEP = 50000;
		public static final int MAX_ROTATION_SPEED = 8000;
		public static final int MIN_COMMAND_INTERVAL = 100;
		private transient long time = 0;

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