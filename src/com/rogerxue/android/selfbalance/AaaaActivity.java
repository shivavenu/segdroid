package com.rogerxue.android.selfbalance;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rogerxue.android.selfbalance.module.DynamicPlotable2D;

public class AaaaActivity extends Activity {
  private MainView mainView;
  private PowerManager mPowerManager;
  private WakeLock mWakeLock;
  private InclineCalculator inclineCalculator;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);

    mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass()
      .getName());

    mainView = new MainView(this);
    inclineCalculator = new InclineCalculator(this);
    inclineCalculator.addObserver(mainView);
    setContentView(mainView);
  }

  @Override
  protected void onResume() {
    super.onResume();
    /*
     * when the activity is resumed, we acquire a wake-lock so that the
     * screen stays on, since the user will likely not be fiddling with the
     * screen or buttons.
     */
    mWakeLock.acquire();
    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    inclineCalculator.start();
    mainView.prepare();
  }

  @Override
  protected void onPause() {
    super.onPause();
    inclineCalculator.stop();

    // and release our wake-lock
    mWakeLock.release();
  }

  class MainView extends View implements InclineCalculator.Observer {
    private static final int MAX = 470;
    private ECGView ecgView;
    private LinearLayout mLinearLayout;
    private TextView tv;
    DynamicPlotable2D data;

    public MainView(Context context) {
      super(context);
      ArrayList<String> dataNames = new ArrayList<String>();
      dataNames.add("inclineX");
      dataNames.add("inclineY");
      dataNames.add("inlcineZ");
      dataNames.add("inclineDerX");
      dataNames.add("inclineDerY");
      dataNames.add("inclineDerZ");
      data = new DynamicPlotable2D(dataNames, MAX);
      tv = new TextView(context);
      ecgView = new ECGView(context, data, 10, 1, MAX, 400);
      mLinearLayout = new LinearLayout(context);
      mLinearLayout.setOrientation(LinearLayout.VERTICAL);
      mLinearLayout.addView(tv);
      mLinearLayout.addView(ecgView);
    }

    public void prepare() {
      setContentView(mLinearLayout);
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
      ecgView.invalidate();
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
  }
}