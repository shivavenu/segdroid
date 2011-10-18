package com.rogerxue.android.selfbalance;

import java.util.ArrayList;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Aggregates accelerometer and gyro data to calculate inclinatio of the device.
 */
public class InclineCalculator implements SensorEventListener {
  private static final long NANO_IN_SEC = 1000000000;
  public interface Observer {
    /** Data has refreshed */
    void onData();
  }

  private SensorManager sensorManager;
  private Sensor mAccelerometer;
  private Sensor mGyro;
  private long mLastT;
  private long mDeltaTime;
  private ArrayList<Observer> observers = new ArrayList<Observer>();

  private float[] mAccSensor = new float[3];
  private float[] mGyroSensor = new float[3];

  private float[] mAccIncline = new float[3];
  private float[] mGyroIncline = new float[3];

  private float[] mPreviousIncline = new float[3];
  private float[] mInclineDerivative = new float[3];
  private float[] mIncline = new float[3];

  public InclineCalculator(Context context) {
    sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    mGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
  }

  public void start() {
    sensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    sensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_UI);
  }

  public void stop() {
    sensorManager.unregisterListener(this);
  }

  public boolean addObserver(Observer observer) {
    return observers.add(observer);
  }
  
  public boolean removeObserver(Observer observer) {
    return observers.remove(observer);
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER && 
        event.sensor.getType() != Sensor.TYPE_GYROSCOPE)
      return;

    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
      for (int i = 0; i < 3; ++i) {
        mAccSensor[i] = event.values[i];
      }
      calculateAccIncline();
    } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
      for (int i = 0; i < 3; ++i) {
        mGyroSensor[i] = event.values[i];
      }
      mDeltaTime = event.timestamp - mLastT;
      mLastT = event.timestamp;
      calculateGyroIncline();
    }
    calculateIncline();
    notifyAllObservers();
  }

  private void notifyAllObservers() {
    for (Observer observer : observers) {
      observer.onData();
    }
  }

  private void calculateGyroIncline() {
    for (int i = 0; i < 3; ++i) {
      mGyroIncline[i] = mIncline[i] + mGyroSensor[i] * mDeltaTime / NANO_IN_SEC;
    }
  }

  private void calculateAccIncline() {
    float gravity = (float) Math.sqrt(square(mAccSensor[0]) +square( mAccSensor[1]) + square(mAccSensor[2]));
    for (int i = 0; i < 3; ++i) {
      mAccIncline[i] = (float) (Math.acos(mAccSensor[i] / gravity) - Math.PI / 2);
    }
  }

  private void calculateIncline() {
    float complementaryFilter = 0.88F;
    for (int i = 0; i < 3; ++i) {
      mPreviousIncline[i] = mIncline[i];
      mIncline[i] = mAccIncline[i] * (1 - complementaryFilter) + mGyroIncline[i] * complementaryFilter;
      mInclineDerivative[i] = (mIncline[i] - mPreviousIncline[i]) / mDeltaTime * NANO_IN_SEC;
    }
  }

  private float square(float value) {
    return value * value;
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
  }

  public float[] getAccIncline() {
    return mAccIncline;
  }

  public float[] getGyroIncline() {
    return mGyroIncline;
  }

  public float[] getIncline() {
    return mIncline;
  }

  public float[] getInclineDerivative() {
    return mInclineDerivative;
  }
}
