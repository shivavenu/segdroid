package com.rogerxue.android.selfbalance;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import com.rogerxue.android.selfbalance.model.MotorCommand;

public class CrossBarView extends View {
	private static int legendSize = 10;
	private final MotorCommand command;
	private final int x;
	private final int y;
	private final int width;
	private final int height;
	private Paint bgPaint = new Paint();
	private Paint textPaint = new Paint();
	private Paint scalerPaint = new Paint();
	private Paint barPaint = new Paint();

	public CrossBarView(Context context, int x, int y, int width, int height, MotorCommand command) {
		super(context);
		this.command = command;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		bgPaint.setColor(0xff111111);
		scalerPaint.setColor(0x70ffffff);
		textPaint.setColor(0xffffffff);
		barPaint.setColor(0xff00ff00);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		initBg(canvas);
		//speed
		float top;
		float botton;
		if (command.speed > 0) {
			top = y + height / 2 - legendSize / 2 - map(command.speed, 127, (height - legendSize) / 2);
			botton = y + height / 2;
		} else {
			top = y + height / 2;
			botton = y + height / 2 - legendSize / 2 - map(command.speed, 127, (height - legendSize) / 2);
		}
		canvas.drawRect(x + width / 2 - legendSize / 2, top, x + width / 2 + legendSize / 2, botton, barPaint);
	}

	private void initBg(Canvas canvas) {
		canvas.drawRect(x, y, x + width, y + height, bgPaint);
		canvas.drawRect(x + width / 2 - legendSize / 2, y + height / 2 - legendSize / 2, x + width / 2 + legendSize / 2, y + height / 2 + legendSize / 2, barPaint);
	}
	
	private float map(int value, int fromRange, int toRange) {
		return (float)value / fromRange * toRange;
	}

	private float map(float value, float fromRange, int toRange) {
		return value / fromRange * toRange;
	}
}
