/* ##########################################
//
//  mein Activity
//
// ##########################################*/

package com.funai.drawpen;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.funai.drawpen.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

public class PenActivity extends Activity implements OnClickListener,
		AnimationListener, OnValueChangeListener {
	boolean DEBUG = false;

	boolean waitt_toggle = true;
	boolean mag_toggle = true;

	boolean color_toggle = true;
	boolean pen_toggle = true;

	final public int UP_DOWN = 0;
	final public int LEFT_RIGHT = 1;
	private NumberPicker nump_waittime;
	private NumberPicker nump_manif;

	private ImageButton imgb_timeconf;
	private ImageButton imgb_magnif;
	private ImageButton imgb_erase;

	private ImageButton imgb_setcolor;
	private ImageButton imgb_colorblack;
	private ImageButton imgb_colorblue;
	private ImageButton imgb_colorred;
	private ImageButton imgb_colorglay;
	private ImageButton imgb_colorgreen;
	private ImageButton imgb_colorcyan;
	private ImageButton imgb_colororange;
	private ImageButton imgb_coloryellow;
	private ImageButton imgb_colorwhite;

	private ImageButton imgb_setpensize;
	private ImageButton imgb_pensizebold;
	private ImageButton imgb_pensizefine;
	private ImageButton imgb_pensizenormal;
	private ImageButton imgb_undo;

	private RelativeInputView inputview;
	private Button bt_move;
	private Button bt_setBmp;
	private ToggleButton tbn_push;
	private EditText et_x, et_y;

	private Button bt_backspace;
	private ImageButton bt_allc;
	private ImageButton bt_pref;

	private Button bt_sendBmp;
	private ImageButton bt_toolIn;
	private ImageButton bt_toolOut;
	private RelativeLayout canvasIn;
	private ImageButton bt_canvasIn;

	private ImageButton bt_canvasOut;

	private RelativeLayout drawTitle;
	private RelativeLayout drawview;
	private RelativeLayout waittimeview;
	private RelativeLayout magnifview;

	private RelativeLayout colorview;
	private RelativeLayout pensizeview;

	private DocumentView docview;

	public boolean isIn = true;
	public boolean isIn_up = true;

	private HorizontalScrollView toolbarView;
	private PenView penView;

	private SharedPreferences mSharedPreferences;

	WifiReceive wifiRecv;
	WifiEventHandler wifiHandle;
	Handler ui_handler = new Handler(); //UIスレッドハンドラ
	ArrayList<ReceiveData> wifiData = new ArrayList<ReceiveData>();

	DataConvert conv;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);

		setContentView(R.layout.activity_main);

		// View取得・リスナーセット
		toolbarView = (HorizontalScrollView) findViewById(R.id.ScrollView);
		drawview = (RelativeLayout) findViewById(R.id.draw_layout);
		drawTitle = (RelativeLayout) findViewById(R.id.draw_titlebar);
		penView = (PenView) findViewById(R.id.penview);
		docview = (DocumentView) findViewById(R.id.documentview);

		inputview = (RelativeInputView) findViewById(R.id.inputview);
		bt_move = (Button) findViewById(R.id.move_btn);
		bt_move.setOnClickListener(this);
		tbn_push = (ToggleButton) findViewById(R.id.toggle_btn);
		tbn_push.setOnClickListener(this);
		et_x = (EditText) findViewById(R.id.edit_x);
		et_x.setOnClickListener(this);
		et_y = (EditText) findViewById(R.id.edit_y);
		et_y.setOnClickListener(this);
		bt_setBmp = (Button) findViewById(R.id.setbmp);
		bt_setBmp.setOnClickListener(this);
		bt_sendBmp = (Button) findViewById(R.id.sendbmp);
		bt_sendBmp.setOnClickListener(this);

		bt_toolOut = (ImageButton) findViewById(R.id.bt_toolOut);
		bt_toolOut.setOnClickListener(this);
		bt_toolIn = (ImageButton) findViewById(R.id.bt_toolIn);
		bt_toolIn.setOnClickListener(this);
		bt_toolIn.setVisibility(Button.INVISIBLE);
		bt_canvasOut = (ImageButton) findViewById(R.id.bt_canvasOut);
		bt_canvasOut.setOnClickListener(this);
		bt_canvasIn = (ImageButton) findViewById(R.id.bt_canvasIn);
		bt_canvasIn.setOnClickListener(this);
		canvasIn = (RelativeLayout) findViewById(R.id.canvasIn);
		canvasIn.setVisibility(Button.INVISIBLE);

		bt_allc = (ImageButton) findViewById(R.id.allc_btn);
		bt_allc.setOnClickListener(this);
		bt_pref = (ImageButton) findViewById(R.id.pref_btn);
		bt_pref.setOnClickListener(this);
		bt_backspace = (Button) findViewById(R.id.backspace_btn);
		bt_backspace.setOnClickListener(this);
		bt_backspace.setVisibility(Button.GONE);
		waittimeview = (RelativeLayout) findViewById(R.id.waittime_layout);
		nump_waittime = (NumberPicker) findViewById(R.id.waittime_picr);
		nump_waittime.setOnValueChangedListener(this);
		nump_waittime.setMaxValue(10);
		nump_waittime.setMinValue(0);
		nump_waittime.setValue(3);
		penView.setWaitTime(3 * 1000);
		magnifview = (RelativeLayout) findViewById(R.id.magnif_layout);
		nump_manif = (NumberPicker) findViewById(R.id.magnif_picr);
		nump_manif.setOnValueChangedListener(this);
		nump_manif.setMaxValue(10);
		nump_manif.setMinValue(1);
		nump_manif.setValue(1);

		colorview = (RelativeLayout) findViewById(R.id.setcolor_layout);
		imgb_setcolor = (ImageButton) findViewById(R.id.setcolor_btn);
		imgb_setcolor.setOnClickListener(this);
		imgb_timeconf = (ImageButton) findViewById(R.id.timeconf_btn);
		imgb_timeconf.setOnClickListener(this);
		imgb_magnif = (ImageButton) findViewById(R.id.magnif_btn);
		imgb_magnif.setOnClickListener(this);
		imgb_erase = (ImageButton) findViewById(R.id.erase_btn);
		imgb_erase.setOnClickListener(this);
		imgb_colorblack = (ImageButton) findViewById(R.id.colorblack_btn);
		imgb_colorblack.setOnClickListener(this);
		imgb_colorblue = (ImageButton) findViewById(R.id.colorblue_btn);
		imgb_colorblue.setOnClickListener(this);
		imgb_colorred = (ImageButton) findViewById(R.id.colorred_btn);
		imgb_colorred.setOnClickListener(this);
		imgb_colorglay = (ImageButton) findViewById(R.id.colorgray_btn);
		imgb_colorglay.setOnClickListener(this);
		imgb_colorcyan = (ImageButton) findViewById(R.id.colorcyan_btn);
		imgb_colorcyan.setOnClickListener(this);
		imgb_colorgreen = (ImageButton) findViewById(R.id.colorgreen_btn);
		imgb_colorgreen.setOnClickListener(this);
		imgb_colororange = (ImageButton) findViewById(R.id.colororange_btn);
		imgb_colororange.setOnClickListener(this);
		imgb_colorwhite = (ImageButton) findViewById(R.id.colorwhite_btn);
		imgb_colorwhite.setOnClickListener(this);
		imgb_coloryellow = (ImageButton) findViewById(R.id.coloryellow_btn);
		imgb_coloryellow.setOnClickListener(this);

		pensizeview = (RelativeLayout) findViewById(R.id.setpensize_layout);
		imgb_setpensize = (ImageButton) findViewById(R.id.setpensize_btn);
		imgb_setpensize.setOnClickListener(this);
		imgb_pensizebold = (ImageButton) findViewById(R.id.pensizebold_btn);
		imgb_pensizebold.setOnClickListener(this);
		imgb_pensizefine = (ImageButton) findViewById(R.id.pensizefine_btn);
		imgb_pensizefine.setOnClickListener(this);
		imgb_pensizenormal = (ImageButton) findViewById(R.id.pensizenormal_btn);
		imgb_pensizenormal.setOnClickListener(this);

		imgb_undo = (ImageButton) findViewById(R.id.undo_btn);
		imgb_undo.setOnClickListener(this);
		// 画面サイズを取得しプレビューに入力
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display disp = wm.getDefaultDisplay();
		int width = disp.getWidth();
		int height = disp.getHeight();
		docview.setWindowSize(width, height);

		// 必要なViewをPenViewへ
		penView.setDocumentView(docview);
		penView.setSharedPreference(mSharedPreferences);
		penView.setParentActivity(this);
		// データ変換関数群
		conv = new DataConvert(new DataConvertEventHandler(){			
		});

		// Wifi通信処理
		wifiRecv = new WifiReceive(new WifiEventHandler(){
			@Override
			public void receiveData(int x, int y, int z, boolean push)
			{
				Log.d("Wifi","set Data");
				wifiData.add(new ReceiveData(x, y, z, push));
				Timer time = new Timer();
				time.schedule(new WifiToDrawTask(), 1);
			}
		});
	}

	public void wifiConnectStart(int port) {
		wifiRecv.setPort(port);
		wifiRecv.openConnect();
	}

	public class WifiToDrawTask extends TimerTask {
		@Override
		public void run() {
			// TODO 自動生成されたメソッド・スタブ
			ui_handler.post(new Runnable() {
				public void run() {
					if (wifiData.isEmpty()){
						return;
					}
					Log.d("Wifi","DrawData");
					ReceiveData data = new ReceiveData(wifiData.get(0));
					wifiData.remove(0);
					penView.setMovePoint(data.push, data.x, data.y);
				}
			});
		}
		
	}

	@Override
	protected void onPause() {
		super.onPause();

		wifiRecv.closeConnection();
	}
	

	@Override
	protected void onResume() {
		super.onResume();

		// 設定画面の値によってUIを変更
		if (mSharedPreferences.getBoolean("ch_input", false)) {
			inputview.setVisibility(RelativeInputView.VISIBLE);
			bt_move.setVisibility(RelativeInputView.VISIBLE);
			tbn_push.setVisibility(RelativeInputView.VISIBLE);
			et_x.setVisibility(RelativeInputView.VISIBLE);
			et_y.setVisibility(RelativeInputView.VISIBLE);
			bt_setBmp.setVisibility(RelativeInputView.VISIBLE);
		} else {
			inputview.setVisibility(RelativeInputView.INVISIBLE);
			bt_move.setVisibility(RelativeInputView.GONE);
			tbn_push.setVisibility(RelativeInputView.GONE);
			et_x.setVisibility(RelativeInputView.GONE);
			et_y.setVisibility(RelativeInputView.GONE);
			bt_setBmp.setVisibility(RelativeInputView.INVISIBLE);
		}

		if (mSharedPreferences.getBoolean("ch_send_button", false)) {
			bt_sendBmp.setVisibility(Button.VISIBLE);
		} else {
			bt_sendBmp.setVisibility(Button.GONE);
		}

		if (mSharedPreferences.getBoolean("ch_draw_button", false)) {
			if (isIn_up) {
				bt_canvasOut.setVisibility(Button.VISIBLE);
			} else {
				canvasIn.setVisibility(Button.VISIBLE);
			}
		} else {
			if (isIn_up) {
				slideOut(UP_DOWN);
				bt_canvasOut.setVisibility(Button.GONE);
				canvasIn.setVisibility(Button.INVISIBLE);
			} else {
				canvasIn.setVisibility(Button.INVISIBLE);
			}
		}

		wifiConnectStart(32346);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {

		// 相対座標入力
		case R.id.move_btn:
			int x = 0,
			y = 0;
			if (!et_x.getText().toString().equals("")) {
				x = Integer.parseInt(et_x.getText().toString());
			} else {
				et_x.setText("0");
			}
			if (!et_y.getText().toString().equals("")) {
				y = Integer.parseInt(et_y.getText().toString());
			} else {
				et_y.setText("0");
			}
			penView.setMovePoint(tbn_push.isChecked(), x, y);
			break;
		case R.id.setbmp:
			int x2 = 0,
			y2 = 0;
			x2 = (int) inputview.getPointX();
			y2 = (int) inputview.getPointY();
			et_x.setText("" + x2);
			et_y.setText("" + y2);
			break;

		// プレビュー画面ツールバー
		case R.id.allc_btn:
			docview.allClear();
			break;
		case R.id.pref_btn:
			final Intent intent = new Intent(getApplicationContext(),
					PenPrefActivity.class);
			startActivity(intent);
			break;
		case R.id.timeconf_btn:
			// 時間設定ボタンの座標を取得し真下に配置
			int[] anchorPos = new int[2];
			view.getLocationOnScreen(anchorPos);

			if (waitt_toggle) {
				mag_toggle = true;
				magnifview.setVisibility(RelativeLayout.INVISIBLE);
				waitt_toggle = false;

				int[] location = new int[2];
				imgb_timeconf.getLocationInWindow(location);
				waittimeview.setTranslationX(location[0]
						- (waittimeview.getWidth() - imgb_timeconf.getWidth())
						/ 2 - 5);

				waittimeview.setVisibility(RelativeLayout.VISIBLE);
			} else {
				waitt_toggle = true;
				waittimeview.setVisibility(RelativeLayout.INVISIBLE);
			}
			break;
		case R.id.magnif_btn:
			int[] anchorPos_mag = new int[2];
			view.getLocationOnScreen(anchorPos_mag);

			if (mag_toggle) {
				waitt_toggle = true;
				waittimeview.setVisibility(RelativeLayout.INVISIBLE);

				mag_toggle = false;

				int[] location = new int[2];
				imgb_magnif.getLocationInWindow(location);
				magnifview.setTranslationX(location[0]
						- (magnifview.getWidth() - imgb_magnif.getWidth()) / 2);

				magnifview.setVisibility(RelativeLayout.VISIBLE);
			} else {
				mag_toggle = true;
				magnifview.setVisibility(RelativeLayout.INVISIBLE);
			}
			break;

		// 描画部ツールバー
		case R.id.erase_btn:
			penView.pathClear();
			break;
		// ペン色変換
		case R.id.setcolor_btn:
			int[] anchorPosc = new int[2];

			view.getLocationOnScreen(anchorPosc);

			if (color_toggle) {
				pen_toggle = true;
				pensizeview.setVisibility(RelativeLayout.INVISIBLE);
				color_toggle = false;

				colorview.setVisibility(RelativeLayout.VISIBLE);
			} else {
				color_toggle = true;
				colorview.setVisibility(RelativeLayout.INVISIBLE);
			}
			break;
		case R.id.colorblack_btn:
			penView.setPenColor(Color.BLACK);
			imgb_setcolor.setImageResource(R.drawable.menu_color_black);
			break;
		case R.id.colorblue_btn:
			penView.setPenColor(Color.BLUE);
			imgb_setcolor.setImageResource(R.drawable.menu_color_blue);
			break;
		case R.id.colorred_btn:
			penView.setPenColor(Color.RED);
			imgb_setcolor.setImageResource(R.drawable.menu_color_red);
			break;
		case R.id.colorgray_btn:
			penView.setPenColor(Color.GRAY);
			imgb_setcolor.setImageResource(R.drawable.menu_color_gray);
			break;
		case R.id.colorcyan_btn:
			penView.setPenColor(Color.CYAN);
			imgb_setcolor.setImageResource(R.drawable.menu_color_cyan);
			break;
		case R.id.colorgreen_btn:
			penView.setPenColor(Color.GREEN);
			imgb_setcolor.setImageResource(R.drawable.menu_color_green);
			break;
		case R.id.colororange_btn:
			penView.setPenColor(Color.rgb(255, 153, 0));
			imgb_setcolor.setImageResource(R.drawable.menu_color_orange);
			break;
		case R.id.colorwhite_btn:
			penView.setPenColor(Color.WHITE);
			imgb_setcolor.setImageResource(R.drawable.menu_color_white);
			break;
		case R.id.coloryellow_btn:
			penView.setPenColor(Color.YELLOW);
			imgb_setcolor.setImageResource(R.drawable.menu_color_yellow);
			break;
		// ペン太さ変換
		case R.id.setpensize_btn:
			int[] penAnchorPosc = new int[2];

			view.getLocationOnScreen(penAnchorPosc);

			if (pen_toggle) {

				color_toggle = true;
				colorview.setVisibility(RelativeLayout.INVISIBLE);

				pen_toggle = false;

				pensizeview.setVisibility(RelativeLayout.VISIBLE);
			} else {
				pen_toggle = true;
				pensizeview.setVisibility(RelativeLayout.INVISIBLE);
			}
			break;
		case R.id.pensizefine_btn:
			penView.setPenWidth(5);
			imgb_setpensize.setImageResource(R.drawable.menu_pensize_fine);
			break;
		case R.id.pensizebold_btn:
			penView.setPenWidth(30);
			imgb_setpensize.setImageResource(R.drawable.menu_pensize_bold);
			break;
		case R.id.pensizenormal_btn:
			penView.setPenWidth(15);
			imgb_setpensize.setImageResource(R.drawable.menu_pensize_normal);
			break;
		// 戻るボタン
		case R.id.undo_btn:
			penView.undo(1);
			break;

		// 画像送信ボタン(debug時のみ表示)
		case R.id.sendbmp:
			penView.sendBitmap();
			break;

		// ツールバー表示/非表示切り替え
		case R.id.bt_toolOut:
			if (!waitt_toggle) {
				waitt_toggle = true;
				waittimeview.setVisibility(RelativeLayout.INVISIBLE);
			}
			if (!color_toggle) {
				color_toggle = true;
				colorview.setVisibility(RelativeLayout.INVISIBLE);
			}
			slideOut(LEFT_RIGHT);
			break;
		case R.id.bt_toolIn:
			slideIn(LEFT_RIGHT);
			break;
		// 描画部表示/非表示切り替え
		case R.id.bt_canvasOut:
			slideOut(UP_DOWN);
			break;
		case R.id.bt_canvasIn:
			slideIn(UP_DOWN);
			break;
		}
	}

	// ツールバー・描画部の表示
	public void slideIn(int menu) {
		switch (menu) {
		case LEFT_RIGHT:
			if (toolbarView != null) {
				isIn = true;
				bt_toolIn.setVisibility(Button.INVISIBLE);
				bt_toolOut.setVisibility(Button.VISIBLE);

				toolbarView.setVisibility(LinearLayout.INVISIBLE);
				toolbarView.setPadding(0, 0, 0, 0);

				TranslateAnimation trans = new TranslateAnimation(
						toolbarView.getWidth(), 0, 0, 0);
				trans.setDuration(500);
				trans.setFillAfter(false);
				trans.setFillBefore(true);
				trans.setAnimationListener(this);

				TranslateAnimation trans2 = new TranslateAnimation(
						toolbarView.getWidth(), 0, 0, 0);
				trans2.setDuration(500);
				trans2.setFillAfter(false);
				trans2.setFillBefore(true);
				trans2.setAnimationListener(this);

				toolbarView.startAnimation(trans);
				bt_toolOut.startAnimation(trans2);

			}
			break;
		case UP_DOWN:
			if (drawview != null) {
				isIn_up = true;

				canvasIn.setVisibility(Button.INVISIBLE);
				if (mSharedPreferences.getBoolean("ch_draw_button", false)) {
					bt_canvasOut.setVisibility(Button.VISIBLE);

				} else {
					bt_canvasOut.setVisibility(Button.GONE);

				}
				drawview.setVisibility(RelativeLayout.INVISIBLE);
				drawview.setPadding(0, 0, 0, 0);

				TranslateAnimation trans = new TranslateAnimation(0, 0,
						drawview.getHeight(), 0);
				trans.setDuration(500);
				trans.setFillAfter(false);
				trans.setFillBefore(true);
				trans.setAnimationListener(this);

				TranslateAnimation trans2 = new TranslateAnimation(0, 0,
						drawview.getHeight(), 0);
				trans2.setDuration(500);
				trans2.setFillAfter(false);
				trans2.setFillBefore(true);
				trans2.setAnimationListener(this);

				drawview.startAnimation(trans);
				drawTitle.startAnimation(trans2);
			}
			break;
		}
	}

	// ツールバー・描画部の非表示
	public void slideOut(int menu) {
		switch (menu) {
		case LEFT_RIGHT:
			if (toolbarView != null) {
				isIn = false;

				toolbarView.setVisibility(LinearLayout.INVISIBLE);
				bt_toolOut.setVisibility(LinearLayout.INVISIBLE);

				TranslateAnimation trans = new TranslateAnimation(0,
						toolbarView.getWidth(), 0, 0);
				trans.setDuration(500);
				trans.setFillAfter(false);
				trans.setFillBefore(true);
				trans.setAnimationListener(this);

				TranslateAnimation trans2 = new TranslateAnimation(0,
						toolbarView.getWidth(), 0, 0);
				trans2.setDuration(500);
				trans2.setFillAfter(false);
				trans2.setFillBefore(true);
				trans2.setAnimationListener(this);

				toolbarView.startAnimation(trans);
				bt_toolOut.startAnimation(trans2);

			}
			break;

		case UP_DOWN:
			if (drawview != null) {
				isIn_up = false;

				drawview.setVisibility(LinearLayout.INVISIBLE);

				TranslateAnimation trans = new TranslateAnimation(0, 0, 0,
						drawview.getHeight());
				trans.setDuration(500);
				trans.setFillAfter(false);
				trans.setFillBefore(true);
				trans.setAnimationListener(this);

				TranslateAnimation trans2 = new TranslateAnimation(0, 0, 0,
						drawview.getHeight());
				trans2.setDuration(500);
				trans2.setFillAfter(false);
				trans2.setFillBefore(true);
				trans2.setAnimationListener(this);

				drawview.startAnimation(trans);
				drawTitle.startAnimation(trans2);
			}
			break;
		}
	}

	// ツールバー・描画部の表示アニメーション終了時の処理
	@Override
	public void onAnimationEnd(Animation animation) {
		if (toolbarView == null) {
		} else if (isIn) {

			toolbarView.setVisibility(LinearLayout.VISIBLE);
			bt_toolOut.setImageDrawable(getResources().getDrawable(
					R.drawable.tool_right));

			bt_toolOut.setVisibility(Button.VISIBLE);
		} else {
			bt_toolOut.setVisibility(Button.INVISIBLE);

			bt_toolIn.setVisibility(Button.VISIBLE);
			bt_toolOut.setImageDrawable(getResources().getDrawable(
					R.drawable.tool_left));

		}

		if (drawview == null) {
		} else if (isIn_up) {

			drawview.setVisibility(LinearLayout.VISIBLE);
			bt_canvasOut.setImageDrawable(getResources().getDrawable(
					R.drawable.arrow_down));

			drawTitle.setVisibility(Button.VISIBLE);
			if (mSharedPreferences.getBoolean("ch_draw_button", false)) {
				bt_canvasOut.setVisibility(Button.VISIBLE);

			} else {
				bt_canvasOut.setVisibility(Button.GONE);

			}

		} else {
			drawTitle.setVisibility(Button.INVISIBLE);

			int heigth = drawview.getHeight();
			drawview.setPadding(0, heigth, 0, -1 * heigth);
			drawview.setVisibility(LinearLayout.INVISIBLE);

			bt_canvasOut.setImageDrawable(getResources().getDrawable(
					R.drawable.arrow_up));
			if (mSharedPreferences.getBoolean("ch_draw_button", false)) {
				canvasIn.setVisibility(Button.VISIBLE);

			} else {
				canvasIn.setVisibility(Button.INVISIBLE);

			}
		}
	}

	// アニメーション繰り返し時の処理
	@Override
	public void onAnimationRepeat(Animation animation) {
		return;
	}

	// アニメーション開始時の処理
	@Override
	public void onAnimationStart(Animation animation) {
		return;
	}

	// NumberPickerの値取得
	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
		switch (picker.getId()) {
		// 画像送信時間変更用
		case R.id.waittime_picr:
			penView.setWaitTime(newVal * 1000);
			break;
		// 線の長さの倍率変更用
		case R.id.magnif_picr:
			penView.setMagnification(newVal);
			break;
		default:
			break;
		}
	}
}
