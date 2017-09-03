package org.secuso.privacyfriendlybreakreminder.database.columns;

import android.content.ContentValues;
import android.database.Cursor;

import org.secuso.privacyfriendlybreakreminder.database.data.Exercise;

/**
 * Created by Christopher Beckmann on 23.08.2017.
 */

public final class ExerciseColumns {

    public static final String TABLE_NAME = "exercises";

    public static final String _ID = "exercise_id";
    public static final String SECTION = "section";
    public static final String IMAGE_ID = "image_id";

    public static final String[] PROJECTION = {
            _ID,
            SECTION,
            IMAGE_ID,
    };
    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static Exercise fromCursor(Cursor c) {
        Exercise e = ExerciseLocalColumns.fromCursor(c);

        e.setId(c.getInt(c.getColumnIndexOrThrow(ExerciseColumns._ID)));
        e.setSection(c.getString(c.getColumnIndexOrThrow(ExerciseColumns.SECTION)));
        e.setImageID(c.getString(c.getColumnIndexOrThrow(ExerciseColumns.IMAGE_ID)));

        return e;
    }

    public static ContentValues getValues(Exercise record) {
        ContentValues values = new ContentValues();
        if(record.getId() != -1) {
            values.put(ExerciseColumns._ID, record.getId());
        }
        values.put(ExerciseColumns.SECTION, record.getSection());
        values.put(ExerciseColumns.IMAGE_ID, record.getImageID());

        values.putAll(ExerciseLocalColumns.getValues(record));

        return values;
    }

    private ExerciseColumns() {}
}
