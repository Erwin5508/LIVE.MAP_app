package com.example.android.livemap.Database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ErwinF on 2/24/2018.
 */

public class ObjectivesContract {

    public static final String AUTHORITY = "com.example.android.livemap";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_TO_ALL_IN_DATA = "objective_save";

    public static final class ObjectivesEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TO_ALL_IN_DATA).build();

        public static final String TABLE_NAME = "objectives";
    }
}
