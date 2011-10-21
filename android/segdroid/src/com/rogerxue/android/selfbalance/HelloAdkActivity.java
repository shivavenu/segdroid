package com.rogerxue.android.selfbalance;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

public class HelloAdkActivity extends Activity {
  private static final String ACTION_USB_PERMISSION = "com.google.android.DemoKit.action.USB_PERMISSION";
  private static final String TAG = "HelloAdkActivity";

  private PendingIntent mPermissionIntent;
  private boolean mPermissionRequestPending;
  private UsbManager mUsbManager;
  private UsbAccessory mAccessory;
  private ParcelFileDescriptor mFileDescriptor;
  private FileInputStream mInputStream;
  private FileOutputStream mOutputStream;

  private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (ACTION_USB_PERMISSION.equals(action)) {
        synchronized (this) {
          UsbAccessory accessory = UsbManager.getAccessory(intent);
          if (intent.getBooleanExtra(
            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
            openAccessory(accessory);
          } else {
            Log.d(TAG, "permission denied for accessory " + accessory);
          }
          mPermissionRequestPending = false;
        }
      } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
        UsbAccessory accessory = UsbManager.getAccessory(intent);
        if (accessory != null && accessory.equals(mAccessory)) {
          closeAccessory();
        }
      }
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mUsbManager = UsbManager.getInstance(this);
    mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
      ACTION_USB_PERMISSION), 0);
    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
    filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
    registerReceiver(mUsbReceiver, filter);

    //      if (getLastNonConfigurationInstance() != null) {
    //          mAccessory = (UsbAccessory) getLastNonConfigurationInstance();
    //          openAccessory(mAccessory);
    //      }
    Button b = new Button(this);
    b.setText("Flash");
    b.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        //TODO
      }
    });
    setContentView(new Button(this));
  }

  @Override
  public void onResume() {
      super.onResume();

      if (mInputStream != null && mOutputStream != null) {
          return;
      }

      UsbAccessory[] accessories = mUsbManager.getAccessoryList();
      UsbAccessory accessory = (accessories == null ? null : accessories[0]);
      if (accessory != null) {
          if (mUsbManager.hasPermission(accessory)) {
              openAccessory(accessory);
          } else {
              synchronized (mUsbReceiver) {
                  if (!mPermissionRequestPending) {
                      mUsbManager.requestPermission(accessory,
                              mPermissionIntent);
                      mPermissionRequestPending = true;
                  }
              }
          }
      } else {
          Log.d(TAG, "mAccessory is null");
      }
  }

  @Override
  public void onPause() {
      super.onPause();
      closeAccessory();
  }

  @Override
  public void onDestroy() {
      unregisterReceiver(mUsbReceiver);
      super.onDestroy();
  }

  private void openAccessory(UsbAccessory accessory) {
    mFileDescriptor = mUsbManager.openAccessory(accessory);
    if (mFileDescriptor != null) {
      mAccessory = accessory;
      FileDescriptor fd = mFileDescriptor.getFileDescriptor();
      mInputStream = new FileInputStream(fd);
      mOutputStream = new FileOutputStream(fd);
      Log.d(TAG, "accessory opened");
    } else {
      Log.d(TAG, "accessory open fail");
    }
  }

  private void closeAccessory() {

    try {
      if (mFileDescriptor != null) {
        mFileDescriptor.close();
      }
    } catch (IOException e) {
    } finally {
      mFileDescriptor = null;
      mAccessory = null;
    }
  }

  public void sendCommand(byte command, byte target, int value) {
    byte[] buffer = new byte[3];
    if (value > 255)
      value = 255;

    buffer[0] = command;
    buffer[1] = target;
    buffer[2] = (byte) value;
    if (mOutputStream != null && buffer[1] != -1) {
      try {
        mOutputStream.write(buffer);
      } catch (IOException e) {
        Log.e(TAG, "write failed", e);
      }
    }
  }
}
