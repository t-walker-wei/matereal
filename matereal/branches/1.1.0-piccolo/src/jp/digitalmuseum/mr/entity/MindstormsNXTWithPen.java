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

import java.util.List;

import jp.digitalmuseum.connector.Connector;
import jp.digitalmuseum.mr.entity.PhysicalResourceAbstractImpl;
import jp.digitalmuseum.mr.entity.ResourceAbstractImpl;
import jp.digitalmuseum.mr.resource.PenController;
import jp.digitalmuseum.mr.service.Service;
import jp.digitalmuseum.mr.service.ServiceAbstractImpl;

public class MindstormsNXTWithPen extends MindstormsNXT {
	private MindstormsNXTPen pen;
	private static int instances = 0;

	public MindstormsNXTWithPen(String connectionString) {
		super(connectionString);
		initialize(null);
	}

	public MindstormsNXTWithPen(String name, String connectionString) {
		super(connectionString);
		initialize(name);
	}

	public MindstormsNXTWithPen(String name, Connector connector) {
		super(connector);
		initialize(name);
	}

	private void initialize(String name) {
		setTypeName("LEGO Mindstorms NXT with Pen");
		pen = new MindstormsNXTPen(this);
		instances ++;
		if (name == null) {
			setName(getTypeName()+" ("+instances+")");
		} else {
			setName(name);
		}
	}


	@Override
	protected List<ResourceAbstractImpl> getResources() {
		List<ResourceAbstractImpl> rs = super.getResources();
		rs.add(pen);
		return rs;
	}

	public static class MindstormsNXTPen extends PhysicalResourceAbstractImpl implements PenController {
		private STATUS status;
		private byte penPort = A;
		private byte power = 100;
		private int tachoLimit = 180;

		final private Service stateWatcher = new ServiceAbstractImpl() {
			final private OutputState outputState = new OutputState();
			public void run() {
				MindstormsNXTWithPen.getOutputState(penPort,
						getConnector(),
						outputState);
				if (outputState.runState ==
						MindstormsNXT.MOTOR_RUN_STATE_IDLE) {
					finishWatchingState();
				}
			}
		};

		public MindstormsNXTPen(MindstormsNXTWithPen robot) {
			super(robot);
			status = STATUS.UP;
		}

		public MindstormsNXTPen(Connector connector) {
			super(connector);
			status = STATUS.UP;
		}

		protected void onFree() {
			// endPen();
		}

		public synchronized STATUS getStatus() {
			return status;
		}

		public void setPenPort(byte penPort) {
			this.penPort = penPort;
		}

		public byte getPenPort() {
			return penPort;
		}

		public synchronized void endPen() {
			if (status == STATUS.DOWN) {
				if (isWatchingState()) {
					finishWatchingState();
				}
				MindstormsNXTWithPen.setOutputState(
						penPort, (byte) -power, MOTORON + REGULATED,
						REGULATION_MODE_IDLE, 0,
						MOTOR_RUN_STATE_RUNNING, tachoLimit,
						getConnector());
				status = STATUS.GOING_UP;
				startWatchingState();
			}
		}

		public synchronized void putPen() {
			if (status == STATUS.UP) {
				if (isWatchingState()) {
					finishWatchingState();
				}
				MindstormsNXTWithPen.setOutputState(
						penPort, power, MOTORON + REGULATED,
						REGULATION_MODE_IDLE, 0,
						MOTOR_RUN_STATE_RUNNING, tachoLimit,
						getConnector());
				status = STATUS.GOING_DOWN;
				startWatchingState();
			}
		}

		private boolean isWatchingState() {
			return stateWatcher.isStarted();
		}

		private void startWatchingState() {
			stateWatcher.start();
		}

		private synchronized void finishWatchingState() {
			stateWatcher.stop();
			if (status == STATUS.GOING_DOWN) {
				status = STATUS.DOWN;
			} else if (status == STATUS.GOING_UP) {
				status = STATUS.UP;
			}
		}
	}
}
