/**
 * ImageCodec
 *
 * Copyright (c) 2009 arc@dmz
 * http://digitalmuseum.jp/
 * All rights reserved.
 */
package jp.digitalmuseum.capture;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

/**
 * BufferedImage <-> JPEG/PNG byte[] Codec.
 * @author Jun Kato
 */
public class ImageCodec {
	static public enum Codec {
		JPEG("jpg"),
		PNG("png");
		private final String text;
		private Codec(String text) { this.text = text; }
		public String toString() { return this.text; }
	}
	static public final String DEFAULT_CODEC = Codec.JPEG.toString();
	static public final float DEFAULT_QUALITY = 0.5f;
	static private final int[] bgrBands = new int[] { 2, 1, 0 };
	static private final ImageTypeSpecifier imageType = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_3BYTE_BGR);

	final static private ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	static private String encoderFormat;
	static private ImageWriter imageWriter;
	static public byte[] encodeImage(BufferedImage image, String format, float quality) {

		// Get an encoder and set parameter.
		if (encoderFormat == null ||
				!encoderFormat.equals(format)) {
			final Iterator<ImageWriter> iws = ImageIO.getImageWritersByFormatName(format);
			if (!iws.hasNext()) return null;
			imageWriter = iws.next();
			encoderFormat = format;
		}
		final ImageWriteParam prm = imageWriter.getDefaultWriteParam();
		prm.setDestinationType(imageType);
		prm.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		prm.setCompressionQuality(quality);

		// Initialize an output stream and write data.
		try {
			imageWriter.setOutput(ImageIO.createImageOutputStream(outStream));
			imageWriter.write(null, new IIOImage(image, null, null), prm);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		// Return data as a byte array.
		final byte[] data = outStream.toByteArray();
		outStream.reset();
		return data;
	}

	static public byte[] encodeImage(BufferedImage image) {
		return encodeImage(image, DEFAULT_CODEC, DEFAULT_QUALITY);
	}

	static public byte[] encodeImage(BufferedImage image, String format) {
		return encodeImage(image, format, DEFAULT_QUALITY);
	}

	static public byte[] encodeImage(BufferedImage image, float quality) {
		return encodeImage(image, DEFAULT_CODEC, quality);
	}

	static private String decoderFormat;
	static private ImageReader imageReader;
	static public BufferedImage decodeImage(byte[] data, String format) {

		// Get a decoder.
		if (decoderFormat == null ||
				!decoderFormat.equals(format)) {
			final Iterator<ImageReader> irs = ImageIO.getImageReadersByFormatName(format);
			if (!irs.hasNext()) return null;
			imageReader = irs.next();
			decoderFormat = format;
		}
		final ImageReadParam prm = imageReader.getDefaultReadParam();
		prm.setSourceBands(bgrBands);
		prm.setDestinationType(imageType);

		// Decode a byte array into a BufferedImage object.
		final ByteArrayInputStream in = new ByteArrayInputStream(data);
		try {
			imageReader.setInput(ImageIO.createImageInputStream(in));
			return imageReader.read(0, prm);
		} catch (IOException e) {
			return null;
		}
	}

	static public BufferedImage decodeImage(byte[] data) {
		return decodeImage(data, DEFAULT_CODEC);
	}
}