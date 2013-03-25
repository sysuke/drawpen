/* ##########################################
//
// 相対座標取得テスト描画部
//
// ##########################################*/

package com.funai.drawpen;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class PenView extends View {
	private final String TAG = "PenView";
	// ログ表示
	private boolean DEBUG = false;
	// ベジェ変換前線と変異点表示
	private boolean DEBUG_DRAW = false;

	private DocumentView mDocView;
	private SharedPreferences mSharedPreferences = null;
	private PenActivity mPenActivity;

	// 補正済み線分リスト
	private ArrayList<Path> bezier_list = new ArrayList<Path>();
	// 線分リスト
	private ArrayList<Path> draw_list = new ArrayList<Path>();
	// ペン先リスト
	private ArrayList<Paint> pen_list = new ArrayList<Paint>();
	// ベジェ補正前曲線座標リスト
	private ArrayList<Point> point_list = new ArrayList<Point>();
	// 送信画像の全座標リスト
	private ArrayList<CoordinateData> point_list_all = new ArrayList<CoordinateData>();
	// ハフ変換で抽出された直線のリスト
	private ArrayList<Path> line_list = new ArrayList<Path>();

	// ベジェ曲線変異点リスト
	private ArrayList<Integer> cp_list = new ArrayList<Integer>();
	private Path mPathCor = null;
	private Paint mPaintCor = null;
	private Path mPathBefor = null;
	private Paint mPaintBefor = null;

	private int mPenColor = Color.BLACK;
	private int mPenWidth = 5;
	private int mCanvasColor = Color.TRANSPARENT;
	private int mMaxX = 0, mMaxY = 0;
	private int mMinX = 0, mMinY = 0;
	private int mLoMaxX = 0, mLoMaxY = 0;
	private int mBaseX = 0, mBaseY = 0;
	private int mMiddleX = 0, mMiddleY = 0;
	private int mLineCount = 0;
	private double mP0LastX = 0, mP0LastY = 0;
	private double mP1LastX = 0, mP1LastY = 0;
	private boolean mFastP = true;

	private int mWaitTime = 1000;
	private int mMagnif = 1;
	private boolean mRotate = true;;

	private boolean mFastLine = true;

	private double Ri[][];

	// 指定時間後画像送信するハンドラ
	private final Handler sendBitmapHandler = new Handler();
	private final Runnable sendBitmapTask = new Runnable() {
		@Override
		public void run() {
			sendBitmap();
		}
	};

	// ペン先が押されていたかどうか
	private boolean isPush;
	// 座標基準点
	private final int baseX = 50;
	private final int baseY = 50;
	// 一つ前の座標
	private int lastX = 50, lastY = 50;
	// 絶対座標
	private int absX = 0;
	private int absY = 0;
	// 画層表示倍率
	private double mMag = 1;

	public PenView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
	}

	// 線分の描画
	public void onDraw(Canvas canvas) {
		canvas.drawColor(mCanvasColor);
		// 描画部への描画
		Bitmap bitmap = Bitmap.createBitmap(mMaxX + mPenWidth, mMaxY
				+ mPenWidth, Bitmap.Config.ARGB_8888);
		Canvas canvas2;
		canvas2 = new Canvas(bitmap);
		canvas2.drawColor(Color.argb(0, 0, 0, 0));

		if (mSharedPreferences != null) {
			if (mSharedPreferences.getBoolean("ch_bezier", false)) {
				if (bezier_list.size() != 0) {
					for (int i = 0; i < bezier_list.size(); i++) {
						Path pt = bezier_list.get(i);
						Paint pen = pen_list.get(i);
						canvas2.drawPath(pt, pen);
					}
				}

				// 描画中線分の描画
				if (mPathCor != null && mPaintCor != null) {
					mPaintCor.setColor(mPenColor);
					canvas.drawPath(mPathCor, mPaintCor);
				}
			} else {
				for (int i = 0; i < draw_list.size(); i++) {
					Path pt = draw_list.get(i);
					Paint pen = pen_list.get(i);
					canvas2.drawPath(pt, pen);
				}
			}
		}

		// 描画中線分の描画
		if (mPathBefor != null) {
			mPaintBefor.setColor(Color.BLUE);

			canvas2.drawPath(mPathBefor, mPaintBefor);
			mPaintBefor.setColor(mPenColor);
		}
		int x = bitmap.getWidth();
		int y = bitmap.getHeight();

		Rect src = new Rect(0, 0, x, y);
		Rect dst = new Rect(0, 0, (int) ((double) x * mMag),
				(int) ((double) y * mMag));
		if (DEBUG)
			Log.i(TAG, "mMag:" + mMag + " X:" + ((int) ((double) x * mMag))
					+ " Y:" + ((int) ((double) y * mMag)));
		Paint paint = new Paint();
		canvas.drawBitmap(bitmap, src, dst, paint);

		// 描画中線分の描画
		if (mPaintBefor != null) {
			for (int i = 0; i < line_list.size(); i++) {
				mPaintBefor.setColor(Color.RED);

				canvas.drawPath(line_list.get(i), mPaintBefor);
			}
			mPaintBefor.setColor(mPenColor);

		}

		// 変異点の描画
		if (DEBUG_DRAW) {
			if (mPaintBefor != null) {
				for (int i = 0; i < cp_list.size(); i++) {
					if ((i + 2) % 6 == 0) {
						mPaintBefor.setColor(Color.RED);
					} else {
						mPaintBefor.setColor(Color.MAGENTA);
					}
					int lx = cp_list.get(i);
					i++;
					int ly = cp_list.get(i);
					canvas.drawPoint(lx, ly, mPaintBefor);
				}
				mPaintBefor.setColor(mPenColor);
			}
		}
	}

	// タッチイベントによる描画
	public boolean onTouchEvent(MotionEvent event) {
		if (mSharedPreferences.getBoolean("ch_touch_event", false)) {
			absX = (int) event.getX();
			absY = (int) event.getY();

			// 線幅が画面幅を超えた場合の縮小倍率設定
			double magX = 1, magY = 1;
			if (mMaxX > this.getWidth()) {
				magX = (double) this.getWidth() / (double) mMaxX;
			}
			if (mMaxY > this.getHeight()) {
				magY = (double) this.getHeight() / (double) mMaxY;
			}
			if (magX > magY) {
				mMag = magY;
			} else {
				mMag = magX;
			}

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:

				// 画像送信メッセージ削除
				sendBitmapHandler.removeMessages(0);
				if (mFastLine) {
					point_list_all.clear();
					mFastLine = false;
					mMaxX = absX;
					mMinX = absX;
					mMaxY = this.getHeight();
					mMinY = 0;
				} else {
					if (absX > mMaxX) {
						mMaxX = absX;
					}
					if (absX < mMinX) {
						mMinX = absX;
					}
					if (absY > mMaxY) {
						mMaxY = absY;
					}
					if (absY < mMinY) {
						mMinY = absY;
					}
					modCoordinate();
				}

				isPush = true;
				// pathとpaintの初期化
				point_list.clear();
				cp_list.clear();

				mPathCor = new Path();
				mPathBefor = new Path();

				mPaintCor = new Paint();
				mPaintCor.setColor(mPenColor);
				mPaintCor.setAntiAlias(true);
				mPaintCor.setStyle(Paint.Style.STROKE);
				mPaintCor.setStrokeWidth(mPenWidth);
				mPaintCor.setStrokeCap(Paint.Cap.ROUND);
				mPaintCor.setStrokeJoin(Paint.Join.ROUND);

				mPaintBefor = mPaintCor;

				// 始点入力
				mPathCor.moveTo(absX, absY);
				mPathBefor.moveTo(absX, absY);
				point_list.add(new Point(absX, absY));
				point_list_all.add(new CoordinateData(absX, absY,
						CoordinateData.DOWN));

				// connect.
				mBaseX = absX;
				mBaseY = absY;
				mMiddleX = absX;
				mMiddleX = absY;

				mLoMaxX = 0;
				mLoMaxY = 0;

				lastX = absX;
				lastY = absY;

				break;
			case MotionEvent.ACTION_MOVE:
				// 補正前曲線座標入力
				mLineCount++;
				// 画像送信メッセージ削除
				sendBitmapHandler.removeMessages(0);
				if (absX > mMaxX) {
					mMaxX = absX;
				}
				if (absX < mMinX) {
					mMinX = absX;
				}
				if (absY > mMaxY) {
					mMaxY = absY;
				}
				if (absY < mMinY) {
					mMinY = absY;
				}
				modCoordinate();

				// 極大算出
				int saX = Math.abs(mBaseX - absX);
				int saY = Math.abs(mBaseY - absY);
				if (saX > mLoMaxX) {
					mLoMaxX = saX;
				}
				if (saY > mLoMaxY) {
					mLoMaxY = saY;
				}
				int vecX = Math.abs(mMiddleX - absX);
				int vecY = Math.abs(mMiddleY - absY);

				double vec = Math.sqrt((double) (vecX * vecX + vecY * vecY));
				if (DEBUG)
					Log.d(TAG, "vec:" + vec + " vecx:" + vecX + " vecy:" + vecY);

				// 補正前曲線座標入力
				mPathBefor.lineTo(absX, absY);

				point_list.add(new Point(absX, absY));
				point_list_all.add(new CoordinateData(absX, absY,
						CoordinateData.MOVE));

				if (vec > 20.0) {

					mMiddleX = absX;
					mMiddleY = absY;

					// 極大で曲線切断
					if (saX < mLoMaxX || saY < mLoMaxY) {
						if (mLineCount > 3) {
							mLineCount = 0;
							if (DEBUG)
								Log.d(TAG, "absX:" + absX + " absY" + absY);

							// 近似曲線算出
							FurtherCorrect furtherCorrect = new FurtherCorrect();
							// furtherCorrect.leastSquare(point_list);

							// 変異点算出
							BezierCP bezierCP = new BezierCP();
							Ri = bezierCP.calControPoint(point_list, 20);

							cp_list.add((int) Ri[0][0]);
							cp_list.add((int) Ri[0][1]);
							cp_list.add((int) Ri[1][0]);
							cp_list.add((int) Ri[1][1]);
							cp_list.add(absX);
							cp_list.add(absY);

							if (DEBUG)
								Log.d(TAG, "R0[" + Ri[0][0] + "," + Ri[0][1]
										+ "] R1[" + Ri[1][0] + "," + Ri[1][1]
										+ "]");

							// // ベジェ曲線接続時の変異点修正 if (mFastP) { mFastP = false; }
							// {
							// double a = mP0LastY - mP1LastY;
							// double b = mP1LastX - mP0LastX;
							// double c = mP0LastX * mP1LastY - mP1LastX
							// * mP0LastY;
							//
							// Ri[0][0] -= (a * Ri[0][0] + b * Ri[0][1] + c)
							// / (a * a + b * b) * a;
							// Ri[0][1] -= (a * Ri[0][0] + b * Ri[0][1] + c)
							// / (a * a + b * b) * b;
							// }

							mP0LastX = Ri[1][0];
							mP0LastY = Ri[1][1];
							mP1LastX = (double) absX;
							mP1LastY = (double) absY;

							// furtherCorrect.houghTransform(point_list,
							// mPathCor, this);

							mPathCor.cubicTo((float) Ri[0][0],
									(float) Ri[0][1], (float) Ri[1][0],
									(float) Ri[1][1], (float) absX,
									(float) absY);

							// pathとpaintの初期化
							point_list.clear();
							// 始点入力
							point_list.add(new Point(absX, absY));

						}
					}
				}
				lastX = absX;
				lastY = absY;

				invalidate();
				break;
			case MotionEvent.ACTION_UP:
				// 終点入力
				// 画像送信メッセージ削除
				sendBitmapHandler.removeMessages(0);
				if (absX > mMaxX) {
					mMaxX = absX;
				}
				if (absX < mMinX) {
					mMinX = absX;
				}
				if (absY > mMaxY) {
					mMaxY = absY;
				}
				if (absY < mMinY) {
					mMinY = absY;
				}
				modCoordinate();

				if (DEBUG)
					Log.d(TAG, "ACTION_UP " + "absX" + absX + " absY" + absY);
				lastX = absX;
				lastY = absY;

				// 終点入力
				mPathBefor.lineTo(absX, absY);
				invalidate();

				point_list.add(new Point(absX, absY));
				point_list_all.add(new CoordinateData(absX, absY,
						CoordinateData.UP));

				if (mLineCount > 2) {
					// 変異点算出
					// FurtherCorrect furtherCorrect= new FurtherCorrect();
					// furtherCorrect.houghTransform(point_list, mPathCor,
					// this);

					BezierCP bezierCP = new BezierCP();
					Ri = bezierCP.calControPoint(point_list, 20);
					if (DEBUG)
						Log.d(TAG, "Last R0[" + Ri[0][0] + "," + Ri[0][1]
								+ "] R1[" + Ri[0][0] + "," + Ri[0][0]
								+ "] Count:" + mLineCount);

					if (!mFastP) {
						double a = mP0LastY - mP1LastY;
						double b = mP1LastX - mP0LastX;
						double c = mP0LastX * mP1LastY - mP1LastX * mP0LastY;

						Ri[0][0] -= (a * Ri[0][0] + b * Ri[0][1] + c)
								/ (a * a + b * b) * a;
						Ri[0][1] -= (a * Ri[0][0] + b * Ri[0][1] + c)
								/ (a * a + b * b) * b;
					}

					cp_list.add((int) Ri[0][0]);
					cp_list.add((int) Ri[0][1]);
					cp_list.add((int) Ri[1][0]);
					cp_list.add((int) Ri[1][1]);
					cp_list.add(absX);
					cp_list.add(absY);
					mPathCor.cubicTo((float) Ri[0][0], (float) Ri[0][1],
							(float) Ri[1][0], (float) Ri[1][1], (float) absX,
							(float) absY);

				} else {
					mPathCor.lineTo(absX, absY);
				}
				bezier_list.add(mPathCor);
				draw_list.add(mPathBefor);
				pen_list.add(mPaintCor);
				mPathBefor = null;

				// 画像送信メッセージ発行
				if (!(mSharedPreferences.getBoolean("ch_send_button", false))) {
					sendBitmapHandler.postDelayed(sendBitmapTask, mWaitTime);
				}
				invalidate();
				mFastP = true;

				isPush = false;

				break;
			default:
				if (DEBUG)
					Log.d(TAG, "ERROR: onTouchEvent default");

				break;
			}
			invalidate();
		}

		return true;
	}

	// 最大・最小値の初期化
	private void initMaxMin() {
		if (DEBUG)
			Log.d(TAG, "FastLine " + "baseX" + baseX + " baseY" + baseY);

		// 絶対座標
		absX = baseX;
		absY = baseY;

		mMaxX = baseX;
		mMinX = baseX;
		mMaxY = this.getHeight();
		mMinY = 0;
	}

	// 絶対座標算出、最大・最小値の更新
	private boolean setMaxMin(int x, int y) {
		// 絶対座標
		absX = lastX + x;
		absY = lastY + y;
		if (DEBUG)
			Log.d(TAG, "nonFastLine " + "absX" + absX + " absY" + absY);

		// *処理中にタッチイベントが来ることがあるのでelseは使わない
		if (absX > mMaxX) {
			mMaxX = absX;
		}
		if (absX < mMinX) {
			mMinX = absX;
		}
		if (absY > mMaxY) {
			mMaxY = absY;
		}
		if (absY < mMinY) {
			mMinY = absY;
		}

		// 例外処理 画像サイズが規定サイズを超えるとクリア
		// Androidの規定により最低サイズは2048x2048
		int maxX = 2048;
		int maxY = 2048;
		if (this.getWidth() > maxX)
			maxX = this.getWidth();
		if (this.getHeight() > maxY)
			maxY = this.getHeight();
		if (mMaxX > maxX || mMaxY > maxY) {
			Toast.makeText(mPenActivity.getApplicationContext(),
					"画像サイズが " + maxX + "x" + maxY + " を超えたのでクリアします。", 10000)
					.show();
			pathClear();
			return false;
		}

		return true;
	}

	// 座標修正
	private void modCoordinate() {
		// X座標修正
		int diffX = 0;
		if (absX < 0) {
			diffX = 0 - absX;
			mMaxX += diffX;
			mMinX = 0;
			absX = 0;
		}
		// Y座標修正
		int diffY = 0;
		if (absY < 0) {
			diffY = 0 - absY;
			mMaxY += diffY;
			mMinY = 0;
			absY = 0;
		}
		// 描画線の位置修正
		if (diffX != 0 || diffY != 0) {
			if (mPathBefor != null) {
				mPathBefor.offset(diffX, diffY);
			}
			if (mPathCor != null) {
				mPathCor.offset(diffX, diffY);
			}
			for (int i = 0; i < point_list.size(); i++) {
				Point point = point_list.get(i);
				point.set(point.x + diffX, point.y + diffY);
				point_list.set(i, point);
			}
			for (int i = 0; i < point_list_all.size(); i++) {
				CoordinateData coordinateData = point_list_all.get(i);
				coordinateData.x += diffX;
				coordinateData.y += diffY;
				point_list_all.set(i, coordinateData);

			}
			for (int i = 0; i < bezier_list.size(); i++) {
				Path pt = bezier_list.get(i);
				pt.offset(diffX, diffY);
				bezier_list.set(i, pt);
			}
			for (int i = 0; i < draw_list.size(); i++) {
				Path pt = draw_list.get(i);
				pt.offset(diffX, diffY);
				draw_list.set(i, pt);
			}
		}
		if (DEBUG)
			Log.d(TAG, "fix abs " + "absX" + absX + " absY" + absY);
	}

	// 相対座標入力による描画
	public boolean setMovePoint(boolean push, int x, int y) {
		Log.d(TAG, "X" + x + " Y" + y + " push" + push);
		x *= mMagnif;
		y *= mMagnif;

		// 線幅が画面幅を超えた場合の縮小倍率設定
		double magX = 1, magY = 1;
		if (mMaxX > this.getWidth()) {
			magX = (double) this.getWidth() / (double) mMaxX;
		}
		if (mMaxY > this.getHeight()) {
			magY = (double) this.getHeight() / (double) mMaxY;
		}
		if (magX > magY) {
			mMag = magY;
		} else {
			mMag = magX;
		}

		if (!isPush) {

			// MotionEvent.ACTION_DOWN:
			// 押されていない状態から押された状態へ

			if (push) {
				if (DEBUG)
					Log.d(TAG, "ACTION_DOWN" + "absX" + absX + " absY" + absY);
				if (!(mSharedPreferences.getBoolean("ch_draw_button", false))) {
					if (!(mPenActivity.isIn_up)) {
						mPenActivity.slideIn(mPenActivity.UP_DOWN);
					}
				}

				// 画像送信メッセージ削除
				sendBitmapHandler.removeMessages(0);
				if (mFastLine) {
					mFastLine = false;
					point_list_all.clear();
					initMaxMin();
				} else {
					if (!(setMaxMin(x, y)))
						return false;
					modCoordinate();
				}

				isPush = true;
				// pathとpaintの初期化
				point_list.clear();
				cp_list.clear();

				mPathCor = new Path();
				mPathBefor = new Path();

				mPaintCor = new Paint();
				mPaintCor.setColor(mPenColor);
				mPaintCor.setAntiAlias(true);
				mPaintCor.setStyle(Paint.Style.STROKE);
				mPaintCor.setStrokeWidth(mPenWidth);
				mPaintCor.setStrokeCap(Paint.Cap.ROUND);
				mPaintCor.setStrokeJoin(Paint.Join.ROUND);

				mPaintBefor = mPaintCor;

				// 始点入力
				mPathCor.moveTo(absX, absY);
				mPathBefor.moveTo(absX, absY);
				point_list.add(new Point(absX, absY));
				point_list_all.add(new CoordinateData(absX, absY,
						CoordinateData.DOWN));

				// connect.
				mBaseX = absX;
				mBaseY = absY;
				mMiddleX = absX;
				mMiddleX = absY;

				mLoMaxX = 0;
				mLoMaxY = 0;

				lastX = absX;
				lastY = absY;

				// 押されていない状態のまま
			} else {
				absX = lastX + x;
				absY = lastY + y;
				lastX = absX;
				lastY = absY;
			}
		} else {
			// 押された状態のまま
			// MotionEvent.ACTION_MOVE:
			if (push) {
				// 画像送信メッセージ削除
				if (!(mSharedPreferences.getBoolean("ch_send_button", false))) {
					if (!(mPenActivity.isIn_up)) {
						mPenActivity.slideIn(mPenActivity.UP_DOWN);
					}
				}
				sendBitmapHandler.removeMessages(0);
				if (!(setMaxMin(x, y)))
					return false;
				modCoordinate();

				if (DEBUG)
					Log.d(TAG, "ACTION_MOVE " + "absX" + absX + " absY" + absY);

				// 極大算出
				int saX = Math.abs(mBaseX - absX);
				int saY = Math.abs(mBaseY - absY);
				if (saX > mLoMaxX) {
					mLoMaxX = saX;
				}
				if (saY > mLoMaxY) {
					mLoMaxY = saY;
				}
				int vecX = Math.abs(mMiddleX - absX);
				int vecY = Math.abs(mMiddleY - absY);

				double vec = Math.sqrt((double) (vecX * vecX + vecY * vecY));
				if (vec > 20) {
					mMiddleX = absX;
					mMiddleX = absY;

					// 補正前曲線座標入力
					mPathBefor.lineTo(absX, absY);

					point_list.add(new Point(absX, absY));
					point_list_all.add(new CoordinateData(absX, absY,
							CoordinateData.MOVE));

					// 極大で曲線切断
					if (saX < mLoMaxX || saY < mLoMaxY) {
						if (mLineCount > 2) {
							mLineCount = 0;
							if (DEBUG)
								Log.d(TAG, "absX:" + absX + " absY" + absY);
							// 変異点算出
							BezierCP bezierCP = new BezierCP();
							Ri = bezierCP.calControPoint(point_list, 20);

							cp_list.add((int) Ri[0][0]);
							cp_list.add((int) Ri[0][1]);
							cp_list.add((int) Ri[1][0]);
							cp_list.add((int) Ri[1][1]);
							cp_list.add(absX);
							cp_list.add(absY);

							if (DEBUG)
								Log.d(TAG, "R0[" + Ri[0][0] + "," + Ri[0][1]
										+ "] R1[" + Ri[1][0] + "," + Ri[1][1]
										+ "]");

							// ベジェ曲線接続時の変異点修正
							if (mFastP) {
								mFastP = false;
							} else {
								double a = mP0LastY - mP1LastY;
								double b = mP1LastX - mP0LastX;
								double c = mP0LastX * mP1LastY - mP1LastX
										* mP0LastY;

								Ri[0][0] -= (a * Ri[0][0] + b * Ri[0][1] + c)
										/ (a * a + b * b) * a;
								Ri[0][1] -= (a * Ri[0][0] + b * Ri[0][1] + c)
										/ (a * a + b * b) * b;
							}
							mP0LastX = Ri[1][0];
							mP0LastY = Ri[1][1];
							mP1LastX = (double) absX;
							mP1LastY = (double) absY;

							mPathCor.cubicTo((float) Ri[0][0],
									(float) Ri[0][1], (float) Ri[1][0],
									(float) Ri[1][1], (float) absX,
									(float) absY);

							// pathとpaintの初期化
							point_list.clear();
							// 始点入力
							point_list.add(new Point(absX, absY));

						}
					}
				}
				lastX = absX;
				lastY = absY;

				invalidate();
				// 押された状態から押されていない状態へ
				// MotionEvent.ACTION_UP:
			} else {
				// 画像送信メッセージ削除
				if (!(mSharedPreferences.getBoolean("ch_send_button", false))) {
					if (!(mPenActivity.isIn_up)) {
						mPenActivity.slideIn(mPenActivity.UP_DOWN);
					}
				}
				sendBitmapHandler.removeMessages(0);
				if (!(setMaxMin(x, y)))
					return false;
				modCoordinate();

				if (DEBUG)
					Log.d(TAG, "ACTION_UP " + "absX" + absX + " absY" + absY);
				lastX = absX;
				lastY = absY;
				// 終点入力
				mPathBefor.lineTo(absX, absY);
				invalidate();

				point_list.add(new Point(absX, absY));
				point_list_all.add(new CoordinateData(absX, absY,
						CoordinateData.UP));

				// 変異点算出
				if (mLineCount > 1) {
					// 変異点算出
					BezierCP bezierCP = new BezierCP();
					Ri = bezierCP.calControPoint(point_list, 20);
					if (DEBUG)
						Log.d(TAG, "Last R0[" + Ri[0][0] + "," + Ri[0][1]
								+ "] R1[" + Ri[0][0] + "," + Ri[0][0]
								+ "] Count:" + mLineCount);

					if (!mFastP) {
						double a = mP0LastY - mP1LastY;
						double b = mP1LastX - mP0LastX;
						double c = mP0LastX * mP1LastY - mP1LastX * mP0LastY;

						Ri[0][0] -= (a * Ri[0][0] + b * Ri[0][1] + c)
								/ (a * a + b * b) * a;
						Ri[0][1] -= (a * Ri[0][0] + b * Ri[0][1] + c)
								/ (a * a + b * b) * b;
					}

					cp_list.add((int) Ri[0][0]);
					cp_list.add((int) Ri[0][1]);
					cp_list.add((int) Ri[1][0]);
					cp_list.add((int) Ri[1][1]);
					cp_list.add(absX);
					cp_list.add(absY);
					mPathCor.cubicTo((float) Ri[0][0], (float) Ri[0][1],
							(float) Ri[1][0], (float) Ri[1][1], (float) absX,
							(float) absY);

				} else {
					mPathCor.lineTo(absX, absY);
				}
				bezier_list.add(mPathCor);
				draw_list.add(mPathBefor);
				pen_list.add(mPaintCor);
				mPathBefor = null;

				// 画像送信メッセージ発行
				if (!(mSharedPreferences.getBoolean("ch_send_button", false))) {
					sendBitmapHandler.postDelayed(sendBitmapTask, mWaitTime);
				}
				invalidate();
				mFastP = true;
				isPush = false;
			}
		}
		return true;
	}

	// 画像送信メッセージの再発行
	public void reissueMessage() {
		sendBitmapHandler.removeMessages(0);
		if (draw_list.size() > 0) {
			if (!(mSharedPreferences.getBoolean("ch_send_button", false))) {

				sendBitmapHandler.postDelayed(sendBitmapTask, mWaitTime);
			}
		}
	}

	// pathをbmp変換しプレビュー部へ送信
	public void sendBitmap() {
		if (!(mSharedPreferences.getBoolean("ch_draw_button", false))) {
			if (mPenActivity.isIn_up) {
				mPenActivity.slideOut(mPenActivity.UP_DOWN);
			}
		}
		mFastLine = true;
		lastX = 0;
		lastY = 0;
		mMag = 1;

		Bitmap bitmap = getBitmap();
		mDocView.setBitmap(bitmap, mMinX - mPenWidth, mMinY - mPenWidth);
		pathClear();
		invalidate();

		return;
	}

	// pathをbmp変換
	public Bitmap getBitmap() {
		Bitmap bitmap = Bitmap.createBitmap(mMaxX + mPenWidth, mMaxY
				+ mPenWidth, Bitmap.Config.ARGB_8888);
		Canvas canvas;
		canvas = new Canvas(bitmap);
		canvas.drawColor(Color.argb(0, 0, 0, 0));

		for (int i = 0; i < bezier_list.size(); i++) {
			Path pt = bezier_list.get(i);
			Paint pen = pen_list.get(i);

			canvas.drawPath(pt, pen);
		}

		if (mRotate) {
			DataConvert dataConvert = new DataConvert(
					new DataConvertEventHandler() {
					});
			float rotate = (float) (dataConvert
					.DataConvert_rotate(point_list_all) * 180 / Math.PI);
			// if (DEBUG)
			Log.d(TAG, "rotate:" + rotate);
			Log.d(TAG, "width:" + mMaxX + " heigth" + mMaxY + " mPenWidth:"
					+ mPenWidth);

			// 回転マトリックス作成
			Matrix mat = new Matrix();
			mat.postRotate(rotate);
			if (Float.isNaN(rotate)) {
				Log.d(TAG, "rotate is NaN");
				return bitmap;

			}
			// 回転したビットマップを作成
			Bitmap bitmap_tmp = Bitmap.createBitmap(bitmap, 0, 0, mMaxX
					+ mPenWidth, mMaxY + mPenWidth, mat, true);

			return bitmap_tmp;
		} else {
			return bitmap;

		}
	}

	// 全消し
	public boolean pathClear() {
		pen_list.clear();
		bezier_list.clear();
		draw_list.clear();
		line_list.clear();
		point_list.clear();
		point_list_all.clear();
		cp_list.clear();
		/*
		 * mPathCor = null;
		 */
		mPathBefor = null;
		mPaintCor = null;

		mFastLine = true;
		sendBitmapHandler.removeMessages(0);

		mFastLine = true;
		lastX = 0;
		lastY = 0;
		isPush = false;

		invalidate();

		return true;
	}

	// ペン色変換
	public boolean setPenColor(int color) {
		mPenColor = color;
		reissueMessage();
		return true;
	}

	// ペンの太さ変換
	public boolean setPenWidth(int width) {
		mPenWidth = width;
		reissueMessage();
		return true;
	}

	// 背景色変換
	public boolean setChanvasColor(int color) {
		mCanvasColor = color;
		reissueMessage();
		return true;
	}

	// 消しゴム
	public boolean setEraser() {
		mPenColor = mCanvasColor;
		reissueMessage();
		return true;
	}

	// 画像送信までの時間設定
	public boolean setWaitTime(int time) {
		mWaitTime = time;
		reissueMessage();
		return true;
	}

	// 移動量の倍率設定
	public boolean setMagnification(int mag) {
		mMagnif = mag;
		reissueMessage();
		return true;
	}

	// x分戻す
	public boolean undo(int x) {
		int bsize = bezier_list.size() - 1;
		int size = draw_list.size() - 1;
		if (DEBUG)
			Log.d(TAG, "undo bsize:" + bsize + " size:" + size);

		reissueMessage();
		isPush = false;

		mPathCor = null;
		mPathBefor = null;
		mPaintCor = null;
		mPaintBefor = null;

		for (int i = 0; i < x; i++) {
			if (size - i < 0 || bsize - i < 0) {
				break;
			}
			bezier_list.remove(size - i);
			draw_list.remove(size - i);
			if (size - i < 0 || bsize - i < 0) {
				mFastLine = true;
				sendBitmapHandler.removeMessages(0);
			}
		}
		invalidate();
		return true;
	}

	// x分やり直し
	public boolean redo(int x) {
		// TODO
		reissueMessage();
		return true;
	}

	// 画面サイズ入力
	public void setDocumentView(DocumentView docView) {
		mDocView = docView;
	}

	// Preference取得
	public void setSharedPreference(SharedPreferences sharedPreferences) {
		mSharedPreferences = sharedPreferences;
	}

	// 親Activity取得
	public void setParentActivity(PenActivity penActivity) {
		mPenActivity = penActivity;
	}

	public void setLine(Path path) {
		line_list.add(path);
	}

	public void setRotate(boolean rotate) {
		mRotate = rotate;
	}
}