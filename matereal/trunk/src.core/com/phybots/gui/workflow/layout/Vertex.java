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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.phybots.gui.workflow.layout.Layout.Coordinate;
import com.phybots.workflow.Edge;
import com.phybots.workflow.Node;


public class Vertex extends LayerElement {

	private Node node;
	private Map<Vertex, Edge> children;
	private Map<Vertex, Edge> parents;
	private int width = 0;

	public Vertex(Node node) {
		this.node = node;
		children = new HashMap<Vertex, Edge>();
		parents = new HashMap<Vertex, Edge>();
	}

	public Node getNode() {
		return node;
	}

	void linkChild(Vertex child, Edge edge) {
		children.put(child, edge);
		child.parents.put(this, edge);
	}

	Edge unlinkChild(Vertex child) {
		Edge edge = children.remove(child);
		if (edge != null) {
			child.parents.remove(this);
		}
		return edge;
	}

	Set<Vertex> getParents() {
		return new HashSet<Vertex>(parents.keySet());
	}

	public Set<Vertex> getChildren() {
		return new HashSet<Vertex>(children.keySet());
	}

	public Map<Vertex, Edge> getChildrenEdges() {
		return new HashMap<Vertex, Edge>(children);
	}

	boolean hasChildren() {
		return children.size() > 0;
	}

	void setWidth(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	@Override
	protected void appendString(StringBuilder sb) {
		if (node != null) {
			sb.append("vx-");
			sb.append(node);
		}
		super.appendString(sb);
	}

	public Coordinate getCoord() {
		Coordinate coord = new Coordinate();
		coord.x = getX();
		coord.y = getDepth();
		return coord;
	}
}