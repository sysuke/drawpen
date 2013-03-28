package com.funai.drawpen;

import java.util.ArrayList;
import android.graphics.Point;
import android.util.Log;

//! @brief データ変換処理群
public class DataConvert {
	DataConvertEventHandler hd;
	public DataConvert(DataConvertEventHandler handler)
	{
		hd = handler;
	}

	public ReceiveData oldData = new ReceiveData();
	public ReceiveData DataConvert_DrawDataConvert(ReceiveData data) {
		ReceiveData retData = new ReceiveData(data);

		return (retData);
	}

	//! 回転補正処理
	private ArrayList<CoordinateData> inputData = new ArrayList<CoordinateData>();
	private ArrayList<CoordinateData> lineData = new ArrayList<CoordinateData>();
	private ArrayList<Point> selectData = new ArrayList<Point>();

	public double DataConvert_rotate(ArrayList<CoordinateData> inData) {
		for (int i=0; i<inData.size(); i++){
			CoordinateData getData = inData.get(i);
			//! 管理用配列に追加
			inputData.add(getData);
			//! 状態毎の処理
			if (getData.state == CoordinateData.DOWN) {
				//! 書き始め(DOWN)
				DataConvert_rotate_down(getData);
			} else if (getData.state == CoordinateData.UP) {
				//! 書き終わり(UP)
				DataConvert_rotate_up(getData);
			} else if (getData.state == CoordinateData.MOVE) {
				//! 書き途中(MOVE)
				DataConvert_rotate_downMove(getData);
			} else if (getData.state == CoordinateData.S_MOVE) {
				//! 移動中(非描画移動)
				DataConvert_rotate_upMove(getData);
			}
		}
		double x = inData.get(inData.size()-1).x - inData.get(0).x;
		double y = inData.get(inData.size()-1).y - inData.get(0).y;
		if (selectData.isEmpty()) return 0;
		return DataConvert_rotate_call(x,y);
	}

	//! 押した時 → 一時保管配列に保存
	private void DataConvert_rotate_down(CoordinateData inData) {
		lineData.add(inData);
	}
	//! 離した時 → 一時保持配列からY最大値(一番下)とその位置のXを取得し近似用配列に保存
	private void DataConvert_rotate_up(CoordinateData inData) {
		Point p1 = new Point(inData.x, inData.y);	// Y最大
		Point p2 = new Point(inData.x, inData.y);	// Y最小
//		Point p3 = new Point(inData.x, inData.y);	// X最大
//		Point p4 = new Point(inData.x, inData.y);	// X最小
		for (int i=0; i<lineData.size(); i++) {
			CoordinateData select = lineData.get(i);
			if (p1.y < select.y) {
				p1.x = select.x;
				p1.y = select.y;
			}
			if (p2.y > select.y) {
				p2.x = select.x;
				p2.y = select.y;
			}
/*			if (p3.x < select.x) {
				p3.x = select.x;
				p3.y = select.y;
			}
			if (p4.x > select.x) {
				p4.x = select.x;
				p4.y = select.y;
			}*/
		}
		selectData.add(new Point(p1));
		selectData.add(new Point(p2));
//		selectData.add(new Point(p3));
//		selectData.add(new Point(p4));
		lineData.clear();
	}
	//! 描画中 → 一時保管配列に保存
	private void DataConvert_rotate_downMove(CoordinateData inData) {
		lineData.add(inData);
	}
	//! 空中移動中 → なにもしない
	private void DataConvert_rotate_upMove(CoordinateData inData) {
	}
	//! 近似直線を求め、直線の傾きから角度を求める。
	private double DataConvert_rotate_call(double x, double y) {
		// 近似直線公式
		Point data ;
		int size = selectData.size();
		double x_bar = 0;
		double y_bar = 0;

		Log.d("rotate","area : "+x+","+y);
		if (selectData.size() < 5) return 0;

		for (int i=0; i<size; i++) {
			data = selectData.get(i);
			x_bar += data.x;
			y_bar += data.y;
		}
		x_bar = x_bar/size;
		y_bar = y_bar/size;
		double xi1 = 0;
		double xi2 = 0;
		for (int i=0; i<size; i++) {
			data = selectData.get(i);
			xi1 += (data.x - x_bar)*(data.x - x_bar);
			xi2 += (data.x - x_bar)*(data.y - y_bar);
		}
		Log.d("rotate","atan2 :"+Math.atan2(xi2, xi1)*180/Math.PI);
		double sita = Math.atan2(xi2, xi1)*180/Math.PI;
		return -sita;
/*
//最小2乗法による近似式
		double xi1 = 0;
		double xi2 = 0;

		double n = (double)selectData.size();
		double t1 = 0;
		double t2 = 0;
		double t3 = 0;
		double t4 = 0;

		for (int i=0; i<n; i++) {
			Point data = selectData.get(i);
			t1 += data.x*data.y;
			t2 += data.x;
			t3 += data.y;
			t4 += data.x*data.x;
		}
		xi1 = n*t4-t2*t2;
		xi2 = n*t1-t2*t3;
		Log.d("rotate","area : "+x+","+y);
		Log.d("rotate","atan2 :"+Math.atan2(xi2, xi1)*180/Math.PI);
		double sita = Math.atan2(xi2, xi1)*180/Math.PI;
		return -sita;
*/
/*		if (x < 0) {
			if (y < 0) {
				return -sita;
			}
		} else if (y < 0) {
			return -sita;
		}
		return -sita;	*/
	}
}
