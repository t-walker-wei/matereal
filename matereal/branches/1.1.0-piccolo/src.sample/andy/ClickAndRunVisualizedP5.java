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
package andy;

import java.awt.Color;
import java.awt.Graphics2D;

import jp.digitalmuseum.mr.andy.MobileRobot;
import jp.digitalmuseum.mr.andy.MrImage;
import jp.digitalmuseum.mr.andy.Position;
import jp.digitalmuseum.mr.gui.utils.VectorFieldPainter;
import jp.digitalmuseum.mr.hakoniwa.Hakoniwa;
import jp.digitalmuseum.mr.hakoniwa.HakoniwaRobot;
import jp.digitalmuseum.mr.task.VectorFieldTask;
import processing.core.PApplet;

public class ClickAndRunVisualizedP5 extends PApplet {
	private static final long serialVersionUID = 1L;
	Hakoniwa hako;
	MrImage hakoImage;
	MobileRobot robot;
	VectorFieldPainter painter;

	public static void main(String[] args) {
		new ClickAndRunVisualizedP5();
	}

	public void setup() {
		  // 箱庭の準備
		  hako = new Hakoniwa(640, 480);
		  hako.start();

		  // ベクトル場描画の準備
		  painter = new VectorFieldPainter(hako);

		  // 箱庭を表示する準備
		  hakoImage = new MrImage(hako.getWidth(), hako.getHeight());

		  // 箱庭にロボットを置く
		  robot = new MobileRobot(new HakoniwaRobot("Test bot"));

		  // 画面の大きさを箱庭に合わせる
		  size(hako.getWidth(), hako.getHeight());
	}

	public void draw() {
		  // 箱庭がらみの描画を始める
		  Graphics2D g2 = hakoImage.beginDraw();
		  hakoImage.clear();

		    // 箱庭の様子を表示する
		    hako.drawImage(g2);
		  
		    // ベクトル場を描画する
		    g2.setColor(Color.blue);
		    painter.setVectorTask((VectorFieldTask) robot.getTask());
		    painter.paint(g2);

		  // 描いた内容を表示する
		  hakoImage.endDraw();
		  image(hakoImage, 0, 0);

		  // ロボットが目指しているゴールを表示する
		  Position goal = robot.getGoal();
		  if (goal != null) {
		    strokeWeight(5);
		    stroke(0);
		    int x = goal.getScreenX();
		    int y = goal.getScreenY();
		    line(x-5, y-5, x+5, y+5);
		    line(x-5, y+5, x+5, y-5);
		  }
	}

	public void mouseClicked() {
		  // クリックされたらロボットにゴールを指示する
		  robot.moveTo(mouseX, mouseY);
	}
}
