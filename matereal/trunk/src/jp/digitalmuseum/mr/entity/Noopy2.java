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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jp.digitalmuseum.connector.Connector;
import jp.digitalmuseum.mr.entity.PhysicalResourceAbstractImpl;
import jp.digitalmuseum.mr.entity.PhysicalRobotAbstractImpl;
import jp.digitalmuseum.mr.entity.ResourceAbstractImpl;
import jp.digitalmuseum.mr.resource.DifferentialWheelsAbstractImpl;
import jp.digitalmuseum.mr.resource.Pen;

/**
 * Noopy, a prototype robot.
 *
 * @author Jun KATO &amp; Yuta SUGIURA
 * @see NoopyWheels
 */
public class Noopy2 extends PhysicalRobotAbstractImpl {
	private static final long serialVersionUID = -5602909671919783633L;
	final public static double WIDTH = 12;
	final public static double HEIGHT = 16;
	private static int instances = 0;
	private NoopyWheels wheels;
	private Shape shape;
	private Set<NoopyExtension> extensions;

	public Noopy2() {
		super();
	}

	public Noopy2(String connectionString) {
		super(connectionString);
	}

	public Noopy2(String connectionString, String name) {
		super(connectionString, name);
	}

	public Noopy2(Connector connector) {
		super(connector);
	}

	public Noopy2(Connector connector, String name) {
		super(connector, name);
	}

	@Override
	protected void initialize() {
		setTypeName("Noopy");
		instances ++;
		if (getName() == null) {
			setName(getTypeName()+" ("+instances+")");
		}
		wheels = new NoopyWheels(this);
		shape = new RoundRectangle2D.Double(
				-WIDTH/2, -HEIGHT/2,
				WIDTH, HEIGHT,
				3, 3);
		extensions = new HashSet<NoopyExtension>();
		super.initialize();
	}

	public void addExtension(Class<? extends NoopyExtension> extensionClass) {
		try {
			extensions.add(
					extensionClass
						.getConstructor(Noopy2.class)
						.newInstance(this));
		} catch (Exception e) {
			//
		}
	}

	@Override
	public void dispose() {
		wheels.stopWheels();
		super.dispose();
	}

	@Override
	protected List<ResourceAbstractImpl> getResources() {
		List<ResourceAbstractImpl> rs = super.getResources();
		rs.add(wheels);
		rs.addAll(extensions);
		return rs;
	}

	public Shape getShape() {
		return shape;
	}

	public static class NoopyExtension extends PhysicalResourceAbstractImpl {
		private static final long serialVersionUID = -7122759624061405292L;
		protected NoopyExtension(PhysicalRobotAbstractImpl robot) {
			super(robot);
		}
	}

	public static class Accelerometer extends NoopyExtension {
		private static final long serialVersionUID = 562978326011866250L;

		public Accelerometer(Noopy2 noopy) {
			super(noopy);
		}

		public void readValues(int[] values) {
			getConnector().write("!ACCRDA");
			if (values == null || values.length < 3) {
				return;
			}
			for (int i = 0; i < 3; i ++) {
				try {
					values[i] = getConnector().readInt();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * Wheels of Noopy.
	 *
	 * @author Jun KATO
	 * @see Noopy2
	 */
	public static class NoopyWheels extends DifferentialWheelsAbstractImpl {
		private static final long serialVersionUID = 6183145475982755979L;

		public NoopyWheels(Noopy2 noopy) {
			super(noopy);
		}

		public NoopyWheels(Connector connector) {
			super(connector);
		}

		@Override
		protected void onFree() {
			stopWheels();
		}

		protected boolean doStopWheels() {
			return getConnector().write("!BRK000")
				&& getConnector().write("!STP000");
		}

		@Override
		public int getRecommendedSpeed() {
			return 50;
		}

		@Override
		public int getRecommendedRotationSpeed() {
			return 30;
		}

		@Override
		protected boolean doDrive(int leftPower, int rightPower) {
			leftPower = leftPower * 255 / 100;
			rightPower = rightPower * 255 / 100;
			if (leftPower == 0) {
				getConnector().write("!DC2BRK");
				getConnector().write("!DC2STP");
			} else if (leftPower > 0) {
				getConnector().write(String.format("!SPG%03d", leftPower));
				getConnector().write("!DC2CCW");
			} else {
				getConnector().write(String.format("!SPG%03d", -leftPower));
				getConnector().write("!DC2CLW");
			}
			if (rightPower == 0) {
				getConnector().write("!DC1BRK");
				getConnector().write("!DC1STP");
			} else 	if (rightPower > 0) {
				getConnector().write(String.format("!SPG%03d", rightPower));
				getConnector().write("!DC1CLW");
			} else {
				getConnector().write(String.format("!SPG%03d", -rightPower));
				getConnector().write("!DC1CCW");
			}
			return true;
		}
	}

	/**
	 * Pen of Noopy.
	 *
	 * @author Jun KATO
	 * @see Noopy2
	 */
	public static class NoopyPen extends PhysicalResourceAbstractImpl implements Pen {
		private static final long serialVersionUID = 529214318121894129L;
		private PenStatus penStatus;

		public NoopyPen(Noopy2 noopy) {
			super(noopy);
			initialize();
		}

		public NoopyPen(Connector connector) {
			super(connector);
			initialize();
		}

		private void initialize() {
			penStatus = PenStatus.UP;
		}

		private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
			ois.defaultReadObject();
			initialize();
		}

		@Override
		protected void onFree() {
			// endPen();
		}

		public void putPen() {
			if (penStatus == PenStatus.UP) {
				getConnector().write("u");
				penStatus = Pen.PenStatus.DOWN;
			}
		}

		public void endPen() {
			if (penStatus == PenStatus.DOWN) {
				getConnector().write("v");
				penStatus = Pen.PenStatus.UP;
			}
		}

		public PenStatus getStatus() {
			return penStatus;
		}
	}
}
