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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import com.phybots.entity.PhysicalResourceAbstractImpl;
import com.phybots.entity.PhysicalRobotAbstractImpl;
import com.phybots.entity.ResourceAbstractImpl;
import com.phybots.resource.Pen;
import com.phybots.resource.WheelsAbstractImpl;

import jp.digitalmuseum.connector.Connector;

/**
 * Noopy, a prototype robot.
 *
 * @author Jun Kato &amp; Yuta SUGIURA
 * @see NoopyWheels
 */
public class Noopy extends PhysicalRobotAbstractImpl {
	private static final long serialVersionUID = -5602909671919783633L;
	final public static double WIDTH = 12;
	final public static double HEIGHT = 16;
	private static int instances = 0;
	private NoopyWheels wheels;
	private Shape shape;

	public Noopy() {
		super();
	}

	public Noopy(String connectionString) {
		super(connectionString);
	}

	public Noopy(String connectionString, String name) {
		super(connectionString, name);
	}

	public Noopy(Connector connector) {
		super(connector);
	}

	public Noopy(Connector connector, String name) {
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
				-WIDTH/2 , -HEIGHT/2,
				WIDTH/2, HEIGHT/2,
				3, 3);
		super.initialize();
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
		return rs;
	}

	public Shape getShape() {
		return shape;
	}

	/**
	 * Wheels of Noopy.
	 *
	 * @author Jun Kato
	 * @see Noopy
	 */
	public static class NoopyWheels extends WheelsAbstractImpl {
		private static final long serialVersionUID = 6183145475982755979L;

		public NoopyWheels(Noopy noopy) {
			super(noopy);
		}

		public NoopyWheels(Connector connector) {
			super(connector);
		}

		@Override
		protected void onFree() {
			stopWheels();
		}

		protected void doGoForward() {
			getConnector().write("f\n");
		}

		protected void doGoBackward() {
			getConnector().write("b\n");
		}

		protected void doSpinLeft() {
			getConnector().write("j\n");
		}

		protected void doSpinRight() {
			getConnector().write("y\n");
		}

		protected void doStopWheels() {
			getConnector().write("s\n");
		}
	}

	/**
	 * Pen of Noopy.
	 *
	 * @author Jun Kato
	 * @see Noopy
	 */
	public static class NoopyPen extends PhysicalResourceAbstractImpl implements Pen {
		private static final long serialVersionUID = 529214318121894129L;
		private PenStatus penStatus;

		public NoopyPen(Noopy noopy) {
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
