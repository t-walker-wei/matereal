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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import jp.digitalmuseum.connector.Connector;
import jp.digitalmuseum.mr.entity.PhysicalRobotAbstractImpl;
import jp.digitalmuseum.mr.entity.ResourceAbstractImpl;
import jp.digitalmuseum.mr.resource.CleanerBrushController;
import jp.digitalmuseum.mr.resource.DifferentialWheelsAbstractImpl;

/**
 * Roomba.
 *
 * @author Jun KATO
 * @see RoombaDriver
 */
public class Roomba extends PhysicalRobotAbstractImpl {
	public final double RADIUS = 17;
	private RoombaDriver driver;
	private RoombaCleanerBrush cleaner;
	private Shape shape;

	public Roomba(String name) {
		super();
		initialize(name);
	}

	public Roomba(String name, String connectionString) {
		super(connectionString);
		initialize(name);
	}

	public Roomba(String name, Connector connector) {
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
		setTypeName("Roomba");
		driver = new RoombaDriver(this);
		cleaner = new RoombaCleanerBrush(this);
		shape = new Ellipse2D.Double(-RADIUS, -RADIUS, RADIUS*2, RADIUS*2);
	}


	@Override
	protected List<ResourceAbstractImpl> getResources() {
		List<ResourceAbstractImpl> rs = super.getResources();
		rs.add(driver);
		rs.add(cleaner);
		return rs;
	}

	public Shape getShape() {
		return shape;
	}

	/**
	 * Differential wheels of Roomba.
	 *
	 * @author Jun KATO
	 * @see Roomba
	 */
	public static class RoombaDriver extends DifferentialWheelsAbstractImpl {
		public final static int MAXIMUM_VELOCITY = 500;
		public final static int DEFAULT_SPEED = 14;
		public final static int DEFAULT_ROTATION_SPEED = 10;

		private MODE mode = MODE.UNKNOWN;

		/** Roomba modes */
		public static enum MODE {
			UNKNOWN,
			PASSIVE,
			SAFE,
			FULL
		};

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

		public RoombaDriver(Connector connector) {
			super(connector);
			start();
			control();
		}

		protected void onFree() {
			stopWheels();
			power();
		}

		public RoombaDriver(Roomba roomba) {
			super(roomba);
			start();
			control();
		}

		public int getRecommendedSpeed() {
			return DEFAULT_SPEED;
		}

		public int getRecommendedRotationSpeed() {
			return DEFAULT_ROTATION_SPEED;
		}

		/**
		 * This command starts the OI. You must always send the Start command before sending any other commands to the OI.
		 * <ul>
		 * 	<li>Available in modes: Passive, Safe, or Full</li>
		 * 	<li>Changes mode to: Passive. Roomba beeps once to acknowledge it is starting from &quot;off&quot; mode.</li>
		 * </ul>
		 */
		public void start() {
			for (int i = 0; i < 3; i ++) {
				getConnector().write(R_START);
				wait(20);
			}
			mode = MODE.PASSIVE;
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
			getConnector().write(R_BAUD);
			getConnector().write(baudCode);
			wait(100);
		}

		/**
		 * @see #safe()
		 */
		public void control() {
			getConnector().write(R_CONTROL);
			wait(20);
			mode = MODE.SAFE;
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
			getConnector().write(R_SAFE);
			wait(20);
			mode = MODE.SAFE;
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
			getConnector().write(R_FULL);
			wait(20);
			mode = MODE.FULL;
		}

		/**
		 * This command starts the default cleaning mode.
		 * <ul>
		 * 	<li>Available in modes: Passive, Safe, or Full</li>
		 * 	<li>Changes mode to: Passive</li>
		 * </ul>
		 */
		public void clean() {
			getConnector().write(R_CLEAN);
			wait(20);
			mode = MODE.PASSIVE;
		}

		/**
		 * This command starts the Max cleaning mode.
		 * <ul>
		 * 	<li>Available in modes: Passive, Safe, or Full</li>
		 * 	<li>Changes mode to: Passive</li>
		 * </ul>
		 */
		public void max() {
			getConnector().write(R_MAX);
			wait(20);
			mode = MODE.PASSIVE;
		}

		/**
		 * This command starts the Spot cleaning mode.
		 * <ul>
		 * 	<li>Available in modes: Passive, Safe, or Full</li>
		 * 	<li>Changes mode to: Passive</li>
		 * </ul>
		 */
		public void spot() {
			getConnector().write(R_SPOT);
			wait(20);
			mode = MODE.PASSIVE;
		}

		/**
		 * This command sends Roomba to the dock.
		 * <ul>
		 * 	<li>Available in modes: Passive, Safe, or Full</li>
		 * 	<li>Changes mode to: Passive</li>
		 * </ul>
		 */
		public void seekDock() {
			getConnector().write(R_SEEKDOCK);
			wait(20);
			mode = MODE.PASSIVE;
		}

		/**
		 * This command powers down Roomba.
		 * <ul>
		 * 	<li>Available in modes: Passive, Safe, or Full</li>
		 * 	<li>Changes mode to: Passive</li>
		 * </ul>
		 */
		public void power() {
			getConnector().write(R_POWER);
			wait(20);
			mode = MODE.UNKNOWN;
		}

		/**
		 * This command controls Roomba’s drive wheels. It takes four data bytes, interpreted as two 16-bit signed
		 * values using two’s complement. The first two bytes specify the average velocity of the drive wheels in
		 * millimeters per second (mm/s), with the high byte being sent first. The next two bytes specify the radius
		 * in millimeters at which Roomba will turn. The longer radii make Roomba drive straighter, while the
		 * shorter radii make Roomba turn more. The radius is measured from the center of the turning circle to the
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
		public void drive_(int velocity, int radius) {
			// System.out.println("drive:"+velocity+","+radius);
			if (mode.equals(MODE.PASSIVE)) { safe(); }
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
			// System.out.println("drive:"+velocity+","+radius);
			if (mode.equals(MODE.PASSIVE)) { safe(); }
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
			if (mode.equals(MODE.PASSIVE)) { safe(); }
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
			if (mode.equals(MODE.PASSIVE)) { safe(); }
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
			if (mode.equals(MODE.PASSIVE)) { safe(); }
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
			if (mode.equals(MODE.PASSIVE)) { safe(); }
			getConnector().write(R_PLAY);
			getConnector().write(index);
			wait(20);
		}

		private void wait(int ms) {
			try { Thread.sleep(ms); }
			catch (InterruptedException e) { e.printStackTrace(); }
		}

		protected boolean doStopWheels() {
			driveDirect(0, 0);
			return true;
		}

		protected boolean doDrive(int leftPower, int rightPower) {
			driveDirect(getVelocity(leftPower), getVelocity(rightPower));
			return true;
		}

		private int getVelocity(int speed) {
			return speed*MAXIMUM_VELOCITY/100;
		}

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
		 * @author Jun KATO
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
		 * @author Jun KATO
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

	public static class RoombaCleanerBrush extends ResourceAbstractImpl implements CleanerBrushController {
		private Roomba roomba;
		private boolean isWorking = false;

		public RoombaCleanerBrush(Roomba roomba) {
			super(roomba);
			this.roomba = roomba;
		}

		public boolean isWorking() {
			return isWorking;
		}

		protected void onFree() {
		}

		public void endCleaning() {
			startRoombaIfNeeded();
			roomba.driver.motors(false);
			isWorking = false;
		}

		public void startCleaning() {
			startRoombaIfNeeded();
			roomba.driver.motors(true);
			isWorking = true;
		}

		private void startRoombaIfNeeded() {
			if (roomba.driver.mode == RoombaDriver.MODE.UNKNOWN) {
				roomba.driver.start();
				roomba.driver.control();
			}
		}
	}
}
