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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.phybots.entity.PhysicalResourceAbstractImpl;
import com.phybots.entity.PhysicalRobotAbstractImpl;
import com.phybots.entity.ResourceAbstractImpl;
import com.phybots.resource.CleanerBrushController;
import com.phybots.resource.DifferentialWheelsAbstractImpl;

import jp.digitalmuseum.connector.Connector;

/**
 * Roomba driver for Phybots.
 *
 * @author Jun Kato
 * @see RoombaCore
 */
public class Roomba extends PhysicalRobotAbstractImpl {
	private static final long serialVersionUID = -8733485049894567603L;
	public static final double RADIUS = 17;
	private static int instances = 0;
	private RoombaCore core;
	private RoombaWheels wheels;
	private RoombaCleanerBrush cleaner;
	private Shape shape;

	public Roomba() {
		super();
	}

	public Roomba(String connectionString) {
		super(connectionString);
	}

	public Roomba(String connectionString, String name) {
		super(connectionString, name);
	}

	public Roomba(Connector connector) {
		super(connector);
	}

	public Roomba(Connector connector, String name) {
		super(connector, name);
	}

	@Override
	protected void initialize() {
		setTypeName("Roomba");
		instances ++;
		if (getName() == null) {
			setName(getTypeName()+" ("+instances+")");
		}
		core = new RoombaCore(this);
		wheels = new RoombaWheels(this);
		cleaner = new RoombaCleanerBrush(this);
		shape = new Ellipse2D.Double(-RADIUS, -RADIUS, RADIUS*2, RADIUS*2);
		super.initialize();
	}

	@Override
	protected List<ResourceAbstractImpl> getResources() {
		List<ResourceAbstractImpl> rs = super.getResources();
		rs.add(core);
		rs.add(wheels);
		rs.add(cleaner);
		return rs;
	}

	public Shape getShape() {
		return shape;
	}

	/** Roomba modes */
	public static enum RoombaMode {
		UNKNOWN,
		PASSIVE,
		SAFE,
		FULL
	};

	/**
	 * Core driver for Roomba.
	 * 
	 * @author Jun Kato
	 */
	public static class RoombaCore extends PhysicalResourceAbstractImpl {
		private static final long serialVersionUID = -4557084824109152696L;

		private transient RoombaMode mode = RoombaMode.UNKNOWN;

		protected RoombaCore(Connector connector) {
			super(connector);
		}

		protected RoombaCore(Roomba roomba) {
			super(roomba);
		}

		private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
			ois.defaultReadObject();
		}

		/**
		 * This command starts the OI. You must always send the Start command before sending any other commands to the OI.
		 * <ul>
		 * 	<li>Available in modes: Passive, Safe, or Full</li>
		 * 	<li>Changes mode to: Passive. Roomba beeps once to acknowledge it is starting from &quot;off&quot; mode.</li>
		 * </ul>
		 */
		public void start() {
			getConnector().write(R_START);
			wait(30); // 20[ms] seems too short.
			mode = RoombaMode.PASSIVE;
		}

		/**
		 * This command sets the baud rate in bits per second (bps) at which OI commands and data are sent
		 * according to the baud code sent in the data byte. The default baud rate at power up is 115200 bps, but
		 * the starting baud rate can be changed to 19200 by holding down the Clean button while powering on
		 * Roomba until you hear a sequence of descending tones. Once the baud rate is changed, it persists until
		 * Roomba is power cycled by pressing the power button or removing the battery, or when the battery
		 * voltage falls below the minimum required for processor operation. You must wait 100ms after sending
		 * this command before sending additional commands at the new baud rate.
		 * <ul>
		 * 	<li>Available in modes: Passive, Safe, or Full</li>
		 * 	<li>Changes mode to: No Change</li>
		 * </ul>
		 * <table border="1">
		 * 	<tr><th>Baud Code</th><th>Baud Rate in BPS</th></tr>
		 * 	<tr><td>0</td><td>300</td></tr>
		 * 	<tr><td>1</td><td>600</td></tr>
		 * 	<tr><td>2</td><td>1200</td></tr>
		 * 	<tr><td>3</td><td>2400</td></tr>
		 * 	<tr><td>4</td><td>4800</td></tr>
		 * 	<tr><td>5</td><td>9600</td></tr>
		 * 	<tr><td>6</td><td>14400</td></tr>
		 * 	<tr><td>7</td><td>19200</td></tr>
		 * 	<tr><td>8</td><td>28800</td></tr>
		 * 	<tr><td>9</td><td>38400</td></tr>
		 * 	<tr><td>10</td><td>57600</td></tr>
		 * 	<tr><td>11</td><td>115200</td></tr>
		 * </table>
		 */
		public void baud(byte baudCode) {
			if (mode == RoombaMode.UNKNOWN) {
				start();
			}
			getConnector().write(R_BAUD);
			getConnector().write(baudCode);
			wait(100);
		}

		/**
		 * @see #safe()
		 */
		public void control() {
			if (mode == RoombaMode.UNKNOWN) {
				start();
			}
			getConnector().write(R_CONTROL);
			wait(30);
			mode = RoombaMode.SAFE;
		}

		/**
		 * This command puts the OI into Safe mode, enabling user control of Roomba. It turns off all LEDs. The OI
		 * can be in Passive, Safe, or Full mode to accept this command. If a safety condition occurs (see above)
		 * Roomba reverts automatically to Passive mode.
		 * <ul>
		 * 	<li>Available in modes: Passive, Safe, or Full</li>
		 * 	<li>Changes mode to: Safe</li>
		 * </ul>
		 */
		public void safe() {
			if (mode == RoombaMode.UNKNOWN) {
				start();
			}
			getConnector().write(R_SAFE);
			wait(30);
			mode = RoombaMode.SAFE;
		}

		/**
		 * This command gives you complete control over Roomba by putting the OI into Full mode, and turning off
		 * the cliff, wheel-drop and internal charger safety features. That is, in Full mode, Roomba executes any
		 * command that you send it, even if the internal charger is plugged in, or command triggers a cliff or wheel
		 * drop condition.
		 * <ul>
		 * 	<li>Available in modes: Passive, Safe, or Full</li>
		 * 	<li>Changes mode to: Safe</li>
		 * </ul>
		 */
		public void full() {
			if (mode == RoombaMode.UNKNOWN) {
				start();
			}
			getConnector().write(R_FULL);
			wait(30);
			mode = RoombaMode.FULL;
		}

		/**
		 * This command starts the default cleaning mode.
		 * <ul>
		 * 	<li>Available in modes: Passive, Safe, or Full</li>
		 * 	<li>Changes mode to: Passive</li>
		 * </ul>
		 */
		public void clean() {
			if (mode == RoombaMode.UNKNOWN) {
				start();
			}
			getConnector().write(R_CLEAN);
			wait(20);
			mode = RoombaMode.PASSIVE;
		}

		/**
		 * This command starts the Max cleaning mode.
		 * <ul>
		 * 	<li>Available in modes: Passive, Safe, or Full</li>
		 * 	<li>Changes mode to: Passive</li>
		 * </ul>
		 */
		public void max() {
			if (mode == RoombaMode.UNKNOWN) {
				start();
			}
			getConnector().write(R_MAX);
			wait(20);
			mode = RoombaMode.PASSIVE;
		}

		/**
		 * This command starts the Spot cleaning mode.
		 * <ul>
		 * 	<li>Available in modes: Passive, Safe, or Full</li>
		 * 	<li>Changes mode to: Passive</li>
		 * </ul>
		 */
		public void spot() {
			if (mode == RoombaMode.UNKNOWN) {
				start();
			}
			getConnector().write(R_SPOT);
			wait(20);
			mode = RoombaMode.PASSIVE;
		}

		/**
		 * This command sends Roomba to the dock.
		 * <ul>
		 * 	<li>Available in modes: Passive, Safe, or Full</li>
		 * 	<li>Changes mode to: Passive</li>
		 * </ul>
		 */
		public void seekDock() {
			if (mode == RoombaMode.UNKNOWN) {
				start();
			}
			getConnector().write(R_SEEKDOCK);
			wait(20);
			mode = RoombaMode.PASSIVE;
		}

		/**
		 * This command powers down Roomba.
		 * <ul>
		 * 	<li>Available in modes: Passive, Safe, or Full</li>
		 * 	<li>Changes mode to: Passive</li>
		 * </ul>
		 */
		public void power() {
			if (mode == RoombaMode.UNKNOWN) {
				start();
			}
			if (mode != RoombaMode.PASSIVE) {
				getConnector().write(R_POWER);
				wait(20);
				mode = RoombaMode.PASSIVE;
			}
		}

		/**
		 * This command controls Roomba’s drive wheels. It takes four data bytes, interpreted as two 16-bit signed
		 * values using two’s complement. The first two bytes specify the average velocity of the drive wheels in
		 * millimeters per second (mm/s), with the high byte being sent first. The next two bytes specify the radius
		 * in millimeters at which Roomba will turn. The longer radius make Roomba drive straighter, while the
		 * shorter radius make Roomba turn more. The radius is measured from the center of the turning circle to the
		 * center of Roomba. A Drive command with a positive velocity and a positive radius makes Roomba drive
		 * forward while turning toward the left. A negative radius makes Roomba turn toward the right. Special
		 * cases for the radius make Roomba turn in place or drive straight, as specified below. A negative velocity
		 * makes Roomba drive backward.
		 * <ul>
		 * 	<li>Available in modes: Safe or Full</li>
		 * 	<li>Changes mode to: <del>No Change</del> This method turns Passive mode into Safe mode.</li>
		 * 	<li>Velocity: -500-500 [mm/s]</li>
		 * 	<li>Radius: -2000-2000 [mm], except special cases:<ul>
		 * 		<li>Straight = 0x8000 or 0x7FFF</li>
		 * 		<li>Turn in place clockwise = -1</li>
		 * 		<li>Turn in place counter-clockwise = 1</li>
		 * 		</ul></li>
		 * </ul>
		 * @param velocity
		 * @param radius
		 */
		public void drive(int velocity, int radius) {
			if (mode == RoombaMode.UNKNOWN ||
					mode.equals(RoombaMode.PASSIVE)) {
				safe();
			}
			getConnector().write(new byte[] {
				(byte) R_DRIVE,
				/** v upper 8 bits */ (byte) (velocity >> 8),
				/** v lower 8 bits */ (byte) (velocity & 0xff),
				/** r upper 8 bits */ (byte) (radius >> 8),
				/** r lower 8 bits */ (byte) (radius & 0xff)
			});
			wait(20);
		}

		/**
		 * This command lets you control the forward and backward motion of Roomba’s drive wheels
		 * independently. It takes four data bytes, which are interpreted as two 16-bit signed values using two’s
		 * complement. The first two bytes specify the velocity of the right wheel in millimeters per second (mm/s),
		 * with the high byte sent first. The next two bytes specify the velocity of the left wheel, in the same
		 * format. A positive velocity makes that wheel drive forward, while a negative velocity makes it drive
		 * backward.
		 * <ul>
		 * 	<li>Available in modes: Safe or Full</li>
		 * 	<li>Changes mode to: <del>No Change</del> This method turns Passive mode into Safe mode.</li>
		 * 	<li>Right wheel velocity: -500-500 [mm/s]</li>
		 * 	<li>Left wheel velocity: -500-500 [mm/s]</li>
		 * </ul>
		 * @param leftVelocity
		 * @param rightVelocity
		 */
		public void driveDirect(int leftVelocity, int rightVelocity) {
			if (mode == RoombaMode.UNKNOWN ||
					mode.equals(RoombaMode.PASSIVE)) {
				safe();
			}
			getConnector().write(new byte[] {
				(byte) R_DRIVEDIRECT,
				/** vr upper 8 bits */ (byte) (rightVelocity >> 8),
				/** vr lower 8 bits */ (byte) (rightVelocity & 0xff),
				/** vl upper 8 bits */ (byte) (leftVelocity >> 8),
				/** vl lower 8 bits */ (byte) (leftVelocity & 0xff)
			});
			wait(20);
		}

		/**
		 * With side brush and main brush motors on, control vacuum motor.
		 * @param vacuum
		 * @see #motors(boolean, boolean, boolean)
		 */
		public void motors(boolean vacuum) {
			motors(vacuum, true, true);
		}

		public void motors(boolean vacuum, boolean sideBrush, boolean mainBrush) {
			motors(vacuum, sideBrush, true, mainBrush, true);
		}

		/**
		 * This command lets you control the forward and backward motion of Roomba’s main brush, side brush,
		 * and vacuum independently. Motor velocity cannot be controlled with this command, all motors will run at
		 * maximum speed when enabled. The main brush and side brush can be run in either direction. The
		 * vacuum only runs forward.
		 * <ul>
		 * 	<li>Available in modes: Safe or Full</li>
		 * 	<li>Changes mode to: <del>No Change</del> This method turns Passive mode into Safe mode.</li>
		 * </ul>
		 * @param vacuum
		 * @param sideBrush
		 * @param sideBrushClockwise Clockwise or counter-clockwise
		 * @param mainBrush
		 * @param mainBrushOutward Outward or inward
		 */
		public void motors(boolean vacuum, boolean sideBrush, boolean sideBrushClockwise, boolean mainBrush, boolean mainBrushOutward) {
			if (mode == RoombaMode.UNKNOWN ||
					mode.equals(RoombaMode.PASSIVE)) {
				safe();
			}
			getConnector().write(R_MOTORS);
			getConnector().write((byte) (
					(mainBrushOutward?		16:0) +
					(sideBrushClockwise?	 8:0) +
					(mainBrush?				 4:0) +
					(vacuum?				 2:0) +
					(sideBrush?				 1:0)));
			wait(20);
		}

		/**
		 * This command controls the LEDs common to all models of Roomba 500. The Clean/Power LED is
		 * specified by two data bytes: one for the color and the other for the intensity.
		 * <ul>
		 * 	<li>Available in modes: Safe or Full</li>
		 * 	<li>Changes mode to: <del>No Change</del> This method turns Passive mode into Safe mode.</li>
		 * </ul>
		 * @param home
		 * @param spot
		 * @param checkRobot
		 * @param debris
		 * @param cleanOrPowerColor 0-255 (green to red)
		 * @param cleanOrPowerIntensity 0-255
		 */
		public void lightLED(boolean home, boolean spot, boolean checkRobot, boolean debris, int cleanOrPowerColor, int cleanOrPowerIntensity) {
			if (mode == RoombaMode.UNKNOWN ||
					mode.equals(RoombaMode.PASSIVE)) {
				safe();
			}
			getConnector().write(R_MOTORS);
			getConnector().write((byte) (
					(checkRobot?	8:0) +
					(home?			4:0) +
					(spot?	 		2:0) +
					(debris?		1:0)));
			getConnector().write(cleanOrPowerColor);
			getConnector().write(cleanOrPowerIntensity);
			wait(20);
		}

		/**
		 * This command controls the four 7 segment displays on the Roomba 560 and 570 using ASCII character
		 * codes. Because a 7 segment display is not sufficient to display alphabetic characters properly, all
		 * characters are an approximation, and not all ASCII codes are implemented.
		 * <ul>
		 * 	<li>Available in modes: Safe or Full</li>
		 * 	<li>Changes mode to: <del>No Change</del> This method turns Passive mode into Safe mode.</li>
		 * </ul>
		 */
		public void lightDigitLED(char c0, char c1, char c2, char c3) {
			if (mode == RoombaMode.UNKNOWN ||
					mode.equals(RoombaMode.PASSIVE)) {
				safe();
			}
			getConnector().write(R_DIGITLED);
			getConnector().write(c0);
			getConnector().write(c1);
			getConnector().write(c2);
			getConnector().write(c3);
			wait(20);
		}

		/**
		 * Register a song to Roomba with index 0.
		 * @see #song(int, Song)
		 */
		public void song(Song song) {
			song(0, song);
		}

		/**
		 * Register a song to Roomba with index 0 and play it immediately.
		 * @see #song(int, Song)
		 */
		public void playSong(Song song) {
			song(0, song);
			play(0);
		}

		/**
		 * This command lets you specify up to four songs to the OI that you can play at a later time. Each song is
		 * associated with a song number. The Play command uses the song number to identify your song selection.
		 * Each song can contain up to sixteen notes. Each note is associated with a note number that uses MIDI
		 * note definitions and a duration that is specified in fractions of a second. The number of data bytes varies,
		 * depending on the length of the song specified. A one note song is specified by four data bytes. For each
		 * additional note within a song, add two data bytes.
		 * <ul>
		 * 	<li>Available in modes: Passive, Safe, or Full</li>
		 * 	<li>Changes mode to: No change</li>
		 * </ul>
		 * @param index 0-4
		 * @param song
		 * @see Song
		 */
		public void song(int index, Song song) {
			if (mode == RoombaMode.UNKNOWN) {
				start();
			}
			final Connector connector = getConnector();
			connector.write(R_SONG);
			connector.write(index);
			connector.write(song.size());
			for (Note n : song) {
				connector.write(n.getNumber());
				connector.write(n.getDuration());
			}
			wait(20);
		}

		/**
		 * This command lets you select a song to play from the songs added to Roomba using the Song command.
		 * You must add one or more songs to Roomba using the Song command in order for the Play command to
		 * work.
		 * <ul>
		 * 	<li>Available in modes: Safe or Full</li>
		 * 	<li>Changes mode to: <del>No Change</del> This method turns Passive mode into Safe mode.</li>
		 * </ul>
		 * @param index 0-4
		 */
		public void play(int index) {
			if (mode == RoombaMode.UNKNOWN ||
					mode.equals(RoombaMode.PASSIVE)) {
				safe();
			}
			getConnector().write(R_PLAY);
			getConnector().write(index);
			wait(20);
		}

		/**
		 * This command requests sensor data from Roomba.
		 * TODO to be implemented. see http://svn.dprg.org/repos/roomba/roombacomm/trunk/src/com/hackingroomba/roombacomm/RoombaComm.java
		 */
		public byte[] sensors(int packetCode) {
			/*
			switch (packetCode) {
			}
			*/
			return null;
		}

		public static final int R_START = 128;
		public static final int R_BAUD = 129;

		/**
		 * identical to the Safe command.
		 * @see #R_SAFE
		 */
		public static final int R_CONTROL = 130;

		/** Mode commands / Safe */
		public static final int R_SAFE = 131;
		/** Mode commands / Full */
		public static final int R_FULL = 132;

		/** Cleaning commands / Clean */
		public static final int R_CLEAN = 135;
		/** Cleaning commands / Max */
		public static final int R_MAX = 136;
		/** Cleaning commands / Spot */
		public static final int R_SPOT = 134;
		/** Cleaning commands / Seek Dock */
		public static final int R_SEEKDOCK = 143;
		/** Cleaning commands / Schedule */
		public static final int R_SCHEDULE = 167;
		/** Cleaning commands / Set Day/Time */
		public static final int R_SETDAYTIME = 168;
		/** Cleaning commands / Power off */
		public static final int R_POWER = 133;

		/** Actuator commands / Drive */
		public static final int R_DRIVE = 137;
		/** Actuator commands / Drive Direct */
		public static final int R_DRIVEDIRECT = 145;
		/**
		 * Actuator commands / Drive <a href="http://monoist.atmarkit.co.jp/fembedded/h8/h8primer09/h8primer09a.html">Pulse Width Modulation</a>
		 */
		public static final int R_DRIVEPWM = 146;
		/** Actuator commands / Motors */
		public static final int R_MOTORS = 138;
		/**
		 * Actuator commands / Motors
		 * @see #R_DRIVEPWM
		 */
		public static final int R_MOTORSPWM = 144;
		/** Actuator commands / LEDs */
		public static final int R_LEDS = 139;
		/** Actuator commands / Scheduling LEDs */
		public static final int R_SCHEDULELEDS = 162;
		/** Actuator commands / Digit LEDs Raw */
		public static final int R_DIGITLEDRAW = 163;
		/** Actuator commands / Digit LEDs ASCII */
		public static final int R_DIGITLED= 164;
		/** Actuator commands / Buttons */
		public static final int R_Buttons= 165;
		/** Actuator commands / Song */
		public static final int R_SONG = 140;
		/** Actuator commands / Play */
		public static final int R_PLAY = 141;

		/** Input commands / Sensors */
		public static final int R_SENSORS = 142;
		/** Input commands / Query list */
		public static final int R_QUERYLIST = 149;
		/** Input commands / Stream */
		public static final int R_STREAM = 148;
		/** Input commands / Pause/Resume stream */
		public static final int R_PAUSERESUMESTREAM = 150;

		public static final int R_SENSORS_ALL = 0;
		public static final int R_SENSORS_PHYSICAL = 1;
		public static final int R_SENSORS_INTERNAL = 2;
		public static final int R_SENSORS_POWER = 3;

		/**
		 * Note consisting Song<br />
		 * <dl>
		 *	<dt>Note Number (31 – 127)</dt><dd>
		 * The pitch of the musical note Roomba will play, according to the MIDI note numbering scheme. The
		 * lowest musical note that Roomba will play is Note #31. Roomba considers all musical notes outside
		 * the range of 31 – 127 as rest notes, and will make no sound during the duration of those notes.</dd>
		 *	<dt>Note Duration (0 – 255)</dt><dd>
		 * The duration of a musical note, in increments of 1/64th of a second. Example: a half-second long
		 * musical note has a duration value of 32.</dd>
		 * </dl>
		 *
		 * @author Jun Kato
		 * @see Song
		 */
		public static class Note {
			final public static int A = 69;
			final public static int A_SHARP = 70;
			final public static int B = 71;
			final public static int C = 72;
			final public static int C_SHARP = 73;
			final public static int D = 74;
			final public static int D_SHARP = 75;
			final public static int E = 76;
			final public static int F = 77;
			final public static int F_SHARP = 78;
			final public static int G = 79;
			final public static int G_SHARP = 80;
			final public static int QUARTER_SECOND = 16;
			final public static int HALF_SECOND = 32;
			final public static int SECOND = 64;
			private int number;
			private int duration;
			public Note(int number, int duration) {
				this.setNumber(number);
				this.setDuration(duration);
			}
			public void setNumber(int number) {
				this.number = number;
			}
			public int getNumber() {
				return number;
			}
			public void setDuration(int duration) {
				this.duration = duration;
			}
			public int getDuration() {
				return duration;
			}
		}

		/**
		 * Roomba song consisted of 16 Notes at maximum.
		 *
		 * @author Jun Kato
		 * @see Note
		 */
		public static class Song implements Iterable<Note> {
			private List<Note> notes;
			public Song() {
				notes = new ArrayList<Note>();
			}
			public Song(Note note) {
				notes = new ArrayList<Note>();
				notes.add(note);
			}
			public Song(Note[] notes) {
				this.notes = Arrays.asList(notes);
			}
			public void add(Note n) {
				notes.add(n);
			}
			public void add(int index, Note n) {
				notes.add(index, n);
			}
			public void add(int number, int duration) {
				add(new Note(number, duration));
			}
			public void add(int index, int number, int duration) {
				add(index, new Note(number, duration));
			}
			public Note get(int index) {
				return notes.get(index);
			}
			public Note remove(int index) {
				return notes.remove(index);
			}
			public int size() {
				return notes.size();
			}
			public Iterator<Note> iterator() {
				return notes.iterator();
			}
		}
	}

	/**
	 * Differential wheels of Roomba.
	 *
	 * @author Jun Kato
	 * @see Roomba
	 */
	public static class RoombaWheels extends DifferentialWheelsAbstractImpl {
		private static final long serialVersionUID = -8077520687089529287L;
		public final static int MAXIMUM_VELOCITY = 500;
		public final static int DEFAULT_SPEED = 14;
		public final static int DEFAULT_ROTATION_SPEED = 10;

		public RoombaWheels(Roomba roomba) {
			super(roomba);
			initialize();
		}

		protected void onFree() {
			stopWheels();
		}

		@Override
		public Roomba getRobot() {
			return (Roomba) super.getRobot();
		}

		public int getRecommendedSpeed() {
			return DEFAULT_SPEED;
		}

		public int getRecommendedRotationSpeed() {
			return DEFAULT_ROTATION_SPEED;
		}

		protected boolean doStopWheels() {
			getRobot().core.driveDirect(0, 0);

			// Set Roomba to PASSIVE mode while stopping.
			// if (getRobot().core.mode != RoombaMode.PASSIVE) {
			getRobot().core.start();
			// }
			return true;
		}

		protected boolean doDrive(int leftPower, int rightPower) {

			// Set Roomba to FULL mode while driving.
			if (getRobot().core.mode != RoombaMode.FULL) {
				getRobot().core.full();
			}

			getRobot().core.driveDirect(getVelocity(leftPower), getVelocity(rightPower));
			return true;
		}

		private int getVelocity(int speed) {
			return speed*MAXIMUM_VELOCITY/100;
		}
	}

	public static class RoombaCleanerBrush extends ResourceAbstractImpl implements CleanerBrushController {
		private static final long serialVersionUID = -293735845062700029L;
		private transient boolean isWorking = false;

		public RoombaCleanerBrush(Roomba roomba) {
			super(roomba);
		}

		@Override
		public Roomba getRobot() {
			return (Roomba) super.getRobot();
		}

		public boolean isWorking() {
			return isWorking;
		}

		@Override
		protected void onFree() {
			endCleaning();
		}

		public void endCleaning() {
			getRobot().core.motors(false);
			isWorking = false;
		}

		public void startCleaning() {
			getRobot().core.motors(true);
			isWorking = true;
		}
	}

	/**
	 * Utility class for controlling RooTooth (FireFly.)
	 */
	public static class RooTooth {

		/**
		 * Wake up Roomba to accept ROI commands.
		 * @param roomba Roomba to wake up.
		 */
		public static void wakeUp(Roomba roomba) {
			Connector connector = roomba.getConnector();
			byte[] response = new byte[5];
			// Dive into command mode.
			sendCommand("$$$", connector, response);
			// Set GPIO7 as output.
			sendCommand("S@,8080\n", connector, response);
			// Toggle GPIO7 to low.
			sendCommand("S&,8000\n", connector, response);
			roomba.core.wait(500);
			// Toggle GPIO7 to high.
			sendCommand("S&,8080\n", connector, response);
			// Get out of the command mode.
			sendCommand("---\n", connector, response);
		}

		/**
		 * Set baud rate of FireFly chip to 115200bps (the default baud rate for ROI communication.) 
		 * @param roomba
		 */
		public static void setBaudRate115K(Roomba roomba) {
			setBaudRate(roomba, "115K");
		}

		public static void setBaudRate(Roomba roomba, String baudRate) {
			Connector connector = roomba.getConnector();
			byte[] response = new byte[5];
			// Dive into command mode.
			sendCommand("$$$", connector, response);
			// Set baud rate to 115K.
			sendCommand(String.format("SU,%s\n", baudRate), connector, response);
			// Get out of the command mode.
			sendCommand("---\n", connector, response);
		}

		private static void sendCommand(String command, Connector connector, byte[] response) {
			// System.out.print(command);
			connector.write(command);
			connector.waitForResponse();
			connector.readAll(response);
			/*
			for (byte b : response) {
				System.out.print((char) (b & 0xff));
			}
			*/
		}
	}
}
