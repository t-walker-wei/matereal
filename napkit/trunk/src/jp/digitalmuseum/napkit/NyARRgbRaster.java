package jp.digitalmuseum.napkit;

import java.lang.reflect.Constructor;

import jp.nyatla.nyartoolkit.core.rasterreader.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster_BasicClass;

public class NyARRgbRaster extends NyARRgbRaster_BasicClass
{
	protected Object buffer;
	protected INyARRgbPixelReader reader;

	/**
	 * バッファオブジェクトがアタッチされていればtrue
	 */
	protected boolean isAttachedBuffer;

	private static int getBufferType(Class<? extends INyARRgbPixelReader> pixelReaderClass) {
		if (NyARRgbPixelReader_BYTE1D_B8G8R8_24.class.isAssignableFrom(pixelReaderClass)) {
			return NyARBufferType.BYTE1D_B8G8R8_24;
		} else if (NyARRgbPixelReader_BYTE1D_R8G8B8_24.class.isAssignableFrom(pixelReaderClass)) {
			return NyARBufferType.BYTE1D_R8G8B8_24;
		}
		return -1;
	}

	public NyARRgbRaster(int width, int height, Class<? extends INyARRgbPixelReader> pixelReaderClass) throws NyARException
	{
		super(width, height, getBufferType(pixelReaderClass));
		if(!initInstance(this._size, pixelReaderClass, true)){
			throw new NyARException();
		}
	}

	public NyARRgbRaster(int width, int height) throws NyARException
	{
		this(width, height, NyARRgbPixelReader_BYTE1D_B8G8R8_24.class);
	}

	protected boolean initInstance(NyARIntSize size, Class<? extends INyARRgbPixelReader> pixelReaderClass, boolean isAlloc)
	{
		this.buffer = isAlloc ? new byte[size.w*size.h*3] : null;
		try {
			Constructor<? extends INyARRgbPixelReader> constructor =
					pixelReaderClass.getConstructor(byte[].class, NyARIntSize.class);
			this.reader = constructor.newInstance((byte[]) this.buffer, size);
		} catch (Exception e) {
			return false;
		}

		this.isAttachedBuffer=isAlloc;
		return true;
	}

	public INyARRgbPixelReader getRgbPixelReader() throws NyARException
	{
		return this.reader;
	}

	public Object getBuffer()
	{
		return this.buffer;
	}

	public boolean hasBuffer()
	{
		return this.buffer!=null;
	}

	public void wrapBuffer(Object buffer) throws NyARException
	{
		//バッファがアタッチされていたら機能しない。
		assert(!this.isAttachedBuffer);

		this.buffer=buffer;

		//ピクセルリーダーの参照バッファを切り替える。
		this.reader.switchBuffer(buffer);
	}
}
