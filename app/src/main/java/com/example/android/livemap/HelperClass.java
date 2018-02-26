package com.example.android.livemap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.graphics.drawable.DrawableCompat;

/**
 * Created by ErwinF on 2/24/2018.
 */

public class HelperClass {

    public Bitmap _getBitmap(int drawableId, Context context) {
        Drawable vectorDrawable;
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
            vectorDrawable = context.getResources().getDrawable(drawableId,null);
        }else {
            vectorDrawable = context.getResources().getDrawable(drawableId);
        }

        Drawable wrapDrawable = DrawableCompat.wrap(vectorDrawable);


        int h = vectorDrawable.getIntrinsicHeight();
        int w = vectorDrawable.getIntrinsicWidth();
        h=h>0?h:96;
        w=w>0?w:96;

        wrapDrawable.setBounds(0, 0, w, h);
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        wrapDrawable.draw(canvas);
        return bm;
    }
}
