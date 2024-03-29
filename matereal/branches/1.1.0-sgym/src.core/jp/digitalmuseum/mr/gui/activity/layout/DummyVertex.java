/*
 * PROJECT: matereal at http://mr.digitalmuseum.jp/
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
 * The Original Code is matereal.
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
package jp.digitalmuseum.mr.gui.activity.layout;


public class DummyVertex extends Vertex {
	private Segment segment;
	private boolean isHead;
	private boolean isTail;

	public DummyVertex() {
		super(null);
		this.segment = null;
		this.isHead = false;
		this.isTail = false;
	}

	public DummyVertex(Segment segment, boolean isHead) {
		super(null);
		this.segment = segment;
		this.isHead = isHead;
		this.isTail = !isHead;
	}

	public boolean isHead() {
		return isHead;
	}

	public boolean isTail() {
		return isTail;
	}

	public Segment getSegment() {
		return segment;
	}

	@Override
	public int getX() {
		if (segment == null) {
			return super.getX();
		} else {
			return segment.getX();
		}
	}

	@Override
	public void setX(int x) {
		if (segment == null) {
			super.setX(x);
		} else {
			segment.setX(x);
		}
	}

	@Override
	protected void appendString(StringBuilder sb) {
		sb.append("v");
		if (isHead) {
			sb.append("h");
		} else if (isTail) {
			sb.append("t");
		} else {
			sb.append("d");
		}
		super.appendString(sb);
	}
}