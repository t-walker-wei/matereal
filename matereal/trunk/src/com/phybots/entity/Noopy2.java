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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.phybots.entity.ExclusiveResource;
import com.phybots.entity.PhysicalResourceAbstractImpl;
import com.phybots.entity.PhysicalRobotAbstractImpl;
import com.phybots.entity.ResourceAbstractImpl;
import com.phybots.resource.DifferentialWheelsAbstractImpl;

import jp.digitalmuseum.connector.Connector;

/**
 * Noopy v.2, a prototype robot.
 *
 * @author Jun Kato, Charith Fernando &amp; Yuta Sugiura
 * @see NoopyWheels
 */
public class Noopy2 extends PhysicalRobotAbstractImpl {
	private static final long serialVersionUID = -5602909671919783633L;

	final public static double WIDTH = 12;
	final public static double HEIGHT = 16;

	public static enum PortType {
		DC, AN, SERVO
	}

	public static enum Port {
		DC1, DC2, DC3, DC4, AN0, AN1, AN2, AN3, AN4, AN5,
		SERVO1, SERVO2, SERVO3, SERVO4
	}

	private static int instances = 0;
	private NoopyWheels wheels;
	private Shape shape;
	private List<NoopyExtension> extensions;

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
				-HEIGHT*3/4, -WIDTH/2,
				HEIGHT, WIDTH,
				3, 3);
		extensions = new ArrayList<NoopyExtension>();
		super.initialize();
	}

	public boolean addExtension(String className) {
		try {
			Class<?> extensionClass = Class.forName(String.format("%s$%s",
					getClass().getName(),
					className));
			if (NoopyExtension.class.isAssignableFrom(extensionClass)) {
				extensions.add(
						(NoopyExtension)
						extensionClass
							.getConstructor(Noopy2.class)
							.newInstance(this));
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public boolean addExtension(String className, Port port) {
		try {
			Class<?> extensionClass = Class.forName(String.format("%s$%s",
					getClass().getName(),
					className));
			if (NoopyExtension.class.isAssignableFrom(extensionClass)) {
				extensions.add(
						(NoopyExtension)
						extensionClass
							.getConstructor(Noopy2.class, Port.class)
							.newInstance(this, port));
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public boolean addExtension(Class<? extends NoopyExtension> extensionClass) {
		try {
			extensions.add(
					extensionClass
						.getConstructor(Noopy2.class)
						.newInstance(this));
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public boolean addExtension(Class<? extends NoopyExtension> extensionClass, Port port) {
		try {
			extensions.add(
					extensionClass
						.getConstructor(Noopy2.class, Port.class)
						.newInstance(this, port));
		} catch (Exception e) {
			return false;
		}
		return true;
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

	private String writeCommand(String command) {
		getConnector().write(command);
		return receiveResponse();
	}

	private String receiveResponse() {
		if (getConnector() == null ||
				!getConnector().isConnected()) {
			return null;
		}
		InputStream is = getConnector().getInputStream();
		StringBuffer sb = new StringBuffer();
		byte[] data = new byte[64];
		try {
			boolean cr = false;
			while (!cr) {
				if (!wait(is)) {
					return null;
				}
				int read = is.read(data);
				for (int i = 0; i < read; i ++) {
					cr |= data[i] == '\r';
					if (!cr) {
						sb.append((char) data[i]);
					}
				}
			}
		} catch (IOException e) {
			return null;
		}
		return sb.toString();
	}

	/**
	 * Wait for a moment (max 100ms)
	 */
	private boolean wait(InputStream is) throws IOException {
		int count = 0;
		while (is.available() <= 0 && count < 10) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				count = 9;
				break;
			}
			count ++;
		}
		return count < 10;
	}

	/**
	 * Wheels of Noopy.
	 *
	 * @author Jun Kato
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
			boolean success =
					getRobot().writeCommand("!BRK000") != null;
			if (success) {
				success &=
					getRobot().writeCommand("!STP000") != null;
			}
			return success;
		}

		public int getRecommendedSpeed() {
			return 50;
		}

		public int getRecommendedRotationSpeed() {
			return 30;
		}

		@Override
		protected boolean doDrive(int leftPower, int rightPower) {
			leftPower = leftPower * 255 / 100;
			rightPower = rightPower * 255 / 100;
			if (leftPower == 0) {
				getRobot().writeCommand("!DC2BRK");
				getRobot().writeCommand("!DC4BRK");
				getRobot().writeCommand("!DC2STP");
				getRobot().writeCommand("!DC4STP");
			} else if (leftPower > 0) {
				getRobot().writeCommand(String.format("!SPG%03d", leftPower));
				getRobot().writeCommand("!DC2CCW");
				getRobot().writeCommand("!DC4CCW");
			} else {
				getRobot().writeCommand(String.format("!SPG%03d", -leftPower));
				getRobot().writeCommand("!DC2CLW");
				getRobot().writeCommand("!DC4CLW");
			}
			if (rightPower == 0) {
				getRobot().writeCommand("!DC1BRK");
				getRobot().writeCommand("!DC3BRK");
				getRobot().writeCommand("!DC1STP");
				getRobot().writeCommand("!DC3STP");
			} else 	if (rightPower > 0) {
				getRobot().writeCommand(String.format("!SPG%03d", rightPower));
				getRobot().writeCommand("!DC1CLW");
				getRobot().writeCommand("!DC3CLW");
			} else {
				getRobot().writeCommand(String.format("!SPG%03d", -rightPower));
				getRobot().writeCommand("!DC1CCW");
				getRobot().writeCommand("!DC3CCW");
			}
			return true;
		}

		public Noopy2 getRobot() {
			return (Noopy2) super.getRobot();
		}
	}

	public static class NoopyExtension extends PhysicalResourceAbstractImpl {
		private static final long serialVersionUID = -7122759624061405292L;
		private Port port;

		protected NoopyExtension(Noopy2 robot) {
			this(robot, null);
		}

		protected NoopyExtension(Noopy2 robot, Port port) {
			super(robot);
			this.port = port;
		}

		protected Port getPort() {
			return port;
		}

		protected int getPortNumber() {
			switch (port) {
			case AN0:
				return 0;
			case SERVO1:
			case DC1:
			case AN1:
				return 1;
			case SERVO2:
			case DC2:
			case AN2:
				return 2;
			case SERVO3:
			case DC3:
			case AN3:
				return 3;
			case SERVO4:
			case DC4:
			case AN4:
				return 4;
			case AN5:
				return 5;
			}
			return -1;
		}

		protected PortType getPortType() {
			switch (port) {
			case AN0:
			case AN1:
			case AN2:
			case AN3:
			case AN4:
			case AN5:
				return PortType.AN;
			case DC1:
			case DC2:
			case DC3:
			case DC4:
				return PortType.DC;
			case SERVO1:
			case SERVO2:
			case SERVO3:
			case SERVO4:
				return PortType.SERVO;
			}
			return null;
		}

		public Noopy2 getRobot() {
			return (Noopy2) super.getRobot();
		}
	}

	public static class AnalogSensor extends NoopyExtension {
		private static final long serialVersionUID = 4871163102057973044L;
		Pattern pattern = Pattern.compile("AN[0-5]:(\\s*)([0-9]+)");

		public AnalogSensor(Noopy2 noopy, Port port) {
			super(noopy, port);
			if (port == null || getPortType() != PortType.AN) {
				throw new IllegalArgumentException("Analog sensor must be plugged into analog ports.");
			}
		}

		/**
		 * @return 0-1024 (-1 when error occurred.)
		 */
		public int readValue() {
			String result = getRobot().writeCommand(
					String.format("!AN%d?RD\r", getPortNumber()));
			if (result != null) {
				Matcher matcher = pattern.matcher(result);
				if (matcher.matches()) {
					try {
						return Integer.parseInt(matcher.group(2));
					} catch (NumberFormatException e) {
						//
					}
				}
			}
			return -1;
		}
	}

	public static class Accelerometer extends NoopyExtension {
		private static final long serialVersionUID = 562978326011866250L;
		Pattern pattern = Pattern.compile("(.+)-X: ([0-9.\\-]+)mg, Y: ([0-9.\\-]+)mg, Z: ([0-9.\\-]+)mg");

		public Accelerometer(Noopy2 noopy) {
			super(noopy);
		}

		public void readValues(float[] values) {
			String result = getRobot().writeCommand("!ACCRDA");
			if (result != null) {
				Matcher matcher = pattern.matcher(result);
				if (matcher.matches()) {
					for (int i = 0; i < values.length && i + 2 < matcher.groupCount() + 1; i ++) {
						try {
							values[i] = Float.parseFloat(matcher.group(i + 2));
						} catch (NumberFormatException e) {
							values[i] = 0f;
						}
					}
				}
			}
		}
	}

	public static class DistanceSensor extends NoopyExtension {
		private static final long serialVersionUID = 8750827824117254956L;
		Pattern pattern = Pattern.compile("(.+)distance:(\\s*)([0-9.\\-]+)mm");

		public DistanceSensor(Noopy2 noopy) {
			super(noopy);
			/*
			if (port == null || getPortType() != PortType.AN) {
				throw new IllegalArgumentException("Distance sensor must be plugged into analog ports.");
			}
			*/
		}

		public float readValue() {
			String result = getRobot().writeCommand("!DIS?RD");
			if (result != null) {
				Matcher matcher = pattern.matcher(result);
				if (matcher.matches()) {
					try {
						return Float.parseFloat(matcher.group(3));
					} catch (NumberFormatException e) {
						//
					}
				}
			}
			return .0f;
		}
	}

	public static class BendSensor extends NoopyExtension {
		private static final long serialVersionUID = 8750827824117254956L;
		Pattern pattern = Pattern.compile("(.+)Bend:(\\s*)([0-9.\\-]+)V");

		public BendSensor(Noopy2 noopy) {
			super(noopy);
			/*
			if (port == null || getPortType() != PortType.AN) {
				throw new IllegalArgumentException("Distance sensor must be plugged into analog ports.");
			}
			*/
		}

		public float readValue() {
			String result = getRobot().writeCommand("!BND?RD");
			if (result != null) {
				Matcher matcher = pattern.matcher(result);
				if (matcher.matches()) {
					try {
						return Float.parseFloat(matcher.group(3));
					} catch (NumberFormatException e) {
						//
					}
				}
			}
			return .0f;
		}
	}

	public static class DCMotorController extends NoopyExtension implements ExclusiveResource {
		private static final long serialVersionUID = -6360472332679514946L;
		private int speed = 100;
		private Command lastCommand;

		public static enum Command {
			CLW, CCW, STP, BRK
		}

		public DCMotorController(Noopy2 noopy, Port port) {
			super(noopy, port);
			if (port == null || getPortType() != PortType.DC) {
				throw new IllegalArgumentException("DC motor must be plugged into DC motor ports.");
			}
			lastCommand = Command.STP;
		}

		@Override
		protected void onFree() {
			stop();
			super.onFree();
		}

		public void clw() {
			drive(false);
		}

		public void ccw() {
			drive(true);
		}

		public void drive() {
			drive(false);
		}

		public void drive(boolean counterClockWise) {
			drive(counterClockWise ? Command.CCW : Command.CLW);
		}

		public void drive(Command command) {
			drive(command, speed);
		}

		public void drive(Command command, int speed) {
			if (command != Command.STP &&
					command != Command.BRK) {

				// TODO This will be implemented in the next Noopy firmware release.
				// System.out.println(String.format("!SP%d%03d\r", getPortNumber(), speed));

				getRobot().writeCommand(String.format("!SPG%03d\r", speed));
			}
			getRobot().writeCommand(String.format("!DC%d%s\r", getPortNumber(), String.valueOf(command)));
			lastCommand = command;
		}

		public void stop() {
			if (lastCommand != Command.BRK
					|| lastCommand != Command.STP) {
				drive(Command.BRK);
				drive(Command.STP);
				lastCommand = Command.STP;
			}
		}

		/**
		 * @param speed Speed (0-255)
		 */
		public void setSpeed(int speed) {
			this.speed = speed;
			drive(lastCommand);
		}

		public int getSpeed() {
			return speed;
		}

		public Command getStatus() {
			return lastCommand;
		}
	}

	public static class ServoMotorController extends NoopyExtension implements ExclusiveResource {
		private static final long serialVersionUID = 9153219675206344713L;
		private int speed = 100;
		private Pattern pattern = Pattern.compile("SERVO_MOTOR[0-9]:([0-9]+)");

		public ServoMotorController(Noopy2 noopy, Port port) {
			super(noopy, port);
			if (port == null || getPortType() != PortType.SERVO) {
				throw new IllegalArgumentException("Servo motor must be plugged into servo motor ports.");
			}
		}

		@Override
		protected void onFree() {
			stop();
			super.onFree();
		}

		public void drive(int angle) {
			getRobot().writeCommand(String.format("!SPG%03d\r", speed));
			getRobot().writeCommand(String.format("!SV%d%03d\r", getPortNumber(), angle));
		}

		/**
		 * @return 0-360 (-1 when error occurred.)
		 */
		public int getAngle() {
			Matcher matcher = pattern.matcher(getRobot().writeCommand(
					String.format("!SV%d%03d\r", getPortNumber(), 0)));
			try {
				if (matcher.matches()) {
					return Integer.parseInt(matcher.group(1));
				}
			} catch (NumberFormatException e) {
				//
			}
			return -1;
		}

		public void stop() {
			drive(0);
		}

		/**
		 * @param speed Speed (0-255)
		 */
		public void setSpeed(int speed) {
			this.speed = speed;
			if (speed == 0) {
				stop();
			}
		}

		public int getSpeed() {
			return speed;
		}
	}
}
