package com.example.android.livemap.Animation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by ErwinF on 2/24/2018.
 *
 * This class allow the smooth animated movement of Integers within a View,
 * that describe the start and the end of the resizing.
 *
 * it is used for now to autocomplete of a Layout resizing
 */

public class ResizeAnimation extends Animation{
    private int startHeight;
    private int deltaHeight;

    private View view;

    public ResizeAnimation(View v) {
        this.view = v;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t){
        view.getLayoutParams().height = (int) (startHeight + deltaHeight * interpolatedTime);
        view.requestLayout();
    }

    public void setParams(int start, int end) {
        this.startHeight = start;
        deltaHeight = end - startHeight;
    }
    @Override
    public void setDuration(long durationMillis){
        super.setDuration(durationMillis);
    }
    @Override
    public boolean willChangeBounds(){
        return true;
    }
}
