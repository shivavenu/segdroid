package com.rogerxue.android.selfbalance;

import com.rogerxue.android.selfbalance.module.DynamicPlotable2D;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class ECGView extends View {
  private final DynamicPlotable2D data;
  private final int x;
  private final int y;
  private final int width;
  private final int height;
  private Paint bgPaint = new Paint();
  private Paint textPaint = new Paint();
  private Paint scalerPaint = new Paint();
  private Paint[] paints = new Paint[6];

  public ECGView(Context context, DynamicPlotable2D data, int x, int y, int width, int height) {
    super(context);
    this.data = data;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    bgPaint.setColor(0xff111111);
    scalerPaint.setColor(0x70ffffff);
    textPaint.setColor(0xffffffff);
    initPaints();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    initBg(canvas);
    for (int j = 0; j < data.getDataCount(); ++j) {
      for (int i = 0, iter = data.getCurrentIndex(); i < data.getBufferSize(); ++i) {
        canvas.drawPoint(x + width - map(i, data.getBufferSize(), width),
          y + height / 2 - map(data.getData(j)[iter], (float)Math.PI / 2, height / 2), paints[j]);
        iter = (iter - 1 + data.getBufferSize()) % data.getBufferSize();
      }
    }
  }

  private void initBg(Canvas canvas) {
    int legendSize = 10;
    int offSet = 2;
    canvas.drawRect(x, y, x + width, y + height, bgPaint);
    canvas.drawText("PI/4", x + width - 30, y + height / 4 - 10, textPaint);
    canvas.drawText("0", x + width - 30, y + height / 2 - 10, textPaint);
    canvas.drawText("-PI/4", x + width - 30, y + height / 4 * 3 - 10, textPaint);
    canvas.drawLine(x, y + height / 4, x + width, y + height / 4, scalerPaint);
    canvas.drawLine(x, y + height / 2, x + width, y + height / 2, scalerPaint);
    canvas.drawLine(x, y + height / 4 * 3, x + width, y + height / 4 * 3, scalerPaint);
    for (int i = 0; i < data.getDataCount(); ++i) {
      int pos = x + i * width / data.getDataCount();
      canvas.drawRect(pos, y + height + offSet, pos + legendSize, y + height + legendSize, paints[i]);
      canvas.drawText(data.getDataNames(i), pos + legendSize + offSet, y + height + legendSize, textPaint);
    }
  }

  private void initPaints() {
    paints[0] = new Paint();
    paints[0].setStrokeWidth(1);
    paints[0].setColor(0xff00ff00);
    paints[1] = new Paint();
    paints[1].setStrokeWidth(1);
    paints[1].setColor(0xffffff00);
    paints[2] = new Paint();
    paints[2].setStrokeWidth(1);
    paints[2].setColor(0xffff00ff);
    paints[3] = new Paint();
    paints[3].setStrokeWidth(1);
    paints[3].setColor(0xff00ffff);
    paints[4] = new Paint();
    paints[4].setStrokeWidth(1);
    paints[4].setColor(0xff0000ff);
    paints[5] = new Paint();
    paints[5].setStrokeWidth(1);
    paints[5].setColor(0xffff0000);
  }

  private float map(int value, int fromRange, int toRange) {
    return (float)value / fromRange * toRange;
  }

  private float map(float value, float fromRange, int toRange) {
    return value / fromRange * toRange;
  }
}
