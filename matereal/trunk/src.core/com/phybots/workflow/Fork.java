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
package com.phybots.workflow;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Fork extends ControlNode {
	private static final long serialVersionUID = 8421184101422231273L;
	private Set<EdgeImpl> edges;
	private Set<Node> nodesToGo;

	public Fork(Node... outs) {
		edges = new HashSet<EdgeImpl>();
		nodesToGo = new HashSet<Node>();
		for (Node out : outs) {
			edges.add(new EdgeImpl(this, out));
		}
	}

	public EdgeImpl[] getEdges() {
		EdgeImpl[] edgesArray = new EdgeImpl[0];
		edgesArray = edges.toArray(edgesArray);
		return edgesArray;
	}

	public Node[] getOutputs() {
		Node[] outs = new Node[edges.size()];
		int i = 0;
		for (EdgeImpl edge : edges) {
			outs[i ++] = edge.getDestination();
		}
		return outs;
	}

	@Override
	protected void onEnter() {
		for (EdgeImpl edge : edges) {
			nodesToGo.add(edge.getDestination());
		}
		isDone();
	}

	@Override
	protected synchronized boolean isDone() {
		Iterator<Node> nodeIterator = nodesToGo.iterator();
		Node node;
		while (nodeIterator.hasNext()) {
			node = nodeIterator.next();
			if (getWorkflow().start(node)) {
				nodeIterator.remove();
			}
		}
		return nodesToGo.isEmpty();
	}

	@Override
	public String toString() {
		return "Fork["+edges.size()+"]";
	}
}
