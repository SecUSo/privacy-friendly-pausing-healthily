package org.secuso.privacyfriendlybreakreminder.database.columns;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import org.secuso.privacyfriendlybreakreminder.database.data.Exercise;
import org.secuso.privacyfriendlybreakreminder.database.data.ExerciseSet;

/**
 * Created by Christopher Beckmann on 03.09.2017.
 */

public final class ExerciseSetColumns {

    public static final String TABLE_NAME = "exercise_set";

    public static final String _ID = "exercise_set_id";
    public static final String NAME = "exercise_set_name";

    public static final String[] PROJECTION = {
            _ID,
            NAME
    };

    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static ExerciseSet fromCursor(Cursor c) {
        ExerciseSet e = new ExerciseSet();

        e.setId(c.getInt(c.getColumnIndexOrThrow(ExerciseSetColumns._ID)));
        e.setName(c.getString(c.getColumnIndexOrThrow(ExerciseSetColumns.NAME)));

        return e;
    }

    public static ContentValues getValues(ExerciseSet record) {
        ContentValues values = new ContentValues();

        if(record.getId() != -1) {
            values.put(ExerciseSetColumns._ID, record.getId());
        }
        values.put(ExerciseSetColumns.NAME, record.getName());

        return values;
    }

    private ExerciseSetColumns() {}
}
