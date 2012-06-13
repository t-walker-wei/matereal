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
package kettle;

import java.util.Date;
import java.util.List;

import com.phybots.Phybots;
import com.phybots.entity.ExclusiveResource;
import com.phybots.entity.ResourceAbstractImpl;
import com.phybots.hakoniwa.HakoniwaRobot;


public class Kettle extends HakoniwaRobot {
	private static final long serialVersionUID = -1044123032265398208L;

	public Kettle(String name) {
		super(name);
	}

	protected List<ResourceAbstractImpl> getResources() {
		List<ResourceAbstractImpl> rs = super.getResources();
		rs.add(new KettleCore(this));
		return rs;
	}

	public static class KettleCore
			extends ResourceAbstractImpl implements ExclusiveResource {
		private static final long serialVersionUID = 8453184701240270679L;
		private boolean isOn = false;
		private int temperature = 25;
		private long time;

		private KettleCore(Kettle kettle) {
			super(kettle);
		}

		public void heat(boolean isOn) {
			getTemperature();
			this.isOn = isOn;
			Phybots.getInstance().getOutStream().println(isOn
					? "Start heating water."
					: "Stop heating water.");
		}

		public int getTemperature() {
			long currentTime = new Date().getTime();
			long diff = currentTime - time;
			if (isOn) {
				temperature += diff / 1000;
				if (temperature > 100) {
					temperature = 100;
				}
			} else {
				temperature -= diff / 2000;
				if (temperature < 25) {
					temperature = 25;
				}
			}
			time = currentTime;
			return temperature;
		}

		public void startPour() {
			Phybots.getInstance().getOutStream().println(
					"Start pouring water.");
		}

		public void stopPour() {
			Phybots.getInstance().getOutStream().println(
					"Stopped pouring water.");
		}
	}
}
