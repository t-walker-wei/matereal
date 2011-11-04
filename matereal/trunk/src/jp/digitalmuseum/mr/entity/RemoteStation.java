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
import java.util.List;

import jp.digitalmuseum.connector.Connector;
import jp.digitalmuseum.mr.entity.PhysicalResourceAbstractImpl;
import jp.digitalmuseum.mr.entity.PhysicalRobotAbstractImpl;
import jp.digitalmuseum.mr.entity.ResourceAbstractImpl;

/**
 * BUFFALO RemoteStation for controllig remote devices through infrared beam.
 *
 * @author Jun KATO
 * @see RemoteStationCore
 */
public class RemoteStation extends PhysicalRobotAbstractImpl {

	private static final long serialVersionUID = 9001139537961310352L;

	public static final double WIDTH = 4;
	public static final double HEIGHT = 6;

	private static int instances = 0;
	private RemoteStationCore core;
	private Shape shape;

	public RemoteStation() {
		super();
	}

	public RemoteStation(String connectionString) {
		super(connectionString);
	}

	public RemoteStation(String connectionString, String name) {
		super(connectionString, name);
	}

	public RemoteStation(Connector connector) {
		super(connector);
	}

	public RemoteStation(Connector connector, String name) {
		super(connector, name);
	}

	@Override
	protected void initialize() {
		setTypeName("RemoteStation");
		instances ++;
		if (getName() == null) {
			setName(getTypeName()+" ("+instances+")");
		}
		core = new RemoteStationCore(this);
		shape = new RoundRectangle2D.Double(
				-HEIGHT*3/4, -WIDTH/2,
				HEIGHT, WIDTH,
				3, 3);
		super.initialize();
	}

	@Override
	protected List<ResourceAbstractImpl> getResources() {
		List<ResourceAbstractImpl> rs = super.getResources();
		rs.add(core);
		return rs;
	}

	public Shape getShape() {
		return shape;
	}

	/**
	 * RemoteStation core.
	 *
	 * @author Jun KATO
	 * @see RemoteStation
	 */
	public static class RemoteStationCore extends PhysicalResourceAbstractImpl {

		private static final long serialVersionUID = 2553168932983621857L;

		protected RemoteStationCore(Connector connector) {
			super(connector);
		}

		protected RemoteStationCore(RemoteStation robot) {
			super(robot);
		}

		@Override
		public RemoteStation getRobot() {
			return (RemoteStation) super.getRobot();
		}

		public boolean blinkLED() {
			getConnector().write(0x69);
			return getConnector().read() == 0x4f;
		}
	}
}
