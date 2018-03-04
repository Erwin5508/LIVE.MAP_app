package com.example.android.livemap.Widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.example.android.livemap.Database.ObjectivesContract;
import com.example.android.livemap.LiveMapActivity;
import com.example.android.livemap.R;

/**
 * Implementation of App Widget functionality.
 */
public class ObjectivesWidget extends AppWidgetProvider {

    private final String TAG = "BakingAppWidget";
    static int index = -1;
    static String[] mDataTitles;
    static String[] mDataDescriptions;
    static Cursor mCursor;

    private static int mAppWidgetId;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        makeData(context);

        // Construct the RemoteViews object
        RemoteViews views;

        Intent intent = new Intent(context, LiveMapActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        if (mDataDescriptions == null) {
            // raw view
            views = new RemoteViews(context.getPackageName(), R.layout.objectives_widget);
            views.setTextViewText(R.id.appwidget_text, "Your Objectives");

            views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);
        } else {
            views = getRecipeListView(context);
            views.setOnClickPendingIntent(R.id.widget_objc_descrs, pendingIntent);
        }

        mAppWidgetId = appWidgetId;
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static RemoteViews getRecipeListView(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.objectives_widget_list_view);

        if (index != -1) {
            try {
                views.setTextViewText(R.id.widget_objc_descrs, mDataDescriptions[index]);
            } catch (Exception e) {
                String error = e.toString() + "\n\n" + "caused by this index: " + index +
                        "\nAlthough max length of array is: " + mDataDescriptions.length;
                views.setTextViewText(R.id.widget_objc_descrs, error);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            setRemoteAdapter(context, views);
        } else {
            setRemoteAdapterV11(context, views);
        }

        Intent ingredientsIntent = new Intent(context, ObjectivesWidget.class);

        PendingIntent liveMapPendingIntent = PendingIntent.getBroadcast(context, 0,
                ingredientsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widget_list_objc_titles, liveMapPendingIntent);

        return views;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {

        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        if (intent != null && mDataDescriptions != null) {
            index = intent.getIntExtra("i", -1);
            mgr.updateAppWidget(mAppWidgetId, getRecipeListView(context));
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(R.id.widget_list_objc_titles,
                new Intent(context, WidgetService.class));
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @SuppressWarnings("deprecation")
    private static void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(0, R.id.widget_list_objc_titles,
                new Intent(context, WidgetService.class));
    }


    public static void makeData(Context context) {
        try {
            if (mCursor != null) mCursor.close();
            mCursor = context.getContentResolver().query(
                    ObjectivesContract.ObjectivesEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    ObjectivesContract.ObjectivesEntry._ID);
            if (mCursor == null) {
                mDataDescriptions[0] = "Most likely no internet:\nNo data was loaded\n" +
                        "Try restarting the App";
                return;
            }
            int i = 0;
            int size = mCursor.getCount();
            mDataTitles = new String[size];
            mDataDescriptions = new String[size];
            while (mCursor.moveToNext()) {
                mDataTitles[i] = mCursor.getString(mCursor.getColumnIndex
                        (ObjectivesContract.ObjectivesEntry.OBJECTIVES_TITLE));
                mDataDescriptions[i] = mCursor.getString(mCursor.getColumnIndex
                        (ObjectivesContract.ObjectivesEntry.OBJECTIVES_DESCRIPTION));
                i++;
            }
        } catch (Exception e) {
            mDataDescriptions[0] = e.toString();
        } finally {
            mCursor.close();
        }
    }
}

