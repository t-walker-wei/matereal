/*
 * PROJECT: Phybots at http://phybots.com/
 * ----------------------------------------------------------------------------
 *
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
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
 * The Original Code is Phybots.
 *
 * The Initial Developer of the Original Code is Jun Kato.
 * Portions created by the Initial Developer are
 * Copyright (C) 2009 Jun Kato. All Rights Reserved.
 *
 * Contributor(s): Jun Kato
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
/**
 *
 */
package jp.digitalmuseum.capture;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ImagePacket implements Comparable<ImagePacket> {
	static public final int MAX_UDPLEN = 65535;
	static public final int MAX_DATALEN = 65400;

	/** UDP package ID */
	private short udpid;
	/** File ID */
	private short id;
	/** File piece data */
	private byte[] data;
	/** Max pages (Number of pieces) */
	private short pages;
	/** Current page */
	private short curpage;
	/** Total length */
	private int length;
	/** File piece data length */
	private int curlength;
	private short width;
	private short height;

	public short getUdpid() {
		return udpid;
	}
	public void setUdpid(short udpid) {
		this.udpid = udpid;
	}
	public short getId() {
		return id;
	}
	public void setId(short id) {
		this.id = id;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public short getPages() {
		return pages;
	}
	public void setPages(short pages) {
		this.pages = pages;
	}
	public short getCurpage() {
		return curpage;
	}
	public void setCurpage(short curpage) {
		this.curpage = curpage;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public int getCurlength() {
		return curlength;
	}
	public void setCurlength(int curlength) {
		this.curlength = curlength;
	}
	public short getWidth() {
		return width;
	}
	public void setWidth(short width) {
		this.width = width;
	}
	public short getHeight() {
		return height;
	}
	public void setHeight(short height) {
		this.height = height;
	}

	public void read(DataInputStream in) throws IOException {
		udpid =			in.readShort();
		id =			in.readShort();
		curpage =		in.readShort();
		pages =			in.readShort();
		curlength =		in.readInt();
		length =		in.readInt();
		width =			in.readShort();
		height =		in.readShort();
		data = new byte[curlength];
		in.read(data);
	}

	public void write(DataOutputStream out) throws IOException {
		out.writeShort(	udpid);
		out.writeShort(	id);
		out.writeShort(	curpage);
		out.writeShort(	pages);
		out.writeInt(	curlength);
		out.writeInt(	data.length);
		out.writeShort(	width);
		out.writeShort(	height);
		out.write(data, curpage*MAX_DATALEN, curlength);
	}

	public void clear() {
		udpid = 0;
		id = 0;
		data = null;
		pages = 0;
		curpage = 0;
		length = 0;
		curlength = 0;
		width = 0;
		height = 0;
	}

	public int compareTo(ImagePacket o) {
		return o.curpage - curpage;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ImagePacket) {
			final ImagePacket o_ = (ImagePacket) o;
			return o_.id == id &&
					o_.udpid == udpid &&
					o_.curpage == curpage;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id*udpid*curpage;
	}

	@Override
	public String toString() {
		return "image no."+id+" (packet no."+(curpage+1)+" of "+pages+", id "+udpid+")";
	}
}