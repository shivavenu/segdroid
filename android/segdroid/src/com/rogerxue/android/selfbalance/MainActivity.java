package com.rogerxue.android.selfbalance;

import java.util.ArrayList;

import android.app.TabActivity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.rogerxue.android.selfbalance.model.DynamicPlotable2D;

public class MainActivity extends TabActivity implements InclineCalculator.Observer{
  private static final int MAX = 470;
  private PowerManager mPowerManager;
  private WakeLock mWakeLock;
  private InclineCalculator inclineCalculator;
  private TabHost mTabHost;
  private ECGView ecgView;
  private LinearLayout mLinearLayout;
  private TextView tv;
  DynamicPlotable2D data;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    mTabHost = getTabHost();
    
    mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
    mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass()
      .getName());
    setupMainView();
    setupTab(mLinearLayout, "visualize");
    setupTab(new TextView(this), "something");
    mTabHost.setCurrentTab(0);

    inclineCalculator = new InclineCalculator(this);
    inclineCalculator.addObserver(this);
  }

  private void setupTab(final View view, final String tag) {
    TabSpec spec = mTabHost.newTabSpec(tag).setIndicator(tag).setContent(new TabContentFactory() {

      @Override
      public View createTabContent(String tag) {
        return view;
      }
    });
    mTabHost.addTab(spec);
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
  }

  @Override
  protected void onPause() {
    super.onPause();
    inclineCalculator.stop();

    // and release our wake-lock
    mWakeLock.release();
  }

  private void setupMainView() {
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
    mLinearLayout = new LinearLayout(this);
    mLinearLayout.setOrientation(LinearLayout.VERTICAL);
    mLinearLayout.addView(tv);
    mLinearLayout.addView(ecgView);
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
    ecgView.invalidate();
  }
}