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
package jp.digitalmuseum.mr.task;

import java.util.List;

import jp.digitalmuseum.mr.entity.Resource;
import jp.digitalmuseum.mr.resource.PenController;
import jp.digitalmuseum.mr.resource.Pen.PenStatus;
import jp.digitalmuseum.mr.task.TaskAbstractImpl;

public class PutPen extends TaskAbstractImpl {
	private static final long serialVersionUID = 112502362134082448L;
	private transient PenController pen;

	@Override
	public String getName() {
		return "Put a pen";
	}

	@Override
	public List<Class<? extends Resource>> getRequirements() {
		final List<Class<? extends Resource>> resourceTypes = super.getRequirements();
		resourceTypes.add(PenController.class);
		return resourceTypes;
	}

	@Override
	protected void onAssigned() {
		pen = (PenController) getResourceMap().get(PenController.class);
	}

	@Override
	protected void onStart() {
		pen.putPen();
	}

	public void run() {
		if (pen != null && pen.getStatus() == PenStatus.DOWN) {
			finish();
		}
	}
}
