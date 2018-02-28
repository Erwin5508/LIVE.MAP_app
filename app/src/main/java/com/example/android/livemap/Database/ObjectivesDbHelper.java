package com.example.android.livemap.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.android.livemap.Database.ObjectivesContract.ObjectivesEntry.*;

/**
 * Created by ErwinF on 2/24/2018.
 */

public class ObjectivesDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "objective_save.db";

    private static final int DATABASE_VERSION = 1;

    public ObjectivesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_OBJECTIVES_TABLE =
                "CREATE TABLE " + OBJECTIVES_TABLE_NAME + " (" +
                        _ID                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        OBJECTIVES_TITLE       + " TEXT NOT NULL, " +
                        OBJECTIVES_DESCRIPTION + " TEXT NOT NULL, " +
                        OBJECTIVES_LAT         + " DECIMAL NOT NULL, " +
                        OBJECTIVES_LNG         + " DECIMAL NOT NULL " + "); ";

        db.execSQL(SQL_CREATE_OBJECTIVES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + OBJECTIVES_TABLE_NAME);
        onCreate(db);
    }
}
