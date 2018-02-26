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
//        Paint paint = new Paint();
//        paint.setColor(Color.parseColor("#01579B"));
//        paint.setAntiAlias(true);
//        paint.setStyle(Paint.Style.FILL_AND_STROKE);
//        paint.setStrokeWidth(20);
//
//        float[] points = generatePoints();
//
//        canvas.drawPoints(points, paint);
        super.onDraw(canvas);
    }

    private float[] generatePoints() {
        float param = 1500;

        float min = 0.0f;
        float max = 1.0f * param;
        float[] result = new float[(int) max ];
        for (int i = 0; i<(int) max ; i++) {
            result[i] = min;
            min += 1;
        }
        return result;
    }
}
