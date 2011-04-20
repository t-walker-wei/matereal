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

import java.util.Iterator;

import jp.digitalmuseum.utils.Array;

public class Layer implements Iterable<LayerElement>, Cloneable {
	private Array<LayerElement> elements;
	private int depth;

	public Layer(int depth) {
		this.elements = new Array<LayerElement>();
		setDepth(depth);
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getDepth() {
		return depth;
	}

	public void push(LayerElement element) {
		elements.push(element);
		element.setDepth(depth);
	}

	public LayerElement get(int i) {
		return elements.get(i);
	}

	public boolean contains(LayerElement element) {
		return elements.contains(element);
	}

	public int size() {
		return elements.size();
	}

	public void clear() {
		elements.clear();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Layer ");
		sb.append(depth);
		sb.append(": [ ");
		for (LayerElement e : this) {
			e.appendString(sb);
			sb.append(" ");
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	public Iterator<LayerElement> iterator() {
		return elements.iterator();
	}

	public Layer clone() {
		Layer layer = new Layer(depth);
		layer.elements = new Array<LayerElement>(elements);
		return layer;
	}
}