/*
 * PROJECT: napkit at http://mr.digitalmuseum.jp/
 * ----------------------------------------------------------------------------
 *
 * This file is part of NyARToolkit Application Toolkit.
 *
 * NyARToolkit Application Toolkit, or simply "napkit",
 * is a simple wrapper library for NyARToolkit.
 *
 * ----------------------------------------------------------------------------
 *
 * License version: GPL 3.0
 *
 * napkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * napkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with napkit. If not, see <http://www.gnu.org/licenses/>.
 */
package jp.digitalmuseum.jogl;

import jp.digitalmuseum.jogl.JoglModel.Point;

/**
 * Coordinate system suitable for use with ARToolKit.
 *
 * <pre>OpenGL default coordinates:
 *
 *       Y
 *       |
 *       |
 *       +------X
 *     ／
 *   ／
 * Z
 *
 *ARToolKit coordinates:
 *       Z
 *       |     Y
 *       |   ／
 *        | ／
 *        +------X</pre>
 */
public class JoglCoordinates_ARToolKit extends JoglCoordinates {

	Point convert(Point point) {
		point.set(point.getX(), point.getZ()*-1, point.getY());
		return point;
	}
}
