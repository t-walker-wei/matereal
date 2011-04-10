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

import java.lang.reflect.Constructor;

import jp.nyatla.nyartoolkit.core.rasterreader.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster_BasicClass;

public class NyARRgbRaster extends NyARRgbRaster_BasicClass
{
	private static final int T_BYTE1D =0x00010000;
	private static final int T_INT1D  =0x00040000;

	protected Object buffer;
	protected INyARRgbPixelReader reader;

	protected boolean isAttachedBuffer;

	public static int getBufferType(Class<? extends INyARRgbPixelReader> pixelReaderClass) {
		if (NyARRgbPixelReader_BYTE1D_B8G8R8_24.class.isAssignableFrom(pixelReaderClass)) {
			return NyARBufferType.BYTE1D_B8G8R8_24;
		} else if (NyARRgbPixelReader_BYTE1D_R8G8B8_24.class.isAssignableFrom(pixelReaderClass)) {
			return NyARBufferType.BYTE1D_R8G8B8_24;
		} else if (NyARRgbPixelReader_BYTE1D_B8G8R8X8_32.class.isAssignableFrom(pixelReaderClass)) {
			return NyARBufferType.BYTE1D_B8G8R8X8_32;
		} else if (NyARRgbPixelReader_BYTE1D_X8R8G8B8_32.class.isAssignableFrom(pixelReaderClass)) {
			return NyARBufferType.BYTE1D_X8R8G8B8_32;
		} else if (NyARRgbPixelReader_INT1D_GRAY_8.class.isAssignableFrom(pixelReaderClass)) {
			return NyARBufferType.INT1D_GRAY_8;
		} else if (NyARRgbPixelReader_INT1D_X8R8G8B8_32.class.isAssignableFrom(pixelReaderClass)) {
			return NyARBufferType.INT1D_X8R8G8B8_32;
		}
		return 0;
	}

	public NyARRgbRaster(int width, int height, Class<? extends INyARRgbPixelReader> pixelReaderClass)
	{
		super(width, height, getBufferType(pixelReaderClass));
		initInstance(this._size, pixelReaderClass, false);
	}

	public NyARRgbRaster(int width, int height) throws NyARException
	{
		this(width, height, NyARRgbPixelReader_BYTE1D_B8G8R8_24.class);
	}

	protected boolean initInstance(NyARIntSize size, Class<? extends INyARRgbPixelReader> pixelReaderClass, boolean isAlloc)
	{
		int bufferType = getBufferType(pixelReaderClass);

		// 0:24bit, 1:32bit, 2:16bit
		int bufferSize = bufferType >> 16 & 0x3;
		bufferSize = bufferSize == 0 ? 3 : (bufferSize == 1 ? 4 : 2);

		// 1:byte[], 2:int[][], 3:short[], 4:int[], ...
		Class<?> type;
		if ((bufferType & T_BYTE1D) != 0) {
			type = byte[].class;
			this.buffer = isAlloc ? new byte[size.w*size.h*bufferSize] : null;
		} else if ((bufferType & T_INT1D) != 0) {
			type = int[].class;
			this.buffer = isAlloc ? new int[size.w*size.h] : null;
		} else {
			return false;
		}
		this.isAttachedBuffer = isAlloc;

		// Instantiate pixel reader.
		try {
			Constructor<? extends INyARRgbPixelReader> constructor =
				pixelReaderClass.getConstructor(type, NyARIntSize.class);
			this.reader = constructor.newInstance(this.buffer, size);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public INyARRgbPixelReader getRgbPixelReader()
	{
		return this.reader;
	}

	public Object getBuffer()
	{
		return this.buffer;
	}

	public boolean hasBuffer()
	{
		return this.buffer != null;
	}

	public void wrapBuffer(Object buffer) throws NyARException
	{
		assert(!this.isAttachedBuffer);
		this.buffer = buffer;
		this.reader.switchBuffer(buffer);
	}
}
