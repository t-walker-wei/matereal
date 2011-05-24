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
package jp.digitalmuseum.mr.gui.workflow;

import edu.umd.cs.piccolo.nodes.PText;
import jp.digitalmuseum.mr.workflow.ControlNode;
import jp.digitalmuseum.mr.workflow.Fork;

public class PControlNode extends PNodeAbstractImpl {
	private static final long serialVersionUID = -3824502669188344253L;
	private static final int width = 3;

	public static int getAreaWidth() {
		return width;
	}

	public PControlNode(ControlNode controlNode) {
		super(controlNode);

		PText pText = new PText();
		pText.setText(controlNode.getClass().getSimpleName());
		addChild(pText);

		if (controlNode instanceof Fork) {
			setPathToRectangle(
					super.getAreaWidth() - width, 0,
					width, super.getAreaHeight());
			pText.setOffset(
					super.getAreaWidth() - width - pText.getWidth() - 10, 0);
		} else {
			setPathToRectangle(
					0, 0,
					width, super.getAreaHeight());
			pText.setOffset(
					10 - width, 0);
		}
	}
}