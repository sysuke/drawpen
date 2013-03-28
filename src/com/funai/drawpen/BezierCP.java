/* ##########################################
//
//  ベジェ曲線変異点算出
//
// ##########################################*/

package com.funai.drawpen;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.graphics.Point;
import android.util.Log;

public class BezierCP {
	private final String TAG = "BezierCP";
	private boolean DEBUG = false;
	private boolean DEBUG2 = false;

	// 制御点の数-1
	private final int n = 3;
	// 許容する評価式の値
	private final double eval = 0.001;
	// ログ成形用フォーマット
	private DecimalFormat df1 = new DecimalFormat("0.0000");

	// ##########################################
	// ###### 数列計算
	// ###########################private#########

	// 階乗
	private int factorial(int n) {
		int fact = 1;
		if (n == 0) {
			return fact;
		} else {
			for (int i = n; i > 0; i--)
				fact *= i;
			return fact;
		}
	}

	// 逆行列
	private double[][] inverse_matrix(double A[][]) {
		double B[][] = new double[2][2];
		double delta = A[0][0] * A[1][1] - A[0][1] * A[1][0];
		B[0][0] = 1 / delta * A[1][1];
		B[0][1] = -1 / delta * A[0][1];
		B[1][1] = 1 / delta * A[0][0];
		B[1][0] = -1 / delta * A[1][0];
		return B;
	}

	// ##########################################
	// ###### 変異点算出
	// ##########################################

	// バーンシュタイン基底関数
	private double bernstein(int i, double t, int n) {
		double ans;
		ans = factorial(n) / (factorial(n - i) * factorial(i)) * Math.pow(t, i)
				* Math.pow((1 - t), (double) (n - i));
		return ans;
	}

	// 変異点算出
	private double[][] mutation(int Qi[][], int m, double t[][]) {
		int i, j, k;
		double Ax[][], Ay[][], Qix, Qiy, x[], y[];
		Ax = new double[2][2];
		Ay = new double[2][2];
		x = new double[2];
		y = new double[2];

		// x=(AT*A)^-1 * AT*b
		for (i = 1; i < n; i++) {
			for (j = 0; j < 2; j++) {
				Ax[i - 1][j] = 0;
				Ay[i - 1][j] = 0;
				x[j] = 0;
				y[j] = 0;
				for (k = 1; k < m; k++) {
					// AT*A
					Ax[i - 1][j] += bernstein(i, t[k][0], n)
							* bernstein(j + 1, t[k][0], n);
					Ay[i - 1][j] += bernstein(i, t[k][1], n)
							* bernstein(j + 1, t[k][1], n);
					// bx
					Qix = ((double) Qi[k][0] - (double) Qi[0][0]
							* bernstein(0, t[k][0], n) - (double) Qi[m - 1][0]
							* bernstein(n, t[k][0], n));
					// by
					Qiy = ((double) Qi[k][1] - (double) Qi[0][1]
							* bernstein(0, t[k][1], n) - (double) Qi[m - 1][1]
							* bernstein(n, t[k][1], n));
					// AT*bx
					x[j] += bernstein(j + 1, t[k][0], n) * Qix;
					// AT*by
					y[j] += bernstein(j + 1, t[k][1], n) * Qiy;
				}
			}
		}
		Ax = inverse_matrix(Ax);
		Ay = inverse_matrix(Ay);

		double Ri[][] = new double[n - 1][2];
		for (i = 0; i < 2; i++) {
			Ri[1][0] = 0;
			Ri[1][1] = 0;
			for (j = 0; j < 2; j++) {
				Ri[i][0] += Ax[i][j] * x[j];
				Ri[i][1] += Ay[i][j] * y[j];
			}
			if (DEBUG)
				Log.d(TAG,
						" x:" + df1.format(Ri[i][0]) + " y:"
								+ df1.format(Ri[i][1]));
		}
		return Ri;

	}

	// ##########################################
	// ###### パラメタ算出
	// ##########################################

	// ベジェ曲線
	private int genBezier(double pCont[][], double point[][], double t[][]) {
		int tc;
		tc = 0;
		for (int i = 0; i < point.length; i++) {

			point[tc][0] = pCont[0][0] * Math.pow((1 - t[i][0]), 3) + 3
					* pCont[1][0] * t[i][0] * Math.pow((1 - t[i][0]), 2) + 3
					* pCont[2][0] * Math.pow(t[i][0], 2) * (1 - t[i][0])
					+ pCont[3][0] * Math.pow(t[i][0], 3);
			point[tc][1] = pCont[0][1] * Math.pow((1 - t[i][1]), 3) + 3
					* pCont[1][1] * t[i][1] * Math.pow((1 - t[i][1]), 2) + 3
					* pCont[2][1] * Math.pow(t[i][1], 2) * (1 - t[i][1])
					+ pCont[3][1] * Math.pow(t[i][1], 3);
			tc++;
		}
		return (tc);
	}

	// 反復解法によるデルタt算出　
	private int deltat(int m, int Qi[][], double Pi[][], double t[][]) {
		if (DEBUG2)
			Log.d(TAG, "delta_t m:" + m);

		// ベジェ曲線P(t)
		double Rt[][] = new double[m][2];
		int tc = genBezier(Pi, Rt, t);
		if (tc == 0)
			return -1;
		int checkJ = 0;

		for (int Dt = 0; Dt < m; Dt++) {
			// J=(P(t)-Qi)^2
			double Jx = Rt[Dt][0] - Qi[Dt][0];
			double Jy = Rt[Dt][1] - Qi[Dt][1];
			Jx *= Jx;
			Jy *= Jy;
			if (DEBUG2)
				Log.d(TAG,
						"delta_t Jx:" + df1.format(Jx) + " Jy:"
								+ df1.format(Jy));
			if (Jx < eval && Jy < eval) {
				checkJ++;
				if (checkJ == (m - 1)) {
					return 1;
				}
				continue;
			}

			// P(t)
			double P1x = 0;
			double P1y = 0;
			for (int i = 0; i <= n; i++) {
				P1x += bernstein(i, t[Dt][0], n) * Pi[i][0];
				P1y += bernstein(i, t[Dt][1], n) * Pi[i][1];
			}
			// (P(t)-Qi)
			P1x -= Qi[Dt][0];
			P1y -= Qi[Dt][1];

			// P'(t)
			double Pdtx = 0;
			double Pdty = 0;
			for (int i = 0; i <= n - 1; i++) {
				Pdtx += n * (Pi[i + 1][0] - Pi[i][0])
						* bernstein(i, t[Dt][0], n - 1);
				Pdty += n * (Pi[i + 1][1] - Pi[i][1])
						* bernstein(i, t[Dt][1], n - 1);
			}

			// P''(t)
			double Pddtx = 0;
			double Pddty = 0;
			for (int i = 0; i <= n - 2; i++) {
				Pddtx += n * (n - 1)
						* (Pi[i + 2][0] - 2 * Pi[i + 1][0] + Pi[i][0])
						* bernstein(i, t[Dt][0], n - 2);
				Pddty += n * (n - 1)
						* (Pi[i + 2][1] - 2 * Pi[i + 1][1] + Pi[i][1])
						* bernstein(i, t[Dt][1], n - 2);
			}

			// δt=(P(t)-Qi)*P'(t) / P'(t)^2+(P(t)-Qi)*P''(t)
			double dtx = P1x * Pdtx / (Pdtx * Pdtx + P1x * Pddtx);
			double dty = P1y * Pdty / (Pdty * Pdty + P1y * Pddty);

			if (Jx > 0.001)
				t[Dt][0] -= (dtx);
			if (Jy > 0.001)
				t[Dt][1] -= (dty);

			if (DEBUG2)
				Log.d(TAG, "delta_t dt:" + Dt + " tx:" + df1.format(t[Dt][0])
						+ " ty:" + df1.format(t[Dt][1]));
		}
		return 0;
	}

	// ##########################################
	// ###### 統合部
	// ##########################################

	public double[][] calControPoint(ArrayList<Point> point_list, final int lp) {
		int m = point_list.size();

		double t[][] = new double[m][2];
		int Qi[][] = new int[m][2];
		double Ri[][] = null;

		// 仮パラメタ入力 t=1/m
		for (int i = 0; i < m; i++) {
			t[i][0] = i * 1 / (double) (m - 1);
			t[i][1] = i * 1 / (double) (m - 1);
		}

		// 曲線点列入力
		for (int i = 0; i < point_list.size(); i++) {
			Qi[i][0] = point_list.get(i).x;
			Qi[i][1] = point_list.get(i).y;
		}

		// 始点・終点入力
		double Pi[][] = new double[4][2];
		Pi[0][0] = Qi[0][0];
		Pi[0][1] = Qi[0][1];
		Pi[3][0] = Qi[m - 1][0];
		Pi[3][1] = Qi[m - 1][1];

		for (int i = 0; i < lp; i++) {

			// 変異点算出
			Ri = mutation(Qi, m, t);
			if (Ri == null) {
				if (DEBUG)
					Log.e(TAG, "Mutation Error");
				return null;
			}

			// 変異点入力
			Pi[1][0] = Ri[0][0];
			Pi[1][1] = Ri[0][1];
			Pi[2][0] = Ri[1][0];
			Pi[2][1] = Ri[1][1];

			// パラメタt算出 返り値=1:評価値が基準以下
			if (deltat(m - 1, Qi, Pi, t) == 1) {
				if (DEBUG) {
					Log.d(TAG, "R 1 x:" + Ri[0][0] + " y:" + Ri[0][1]);
					Log.d(TAG, "R 2 x:" + Ri[1][0] + " y:" + Ri[1][1]);
				}
				return Ri;
			}
		}
		return Ri;
	}
}