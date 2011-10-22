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
	private TextView mVisualizeLabel;
	private TextView mSomeLabel;
	private LinearLayout mVisualizeContainer;
	private LinearLayout mSomeContainer;
	private Drawable mFocusedTabImage;
	private Drawable mNormalTabImage;
	
	private int maxWidth;
	private PowerManager mPowerManager;
	private WakeLock mWakeLock;
	private InclineCalculator mInclineCalculator;
	private ECGView mEcgView;
	private TextView mTextView;
	private DynamicPlotable2D data;
	private Timer mGraphicTimer = new Timer();;
	private TimerTask mGraphicTimerTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		mFocusedTabImage = getResources().getDrawable(
				R.drawable.tab_focused_holo_dark);
		mNormalTabImage = getResources().getDrawable(
				R.drawable.tab_normal_holo_dark);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		maxWidth = getWindowManager().getDefaultDisplay().getWidth();
		mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass()
				.getName());
		setupMainView();

		mInclineCalculator = new InclineCalculator(this);
		mInclineCalculator.addObserver(this);
		mGraphicTimerTask = new TimerTask() {
			public void run() {
				mEcgView.postInvalidate();
				if (mInclineCalculator.getIncline()[0] > 0) {
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

		mInclineCalculator.start();
		mGraphicTimer.schedule(mGraphicTimerTask, 0, 10);
	}

	@Override
	public void onPause() {
		super.onPause();
		mInclineCalculator.stop();

		// and release our wake-lock
		mWakeLock.release();
		mGraphicTimer.cancel();
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
		data = new DynamicPlotable2D(dataNames, maxWidth);
		mTextView = new TextView(this);
		mEcgView = new ECGView(this, data, 1, 1, maxWidth, 400);
		mVisualizeContainer.addView(mTextView);
		mVisualizeContainer.addView(mEcgView);
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
		mTextView.setText(
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
				mInclineCalculator.getIncline()[0],
				mInclineCalculator.getIncline()[1],
				mInclineCalculator.getIncline()[2],
				mInclineCalculator.getInclineDerivative()[0],
				mInclineCalculator.getInclineDerivative()[1],
				mInclineCalculator.getInclineDerivative()[2]});
		setContent(
				mInclineCalculator.getAccIncline()[0],
				mInclineCalculator.getAccIncline()[1],
				mInclineCalculator.getAccIncline()[2],
				mInclineCalculator.getIncline()[0],
				mInclineCalculator.getIncline()[1],
				mInclineCalculator.getIncline()[2]);
	}
}