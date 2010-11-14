package jp.digitalmuseum.napkit;

import jp.nyatla.nyartoolkit.core.rasterreader.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster_BasicClass;

public class NyARRgbRaster_BGR extends NyARRgbRaster_BasicClass
{
	protected Object _buf;
	protected INyARRgbPixelReader _reader;
	/**
	 * バッファオブジェクトがアタッチされていればtrue
	 */
	protected boolean _is_attached_buffer;
	/**
	 *
	 * @param i_width
	 * @param i_height
	 * @param i_is_alloc
	 * @throws NyARException
	 */
	public NyARRgbRaster_BGR(int i_width, int i_height,boolean i_is_alloc) throws NyARException
	{
		super(i_width,i_height,NyARBufferType.BYTE1D_B8G8R8_24);
		if(!initInstance(this._size,i_is_alloc)){
			throw new NyARException();
		}
	}
	/**
	 *
	 * @param i_width
	 * @param i_height
	 * @throws NyARException
	 */
	public NyARRgbRaster_BGR(int i_width, int i_height) throws NyARException
	{
		super(i_width,i_height,NyARBufferType.BYTE1D_B8G8R8_24);
		if(!initInstance(this._size,true)){
			throw new NyARException();
		}
	}
	protected boolean initInstance(NyARIntSize i_size,boolean i_is_alloc)
	{
		// case NyARBufferType.BYTE1D_B8G8R8_24:
		this._buf=i_is_alloc?new byte[i_size.w*i_size.h*3]:null;
		this._reader=new NyARRgbPixelReader_BYTE1D_B8G8R8_24((byte[])this._buf,i_size);

		this._is_attached_buffer=i_is_alloc;
		return true;
	}
	public INyARRgbPixelReader getRgbPixelReader() throws NyARException
	{
		return this._reader;
	}
	public Object getBuffer()
	{
		return this._buf;
	}
	public boolean hasBuffer()
	{
		return this._buf!=null;
	}
	public void wrapBuffer(Object i_ref_buf) throws NyARException
	{
		assert(!this._is_attached_buffer);//バッファがアタッチされていたら機能しない。
		this._buf=i_ref_buf;
		//ピクセルリーダーの参照バッファを切り替える。
		this._reader.switchBuffer(i_ref_buf);
	}
}
