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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.RoundRectangle2D;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import com.phybots.Phybots;
import com.phybots.entity.ExclusiveResource;
import com.phybots.entity.PhysicalResourceAbstractImpl;
import com.phybots.entity.PhysicalRobotAbstractImpl;
import com.phybots.entity.ResourceAbstractImpl;
import com.phybots.resource.Battery;
import com.phybots.resource.DifferentialWheelsAbstractImpl;

import jp.digitalmuseum.connector.BluetoothConnector;
import jp.digitalmuseum.connector.Connector;

/**
 * LEGO Mindstorms NXT
 *
 * @author Jun Kato
 * @see MindstormsNXTDifferentialWheels
 */
public class MindstormsNXT extends PhysicalRobotAbstractImpl {
	private static final long serialVersionUID = -2489251030390060500L;

	final public static double WIDTH = 14;
	final public static double HEIGHT = 14;

	public static enum Port {
		A(0), B(1), C(2), D(3), ALL(0xff);
		private int portNumber;
		private Port(int portNumber) {
			this.portNumber = portNumber;
		}
		public int getPortNumber() {
			return portNumber;
		}
		public static Port get(int portNumber) {
			for (Port port : EnumSet.allOf(Port.class)) {
				if (port.getPortNumber() == portNumber) {
					return port;
				}
			}
			return null;
		}
	}

	public static final byte TACHO_FOREVER = 0;

	private static int instances = 0;
	private MindstormsNXTDifferentialWheels dw;
	private MindstormsNXTBattery b;
	private Shape shape;
	private List<MindstormsNXTExtension> extensions;

	public MindstormsNXT() {
		super();
	}

	public MindstormsNXT(String connectionString) {
		super(connectionString);
	}

	public MindstormsNXT(String connectionString, String name) {
		super(connectionString, name);
	}

	public MindstormsNXT(Connector connector) {
		super(connector);
	}

	public MindstormsNXT(Connector connector, String name) {
		super(connector, name);
	}

	@Override
	protected void initialize() {
		setTypeName("LEGO MindstormsNXT");
		instances ++;
		if (getName() == null) {
			setName(getTypeName()+" ("+instances+")");
		}
		dw = new MindstormsNXTDifferentialWheels(this, Port.C, Port.B);
		b = new MindstormsNXTBattery(this);
		shape = new RoundRectangle2D.Double(
				-HEIGHT/2, -WIDTH/2,
				HEIGHT, WIDTH,
				3, 3);
		extensions = new ArrayList<MindstormsNXTExtension>();
		super.initialize();
	}

	@Override
	public void dispose() {
		if (dw != null) {
			dw.stopWheels();
		}
		super.dispose();
	}

	@Override
	protected List<ResourceAbstractImpl> getResources() {
		List<ResourceAbstractImpl> rs = super.getResources();
		if (dw != null) {
			rs.add(dw);
		}
		rs.addAll(extensions);
		rs.add(b);
		return rs;
	}

	public Shape getShape() {
		return shape;
	}

	protected static boolean drive(final int port,
			final byte power, final int mode,
			final Connector connector) {
		return setOutputState(port, power, mode,
				REGULATION_MODE_MOTOR_SPEED, 0,
				MOTOR_RUN_STATE_RUNNING, TACHO_FOREVER,
				connector);
	}

	public void removeDifferentialWheels() {
		dw = null;
	}

	public void addDifferentialWheels(Port leftWheelPort, Port rightWheelPort) {
		dw = new MindstormsNXTDifferentialWheels(this, leftWheelPort, rightWheelPort);
	}

	public boolean addExtension(String className, Port port) {
		try {
			Class<?> extensionClass = Class.forName(String.format("%s$%s",
					getClass().getName(),
					className));
			if (MindstormsNXTExtension.class.isAssignableFrom(extensionClass)) {
				addExtension(
						(MindstormsNXTExtension)
						extensionClass
							.getConstructor(MindstormsNXT.class, Port.class)
							.newInstance(this, port));
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public boolean addExtension(Class<? extends MindstormsNXTExtension> extensionClass, Port port) {
		try {
			addExtension(
					extensionClass
						.getConstructor(MindstormsNXT.class, Port.class)
						.newInstance(this, port));
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public void addExtension(MindstormsNXTExtension extension) {
		extensions.add(extension);
	}

	protected boolean setOutputState(OutputState outputState) {
		return setOutputState(outputState.port,
				outputState.powerSetpoint, outputState.mode,
				outputState.regulationMode, outputState.turnRatio,
				outputState.runState, outputState.tachoLimit);
	}

	protected static boolean setOutputState(OutputState outputState, Connector connector) {
		return setOutputState(outputState.port,
				outputState.powerSetpoint, outputState.mode,
				outputState.regulationMode, outputState.turnRatio,
				outputState.runState, outputState.tachoLimit,
				connector);
	}

	/**
	 * @see #setOutputState(int, byte, int, int, int, int, int, Connector)
	 */
	protected boolean setOutputState(final int port,
			final byte power, final int mode,
			final int regulationMode, final int turnRatio,
			final int runState, final long tachoLimit) {
		return setOutputState(port,
				power, mode,
				regulationMode, turnRatio,
				runState, tachoLimit,
				getConnector());
	}

	/**
	 * @param port - Output port (0 - 2 or 0xFF for all three)
	 * @param power - Set point for power. (-100 to 100)
	 * @param mode - Setting the modes MOTORON, BRAKE, and/or REGULATED. This parameter is a bitfield, so to put it in brake mode and regulated, use BRAKEMODE + REGULATED
	 * @param regulationMode - see NXTProtocol for enumerations
	 * @param turnRatio - Need two motors? (-100 to 100)
	 * @param runState - see NXTProtocol for enumerations
	 * @param tachoLimit - Number of degrees(?) to rotate before stopping.
	 */
	protected static boolean setOutputState(final int port,
			final byte power, final int mode,
			final int regulationMode, final int turnRatio,
			final int runState, final long tachoLimit,
			final Connector connector) {
		if (connector == null) {
			return false;
		}
		synchronized (connector) {
			return write(new byte[] {
					DIRECT_COMMAND_NOREPLY,
					SET_OUTPUT_STATE,
					(byte)port,
					power,
					(byte)mode,
					(byte)regulationMode,
					(byte)turnRatio,
					(byte)runState,
					(byte)tachoLimit,
					(byte)(tachoLimit>>>8),
					(byte)(tachoLimit>>>16),
					(byte)(tachoLimit>>>24)}, connector);
		}
	}

	/**
	 * @see #getOutputState(int, Connector)
	 */
	public OutputState getOutputState(final int port) {
		return getOutputState(port, getConnector());
	}

	/**
	 * @see #getOutputState(int, Connector, OutputState)
	 */
	public void getOutputStateOut(final int port, OutputState outputState) {
		getOutputState(port, getConnector(), outputState);
	}

	/**
	 * Retrieves the current output state for a port.
	 * @param port - 0 to 3
	 * @return OutputState - returns a container object for output state variables.
	 */
	public static OutputState getOutputState(final int port, final Connector connector) {
		final OutputState outputState = new OutputState(port);
		getOutputState(port, connector, outputState);
		return outputState;
	}

	/**
	 * Retrieves the current output state for a port.
	 * @param port 0 to 3
	 * @param outputState a container object for output state variables.
	 */
	public static void getOutputState(final int port, final Connector connector,
			final OutputState outputState) {
		if (connector == null) {
			return;
		}

		byte[] ret;
		synchronized (connector) {
			if (!write(new byte[] {
							DIRECT_COMMAND_REPLY,
							GET_OUTPUT_STATE,
							(byte)port
					}, connector)) {
				return;
			}

			// Wait for the latency and read the response.
			try {
				Thread.sleep(30);
				ret = read(connector);
			} catch (Exception e) {
				outputState.status = -1;
				return;
			}
		}

		if(ret == null || ret.length < 25 || ret[1] != GET_OUTPUT_STATE) {
			outputState.status = -1;
			return;
		}

		outputState.status = ret[2];
		outputState.port = ret[3];
		outputState.powerSetpoint = ret[4];
		outputState.mode = ret[5];
		outputState.regulationMode = ret[6];
		outputState.turnRatio = ret[7];
		outputState.runState = ret[8];
		outputState.tachoLimit = (0xFF & ret[9]) | ((0xFF & ret[10]) << 8)| ((0xFF & ret[11]) << 16)| ((0xFF & ret[12]) << 24);
		outputState.tachoCount = (0xFF & ret[13]) | ((0xFF & ret[14]) << 8)| ((0xFF & ret[15]) << 16)| ((0xFF & ret[16]) << 24);
		outputState.blockTachoCount = (0xFF & ret[17]) | ((0xFF & ret[18]) << 8)| ((0xFF & ret[19]) << 16)| ((0xFF & ret[20]) << 24);
		outputState.rotationCount = (0xFF & ret[21]) | ((0xFF & ret[22]) << 8)| ((0xFF & ret[23]) << 16)| ((0xFF & ret[24]) << 24);
	}

	public boolean sendAck() {
		return sendAck(getConnector());
	}

	public static boolean sendAck(final Connector connector) {
		if (connector == null) {
			return false;
		}

		byte[] ret;
		synchronized (connector) {
			if (!write(new byte[] {
							SYSTEM_COMMAND_REPLY,
							GET_FIRMWARE_VERSION
					}, connector)) {
				return false;
			}

			// Wait for the latency and read the response.
			try {
				Thread.sleep(30);
				ret = read(connector);
			} catch (Exception e) {
				return false;
			}
		}

		// Get the result.
		if (0 >= ret[2]) {
			Phybots.getInstance().getOutStream().println(
					"NXT Firmware version: "
					+ ret[4] + "." + ret[3]
					+ ", " + ret[6] + "." + ret[5]);
		}
		return true;
	}

	protected static boolean write(final byte[] buf, final Connector connector) {
		if (connector == null) {
			return false;
		}
		if (connector instanceof BluetoothConnector) {
			final byte LSB = (byte) buf.length;
			final byte MSB = (byte) (buf.length >>> 8);
			if (!(connector.write(LSB) &&
					connector.write(MSB))) {
				return false;
			}
		}
		return connector.write(buf);
	}

	protected static byte[] read(final Connector connector) throws IOException {
		final InputStream inStream = connector.getInputStream();
		byte[] reply = null;
		int length = -1;
		length = (inStream.read() & 0xff) | ((inStream.read() & 0xff << 8));
		reply = new byte[length];
		while (length > 0) {
			length -= inStream.read(reply,
					reply.length-length, length);
		}
		return reply;
	}

	public Port getLeftWheelPort() {
		return dw.getLeftWheelPort();
	}

	public Port getRightWheelPort() {
		return dw.getRightWheelPort();
	}

	public void setLeftWheelPort(Port leftWheelPort) {
		dw.setLeftWheelPort(leftWheelPort);
	}

	public void setRightWheelPort(Port rightWheelPort) {
		dw.setRightWheelPort(rightWheelPort);
	}

	public static class MindstormsNXTBattery extends PhysicalResourceAbstractImpl implements Battery {
		private static final long serialVersionUID = -7810540994535818261L;

		public MindstormsNXTBattery(MindstormsNXT mindstormsNXT) {
			super(mindstormsNXT);
		}

		public int getBatteryLevel() {
			if (write(new byte[] {
					DIRECT_COMMAND_REPLY,
					GET_BATTERY_LEVEL}, getConnector())) {

				// Wait for ~100ms latency.
				try { Thread.sleep(100); }
				catch (InterruptedException e) { }

				// Get the result.
				byte[] ret;
				try {
					ret = read(getConnector());
					if (ret != null &&
							ret.length == 5 &&
							ret[1] == GET_BATTERY_LEVEL &&
							ret[2] == 0) {
						int batteryLevel = ((0xff & ret[3]) | (((0xff & ret[4]) << 8))) / 90 /* * 100 / 9000 */;
						if (batteryLevel < 0) {
							return 0;
						} else if (batteryLevel > 100) {
							return 100;
						}
						return batteryLevel;
					}
				} catch (IOException e) {
					// Do nothing.
				}
			}
			return 0;
		}
	}

	public static class MindstormsNXTExtension extends PhysicalResourceAbstractImpl implements ExclusiveResource {
		private static final long serialVersionUID = 1657289083336290551L;
		private Port port;
		private OutputState latestOutputState;
		private Set<JLabel> labels = new HashSet<JLabel>();

		public MindstormsNXTExtension(MindstormsNXT mindstormsNXT, Port port) {
			super(mindstormsNXT);
			this.port = port;
		}

		public MindstormsNXTExtension(Connector connector, Port port) {
			super(connector);
			this.port = port;
		}

		public Port getPort() {
			return port;
		}

		/**
		 * @see MindstormsNXT#setOutputState(OutputState)
		 */
		public boolean setOutputState(OutputState outputState) {
			if (outputState.port != port.getPortNumber()) {
				return false;
			}
			return MindstormsNXT.setOutputState(outputState,
					getConnector());
		}

		/**
		 * @see MindstormsNXT#setOutputState(int, byte, int, int, int, int, int, Connector)
		 */
		public boolean setOutputState(
				byte power,
				int mode,
				int regulationMode,
				int turnRatio,
				int runState,
				int tachoLimit) {
			return MindstormsNXT.setOutputState(
					port.getPortNumber(), power, mode,
					regulationMode, turnRatio,
					runState, tachoLimit,
					getConnector());
		}

		/**
		 * @see MindstormsNXT#getOutputState(int, Connector)
		 */
		public OutputState getOutputState() {
			latestOutputState = MindstormsNXT.getOutputState(
					port.getPortNumber(),
					getConnector());
			final String text = String.format("Rotation count: %d", latestOutputState.rotationCount);
			if (labels.size() > 0) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						for (JLabel label : labels) {
							label.setText(text);
						}
					}
				});
			}
			return latestOutputState;
		}

		/**
		 * This component is updated every time {{@link #getOutputState()} is called.
		 */
		@Override
		public JComponent getConfigurationComponent() {
			final JLabel label = new JLabel() {
				private static final long serialVersionUID = -1821483937025749491L;
				@Override
				public String toString() {
					return MindstormsNXTExtension.this.toString();
				}
			};
			label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			label.setFont(Phybots.getInstance().getDefaultFont());
			label.setText("Connecting to the Mindstorms NXT brick...");
			label.addComponentListener(new ComponentListener() {
				public void componentShown(ComponentEvent e) {
					labels.add(label);
				}
				public void componentResized(ComponentEvent e) {
					labels.add(label);
				}
				public void componentMoved(ComponentEvent e) {
					labels.add(label);
				}
				public void componentHidden(ComponentEvent e) {
					labels.remove(label);
				}
			});
			return label;
		}
	}

	/**
	 * Differential wheels of LEGO Mindstorms NXT.
	 *
	 * @author Jun Kato
	 * @see MindstormsNXT
	 */
	public static class MindstormsNXTDifferentialWheels extends DifferentialWheelsAbstractImpl {
		private static final long serialVersionUID = 3164832272022311974L;
		public final static int DEFAULT_SPEED = 20;
		public final static int DEFAULT_ROTATION_SPEED = 10;
		private Port leftWheelPort;
		private Port rightWheelPort;

		public MindstormsNXTDifferentialWheels(MindstormsNXT mindstormsNXT) {
			this(mindstormsNXT, Port.C, Port.B);
		}

		public MindstormsNXTDifferentialWheels(MindstormsNXT mindstormsNXT, Port leftWheelPort, Port rightWheelPort) {
			super(mindstormsNXT);
			this.leftWheelPort = leftWheelPort;
			this.rightWheelPort = rightWheelPort;
		}

		public MindstormsNXTDifferentialWheels(Connector connector) {
			this(connector, Port.C, Port.B);
		}

		public MindstormsNXTDifferentialWheels(Connector connector, Port leftWheelPort, Port rightWheelPort) {
			super(connector);
			this.leftWheelPort = leftWheelPort;
			this.rightWheelPort = rightWheelPort;
		}

		@Override
		protected void initialize() {
			super.initialize();
			sendAck(getConnector());
		}

		public Port getLeftWheelPort() {
			return leftWheelPort;
		}

		public void setLeftWheelPort(Port leftWheelPort) {
			this.leftWheelPort = leftWheelPort;
		}

		public Port getRightWheelPort() {
			return rightWheelPort;
		}

		public void setRightWheelPort(Port rightWheelPort) {
			this.rightWheelPort = rightWheelPort;
		}

		public int getRecommendedRotationSpeed() {
			return DEFAULT_ROTATION_SPEED;
		}

		public int getRecommendedSpeed() {
			return DEFAULT_SPEED;
		}

		protected boolean doDrive(int leftPower, int rightPower) {
			final Connector connector = getConnector();
			return
				MindstormsNXT.drive(leftWheelPort.getPortNumber(),
						(byte)leftPower, MOTORON + REGULATED,
						connector) &&
				MindstormsNXT.drive(rightWheelPort.getPortNumber(),
						(byte)rightPower, MOTORON + REGULATED,
						connector);
		}

		protected boolean doStopWheels() {
			final Connector connector = getConnector();
			return
				MindstormsNXT.drive(leftWheelPort.getPortNumber(),
						(byte)0, BRAKE + MOTORON + REGULATED,
						connector) &&
				MindstormsNXT.drive(rightWheelPort.getPortNumber(),
						(byte)0, BRAKE + MOTORON + REGULATED,
						connector);
		}
	}

	// ---
	// Constants below are copied from iCommand,
	// an open-source Java package to control NXT over Bluetooth connection.
	//
	// For detail, see http://lejos.sourceforge.net/p_technologies/nxt/icommand/icommand.php
	// (though its development has been already suspended.)

	// Command types constants. Indicates type of packet being sent or received.
	public static byte DIRECT_COMMAND_REPLY = 0x00;
	public static byte SYSTEM_COMMAND_REPLY = 0x01;
	public static byte REPLY_COMMAND = 0x02;
	public static byte DIRECT_COMMAND_NOREPLY = (byte)0x80; // Avoids ~100ms latency
	public static byte SYSTEM_COMMAND_NOREPLY = (byte)0x81; // Avoids ~100ms latency

	// System Commands:
	public static byte OPEN_READ = (byte)0x80;
	public static byte OPEN_WRITE = (byte)0x81;
	public static byte READ = (byte)0x82;
	public static byte WRITE = (byte)0x83;
	public static byte CLOSE = (byte)0x84;
	public static byte DELETE = (byte)0x85;
	public static byte FIND_FIRST = (byte)0x86;
	public static byte FIND_NEXT = (byte)0x87;
	public static byte GET_FIRMWARE_VERSION = (byte)0x88;
	public static byte OPEN_WRITE_LINEAR = (byte)0x89;
	public static byte OPEN_READ_LINEAR = (byte)0x8A;
	public static byte OPEN_WRITE_DATA = (byte)0x8B;
	public static byte OPEN_APPEND_DATA = (byte)0x8C;
	// Many commands could be hidden between 0x8D and 0x96!
	public static byte BOOT = (byte)0x97;
	public static byte SET_BRICK_NAME = (byte)0x98;
	// public static byte MYSTERY_COMMAND = (byte)0x99;
	// public static byte MYSTERY_COMMAND = (byte)0x9A;
	public static byte GET_DEVICE_INFO = (byte)0x9B;
	// commands could be hidden here...
	public static byte DELETE_USER_FLASH = (byte)0xA0;
	public static byte POLL_LENGTH = (byte)0xA1;
	public static byte POLL = (byte)0xA2;

	// Poll constants:
	public static byte POLL_BUFFER = (byte)0x00;
	public static byte HIGH_SPEED_BUFFER = (byte)0x01;

	// Direct Commands
	public static byte START_PROGRAM = 0x00;
	public static byte STOP_PROGRAM = 0x01;
	public static byte PLAY_SOUND_FILE = 0x02;
	public static byte PLAY_TONE = 0x03;
	public static byte SET_OUTPUT_STATE = 0x04;
	public static byte SET_INPUT_MODE = 0x05;
	public static byte GET_OUTPUT_STATE = 0x06;
	public static byte GET_INPUT_VALUES = 0x07;
	public static byte RESET_SCALED_INPUT_VALUE = 0x08;
	public static byte MESSAGE_WRITE = 0x09;
	public static byte RESET_MOTOR_POSITION = 0x0A;
	public static byte GET_BATTERY_LEVEL = 0x0B;
	public static byte STOP_SOUND_PLAYBACK = 0x0C;
	public static byte KEEP_ALIVE = 0x0D;
	public static byte LS_GET_STATUS = 0x0E;
	public static byte LS_WRITE = 0x0F;
	public static byte LS_READ = 0x10;
	public static byte GET_CURRENT_PROGRAM_NAME = 0x11;
	// public static byte MYSTERY_OPCODE = 0x12; // ????
	public static byte MESSAGE_READ = 0x13;
	// public static byte POSSIBLY_MORE_HIDDEN = 0x14; // ????

	// Custom leJOS NXJ commands:
	// public static byte NXJ_DISCONNECT = 0x20;
	// public static byte NXJ_DEFRAG = 0x21;

	// Output state constants
	// Mode:
	/** Turn on the specified motor */
	public static byte MOTORON = 0x01;
	/** Use run/brake instead of run/float in PWM */
	public static byte BRAKE = 0x02;
	/** Turns on the regulation */
	public static byte REGULATED = 0x04;

	// Regulation Mode:
	/** No regulation will be enabled */
	public static byte REGULATION_MODE_IDLE = 0x00;
	/** Power control will be enabled on specified output */
	public static byte REGULATION_MODE_MOTOR_SPEED = 0x01;
	/** Synchronization will be enabled (Needs enabled on two output) */
	public static byte REGULATION_MODE_MOTOR_SYNC = 0x02;

	// RunState:
	/** Error */
	public static byte MOTOR_RUN_STATE_ERROR = -1;
	/** Output will be idle */
	public static byte MOTOR_RUN_STATE_IDLE = 0x00;
	/** Output will ramp-up */
	public static byte MOTOR_RUN_STATE_RAMPUP = 0x10;
	/** Output will be running */
	public static byte MOTOR_RUN_STATE_RUNNING = 0x20;
	/** Output will ramp-down */
	public static byte MOTOR_RUN_STATE_RAMPDOWN = 0x40;

	// Input Mode Constants
	// PortType
	/**  */
	public static byte NO_SENSOR = 0x00;
	/**  */
	public static byte SWITCH = 0x01;
	/**  */
	public static byte TEMPERATURE = 0x02;
	/**  */
	public static byte REFLECTION = 0x03;
	/**  */
	public static byte ANGLE = 0x04;
	/**  */
	public static byte LIGHT_ACTIVE = 0x05;
	/**  */
	public static byte LIGHT_INACTIVE = 0x06;
	/**  */
	public static byte SOUND_DB = 0x07;
	/**  */
	public static byte SOUND_DBA = 0x08;
	/**  */
	public static byte CUSTOM = 0x09;
	/**  */
	public static byte LOWSPEED = 0x0A;
	/**  */
	public static byte LOWSPEED_9V = 0x0B;
	/**  */
	public static byte NO_OF_SENSOR_TYPES = 0x0C;

	// PortMode
	/**  */
	public static byte RAWMODE = 0x00;
	/**  */
	public static byte BOOLEANMODE = 0x20;
	/**  */
	public static byte TRANSITIONCNTMODE = 0x40;
	/**  */
	public static byte PERIODCOUNTERMODE = 0x60;
	/**  */
	public static byte PCTFULLSCALEMODE = (byte)0x80;
	/**  */
	public static byte CELSIUSMODE = (byte)0xA0;
	/**  */
	public static byte FAHRENHEITMODE = (byte)0xC0;
	/**  */
	public static byte ANGLESTEPSMODE = (byte)0xE0;
	/**  */
	public static byte SLOPEMASK = 0x1F;
	/**  */
	public static byte MODEMASK = (byte)0xE0;

	/**
	 * Container for holding the output state values.
	 */
	public static class OutputState implements Serializable {
		private static final long serialVersionUID = 3684447310030845407L;

		/** Status of GET_OUTPUT_STATE command. */
		public byte status;
		/** Output port [0 to 2]. */
		public int port;
		/** Power [-100 to 100]. */
		public byte powerSetpoint;
		/** Mode. */
		public int mode;
		/** Regulation mode. */
		public int regulationMode;
		/** Turn rate [-100 to 100]. */
		public byte turnRatio;
		/** MOTOR_RUN_STATE_* */
		public int runState;
		/** Current limit on a movement in progress, if any. */
		public long tachoLimit;
		/** Internal count. Number of counts since last reset of the motor counter. */
		public int tachoCount;
		/** Current position relative to last programmed movement. */
		public int blockTachoCount;
		/** Current position relative to last reset of the rotation sensor for this motor. */
		public int rotationCount;

		public OutputState(int port) {
			this.port = port;
		}

		public OutputState() {
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			String lineSeparator = System.getProperty("line.separator");
			sb.append(String.format("Status: %d%s",
					status, lineSeparator));
			sb.append(String.format("Port: %s%s",
					Port.get(port), lineSeparator));
			sb.append(String.format("Power: %d [-100 to 100]%s",
					powerSetpoint, lineSeparator));
			sb.append(String.format("Mode: %d%s",
					mode, lineSeparator));
			sb.append(String.format("Regulation Mode: %d%s",
					regulationMode, lineSeparator));
			sb.append(String.format("Turn ratio: %d [-100 to 100]%s",
					turnRatio, lineSeparator));
			sb.append(String.format("Motor running state: %d%s",
					runState, lineSeparator));
			sb.append(String.format("Current limit on a movement: %d%s",
					tachoLimit, lineSeparator));
			sb.append(String.format("Current position relative to last programmed movement: %d%s",
					blockTachoCount, lineSeparator));
			sb.append(String.format("Current position relative to last reset of the sensor: %d%s",
					rotationCount, lineSeparator));
			return sb.toString();
		}
	}
}
