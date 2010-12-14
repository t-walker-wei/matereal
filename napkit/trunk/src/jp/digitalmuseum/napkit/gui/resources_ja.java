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
package jp.digitalmuseum.napkit.gui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.PropertyResourceBundle;

public class resources_ja extends PropertyResourceBundle {

	private final static String resourcesString =
		"MarkerDetectorPanel.descriptionHere=選択された映像ソースの情報がここに表示されます.\n" +
		"MarkerDetectorPanel.width=幅:\n" +
		"MarkerDetectorPanel.height=高さ:\n" +
		"MarkerDetectorPanel.fps=fps:\n" +
		"MarkerDetectorPanel.threshold=閾値:\n" +
		"MarkerDetectorPanel.showBinarizedImage=二値化された画像を表示\n" +
		"MarkerDetectorPanel.slideToDetectRobustlly=正方形が安定して検出されるようにバーをドラッグして閾値を調整してください.\n" +
		"TypicalMDCPane.detector=マーカー検出器\n" +
		"TypicalMDCPane.entity=検出対象\n" +
		"TypicalMDCPane.worldCoord=世界座標系\n" +
		"MarkerEntityPanel.2=選択されたマーカー:\n" +
		"MarkerEntityPanel.0=※マーカーパターンは↑←↓→の向きに表示されています";

	public resources_ja() throws IOException {
		super(new ByteArrayInputStream(resourcesString.getBytes()));
	}
}
