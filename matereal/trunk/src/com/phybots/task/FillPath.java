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
package com.phybots.task;

import java.util.ArrayList;
import java.util.List;

import com.phybots.utils.Position;


/**
 *
 *
 * @author shigeo
 */
public class FillPath extends TracePath {
	private static final long serialVersionUID = -9063449324838882435L;

	public FillPath(List<Position> path) {
		super(path);
	}

	@Override
	protected void updateSubflow() {
		path = getCleaningPath(path,
				getAssignedRobot().getShape().getBounds().getWidth());
		super.updateSubflow();
	}

	public static List<Position> getCleaningPath(List<Position> cleanArea, double robotSize) {
		List<Position> cleaningPath = new ArrayList<Position>();

		double[] xPoints=new double[cleanArea.size()];
		double[] yPoints=new double[cleanArea.size()];

		for(int i=0; i<cleanArea.size(); i++){
			xPoints[i]=(int) cleanArea.get(i).getX();
			yPoints[i]=(int) cleanArea.get(i).getY();
		}
		int minIndex=getMinPointIndex(xPoints);
		int maxIndex=getMaxPointIndex(xPoints);

		//ラインの本数
		int div=(int) (((xPoints[maxIndex]-xPoints[minIndex])/(robotSize))-1);

		//一周したあとに一番最初に通る点->xが最小である点
		cleaningPath.add(new Position(cleanArea.get(minIndex)));

		int count=0;

		int size=cleanArea.size();

		double x=cleanArea.get(minIndex).getX();
		for(int i=0; i<div; i++){
			x+=robotSize;
			List<Position> temp=new ArrayList<Position>();
			for(int j=0; j<size-1; j++){
				Position p1=cleanArea.get(j);
				Position p2=cleanArea.get(j+1);
				if((p1.getX()<x && x<p2.getX())
						|| (p2.getX()<x && x<p1.getX())){
					//cleanArea.add(getPointBetweenTwoPoint(x, p1, p2));
					temp.add(getPointBetweenTwoPoint(x, p1, p2));
				}
			}
			if((cleanArea.get(0).getX()<x && x<cleanArea.get(cleanArea.size()-1).getX())
					|| (cleanArea.get(cleanArea.size()-1).getX()<x && x<cleanArea.get(0).getX())){
				//cleanArea.add(getPointBetweenTwoPoint(x, cleanArea.get(0), cleanArea.get(cleanArea.size()-1)));
				temp.add(getPointBetweenTwoPoint(x, cleanArea.get(0), cleanArea.get(cleanArea.size()-1)));
			}
			int tempSize=temp.size();
			if((count++)%2==0){
				//yが小さいもの順にcleanAreaに格納しておく
				for(int j=0; j<tempSize; j++){
					cleaningPath.add(getMinimumValue(temp));
				}
			}else{
				//yが大きいもの順にcleanAreaに格納しておく
				for(int j=0; j<tempSize; j++){
					cleaningPath.add(getMaximumValue(temp));
				}
			}
		}

		cleaningPath.add(new Position(cleanArea.get(maxIndex).getX(), cleanArea.get(maxIndex).getY()));
		return cleaningPath;
	}

	/**
	 * 最小値を得る
	 *
	 * @param value
	 * @return
	 */
	private static int getMinPointIndex(double[] value) {
		double min = value[0];
		int index = 0;

		for (int i = 1; i < value.length; i++) {
			if (value[i] < min) {
				min = value[i];
				index = i;
			}
		}

		return index;
	}

	/**
	 * 最大値を得る
	 *
	 * @param value
	 * @return
	 */
	private static int getMaxPointIndex(double[] value) {
		double max = value[0];
		int index = 0;

		for (int i = 1; i < value.length; i++) {
			if (value[i] > max) {
				max = value[i];
				index = i;
			}
		}

		return index;
	}

	/**
	 * 指定された二地点とその間のx座標から 二地点を通る線分上のx座標に対応するy座標を計算して、 (x,y)を返す
	 *
	 * @param x
	 * @param p1
	 * @param p2
	 * @return
	 */
	private static Position getPointBetweenTwoPoint(double x, Position p1, Position p2) {
		double y = (p1.getY() - p2.getY()) * x / (p1.getX() - p2.getX())
				+ (p1.getX() * p2.getY() - p2.getX() * p1.getY())
				/ (p1.getX() - p2.getX());
		return new Position(x, y);
	}

	/**
	 * 与えられたlistの中からy値が最小のものを返す
	 *
	 * @param list
	 * @return
	 */
	private static Position getMinimumValue(List<Position> list) {
		double min = list.get(0).getY();
		int index = 0;

		for (int i = 1; i < list.size(); i++) {
			double y = list.get(i).getY();
			if (y < min) {
				min = y;
				index = i;
			}
		}

		return list.remove(index);
	}

	/**
	 * 与えられたlistの中からy値が最小のものを返す
	 *
	 * @param list
	 * @return
	 */
	private static Position getMaximumValue(List<Position> list) {
		double max = list.get(0).getY();
		int index = 0;

		for (int i = 1; i < list.size(); i++) {
			double y = list.get(i).getY();
			if (y > max) {
				max = y;
				index = i;
			}
		}

		return list.remove(index);
	}
}
