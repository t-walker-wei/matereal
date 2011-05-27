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
package jp.digitalmuseum.mr.workflow;

import java.util.HashSet;
import java.util.Set;

import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.message.WorkflowNodeEvent;
import jp.digitalmuseum.mr.message.WorkflowNodeStatus;

public class Join extends ControlNode implements EventListener {
	private static final long serialVersionUID = -6755092474761544807L;
	private Set<EdgeImpl> edges;
	private Set<Node> nodesToWait;

	public Join(Node... ins) {
		edges = new HashSet<EdgeImpl>();
		nodesToWait = new HashSet<Node>();
		for (Node in : ins) {
			edges.add(new EdgeImpl(in, this));
		}
	}

	public EdgeImpl[] getEdges() {
		EdgeImpl[] edgesArray = new EdgeImpl[0];
		edgesArray = edges.toArray(edgesArray);
		return edgesArray;
	}

	public Node[] getInputs() {
		Node[] inputs = new Node[edges.size()];
		int i = 0;
		for (EdgeImpl edge : edges) {
			inputs[i ++] = edge.getSource();
		}
		return inputs;
	}

	@Override
	protected void onEnter() {
		for (EdgeImpl edge : edges) {
			Node node = edge.getSource();
			nodesToWait.add(node);
			node.addEventListener(this);
		}
	}

	@Override
	protected synchronized boolean isDone() {
		return nodesToWait.isEmpty();
	}

	public void eventOccurred(Event e) {
		if (e instanceof WorkflowNodeEvent) {
			WorkflowNodeEvent wne = (WorkflowNodeEvent) e;
			if (wne.getStatus() == WorkflowNodeStatus.LEFT) {
				Node node = wne.getSource();
				nodesToWait.remove(node);
				node.removeEventListener(this);
			}
		}
	}

	@Override
	public String toString() {
		return "Join["+edges.size()+"]";
	}
}
