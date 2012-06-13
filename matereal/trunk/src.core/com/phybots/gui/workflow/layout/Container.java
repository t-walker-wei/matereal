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
package com.phybots.gui.workflow.layout;

import java.util.Iterator;
import java.util.LinkedList;

public class Container extends LayerElement implements Cloneable, Iterable<Segment> {
	private static final long serialVersionUID = -943946970450778123L;
	private static long globalId = 0;
	private long id;
	private LinkedList<Segment> list;

	public Container() {
		id = globalId ++;
		this.list = new LinkedList<Segment>();
	}

	public long getId() {
		return id;
	}

	public void append(Segment segment) {
		list.addLast(segment);
		segment.setContainer(this);
	}

	public void join(Container container) {
		if (this.equals(container)) {
			throw new IllegalArgumentException("Target container equals to the original container.");
		}
		for (Segment segment : container.list) {
			append(segment);
		}
	}

	public Container split(Segment segment) {
		Container container = new Container();
		Iterator<Segment> it = list.iterator();
		boolean isFound = false;
		while (it.hasNext()) {
			Segment s = it.next();
			if (!isFound) {
				if (s.equals(segment)) {
					isFound = true;
					it.remove();
				}
			} else {
				container.append(s);
				it.remove();
			}
		}
		return container;
	}

	public Container split(int i) {
		Container container = new Container();
		Iterator<Segment> it = list.iterator();
		for (int j = 0; j < i && it.hasNext(); j ++) {
			it.next();
		}
		while (it.hasNext()) {
			container.append(it.next());
			it.remove();
		}
		return container;
	}

	public int size() {
		return list.size();
	}

	public Iterator<Segment> iterator() {
		return list.iterator();
	}

	@Override
	void setDepth(int depth) {
		throw new UnsupportedOperationException();
	}

	@Override
	int getDepth() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void appendString(StringBuilder sb) {
		sb.append("[#");
		sb.append(id);
		sb.append("/");
		sb.append((int) getMeasure());
		for (Segment s : this.list) {
			sb.append(" ");
			s.appendString(sb);
		}
		sb.append(" ]");
	}

	@Override
	public Container clone() {
		try {
			Container container = (Container) super.clone();
			container.list = new LinkedList<Segment>(list);
			return container;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}