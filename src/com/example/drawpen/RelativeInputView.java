/* ##########################################
//
//  相対座標入力View
//
// ##########################################*/

package com.example.drawpen;	

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class RelativeInputView extends View  {
	public RelativeInputView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
	}
	
	private float x=75,y=75;
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		x=event.getX();
		y=event.getY();
		invalidate();

		return false;
	}
	public void onDraw(Canvas canvas) {
		canvas.drawColor(Color.argb(128, 0, 0, 0));
		
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(3);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeJoin(Paint.Join.ROUND);

		canvas.drawLine(75, 0, 75, 150, paint);
		canvas.drawLine(0, 75, 150, 75, paint);

		paint.setColor(Color.CYAN);

		canvas.drawPoint(x, y, paint);
	}
	
	public float getPointX(){
		return x-75;
	}
	public float getPointY(){
		return y-75;
	}
}
