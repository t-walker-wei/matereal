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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARCode;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPatt_Color_WITHOUT_PCA;
import jp.nyatla.nyartoolkit.core.transmat.NyARRectOffset;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResult;

/**
 * Marker class. Immutable.
 *
 * @author Jun KATO
 */
public class NapMarker {
	private NyARCode code;
	private NyARRectOffset offset;
	private double size;
	private NyARMatchPatt_Color_WITHOUT_PCA pattern;
	private Image[] images;
	NyARTransMatResult transMatResult = new NyARTransMatResult();

	/**
	 * Constructor with a marker file name and real size in [mm] specified.
	 * @throws NyARException
	 */
	public NapMarker(String fileName, double size) {
		this.size = size;
		offset = new NyARRectOffset();
		offset.setSquare(size);
		try {
			code = new NyARCode(16, 16);
			code.loadARPattFromFile(fileName);
			images = load(fileName);
		} catch (NyARException e) {
			throw new IllegalArgumentException("Failed to instantiate a marker object from file: "+fileName);
		}
		pattern = new NyARMatchPatt_Color_WITHOUT_PCA(code);
	}

	/**
	 * Get the pattern image..
	 */
	public NyARMatchPatt_Color_WITHOUT_PCA getPattern() {
		return pattern;
	}

	/**
	 * Get width of the pattern image.
	 */
	public int getWidth() {
		return code.getWidth();
	}

	/**
	 * Get height of the pattern image.
	 */
	public int getHeight() {
		return code.getHeight();
	}

	/**
	 * Get real size(width and height) of the marker in [mm].
	 */
	public double getRealSize() {
		return size;
	}

	/**
	 * Get marker image.
	 * @param direction Direction of the image.
	 */
	public Image getImage(int direction) {
		return images[direction];
	}

	NyARRectOffset getOffset() {
		return offset;
	}

	/**
	 * Load marker pattern file and return images.
	 * @param fileName
	 * @return An array of loaded images.
	 */
	public static Image[] load(String fileName) {
		BufferedImage[] images = new BufferedImage[4];
		try {
			StreamTokenizer st = new StreamTokenizer(new FileReader(fileName));
			boolean eof = false;
			int[] rgbTable = new int[] { 1, 0, 2 };
			for (int direction = 0; direction < 4; direction++) {
				images[direction] = new BufferedImage(16, 16,
						BufferedImage.TYPE_3BYTE_BGR);
				byte[] data = ((DataBufferByte) images[direction].getRaster()
						.getDataBuffer()).getData();
				for (int rgbFlag = 0; rgbFlag < 3 && !eof; rgbFlag++) {
					for (int i = 0; i < 256; i++) {
						switch (st.nextToken()) {
						case StreamTokenizer.TT_NUMBER:
							data[i * 3 + rgbTable[rgbFlag]] = (byte) (0xff & (int) st.nval);
							break;
						case StreamTokenizer.TT_EOF:
							eof = true;
							break;
						}
					}
				}
			}
		} catch (IOException e) {
			// Do nothing.
		}
		return images;
	}
}
