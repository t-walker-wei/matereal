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
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;

import jp.digitalmuseum.connector.Connector;
import jp.digitalmuseum.connector.SocketConnector;
import jp.digitalmuseum.mr.entity.PhysicalResourceAbstractImpl;
import jp.digitalmuseum.mr.entity.PhysicalRobotAbstractImpl;
import jp.digitalmuseum.mr.entity.ResourceAbstractImpl;
import jp.digitalmuseum.mr.resource.Camera;
import jp.digitalmuseum.mr.resource.DifferentialWheelsAbstractImpl;

/**
 * NetTansor/NetTansor Web
 *
 * @author Jun KATO
 * @see NetTansorDriver
 */
public class NetTansor extends PhysicalRobotAbstractImpl {
	final public double RADIUS = 8;
	private NetTansorDriver driver;
	private NetTansorHeadmountedCamera camera;
	private Shape shape;

	public NetTansor(String name) {
		super();
		initialize(name);
	}

	public NetTansor(String name, String connectionString) {
		super(connectionString);
		initialize(name);
	}

	public NetTansor(String name, Connector connector) {
		super(connector);
		initialize(name);
	}

	@Override
	public void dispose() {
		driver.stopWheels();
		super.dispose();
	}

	private void initialize(String name) {
		setName(name);
		setTypeName("NetTansor");
		driver = new NetTansorDriver(this);
		camera = new NetTansorHeadmountedCamera(this);
		shape = new Ellipse2D.Double(-RADIUS, -RADIUS, RADIUS*2, RADIUS*2);
	}

	@Override
	protected List<ResourceAbstractImpl> getResources() {
		List<ResourceAbstractImpl> rs = super.getResources();
		rs.add(driver);
		rs.add(camera);
		return rs;
	}

	private URL getURL() throws MalformedURLException {
		return new URL("http://"+((SocketConnector) getConnector()).getHost()+"/goform/video/");
	}

	public Shape getShape() {
		return shape;
	}

	/**
	 * Differential wheels of NetTansor.
	 *
	 * @author Jun KATO
	 * @see NetTansor
	 */
	public static class NetTansorDriver extends DifferentialWheelsAbstractImpl {
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
		private BufferedImage image;
		private long lastTime = 0;
		private URL url;

		public NetTansorHeadmountedCamera(final NetTansor netTansor) {
			super(netTansor);
			try {
				url = ((NetTansor)getRobot()).getURL();
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException(e);
			}
		}

		public synchronized BufferedImage getImage() {
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
