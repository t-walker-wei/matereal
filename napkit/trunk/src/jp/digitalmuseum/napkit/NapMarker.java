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

import java.util.HashMap;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARCode;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPatt_Color_WITHOUT_PCA;


/**
 * Marker class. Immutable.
 *
 * @author Jun KATO
 */
public class NapMarker {
	private static HashMap<String, NyARCode> codeMap;
	private NyARCode code;
	private double size;
	private NyARMatchPatt_Color_WITHOUT_PCA pattern;

	static {
		codeMap = new HashMap<String, NyARCode>();
	}

	/**
	 * Constructor with a marker file name and real size in [mm] specified.
	 * @throws NyARException
	 */
	public NapMarker(String fileName, double size) {
		this.size = size;
		code = getCode(fileName);
		pattern = new NyARMatchPatt_Color_WITHOUT_PCA(code);
	}

	private static NyARCode getCode(String fileName) {
		NyARCode code = codeMap.get(fileName);
		if (code == null) {
			try {
				code = new NyARCode(16, 16);
				code.loadARPattFromFile(fileName);
			} catch (NyARException e) {
				throw new IllegalArgumentException("Failed to instantiate a marker object from file: "+fileName);
			}
			codeMap.put(fileName, code);
		}
		return code;
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
}
