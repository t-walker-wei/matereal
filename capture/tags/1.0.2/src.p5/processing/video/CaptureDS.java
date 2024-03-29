/*
 * PROJECT: capture at http://matereal.sourceforge.jp/
 * ----------------------------------------------------------------------------
 *
 * This file is part of Webcam capture package.
 * Webcam capture package, or simply "capture",
 * is a simple package for capturing real-time images using webcams.
 *
 * ----------------------------------------------------------------------------
 *
 * License version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is capture.
 *
 * The Initial Developer of the Original Code is Jun KATO.
 * Portions created by the Initial Developer are
 * Copyright (C) 2009 Jun KATO. All Rights Reserved.
 *
 * Contributor(s): Jun KATO
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 */
package processing.video;

import java.lang.reflect.Method;

import de.humatic.dsj.DSCapture;
import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSFiltergraph;
import de.humatic.dsj.DSMediaType;
import de.humatic.dsj.DSFilterInfo.DSPinInfo;

import processing.core.*;

/**
 * Processing library for capturing images via DirectShow on Windows.<br />
 * DirectShowによるキャプチャを実行するProcessing用ライブラリ<br />
 * <br />
 * Interface of this class is compatible with that of processing.video.Capture, a default class provided with Processing installation that captures images via QuickTime.<br />
 * このクラスの使い方は、Processingをインストールしたときに付属しているデフォルトのキャプチャ用クラスprocessing.video.Captureと同じです。
 *
 * @author arc@dmz
 */
public class CaptureDS extends PImage implements Runnable {

	/**
	 * <br />
	 * キャプチャ画像が更新されたとき呼ばれる、親アプレットのメソッド
	 */
	Method captureEventMethod;

	/** キャプチャデバイスの名前 */
	String name;

	/** スレッド */
	Thread runner;

	/** キャプチャデータが利用可能かどうかのフラグ */
	boolean available = false;

	/** キャプチャデータの幅 */
	public int dataWidth;
	/** キャプチャデータの高さ */
	public int dataHeight;

	/** トリミングのフラグ */
	public boolean crop;
	/** トリミングの範囲 */
	public int cropX, cropY, cropW, cropH;

	/**
	 * 画像の一時データ格納場所
	 * (DirectShowはRGBのバイト列でデータを渡してくるが、
	 *  ProcessingはARGBのint列で画像を処理する)
	 */
	public byte data[];

	/** キャプチャのフレームレート */
	public int frameRate;

	// DirectShowがらみのいろいろ
	/** DirectShowキャプチャデバイス */
	private DSCapture dsCapture;
	/** DirectShowフィルタ */
	private DSFilterInfo dsFilter;

	// コンストラクタ群
	public CaptureDS(PApplet parent, int requestWidth, int requestHeight) {
		this(parent, requestWidth, requestHeight, null, 30);
	}
	public CaptureDS(PApplet parent, int reqWidth, int reqHeight, int frameRate) {
		this(parent, reqWidth, reqHeight, null, frameRate);
	}
	public CaptureDS(PApplet parent, int reqWidth, int reqHeight, Object input) {
		this(parent, reqWidth, reqHeight, input, 30);
	}
	public CaptureDS(final PApplet parent, final int requestWidth,
		final int requestHeight, final Object input, final int frameRate) {
		init(parent, requestWidth, requestHeight, input, frameRate);
	}

	/**
	 * キャプチャデバイスを初期化する
	 * @param parent 親となるアプレット
	 * @param requestWidth 幅
	 * @param requestHeight 高さ
	 * @param input 入力デバイス(文字列かDSFilterInfoを指定する)
	 * @param frameRate フレームレート
	 */
	public void init(PApplet parent, int requestWidth, int requestHeight,
			Object input, int frameRate) {
		this.parent = parent;
		this.frameRate = frameRate;

		DSFilterInfo[] filters = null;
		DSPinInfo pin = null;
		DSMediaType[] formats = null;

		// 入力デバイスの指定
		if (input != null) {

			// フィルタの直接指定
			if (DSFilterInfo.class == input.getClass()) {
				dsFilter = (DSFilterInfo) input;

			// 名前による指定
			} else {
				final String name = input.toString();
				filters = DSCapture.queryDevices()[0];
				for (DSFilterInfo f : filters) {
					if (name.equals(f.getName())) {
						dsFilter = f;
					}
				}
			}
		}

		// 入力デバイスが指定されていなかったら適当に取得
		if (dsFilter == null) {
			if (filters == null) {
				filters = DSCapture.queryDevices()[0];
			}
			if (filters.length <= 0) {
				parent.die("No devices found.");
				return;
			}
			dsFilter = filters[0];
		}

		// キャプチャピンを取得
		DSPinInfo[] pins = dsFilter.getPins();
		if (pins == null) {
			parent.die("No devices with available pins found.");
			return;
		}
		pin = null;
		float fps = frameRate;
		float currentFps = Float.MAX_VALUE;
		for (DSPinInfo p : pins) {

			// Set the first pin as default.
			if (pin == null) pin = p;

			// Capture YUY2 and RGB none-compressed images by default.
			formats = p.getFormats();
			for (int i = 0; i < formats.length; i ++) {
				final DSMediaType format = formats[i];
				if (format.getWidth() == requestWidth
						&& format.getHeight() == requestHeight
						&& (format.getSubTypeString().contains("RGB")
								|| format.getSubTypeString().contains("YUY2"))) {
					if (Math.abs(fps - format.getFrameRate()) < Math.abs(fps - currentFps)) {
						pin = p;
						p.setPreferredFormat(i);
						currentFps = format.getFrameRate();
					}
					break;
				}
			}
			if (pin != null && (int) currentFps == (int) fps) {
				break;
			}
		}
		if (pin == null) {
			parent.die("No suitable pin to capture was found for filter:"+dsFilter);
		}

		// キャプチャを始める
		dsCapture = new DSCapture(
				DSFiltergraph.JAVA_POLL_RGB,
				dsFilter, false, DSFilterInfo.doNotRender(), null);
		dsCapture.play();

		// 実際に画像を取れるようになるまで待つ
		while (true) {
			final int dataSize = dsCapture.getDataSize();
			if (dataSize > 0) {
				dataWidth = requestWidth;
				dataHeight = dataSize / requestWidth / 3;
				break;
			}
			try { Thread.sleep(100); }
			catch (InterruptedException e) { }
		}

		// PImageとして初期化を実行
		super.init(dataWidth, dataHeight, RGB);

		// キャプチャ実行時に呼ぶコールバックメソッドを取得
		try {
			captureEventMethod = parent.getClass().getMethod("captureEvent",
					new Class[] { CaptureDS.class });
		} catch (Exception e) {
			// なければないで、別に問題ない
		}

		// 確実にdisposeされるように設定
		parent.registerDispose(this);

		// スレッドとして自身を開始する
		runner = new Thread(this);
		runner.start();
	}

	/** キャプチャできる状態かを返す */
	public boolean available() {
		return available;
	}

	/**
	 * キャプチャ結果からトリミングする範囲を指定する
	 * @param x 左上X座標
	 * @param y 左上Y座標
	 * @param w 幅
	 * @param h 高さ
	 */
	public void crop(int x, int y, int w, int h) {
		crop = true;
		cropX = Math.max(0, x);
		cropY = Math.max(0, y);
		cropW = Math.min(w, dataWidth);
		cropH = Math.min(dataHeight, y + h) - cropY;

		// 画像サイズが変わったら画像バッファを初期化しなおす
		if ((cropW != width) || (cropH != height)) {
			init(w, h, RGB);
		}
	}

	/**
	 * トリミングをしないようにする
	 */
	public void noCrop() {
		crop = false;
	}

	/**
	 * キャプチャした画像データを読む
	 * (通常、captureEventMethodのなかから呼ばれる)
	 */
	public void read() {
		loadPixels();
		synchronized (pixels) {
			data = dsCapture.getData();

			int index = 0;
			if (crop) {
				int byteIndex = cropX*3;
				final int byteOffset = (dataWidth - cropW)*3;
				for (int y = 0; y < cropH; y++) {
					for (int x = 0; x < cropW; x ++) {
						pixels[index ++] =
							((data[byteIndex ++] & 0xff) << 16) |
							((data[byteIndex ++] & 0xff) << 8) |
							(data[byteIndex ++] & 0xff);
					}
					byteIndex += byteOffset;
				}
			} else {
				int byteIndex = 0;
				final int dataLength = dataWidth*dataHeight;
				while (index < dataLength) {
					pixels[index ++] =
						((data[byteIndex ++] & 0xff) << 16) |
						((data[byteIndex ++] & 0xff) << 8) |
						(data[byteIndex ++] & 0xff);
				}
			}
			available = false;
			updatePixels();
		}
	}

	/** (画像を読める状態になったことにする) */
	public void run() {
		while ((Thread.currentThread() == runner) && (dsCapture != null)) {
			synchronized (dsCapture) {
				available = true;

				if (captureEventMethod != null) {
					try {
						captureEventMethod.invoke(parent,
								new Object[] { this });
					} catch (Exception e) {
						System.err.println("Disabling captureEvent() for "
								+ name + " because of an error.");
						e.printStackTrace();
						captureEventMethod = null;
					}
				}
			}
			try {
				Thread.sleep(1000 / frameRate);
			} catch (InterruptedException e) {
				//
			}
		}
	}

	/** フレームレートを指定し直す */
	public void frameRate(int iframeRate) {
		if (iframeRate <= 0) {
			System.err.println("Capture: ignoring bad frameRate of "
					+ iframeRate + " fps.");
			return;
		}
		frameRate = iframeRate;
	}

	/** キャプチャを止める */
	public void stop() {
		if (dsCapture != null) {
			dsCapture.stop();
			dsCapture = null;
		}
		runner = null;
	}

	/** (アプレットのdispose時に呼ばれる) */
	public void dispose() {
		stop();
	}

	/** 使えるデバイスの一覧を文字型配列で取得する */
	public static String[] list() {
		final DSFilterInfo[] filters = DSCapture.queryDevices()[0];
		final String[] names = new String[filters.length];
		for (int i = 0; i < filters.length; i ++) {
			names[i] = filters[i].getName();
		}
		return names;
	}
}
