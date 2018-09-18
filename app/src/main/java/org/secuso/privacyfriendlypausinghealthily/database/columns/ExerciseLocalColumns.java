package org.secuso.privacyfriendlypausinghealthily.database.columns;

import android.content.ContentValues;
import android.database.Cursor;

import org.secuso.privacyfriendlypausinghealthily.database.data.Exercise;

/**
 * Localised strings of an exercise.
 * @author Christopher Beckmann
 * @version 2.0
 * @since 25.08.2017
 * created 25.08.2017
 */
public final class ExerciseLocalColumns {

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

    public static Exercise fromCursor(Cursor c) {
        Exercise e = new Exercise();

        e.setLocalId(c.getInt(c.getColumnIndexOrThrow(ExerciseLocalColumns._ID)));
        e.setLanguage(c.getString(c.getColumnIndexOrThrow(ExerciseLocalColumns.LANGUAGE)));
        e.setDescription(c.getString(c.getColumnIndexOrThrow(ExerciseLocalColumns.DESCRIPTION)));
        e.setExecution(c.getString(c.getColumnIndexOrThrow(ExerciseLocalColumns.EXECUTION)));
        e.setName(c.getString(c.getColumnIndexOrThrow(ExerciseLocalColumns.NAME)));

        return e;
    }

    public static ContentValues getValues(Exercise record) {
        ContentValues values = new ContentValues();

        if(record.getLocalId() != -1) {
            values.put(ExerciseLocalColumns._ID, record.getLocalId());
        }
        values.put(ExerciseLocalColumns.LANGUAGE, record.getLanguage());
        values.put(ExerciseLocalColumns.DESCRIPTION, record.getDescription());
        values.put(ExerciseLocalColumns.EXECUTION, record.getExecution());
        values.put(ExerciseLocalColumns.NAME, record.getName());

        return values;
    }

    private ExerciseLocalColumns() {}
}
