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
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;

import com.phybots.entity.PhysicalResourceAbstractImpl;
import com.phybots.entity.PhysicalRobotAbstractImpl;
import com.phybots.entity.ResourceAbstractImpl;
import com.phybots.resource.Camera;
import com.phybots.resource.DifferentialWheelsAbstractImpl;

import jp.digitalmuseum.connector.Connector;
import jp.digitalmuseum.connector.SocketConnector;

/**
 * NetTansor/NetTansor Web
 *
 * @author Jun Kato
 * @see NetTansorDriver
 */
public class NetTansor extends PhysicalRobotAbstractImpl {
	private static final long serialVersionUID = 4329906174875793193L;
	public static final double RADIUS = 8;
	private static int instances = 0;
	private NetTansorDriver driver;
	private NetTansorHeadmountedCamera camera;
	private Shape shape;

	public NetTansor() {
		initialize();
	}

	public NetTansor(String connectionString) {
		super(connectionString);
		initialize();
	}

	public NetTansor(String connectionString, String name) {
		super(connectionString, name);
		initialize();
	}

	public NetTansor(Connector connector) {
		super(connector);
		initialize();
	}

	public NetTansor(Connector connector, String name) {
		super(connector, name);
		initialize();
	}

	@Override
	protected void initialize() {
		setTypeName("NetTansor");
		instances ++;
		if (getName() == null) {
			setName(getTypeName()+" ("+instances+")");
		}
		driver = new NetTansorDriver(this);
		camera = new NetTansorHeadmountedCamera(this);
		shape = new Ellipse2D.Double(-RADIUS, -RADIUS, RADIUS*2, RADIUS*2);
		super.initialize();
	}

	@Override
	public void dispose() {
		driver.stopWheels();
		super.dispose();
	}

	@Override
	protected List<ResourceAbstractImpl> getResources() {
		List<ResourceAbstractImpl> rs = super.getResources();
		rs.add(driver);
		rs.add(camera);
		return rs;
	}

	public Shape getShape() {
		return shape;
	}

	private URL getURL() throws MalformedURLException {
		return new URL("http://"+((SocketConnector) getConnector()).getHost()+"/goform/video/");
	}

	/**
	 * Differential wheels of NetTansor.
	 *
	 * @author Jun Kato
	 * @see NetTansor
	 */
	public static class NetTansorDriver extends DifferentialWheelsAbstractImpl {
		private static final long serialVersionUID = -4990542634338107040L;
		public final static int DEFAULT_SPEED = 80;
		public final static int DEFAULT_ROTATION_SPEED = 60;
		private static final int MAXIMUM_DISTANCE = 250;
		private static final int MAXIMUM_ANGLE = 360;
		private static final int MAXIMUM_POWER = 31;

		public NetTansorDriver(NetTansor netTansor) {
			super(netTansor);
			initialize();
		}

		public NetTansorDriver(Connector connector) {
			super(connector);
			initialize();
		}

		@Override
		protected void onFree() {
			stopWheels();
		}

		public int getRecommendedSpeed() {
			return DEFAULT_SPEED;
		}

		public int getRecommendedRotationSpeed() {
			return DEFAULT_ROTATION_SPEED;
		}

		protected boolean doStopWheels() {
			return write("st");
		}

		protected boolean doDrive(int leftPower, int rightPower) {
			String cmd;
			if (leftPower == rightPower) {
				if (leftPower > 0) {
					cmd = String.format("fd,%d,%d,0,0,0", getPower(leftPower), MAXIMUM_DISTANCE);
				} else {
					cmd = String.format("bk,%d,%d,0,0,0", getPower(-leftPower), MAXIMUM_DISTANCE);
				}
			}
			else if (leftPower == -rightPower) {
				if (leftPower > 0) {
					cmd = String.format("rt,%d,%d,0,0,0", getPower(leftPower), MAXIMUM_ANGLE);
				} else {
					cmd = String.format("lt,%d,%d,0,0,0", getPower(rightPower), MAXIMUM_ANGLE);
				}
			} else {
				cmd = String.format("mm,%d,%d", getPower(rightPower), getPower(leftPower));
			}
			return write(cmd);
		}

		private int getPower(int speed) {
			return speed*MAXIMUM_POWER/100;
		}

		private boolean write(String message) {
			final Connector connector = getConnector();
			return
					connector.write(message) &&
					connector.write(0x0d);
		}
	}

	public static class NetTansorHeadmountedCamera extends PhysicalResourceAbstractImpl
			implements Camera {
		private static final long serialVersionUID = -1792941087488727942L;
		private transient BufferedImage image;
		private transient long lastTime = 0;
		private URL url = null;

		public NetTansorHeadmountedCamera(NetTansor netTansor) {
			super(netTansor);
		}

		public synchronized BufferedImage getImage() {

			if (url == null) {
				try {
					url = ((NetTansor)getRobot()).getURL();
				} catch (MalformedURLException e) {
					return null;
				}
			}

			long time = System.currentTimeMillis();
			if (time - lastTime > 50) {
				try {
					image = ImageIO.read(url);
				} catch(IOException e) {
					return image;
				}
				lastTime = time;
			}
			return image;
		}
	}
}
