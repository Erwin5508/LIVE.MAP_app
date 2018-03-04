package com.example.android.livemap.Widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by ErwinF on 3/3/2018.
 */

public class WidgetService extends RemoteViewsService {
    private final String TAG = "WidgetService";
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(getApplicationContext(), intent);
    }
}
