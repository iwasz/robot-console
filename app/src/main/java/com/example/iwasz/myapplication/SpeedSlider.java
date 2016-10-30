package com.example.iwasz.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by iwasz on 19.10.16.
 */

public class SpeedSlider extends View {

    // Outer circle
    private Paint oCPaint;
    private float oCRadius = 0;
    private float oCX, oCY;
    private static final int oCWidth = 50;

    // Inner circle
    private Paint iCPaint;
    private float iCRadius = 0;

    private double lSpeed; // Left motor speed.
    private double rSpeed; // Right motor speed.

    private OnSpeedSliderChangeListener speedSliderChangeListener;

    /**
     * Listener interface.
     */
    public interface OnSpeedSliderChangeListener {
        void onChanged(SpeedSlider speedSlider, double lSpeed, double rSpeed);
    }

    public SpeedSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        oCPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        oCPaint.setColor(0xffffffff);
        oCPaint.setStrokeWidth(oCWidth);
        oCPaint.setStyle(Paint.Style.STROKE);

        iCPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        iCPaint.setColor(0xffffffff);
        iCPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float xpad = (float) (getPaddingLeft() + getPaddingRight());
        float ypad = (float) (getPaddingTop() + getPaddingBottom());
        oCRadius = (Math.min(w, h) - Math.max(xpad, ypad)) / 2 - oCWidth / 2;
        oCX = oCRadius + getPaddingLeft() + oCWidth / 2;
        oCY = oCRadius + getPaddingTop() + oCWidth / 2;

        iCRadius = oCRadius / 3;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        canvas.drawCircle(oCX, oCY, oCRadius, oCPaint);
        canvas.drawCircle(oCX, oCY, iCRadius, iCPaint);
//        Log.v("MyView", "onDraw");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
            double x = event.getX() - oCX;
            double y = event.getY() - oCY;
            double r = Math.hypot(x, y);

            r = Math.min(oCRadius, r);

            if (r < iCRadius) {
                r = 0;
                return true;
            }

            r -= iCRadius;

            double phi = Math.atan2(x, y);

            // 1. Obliczyć l i rSpeed w zależności od r, ale tak jakby jechał do przodu (l == r).
            lSpeed = r * 127 / (oCRadius - iCRadius);
            rSpeed = lSpeed;

            /*
            // 2. Skierować prędkości l i r w przeciwnym kierunku, jeśli palec na dole
            phi E [-PI : -PI/2] || phi E [PI/2 : PI] **jedziemy do przodu**.
            phi E [-PI/2 : 0] || phi E [0 : PI/2] **jedziemy do tyłu**.

            // 3. zwolnić jeden z silników proporcjonalnie do phi:

            */


            // Góra lewa.
            if (phi < -Math.PI * 3 / 4 && phi > -Math.PI) {
                lSpeed = (-Math.PI * 3 / 4 - phi) / (Math.PI / 4) * rSpeed;
                // Log.v("MyView", "r = " + r + ", ph = " + phi + ", lSpeed = " + lSpeed + ", rSpeed = " + rSpeed);
            }
            // Góra lewa niżej.
            if (phi < -Math.PI / 2 && phi > -Math.PI * 3 / 4) {
                lSpeed = -(Math.PI * 3 / 4 + phi) / (Math.PI / 4) * rSpeed;
                // Log.v("MyView", "r = " + r + ", ph = " + phi + ", lSpeed = " + lSpeed + ", rSpeed = " + rSpeed);
            }

            if (phi > Math.PI * 3 / 4 && phi < Math.PI) {
                rSpeed = -(Math.PI * 3 / 4 - phi) / (Math.PI / 4) * lSpeed;
                // Log.v("MyView", "r = " + r + ", ph = " + phi + ", lSpeed = " + lSpeed + ", rSpeed = " + rSpeed);
            }
            // Góra lewa niżej.
            if (phi < Math.PI * 3 / 4 && phi > Math.PI / 2) {
                rSpeed = (-Math.PI * 3 / 4 + phi) / (Math.PI / 4) * lSpeed;
            }

            // Log.v("MyView", "r = " + r + ", ph = " + phi + ", lSpeed = " + lSpeed + ", rSpeed = " + rSpeed);
            if (speedSliderChangeListener != null) {
                speedSliderChangeListener.onChanged(this, lSpeed, rSpeed);
            }

//            // Dół lewa wyżej.
//            if (phi < -Math.PI / 4 && phi > -Math.PI / 2) {
//                lSpeed = -(Math.PI * 3 / 4 + phi) / (Math.PI / 4) * rSpeed;
//                Log.v("MyView", "r = " + r + ", ph = " + phi + ", lSpeed = " + lSpeed + ", rSpeed = " + rSpeed);
//            }

        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            Log.v("MyView", "FULL STOP");
            rSpeed = 0;
            lSpeed = 0;

            if (speedSliderChangeListener != null) {
                speedSliderChangeListener.onChanged(this, lSpeed, rSpeed);
            }
        }


        return true;
    }

    public void setSpeedSliderChangeListener(OnSpeedSliderChangeListener speedSliderChangeListener) {
        this.speedSliderChangeListener = speedSliderChangeListener;
    }

}
