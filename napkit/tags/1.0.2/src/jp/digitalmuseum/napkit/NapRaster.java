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

import java.awt.image.BufferedImage;

import jp.nyatla.nyartoolkit.core.raster.INyARRaster;


/**
 * Raster interface compatible with NyARToolkit.
 *
 * @author Jun KATO
 * @see INyARRaster
 */
public interface NapRaster extends INyARRaster {

	/**
	 * Get the image as a BufferedImage object.
	 * @return
	 */
	public BufferedImage getImage();
}
