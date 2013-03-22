package com.funai.drawpen;

import java.util.ArrayList;
import java.util.Random;

import android.graphics.Path;
import android.graphics.Point;
import android.util.Log;

public class FurtherCorrect {
	private final String TAG = "FurtherCorrect";
	private boolean DEBUG = true;
	// 3次多項式で近似
	int N = 4;
	//
	int TH = 5;

	// ガウス変換
	private void gauss(double a[][], double xx[]) {
		int i, j, k, l, pivot;
		double x[] = new double[N];
		double p, q, m;
		double b[][] = new double[1][N + 1];

		for (i = 0; i < N; i++) {
			m = 0;
			pivot = i;

			// 列ごとの最大値算出
			for (l = i; l < N; l++) {
				if (Math.abs(a[l][i]) > m) {
					m = Math.abs(a[l][i]);
					pivot = l;
				}
			}

			// 行の入れ替え
			if (pivot != i) {
				for (j = 0; j < N + 1; j++) {
					b[0][j] = a[i][j];
					a[i][j] = a[pivot][j];
					a[pivot][j] = b[0][j];
				}
			}
		}

		for (k = 0; k < N; k++) {
			p = a[k][k];
			a[k][k] = 1;

			for (j = k + 1; j < N + 1; j++) {
				a[k][j] /= p;
			}

			for (i = k + 1; i < N; i++) {
				q = a[i][k];

				for (j = k + 1; j < N + 1; j++) {
					a[i][j] -= q * a[k][j];
				}
				a[i][k] = 0;
			}
		}

		// 解の計算
		for (i = N - 1; i >= 0; i--) {
			x[i] = a[i][N];
			for (j = N - 1; j > i; j--) {
				x[i] -= a[i][j] * x[j];
			}
		}

		for (i = 0; i < N; i++) {
			if (DEBUG)
				Log.d(TAG, i + ": x:" + x[i]);
			xx[i] = x[i];
		}

	}

	// 最小二乗法
	public void leastSquare(ArrayList<Point> point_list) {
		if (DEBUG)
			Log.d(TAG, "leastSquare");
		int listSize = point_list.size();
		int i, j, k;
		double X, Y;
		double x[] = new double[listSize];
		double y[] = new double[listSize];
		double A[][] = new double[N][N + 1];
		double xx[] = new double[N];

		for (i = 0; i < point_list.size(); i++) {
			x[i] = (double) point_list.get(i).x;
			y[i] = (double) point_list.get(i).y;
			if (DEBUG)
				Log.d(TAG, i / 2 + "x:\t" + x[(i - 1) / 2] + "\ty:\t"
						+ y[i / 2]);
		}

		/* 初期化 */
		for (i = 0; i < N; i++) {
			for (j = 0; j < N + 1; j++) {
				A[i][j] = 0.0;
			}
		}

		/* 行列の作成 */
		for (i = 0; i < N; i++) {
			for (j = 0; j < N; j++) {
				for (k = 0; k < listSize; k++) {
					A[i][j] += Math.pow(x[k], i + j);
				}
			}
		}
		for (i = 0; i < N; i++) {
			for (k = 0; k < listSize; k++) {
				A[i][N] += Math.pow(x[k], i) * y[k];
			}
		}
		/* ガウスの消去法の実行 */
		gauss(A, xx);

		/* 座標を近似曲線のものに入れ替え */
		point_list.clear();
		if (x[0] < x[listSize - 1]) {
			for (X = x[0]; X < x[listSize - 1]; X += (x[listSize - 1] - x[0]) / 10.0) {
				Y = 0.0;
				for (i = 0; i < N; i++) {
					Y += xx[i] * Math.pow(X, i);
				}
				point_list.add(new Point((int) X, (int) Y));

				if (DEBUG)
					Log.d(TAG, "X:\t" + X + "\t Y:\t" + Y);
			}
		} else {
			for (X = x[0]; X > x[listSize - 1]; X += (x[listSize - 1] - x[0]) / 10.0) {
				Y = 0.0;
				for (i = 0; i < N; i++) {
					Y += xx[i] * Math.pow(X, i);
				}
				point_list.add(new Point((int) X, (int) Y));

				if (DEBUG)
					Log.d(TAG, "X:\t" + X + "\t Y:\t" + Y);
			}

		}
		if (point_list.size() == 0) {
			for (i = 0; i < listSize; i++) {
				point_list.add(new Point((int) x[i], (int) y[i]));
			}
		}

		return;
	}

	// ハフ変換(を一部流用したハフ変換ではない何か)
	public void houghTransform(ArrayList<Point> point_list, Path path) {
		if (DEBUG)
			Log.d(TAG, "houghTransform");
		int i = 0, j = 0, last = 0;
		Point point_n = null;
		for (i = 0; i < point_list.size(); i++) {
			Point point_1 = point_list.get(i);
			Point point_2 = point_list.get(i + 1);
			int a, b, count = 0;

			a = (point_2.y - point_1.y) / (point_2.x - point_1.x);
			b = point_1.y - a * point_1.x;
			for (j = i + 2; j < point_list.size(); j++) {
				point_n = point_list.get(j);
				if (point_n.y == a * point_n.x + b) {
					count++;
				} else {
					break;
				}
			}

			if (count > TH) {
				if (last > 2) {
					ArrayList<Point> tmp_point_list = new ArrayList<Point>();
					for (int k = i - last; k < i; k++) {
						tmp_point_list.add(point_list.get(k));
					}
					double Ri[][] = new double[2][2];
					BezierCP bezierCP = new BezierCP();

					Ri = bezierCP.calControPoint(tmp_point_list, 20);
					path.cubicTo((float) Ri[0][0], (float) Ri[0][1],
							(float) Ri[1][0], (float) Ri[1][1],
							(float) point_list.get(i).x,
							(float) point_list.get(i).y);

					path.lineTo((float) point_n.x, (float) point_n.y);

					i = j;
					last = 0;
				} else {
					path.lineTo((float) point_n.x, (float) point_n.y);
					i = j;
				}

			} else {
				last++;
			}

		}
		if (last == 0) {
			return;
		} else if (last > 2) {
			ArrayList<Point> tmp_point_list = new ArrayList<Point>();
			for (int k = i - last; k < i; k++) {
				tmp_point_list.add(point_list.get(k));
			}
			double Ri[][] = new double[2][2];
			BezierCP bezierCP = new BezierCP();

			Ri = bezierCP.calControPoint(tmp_point_list, 20);
			path.cubicTo((float) Ri[0][0], (float) Ri[0][1], (float) Ri[1][0],
					(float) Ri[1][1], (float) point_list.get(i).x,
					(float) point_list.get(i).y);

			i = j;
			last = 0;
		} else {
			path.lineTo((float) point_list.get(i).x,
					(float) point_list.get(i).y);
		}

		return;
	}
}
