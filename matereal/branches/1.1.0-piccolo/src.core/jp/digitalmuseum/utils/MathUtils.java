/*
 * PROJECT: mataereal at http://matereal.sourceforge.jp/
 * ----------------------------------------------------------------------------
 *
 * This file is part of matereal.
 * matereal is a Java toolkit that allows to operate home-robots easily.
 *
 * ----------------------------------------------------------------------------
 *
 * License version: MPL 1.1/GPL 2.0/LGPL 2.1
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
package jp.digitalmuseum.utils;


/**
 * Utility class for math calculation.
 *
 * @author Jun KATO
 */
public class MathUtils {

	/**
	 * Round radian value in range -pi/2 to pi/2
	 * @param rad
	 * @return
	 */
	public static double roundRadian(double rad) {
		final double pi2 = Math.PI*2;
		if (rad > 0) {
			return rad - pi2*(int)(( rad+Math.PI)/pi2);
			// while (rad > Math.PI) { rad += 2*Math.PI; }
		} else {
			return rad + pi2*(int)((-rad+Math.PI)/pi2);
			// while (rad < Math.PI) { rad -= 2*Math.PI; }
		}
	}
}
