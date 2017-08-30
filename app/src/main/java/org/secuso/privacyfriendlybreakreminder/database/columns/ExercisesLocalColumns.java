package org.secuso.privacyfriendlybreakreminder.database.columns;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import org.secuso.privacyfriendlybreakreminder.database.data.Exercise;

/**
 * Created by Christopher Beckmann on 25.08.2017.
 */

public class ExercisesLocalColumns implements BaseColumns {

    public static final String TABLE_NAME = "exercises_local";

    public static final String _ID = "local_id";
    public static final String LANGUAGE = "language";
    public static final String EXERCISE_ID = "exercise_id";
    public static final String DESCRIPTION = "description";
    public static final String EXECUTION = "execution";
    public static final String NAME = "name";

    public static final String[] PROJECTION = {
            _ID,
            LANGUAGE,
            EXERCISE_ID,
            DESCRIPTION,
            EXECUTION,
            NAME
    };
    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static Exercise getExercise(Cursor c) {
        Exercise e = new Exercise();

        e.setLocalId(c.getInt(c.getColumnIndexOrThrow(ExercisesLocalColumns._ID)));
        e.setLanguage(c.getString(c.getColumnIndexOrThrow(ExercisesLocalColumns.LANGUAGE)));
        e.setDescription(c.getString(c.getColumnIndexOrThrow(ExercisesLocalColumns.DESCRIPTION)));
        e.setExecution(c.getString(c.getColumnIndexOrThrow(ExercisesLocalColumns.EXECUTION)));
        e.setName(c.getString(c.getColumnIndexOrThrow(ExercisesLocalColumns.NAME)));

        return e;
    }

    public static ContentValues getValues(Exercise record) {
        ContentValues values = new ContentValues();

        if(record.getLocalId() != -1) {
            values.put(ExercisesLocalColumns._ID, record.getLocalId());
        }
        values.put(ExercisesLocalColumns.LANGUAGE, record.getLanguage());
        values.put(ExercisesLocalColumns.DESCRIPTION, record.getDescription());
        values.put(ExercisesLocalColumns.EXECUTION, record.getExecution());
        values.put(ExercisesLocalColumns.NAME, record.getName());

        return values;
    }
}
