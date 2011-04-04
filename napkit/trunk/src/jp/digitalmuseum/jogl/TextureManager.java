package jp.digitalmuseum.jogl;

import java.io.IOException;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.media.opengl.GL;

import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

/**
 *
 */
public class TextureManager {

	private GL gl;

	private HashMap<String, Integer> textures;

	public TextureManager(GL gl) {
		this.gl = gl;
		textures = new HashMap<String, Integer>();
	}

	public int getGLTexture(URL url, String textureFileName, String textureAlphaFileName, boolean forceReload) {

		if (textureFileName == null) {
			return 0;
		}

		// Get parent directory for the texture files.
		String dir = url.toString();
		dir = dir.substring(0, dir.lastIndexOf('/') + 1);

		// Get ID for already generated texture.
		String key = dir + textureFileName + textureAlphaFileName;
		int id;
		if (textures.containsKey(key)) {
			id = textures.get(key);
			if (forceReload) {
				gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
				glDeleteTexture(id);
				textures.remove(key);
			} else {
				return id;
			}
		}

		// Load texture data.
		TextureData textureData;
		try {
			textureData = TextureIO.newTextureData(new URL(dir + textureFileName), false, null);
		} catch (IOException e) {
			// Failed to load texture data.
			return 0;
		}
		Buffer textureBuffer = textureData.getBuffer();
		byte[] textureBufferArray = null;
		if (textureBuffer instanceof ByteBuffer) {
			textureBufferArray = ((ByteBuffer) textureBuffer).array();
		} else {
			// Failed to load texture data.
			return 0;
		}
		int width = textureData.getWidth();
		int height = textureData.getHeight();

		// Load texture alpha data.
		byte[] textureAlphaBufferArray = null;
		boolean alphaMustFlipVertically = false;
		try {
			if (textureAlphaFileName != null) {
				TextureData textureAlphaData = TextureIO.newTextureData(
						new URL(dir + textureAlphaFileName), false, null);
				if (textureAlphaData != null) {
					textureBuffer = textureAlphaData.getBuffer();
					if (textureBuffer instanceof ByteBuffer) {
						if (width == textureAlphaData.getWidth() &&
								height == textureAlphaData.getHeight()) {
							textureAlphaBufferArray = ((ByteBuffer) textureBuffer).array();
						}
					}
					alphaMustFlipVertically = textureAlphaData.getMustFlipVertically();
				}
			}
		} catch (IOException ioe) {
			try {
				Tga tga = new Tga(new URL(dir + textureAlphaFileName));
				if (height == tga.getHeight() && width == tga.getWidth()) {
					textureAlphaBufferArray = tga.getDataReference();
					alphaMustFlipVertically = tga.isVerticallyInverted();
				}
			} catch (IOException e) {
				// Failed to load alpha texture data.
			}
		}

		// Create RGBA byte buffer for texture generation.
		ByteBuffer buf = ByteBuffer.allocateDirect(width * height * 4);
		buf.order(ByteOrder.nativeOrder());

		boolean mustFlipVertically = textureData.getMustFlipVertically();
		int initialY, dh;
		if (mustFlipVertically) {
			initialY = 0;
			dh = 1;
		} else {
			initialY = height - 1;
			dh = -1;
		}
		int pixelFormat = textureData.getPixelFormat();
		int bytePerPixel = textureBufferArray.length / (width * height);
		int alphaBytePerPixel = textureAlphaBufferArray == null ?
				0 : textureAlphaBufferArray.length / (width * height);
		for (int y = initialY; 0 <= y && y < height; y += dh) {
			for (int wp = 0; wp < width; wp++) {
				if (pixelFormat == GL.GL_BGR || pixelFormat == GL.GL_BGRA) {
					buf.put(textureBufferArray[(y * width + wp) * bytePerPixel + 2]);
					buf.put(textureBufferArray[(y * width + wp) * bytePerPixel + 1]);
					buf.put(textureBufferArray[(y * width + wp) * bytePerPixel + 0]);
				} else {
					buf.put(textureBufferArray[(y * width + wp) * bytePerPixel + 0]);
					buf.put(textureBufferArray[(y * width + wp) * bytePerPixel + 1]);
					buf.put(textureBufferArray[(y * width + wp) * bytePerPixel + 2]);
				}
				byte alpha;
				if (textureAlphaBufferArray != null) {
					int ay = mustFlipVertically == alphaMustFlipVertically ? y : height - y - 1;
					alpha = textureAlphaBufferArray[(ay * width + wp) * alphaBytePerPixel + alphaBytePerPixel - 1];
				} else if (pixelFormat == GL.GL_BGRA || pixelFormat == GL.GL_RGBA) {
					alpha = textureBufferArray[(y * width + wp) * bytePerPixel + 3];
				} else {
					alpha = (byte) 255;
				}
				buf.put(alpha);
			}
		}
		buf.position(0);

		// Generate texture object on OpenGL.
		id = glGenTexture();
		if (id == 0) {
			return 0;
		}
		gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
		gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1);

		gl.glBindTexture(GL.GL_TEXTURE_2D, id);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0,
				GL.GL_RGBA8, width, height, 0,
				GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buf);

		gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
		textures.put(key, id);
		return id;
	}

	private int glGenTexture() {
		int texs[] = new int[1];
		gl.glGenTextures(1, texs, 0);
		return texs[0];
	}

	private void glDeleteTexture(int tex) {
		int texs[] = new int[1];
		texs[0] = tex;
		gl.glDeleteTextures(1, texs, 0);
		return;
	}

	public void clear() {
		Collection<Integer> ids = textures.values();
		if (ids.size() <= 0) {
			return;
		}
		int[] idsArray = new int[ids.size()];
		Iterator<Integer> it = ids.iterator();
		for(int i = 0 ; i < idsArray.length ; i++) {
			idsArray[i] = it.next();
		}
		gl.glDeleteTextures(idsArray.length, idsArray, 0);
		textures.clear();
	}
}
