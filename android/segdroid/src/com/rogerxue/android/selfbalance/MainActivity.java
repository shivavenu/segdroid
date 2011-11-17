package com.rogerxue.android.selfbalance;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rogerxue.android.selfbalance.model.DynamicPlotable2D;
import com.rogerxue.android.selfbalance.model.MotorCommand;

public class MainActivity extends BasicAdkActivity implements OnClickListener, InclineCalculator.Observer {
	private static final byte MD49_COMMAND = 0;
	private MotorCommand command;
	private PIDController pidController;
	
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
	private CrossBarView mCrossBarView;
	private TextView mTextView;
	private DynamicPlotable2D mData;
	private Timer mGraphicTimer = new Timer();;
	private TimerTask mGraphicTimerTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		command = new MotorCommand((byte)0, (byte)0);
		pidController = new PIDController(300, 0, 5000, command);
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
				mCrossBarView.postInvalidate();
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
		mSomeContainer = (LinearLayout) findViewById(R.id.SomeContainer);
		mVisualizeLabel.setOnClickListener(this);
		mSomeLabel.setOnClickListener(this);
		mTextView = new TextView(this);
		getEcgView();
		mCrossBarView = new CrossBarView(this, 1, 1, 200, 200, command);
		
		final EditText pEdit = (EditText) findViewById(R.id.paramP);
		pEdit.setText(Double.toString(pidController.getParamP()));
		pEdit.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				pidController.setParamP(Double.parseDouble(pEdit.getText().toString()));
				return true;
			}
		});
		
		final EditText dEdit = (EditText) findViewById(R.id.paramD);
		dEdit.setText(Double.toString(pidController.getParamD()));
		dEdit.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				pidController.setParamD(Double.parseDouble(dEdit.getText().toString()));
				return true;
			}
		});
		
		((LinearLayout) findViewById(R.id.sensorGraph)).addView(mEcgView);
		((LinearLayout) findViewById(R.id.motorCommand)).addView(mCrossBarView);
//		((LinearLayout) findViewById(R.id.motorCommand)).addView(mTextView);
	}
	
	private void getEcgView() {
		ArrayList<String> dataNames = new ArrayList<String>();
		dataNames.add("inclineY");
		dataNames.add("GyroY");
		dataNames.add("GyroZ");
		mData = new DynamicPlotable2D(dataNames, maxWidth);
		mEcgView = new ECGView(this, mData, 1, 1, maxWidth, 200); 
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

	@Override
	public void onData() {
		mData.add(new float[] {
				mInclineCalculator.getIncline()[1],
				- mInclineCalculator.getGyroInclineDerivative()[0] * 20,
				mInclineCalculator.getGyroInclineDerivative()[2] * 20});
		pidController.calculateMotorCommand(mInclineCalculator.getIncline()[1], 0, -mInclineCalculator.getGyroInclineDerivative()[0]);
		sendCommand(MD49_COMMAND, command.speed, command.turn);
		mTextView.setText("speed: " + command.speed);
	}
}