package com.rogerxue.android.selfbalance.module;

import java.util.ArrayList;

/**
 * A plotable ECG data. It could have multiple items, but all item have the same number of data. It
 * reuse the buffer if data over flows.
 */
public class DynamicPlotable2D {
  private final ArrayList<float[]> mData;
  private final ArrayList<String> mDataNames;
  private final int dataCount;
  private final int bufferSize;
  private int nextIndex = 0;
  
  public DynamicPlotable2D(ArrayList<String> dataNames, int bufferSize) {
    mData = new ArrayList<float[]>();
    mDataNames = new ArrayList<String>(dataNames);
    this.bufferSize = bufferSize;
    dataCount = dataNames.size();
    for (int i = 0; i < dataNames.size(); ++i) {
      mData.add(new float[bufferSize]);
    }
  }

  /**
   * @param data must be the same order and number when construct this class.
   */
  public void add(float[] data) {
    if (data.length != dataCount) {
      throw new RuntimeException("data length inconsistent with constructed data");
    }
    for (int i = 0; i < data.length; ++i) {
      mData.get(i)[nextIndex] = data[i];
    }
    nextIndex = (nextIndex + 1) % getBufferSize();
  }

  public int getBufferSize() {
    return bufferSize;
  }

  public int getNextIndex() {
    return nextIndex;
  }

  public int getCurrentIndex() {
    return (nextIndex - 1 + getBufferSize()) % getBufferSize();
  }

  public int getDataCount() {
    return dataCount;
  }

  public String getDataNames(int index) {
    return mDataNames.get(index);
  }

  public float[] getData(int index) {
    return mData.get(index);
  }
}
