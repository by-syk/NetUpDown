package com.by_syk.netupdown.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by By_syk on 2016-11-08.
 */

public class FloatTextView extends TextView {
    private float lastX = 0;
    private float lastY = 0;

    private float viewStartX = 0;
    private float viewStartY = 0;

    private float offsetY = 0;

    private long lastTapTime = 0;
    private int tapTimes = 0;

//    private boolean isMoving = false;

//    private static final int TIME_LONG_PRESS = 2000;

    private OnMoveListener onMoveListener = null;

    public interface OnMoveListener {
        void onMove(int x, int y);
        void onDoubleTap();
        void onTripleTap();
//        void onLongPress();
    }

    public void setOnMoveListener(OnMoveListener onMoveListener) {
        this.onMoveListener = onMoveListener;
    }

    public FloatTextView(Context context) {
        this(context, null);
    }

    public FloatTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOffsetY(float offset) {
        offsetY = offset;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                lastX = event.getRawX();
                lastY = event.getRawY();
                viewStartX = event.getX();
                viewStartY = event.getY();
//                isMoving = false;
                long time = System.currentTimeMillis();
                if (time - lastTapTime < 600) {
                    ++tapTimes;
                    if (onMoveListener != null) {
                        if (tapTimes == 2) {
                            onMoveListener.onDoubleTap();
                        } else if (tapTimes == 3) {
                            onMoveListener.onTripleTap();
                        }
                    }
                } else {
                    lastTapTime = time;
                    tapTimes = 1;
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float x = event.getRawX();
                float y = event.getRawY();
                if (Math.abs(x - lastX) > 1 || Math.abs(y - lastY) > 1) {
//                    isMoving = true;
                    if (onMoveListener != null) {
                        onMoveListener.onMove((int) (x - viewStartX), (int) (y - viewStartY - offsetY));
                    }
                }
                lastX = x;
                lastY = y;
                break;
            }
//            case MotionEvent.ACTION_UP: {
//                if (!isMoving) {
//                    long time = System.currentTimeMillis();
//                    if (time - lastTapTime > TIME_LONG_PRESS) {
//                        if (onMoveListener != null) {
//                            onMoveListener.onLongPress();
//                        }
//                    }
//                }
//            }
        }
        return true;
    }
}
