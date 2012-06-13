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
package com.phybots.gui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.PropertyResourceBundle;

import com.phybots.utils.StringResourceParser;


public class resources_ja extends PropertyResourceBundle {

	private final static String resourcesString =
		"Phybots.fonts=メイリオ,Meiryo,ＭＳ Ｐゴシック,MS UI Gothic\n" +
		"Phybots.debugTitle=ランタイム デバッグ ツール\n" +
		"CoordProviderPanel.centimeter=[cm]\n" +
		"CoordProviderPanel.apply=サイズの変更を適用\n" +
		"CoordProviderPanel.errorDescWidth=矩形の幅を半角の数字で入力してください.\n" +
		"CoordProviderPanel.errorTitle=Parse Error\n" +
		"CoordProviderPanel.errorDescHeight=矩形の高さを半角の数字で入力してください.\n" +
		"CoordProviderPanel.width=矩形の幅:\n" +
		"CoordProviderPanel.height=矩形の高さ:\n" +
		"CoordProviderPanel.reset=矩形のリセット\n" +
		"MonitorPane.graphs=ワークフロー グラフ\n" +
		"MonitorPane.entities=ロボットとその他の物体\n" +
		"MonitorPane.services=サービス\n" +
		"MonitorPane.bluetooth=Bluetooth\n" +
		"EntityMonitorPanel.selectedEntity=-\n" +
		"EntityMonitorPanel.nameOfSelectedEntity=選択された物体の名称.\n" +
		"EntityMonitorPanel.ok=OK\n" +
		"EntityMonitorPanel.cancel=キャンセル\n" +
		"EntityPanel.entityType=物体の種類\n" +
		"RobotPanel.resources=ロボットの部品\n" +
		"PhysicalRobotPanel.connector=コネクタ\n" +
		"ConnectorPanel.change=変更\n" +
		"ConnectorPanel.ok=OK\n" +
		"ResourceViewer.implementation=実装クラス:\n" +
		"ResourceViewer.status=状態:\n" +
		"WheelsViewer.rightWheel=右の車輪:\n" +
		"WheelsViewer.leftWheel=左の車輪:\n" +
		"MonitorPanel.nameOfSelectedService=選択されたサービスの名称.\n" +
		"MonitorPanel.nameOfSelectedServiceGroup=サービスグループの名称.\n" +
		"MonitorPanel.serviceGroup=サービスグループ:\n" +
		"MonitorPanel.interval=実行間隔:\n" +
		"MonitorPanel.millisecond=[ms]\n" +
		"ServiceMonitorPanel.start=開始\n" +
		"ServiceMonitorPanel.startIcon=○\n" +
		"ServiceMonitorPanel.stop=停止\n" +
		"ServiceMonitorPanel.stopIcon=□\n" +
		"GraphMonitorPanel.selectedGraph=-\n" +
		"GraphMonitorPanel.nameOfSelectedGraph=選択されたグラフの名称.\n" +
		"BluetoothDiscoveryPanel.search=検索\n" +
		"BluetoothDiscoveryPanel.address=Bluetooth アドレス\n" +
		"BluetoothDiscoveryPanel.identifier=識別子\n" +
		"BluetoothDiscoveryPanel.name=デバイス名\n";

	public resources_ja() throws IOException {
		super(new ByteArrayInputStream(
				StringResourceParser.escapeUnicode(resourcesString).getBytes()));
	}
}
