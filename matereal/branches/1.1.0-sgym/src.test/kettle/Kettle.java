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
package kettle;

//import java.io.IOException;
import java.util.List;

import jp.digitalmuseum.mr.entity.ExclusiveResource;
import jp.digitalmuseum.mr.entity.PhysicalResourceAbstractImpl;
import jp.digitalmuseum.mr.entity.PhysicalRobotAbstractImpl;
import jp.digitalmuseum.mr.entity.ResourceAbstractImpl;

public class Kettle extends PhysicalRobotAbstractImpl {
	public Kettle(String name) {
		setName(name);
	}
	protected List<ResourceAbstractImpl> getResources() {
		List<ResourceAbstractImpl> rs = super.getResources();
		rs.add(new KettleCore(this)); return rs;
	}
	public static class KettleCore
			extends PhysicalResourceAbstractImpl
			implements ExclusiveResource {
		private KettleCore(Kettle k) { super(k); }
		public void heat(boolean h) { System.out.println(h?"b":"d");/*getConnector().write(h ? "b" : "d");*/ }
		public int getTemperature() {
			System.out.println("t");
			return 100;
			/*getConnector().write("t");
			try { return getConnector().readInt(); }
			catch (IOException e) { return 100; }*/ }
		public void pour() { System.out.println("p");/*getConnector().write("p");*/ }
		public void stop() { System.out.println("s");/*getConnector().write("s");*/ }
	}
}
