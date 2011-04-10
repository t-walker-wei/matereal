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
package jp.digitalmuseum.napkit;

import java.awt.image.BufferedImage;

import jp.nyatla.nyartoolkit.NyARException;

/**
 * Binary raster image whose data is stored in an integer array.
 *
 * @author Jun KATO
 */
public class NyARBinRaster extends jp.nyatla.nyartoolkit.core.raster.NyARBinRaster implements NapRaster {

	/** Image object */
	private BufferedImage image;
	/** Image data */
	private byte[] pixels;

	public NyARBinRaster(int width, int height) throws NyARException {
		super(width, height);
	}

	public BufferedImage getImage() {
		if (image == null ||
				image.getWidth() != _size.w ||
				image.getHeight() != _size.h) {
			image = new BufferedImage(_size.w, _size.h, BufferedImage.TYPE_BYTE_BINARY);
			pixels = new byte[_size.w*_size.h];
		}
		for (int i = 0; i < pixels.length; i ++) { pixels[i] = (byte) ((int[])_buf)[i]; }
		image.getRaster().setDataElements(0, 0, _size.w, _size.h, pixels);
		return image;
	}
}
