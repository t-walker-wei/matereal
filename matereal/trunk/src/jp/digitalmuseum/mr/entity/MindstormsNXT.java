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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import jp.digitalmuseum.connector.BluetoothConnector;
import jp.digitalmuseum.connector.Connector;
import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.PhysicalRobotAbstractImpl;
import jp.digitalmuseum.mr.entity.ResourceAbstractImpl;
import jp.digitalmuseum.mr.resource.DifferentialWheelsAbstractImpl;

/**
 * LEGO Mindstorms NXT
 *
 * @author Jun KATO
 * @see MindstormsNXTDifferentialWheels
 */
public class MindstormsNXT extends PhysicalRobotAbstractImpl {
	private static final long serialVersionUID = -2489251030390060500L;
	public static final int A = 0;
	public static final int B = 1;
	public static final int C = 2;
	public static final int ALL = 0xff;
	public static final byte TACHO_FOREVER = 0;
	private static int instances = 0;
	private MindstormsNXTDifferentialWheels dw;

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
		dw = new MindstormsNXTDifferentialWheels(this);
		super.initialize();
	}

	@Override
	public void dispose() {
		dw.stopWheels();
		super.dispose();
	}

	@Override
	protected List<ResourceAbstractImpl> getResources() {
		List<ResourceAbstractImpl> rs = super.getResources();
		rs.add(dw);
		return rs;
	}

	public Shape getShape() {
		return null;
	}

	protected boolean drive(final int port,
			final byte power, final int mode) {
		return drive(port, power, mode, getConnector());
	}

	protected static boolean drive(final int port,
			final byte power, final int mode,
			final Connector connector) {
		return setOutputState(port, power, mode,
				REGULATION_MODE_MOTOR_SPEED, 0,
				MOTOR_RUN_STATE_RUNNING, TACHO_FOREVER,
				connector);
	}

	/**
	 * @see #setOutputState(int, byte, int, int, int, int, int, Connector)
	 */
	protected boolean setOutputState(final int port,
			final byte power, final int mode,
			final int regulationMode, final int turnRatio,
			final int runState, final int tachoLimit) {
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
			final int runState, final int tachoLimit,
			final Connector connector) {
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
		write(new byte[] {
				DIRECT_COMMAND_REPLY,
				GET_OUTPUT_STATE,
				(byte)port}, connector);
		final byte [] ret = read(connector);

		if(ret == null || ret.length <= 1 || ret[1] != GET_OUTPUT_STATE) {
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
		if (write(new byte[] {
				SYSTEM_COMMAND_REPLY,
				GET_FIRMWARE_VERSION}, connector)) {

			// Wait for ~100ms latency.
			try { Thread.sleep(100); }
			catch (InterruptedException e) { }

			// Get the result.
			final byte[] ret = read(connector);
			if (0 >= ret[2]) {
				Matereal.getInstance().getOutStream().println(
						"NXT Firmware version: "
						+ ret[4] + "." + ret[3]
						+ ", " + ret[6] + "." + ret[5]);
			}
		}
		return false;
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

	protected static byte[] read(final Connector connector) {
		final InputStream inStream = connector.getInputStream();
		byte[] reply = null;
		int length = -1;
		try {
			length = (inStream.read() & 0xff) | ((inStream.read() & 0xff << 8));
			reply = new byte[length];
			while (length > 0) {
				length -= inStream.read(reply,
						reply.length-length, length);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return reply;
	}

	/**
	 * Differential wheels of LEGO Mindstorms NXT.
	 *
	 * @author Jun KATO
	 * @see MindstormsNXT
	 */
	public static class MindstormsNXTDifferentialWheels extends DifferentialWheelsAbstractImpl {
		private static final long serialVersionUID = 3164832272022311974L;
		public final static int DEFAULT_SPEED = 10;
		public final static int DEFAULT_ROTATION_SPEED = 5;
		private byte leftWheelPort = C;
		private byte rightWheelPort = B;

		public MindstormsNXTDifferentialWheels(MindstormsNXT mindstormsNXT) {
			super(mindstormsNXT);
		}

		public MindstormsNXTDifferentialWheels(Connector connector) {
			super(connector);
		}

		@Override
		protected void initialize() {
			super.initialize();
			sendAck(getConnector());
		}

		public int getLeftWheelPort() {
			return leftWheelPort;
		}

		public void setLeftWheelPort(int leftWheelPort) {
			this.leftWheelPort = (byte) leftWheelPort;
		}

		public int getRightWheelPort() {
			return rightWheelPort;
		}

		public void setRightWheelPort(int rightWheelPort) {
			this.rightWheelPort = (byte) rightWheelPort;
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
				MindstormsNXT.drive(leftWheelPort,
						(byte)leftPower, MOTORON + REGULATED,
						connector) &&
				MindstormsNXT.drive(rightWheelPort,
						(byte)rightPower, MOTORON + REGULATED,
						connector);
		}

		protected boolean doStopWheels() {
			final Connector connector = getConnector();
			return
				MindstormsNXT.drive(leftWheelPort,
						(byte)0, BRAKE + MOTORON + REGULATED,
						connector) &&
				MindstormsNXT.drive(rightWheelPort,
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
	}
}
