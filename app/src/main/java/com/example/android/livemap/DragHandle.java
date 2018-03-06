package com.example.android.livemap;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

/**
 * Created by ErwinF on 2/23/2018.
 */

public class DragHandle extends android.support.v7.widget.AppCompatImageView {

    public DragHandle(Context context) {
        super(context);
    }

    public DragHandle(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public DragHandle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
    }
}
