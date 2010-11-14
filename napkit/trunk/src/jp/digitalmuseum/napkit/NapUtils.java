/*
 * PROJECT: napkit at http://matereal.sourceforge.jp/
 * ----------------------------------------------------------------------------
 *
 * This file is part of NyARToolkit Application Toolkit.
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
package jp.digitalmuseum.napkit;

import jp.digitalmuseum.utils.ScreenRectangle;
import jp.digitalmuseum.utils.ScreenPosition;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

/**
 * Napkit-related utility class.
 *
 * @author Jun KATO
 */
public class NapUtils {

	public static ScreenPosition convertToScreenPosition(NyARIntPoint2d vertex) {
		return new ScreenPosition(vertex.x, vertex.y);
	}

	public static ScreenRectangle convertToScreenRectangle(NyARIntPoint2d[] vertex) {
		return new ScreenRectangle(
				NapUtils.convertToScreenPosition(vertex[0]),
				NapUtils.convertToScreenPosition(vertex[1]),
				NapUtils.convertToScreenPosition(vertex[2]),
				NapUtils.convertToScreenPosition(vertex[3])
			);
	}

	/**
	 * Returns a default camera parameter object.
	 */
	public static NyARParam getInitialCameraParameter() {
		final NyARParam p = new NyARParam();
		final NyARIntSize size = p.getScreenSize();
		size.w = 640;
		size.h = 480;
		p.setValue(new double[] {
			318.5,
			263.5,
			26.2,
			1.0127565206658486
		}, new double[] {
			700.9514702992245,
			0.0,
			316.5,
			0.0,
			0.0,
			726.0941816535367,
			241.5,
			0.0,
			0.0,
			0.0,
			1.0,
			0.0
		});
		return p;
	}
}
