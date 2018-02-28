package com.example.android.livemap.Database;

import android.content.ContentValues;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.livemap.Database.ObjectivesContract.ObjectivesEntry.OBJECTIVES_DESCRIPTION;
import static com.example.android.livemap.Database.ObjectivesContract.ObjectivesEntry.OBJECTIVES_LAT;
import static com.example.android.livemap.Database.ObjectivesContract.ObjectivesEntry.OBJECTIVES_LNG;
import static com.example.android.livemap.Database.ObjectivesContract.ObjectivesEntry.OBJECTIVES_TITLE;

/**
 * Created by ErwinF on 2/27/2018.
 *
 * This class is a temporary test to input fake data in the Database.
 */

public class TestObjectivesData {

    private static final String TAG = "TestObjectivesData";

    public List<ContentValues> insertFakeData() {

        List<ContentValues> list = new ArrayList<ContentValues>();
        list.add(makeObjectivesContentValues
                ("Title", "Description: \n yeah boy!", -33.000, 20.000));
        list.add(makeObjectivesContentValues
                ("THE FLAG", "YOU BOYS GOT TO GET THAT FLAG, NO SURRENDER LADS", -30.852, 50.045));


        return list;
    }

    private static ContentValues makeObjectivesContentValues(String title, String description,
                                                      double lat, double lng) {
        ContentValues cv = new ContentValues();
        cv.put(OBJECTIVES_TITLE, title);
        cv.put(OBJECTIVES_DESCRIPTION, description);
        cv.put(OBJECTIVES_LAT, lat);
        cv.put(OBJECTIVES_LNG, lng);
        return cv;
    }
}
