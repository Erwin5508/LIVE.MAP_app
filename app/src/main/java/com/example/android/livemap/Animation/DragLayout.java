package com.example.android.livemap.Animation;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Created by ErwinF on 2/27/2018.
 */

public class DragLayout implements View.OnTouchListener {

    // Handle Handle Handler ;)
    private ViewGroup mBottomFrame;
    private Context mContext;

    private float mLastTouchY;

    public DragLayout(ViewGroup frame, Context context) {
        this.mBottomFrame = frame;
        this.mContext = context;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int MaxHeight = 1200;
        int MinHeight = 450;

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN: {
                // start point
                mLastTouchY = event.getRawY();
                break;
            }

            case MotionEvent.ACTION_MOVE: {

                final float y = event.getRawY();

                // Calculate the distance moved since last move
                final float dy = mLastTouchY - y;

                // move accordingly
                mBottomFrame.getParent();
                ViewGroup.LayoutParams layoutParams = (ViewGroup.LayoutParams) mBottomFrame.getLayoutParams();
                float newHeight = layoutParams.height + dy;
                if (newHeight <= MaxHeight && newHeight >= MinHeight) {
                    layoutParams.height = (int) newHeight;
                }

                // reset for next measured move
                mLastTouchY = y;
                mBottomFrame.requestLayout();
                mBottomFrame.invalidate();
                return true;
            }
            case MotionEvent.ACTION_UP: {
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                break;
            }
        }
        return true;
    }

    private void toast(String position, String message) {
        Toast.makeText(mContext, position + ":// \n" + message, Toast.LENGTH_SHORT).show();
    }
}

