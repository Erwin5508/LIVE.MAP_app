package com.example.android.livemap.Widget;

/**
 * Created by ErwinF on 3/4/2018.
 */

import android.appwidget.AppWidgetManager;
import android.content.Intent;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

public class MyJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters job) {
        // Do some work here
        Intent intent = new Intent(this, ObjectivesWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        sendBroadcast(intent);
        return false; //return false if job done otherwise return true
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false; //Should this job be retried?"
    }
}
