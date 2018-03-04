package com.example.android.livemap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by ErwinF on 2/23/2018.
 */

public class DragHandleLayout extends View {

    private Context mContext;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DragHandleLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
    }

    public DragHandleLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onDraw(Canvas canvas) {

        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#01579B"));
        paint.setAntiAlias(false);

        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAlpha(0xFF);

        canvas.drawRect(0, 0, (float) getMeasuredWidth(), (float) getMeasuredHeight()/2.0f, paint);
        canvas.drawRect((float) getMeasuredWidth() /3, (float) getMeasuredHeight() /2,
                (float) 2 * (getMeasuredWidth() /3), (float) getMeasuredHeight(), paint);
        canvas.drawCircle((float) getMeasuredWidth() /3, (float) getMeasuredHeight() /2, (float) getMeasuredHeight() /2, paint);
        canvas.drawCircle((float) 2 * (getMeasuredWidth() /3), (float) getMeasuredHeight() /2, (float) getMeasuredHeight() /2, paint);

        super.onDraw(canvas);
    }

}
