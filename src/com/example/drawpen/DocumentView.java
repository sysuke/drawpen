/* ##########################################
//
//  プレビュー部
//
// ##########################################*/

package com.example.drawpen;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class DocumentView extends View {
	private int mHeigth = 0, mWidth = 0;
	private ArrayList<Bitmap> bmp_list = new ArrayList<Bitmap>();
	private ArrayList<Integer> xy_list = new ArrayList<Integer>();

	public DocumentView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
	}

	protected void onDraw(Canvas canvas) {

		canvas.drawColor(Color.rgb(255, 240, 175));

		int lastX = 0;
		int lastY = 0;

		// 画像描画
		for (int i = 0; i < bmp_list.size(); i++) {
			Bitmap bitmap = bmp_list.get(i);
			int x = bitmap.getWidth();
			int y = bitmap.getHeight();

			int minX = xy_list.get(2 * i);
			int minY = xy_list.get(2 * i + 1);
			int bmX = x - minX;
			int bmY = y - minY;
			if (bmX * 100 / bmY + lastX + 10 > mWidth) {
				lastX = 0;
				lastY += 100;
			}

			Rect src = new Rect(minX, minY, x, y);
			Rect dst = new Rect(lastX + 10, lastY + 10, bmX * 100 / bmY + lastX
					+ 10, lastY + 110);
			lastX = bmX * 100 / bmY + lastX + 10;
			Paint paint = new Paint();
			canvas.drawBitmap(bitmap, src, dst, paint);
		}
		// 原稿線描画
		if (mWidth != 0 && mHeigth != 0) {
			Paint paint = new Paint();
			paint.setColor(Color.BLACK);
			paint.setAntiAlias(true);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(1);
			paint.setStrokeCap(Paint.Cap.ROUND);
			paint.setStrokeJoin(Paint.Join.ROUND);

			for (int i = 0; i * 100 + 10 < mHeigth; i++) {
				canvas.drawLine(10, 100 * i + 10, mWidth - 10, 100 * i + 10,
						paint);
			}
		}

		

	}

	// 全消し
	public boolean allClear() {
		bmp_list.clear();
		xy_list.clear();
		invalidate();
		return true;
	}

	// x分戻る
	public boolean undo(int x) {
		int size = bmp_list.size() - 1;
		for (int i = 0; i < x; i++) {
			if (size - i < 0)
				break;
			bmp_list.remove(size - i);
		}
		

		size = xy_list.size() - 1;
		for (int i = 0; i < x * 2; i++) {
			if (size - i < 0)
				break;
			xy_list.remove(size - i);
		}
		invalidate();
		return true;
	}

	// 画像追加
	public void setBitmap(Bitmap bitmap, int minx, int miny) {
		bmp_list.add(bitmap);
		xy_list.add(minx);
		xy_list.add(miny);
		invalidate();
	}

	// 画面サイズ入力
	public void setWindowSize(int widht, int heigth) {
		mWidth = widht;
		mHeigth = heigth;
	}
}
