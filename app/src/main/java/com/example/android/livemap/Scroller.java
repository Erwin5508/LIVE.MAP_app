package com.example.android.livemap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.NumberPicker;

/**
 * Created by ErwinF on 2/23/2018.
 */

public class Scroller extends NumberPicker {

    public Scroller(Context context) {
        super(context);
    }

    public Scroller(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public Scroller(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context,attrs, defStyleAttr);
    }

    @Override
    public void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#01579B"));
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        //canvas.drawCircle(1,1,5, paint);
        canvas.drawOval(0,0, getMeasuredWidth(), getMeasuredHeight(), paint);
        super.onDraw(canvas);

    }

    @Override
    protected void onMeasure(int width, int height){
        super.onMeasure(width, height);
    }

}
