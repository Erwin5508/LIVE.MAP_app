package com.example.android.livemap.Database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.example.android.livemap.Database.ObjectivesContract.ObjectivesEntry.*;

/**
 * Created by ErwinF on 2/24/2018.
 */

public class ObjectivesContentProvider extends ContentProvider {

    public static final UriMatcher sUriMatcher = buildUriMatcher();

    public static final int ALL_DATA = 100;

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(ObjectivesContract.AUTHORITY,
                ObjectivesContract.PATH_TO_ALL_IN_DATA, ALL_DATA);

        return uriMatcher;
    }

    private ObjectivesDbHelper mObjectivesDbHelper;

    @Override
    public boolean onCreate() {
        mObjectivesDbHelper = new ObjectivesDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = mObjectivesDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match) {
            case ALL_DATA:
                retCursor = db.query(TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = mObjectivesDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);

        Uri returnUri;
        switch (match) {
            case ALL_DATA:
                long id = db.insert(TABLE_NAME, null, values);
                if (id >= 0) {
                    returnUri = ContentUris.withAppendedId
                            (CONTENT_URI, id);
                } else {
                    throw new UnsupportedOperationException("Failed to insert row into " + uri);
                }
                break;

            default:
                throw new UnsupportedOperationException("Unkown Uri:" + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mObjectivesDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);

        int tasksDeleted;
        switch (match) {

            case ALL_DATA:
                tasksDeleted = db.delete(TABLE_NAME, null, null);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (tasksDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return tasksDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
