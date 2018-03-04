package com.example.android.livemap.Database;

import android.content.ContentValues;

import com.google.firebase.database.DatabaseReference;
import static com.example.android.livemap.Database.ObjectivesContract.ObjectivesEntry.*;

/**
 * Created by ErwinF on 3/3/2018.
 */

public class DatabaseIntermediate {

    public void uploadObjective(ContentValues contentValues, String userName, DatabaseReference reference) {
        String title = contentValues.get(OBJECTIVES_TITLE).toString() + " ~" + userName;
        String description = contentValues.get(OBJECTIVES_DESCRIPTION).toString();
        double lat = (double) contentValues.get(OBJECTIVES_LAT);
        double lng = (double) contentValues.get(OBJECTIVES_LNG);
        Objectives objective = new Objectives(title, description, lat, lng);
        reference.push().setValue(objective);
    }

    public void uploadUserData(Users user, DatabaseReference reference) {
        reference.push().setValue(user);
    }

    public ContentValues transferBackObjectivesToCv( Objectives objectives ) {
        ContentValues cv = new ContentValues();
        cv.put(OBJECTIVES_TITLE, objectives.getTitle());
        cv.put(OBJECTIVES_DESCRIPTION, objectives.getDescription());
        cv.put(OBJECTIVES_LAT, objectives.getLatitude());
        cv.put(OBJECTIVES_LNG, objectives.getLongitude());
        return cv;
    }
}
