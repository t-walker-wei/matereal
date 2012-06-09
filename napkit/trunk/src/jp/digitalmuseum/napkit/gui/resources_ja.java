/*
 * PROJECT: napkit at http://mr.digitalmuseum.jp/
 * ----------------------------------------------------------------------------
 *
 * This file is part of NyARToolkit Application Toolkit.
 *
 * NyARToolkit Application Toolkit, or simply "napkit",
 * is a simple wrapper library for NyARToolkit.
 *
 * ----------------------------------------------------------------------------
 *
 * License version: GPL 3.0
 *
 * napkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * napkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with napkit. If not, see <http://www.gnu.org/licenses/>.
 */
package jp.digitalmuseum.napkit.gui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.PropertyResourceBundle;

import com.phybots.utils.StringResourceParser;


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
		super(new ByteArrayInputStream(
				StringResourceParser.escapeUnicode(resourcesString).getBytes()));
	}
}
