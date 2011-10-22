package com.rogerxue.android.selfbalance;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rogerxue.android.selfbalance.model.DynamicPlotable2D;

public class MainActivity extends HelloAdkActivity implements OnClickListener, InclineCalculator.Observer{
	TextView mVisualizeLabel;
	TextView mSomeLabel;
	LinearLayout mVisualizeContainer;
	LinearLayout mSomeContainer;
	Drawable mFocusedTabImage;
	Drawable mNormalTabImage;
	
	private static final int MAX = 470;
	private PowerManager mPowerManager;
	private WakeLock mWakeLock;
	private InclineCalculator inclineCalculator;
	private ECGView ecgView;
	private TextView tv;
	DynamicPlotable2D data;
	Timer graphicTimer;
	TimerTask graphicTimerTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		mFocusedTabImage = getResources().getDrawable(
				R.drawable.tab_focused_holo_dark);
		mNormalTabImage = getResources().getDrawable(
				R.drawable.tab_normal_holo_dark);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass()
				.getName());
		setupMainView();

		inclineCalculator = new InclineCalculator(this);
		inclineCalculator.addObserver(this);
		graphicTimerTask = new TimerTask() {
			public void run() {
				ecgView.postInvalidate();
				if (inclineCalculator.getIncline()[0] > 0) {
					sendCommand((byte)2, (byte)0, 255);
				} else {
					sendCommand((byte)2, (byte)0, 0);
				}
			}
		};
	}

	void showTabContents(Boolean showInput) {
		if (showInput) {
			mVisualizeContainer.setVisibility(View.VISIBLE);
			mVisualizeLabel.setBackgroundDrawable(mFocusedTabImage);
			mSomeContainer.setVisibility(View.GONE);
			mSomeLabel.setBackgroundDrawable(mNormalTabImage);
		} else {
			mVisualizeContainer.setVisibility(View.GONE);
			mVisualizeLabel.setBackgroundDrawable(mNormalTabImage);
			mSomeContainer.setVisibility(View.VISIBLE);
			mSomeLabel.setBackgroundDrawable(mFocusedTabImage);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		/*
		 * when the activity is resumed, we acquire a wake-lock so that the
		 * screen stays on, since the user will likely not be fiddling with the
		 * screen or buttons.
		 */
		mWakeLock.acquire();
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		inclineCalculator.start();
		graphicTimer = new Timer();
		graphicTimer.schedule(graphicTimerTask, 0, 10);
	}

	@Override
	public void onPause() {
		super.onPause();
		inclineCalculator.stop();

		// and release our wake-lock
		mWakeLock.release();
	}

	private void setupMainView() {
		mVisualizeLabel = (TextView) findViewById(R.id.VisualizeLabel);
		mSomeLabel = (TextView) findViewById(R.id.SomeLabel);
		mVisualizeContainer = (LinearLayout) findViewById(R.id.VisualizeContainer);
		mSomeContainer = (LinearLayout) findViewById(R.id.SomeContainer);
		mVisualizeLabel.setOnClickListener(this);
		mSomeLabel.setOnClickListener(this);
		ArrayList<String> dataNames = new ArrayList<String>();
		dataNames.add("inclineX");
		dataNames.add("inclineY");
		dataNames.add("inlcineZ");
		dataNames.add("inclineDerX");
		dataNames.add("inclineDerY");
		dataNames.add("inclineDerZ");
		data = new DynamicPlotable2D(dataNames, MAX);
		tv = new TextView(this);
		ecgView = new ECGView(this, data, 10, 1, MAX, 400);
		mVisualizeContainer.addView(tv);
		mVisualizeContainer.addView(ecgView);
	}

	public void onClick(View v) {
		int vId = v.getId();
		switch (vId) {
		case R.id.VisualizeLabel:
			showTabContents(true);
			break;

		case R.id.SomeLabel:
			showTabContents(false);
			break;
		}
	}

	private void setContent(float xAcc, float yAcc, float zAcc, float xGyro, float yGyro,
			float zGyro) {
		tv.setText(
				"X acc:" + xAcc + 
				"\nY acc:" + yAcc +
				"\nZ acc:" + zAcc +
				"\nX gyr:" + xGyro + 
				"\nY gyr:" + yGyro + 
				"\nZ gyr:" + zGyro);
	}

	@Override
	public void onData() {
		data.add(new float[] {
				inclineCalculator.getIncline()[0],
				inclineCalculator.getIncline()[1],
				inclineCalculator.getIncline()[2],
				inclineCalculator.getInclineDerivative()[0],
				inclineCalculator.getInclineDerivative()[1],
				inclineCalculator.getInclineDerivative()[2]});
		setContent(
				inclineCalculator.getAccIncline()[0],
				inclineCalculator.getAccIncline()[1],
				inclineCalculator.getAccIncline()[2],
				inclineCalculator.getIncline()[0],
				inclineCalculator.getIncline()[1],
				inclineCalculator.getIncline()[2]);
	}
}