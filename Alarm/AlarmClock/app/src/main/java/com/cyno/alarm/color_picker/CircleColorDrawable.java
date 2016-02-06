package com.cyno.alarm.color_picker;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;


@SuppressLint("NewApi")
public class CircleColorDrawable extends ColorDrawable {
	private float strokeWidth = 3f;
	private Paint strokePaint = PaintBuilder.newPaint().style(Paint.Style.STROKE).stroke(strokeWidth).color(0xff000000).build();
	private Paint fillPaint = PaintBuilder.newPaint().style(Paint.Style.FILL).color(0).build();
	private Paint fillBackPaint = PaintBuilder.newPaint().shader(PaintBuilder.createAlphaPatternShader((int) strokeWidth * 2)).build();

	public CircleColorDrawable() {
		super();
	}

	public CircleColorDrawable(int color) {
		super(color);
	}

	@SuppressLint("NewApi")
	@Override
	public void draw(Canvas canvas) {
		canvas.drawColor(0);

		int width = canvas.getWidth();
		float radius = width / 2f;
		strokeWidth = radius / 8f;

		this.strokePaint.setStrokeWidth(strokeWidth);
		this.fillPaint.setColor(getColor());
		canvas.drawCircle(radius, radius, radius - strokeWidth * 1.5f, fillBackPaint);
		canvas.drawCircle(radius, radius, radius - strokeWidth * 1.5f, fillPaint);
		canvas.drawCircle(radius, radius, radius - strokeWidth, strokePaint);
	}

	@Override
	public void setColor(int color) {
		super.setColor(color);
		invalidateSelf();
	}
}