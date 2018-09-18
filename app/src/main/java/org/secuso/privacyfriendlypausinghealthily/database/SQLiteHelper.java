package org.secuso.privacyfriendlypausinghealthily.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import org.secuso.privacyfriendlypausinghealthily.database.columns.ExerciseSetColumns;
import org.secuso.privacyfriendlypausinghealthily.database.data.Exercise;
import org.secuso.privacyfriendlypausinghealthily.database.columns.ExerciseColumns;
import org.secuso.privacyfriendlypausinghealthily.database.columns.ExerciseLocalColumns;
import org.secuso.privacyfriendlypausinghealthily.database.data.ExerciseSet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Christopher Beckmann
 * @version 2.0
 */
public class SQLiteHelper extends SQLiteAssetHelper {

    private static final String TAG = SQLiteHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "exercises.sqlite";
    private static final int DATABASE_VERSION = 1;

    private static final String[] deleteQueryList = {
            ExerciseColumns.SQL_DELETE_ENTRIES,
            ExerciseLocalColumns.SQL_DELETE_ENTRIES,
            ExerciseSetColumns.SQL_DELETE_ENTRIES};

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        super.setForcedUpgrade();
    }

    public synchronized void deleteExerciseSet(long id) {
        SQLiteDatabase database = getReadableDatabase();
        database.delete(ExerciseSetColumns.TABLE_NAME, ExerciseSetColumns._ID + " = ?", new String[]{String.valueOf(id)});
        database.close();
    }

    public void updateExerciseSet(ExerciseSet exerciseSet) {
        SQLiteDatabase database = getReadableDatabase();
        database.update(ExerciseSetColumns.TABLE_NAME, ExerciseSetColumns.getValues(exerciseSet), ExerciseSetColumns._ID + " = ?", new String[]{String.valueOf(exerciseSet.getId())});
        database.close();
    }

    public synchronized long addExerciseSet(String name) {
        SQLiteDatabase database = getReadableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(ExerciseSetColumns.NAME, name);

        long id = database.insert(ExerciseSetColumns.TABLE_NAME, null, cv);
        database.close();

        return id;
    }


    public void clearExercisesFromSet(int exerciseSetId) {
        SQLiteDatabase database = getReadableDatabase();
        database.delete("exercise_set_exercises", ExerciseSetColumns._ID + " = ?", new String[]{String.valueOf(exerciseSetId)});
        database.close();
    }

    public synchronized void addExerciseToExerciseSet(int exerciseSetId, int exerciseId) {
        SQLiteDatabase database = getReadableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(ExerciseSetColumns._ID, exerciseSetId);
        cv.put(ExerciseColumns._ID, exerciseId);

        database.insert("exercise_set_exercises", null, cv);
        database.close();
    }

    public synchronized Cursor getExerciseSetsCursor() {
        SQLiteDatabase database = getReadableDatabase();

        return database.query(
                ExerciseSetColumns.TABLE_NAME,
                ExerciseSetColumns.PROJECTION,
                null,
                null,
                null,
                null,
                ExerciseSetColumns._ID + " DESC");
    }

    public synchronized List<ExerciseSet> getExerciseSetsWithExercises(String language) {

        List<ExerciseSet> result = new LinkedList<>();

        Cursor c = getExerciseSetsCursor();

        if(c != null) {
            c.moveToFirst();

            while(!c.isAfterLast()) {

                int id = c.getInt(c.getColumnIndex(ExerciseSetColumns._ID));

                ExerciseSet set = getExerciseListForSet(id, language);

                if(set != null) {
                    result.add(set);

                } else {
                    ExerciseSet e = new ExerciseSet();
                    e.setId(id);
                    e.setName(c.getString(c.getColumnIndexOrThrow(ExerciseSetColumns.NAME)));
                    result.add(e);
                }
                c.moveToNext();
            }
            c.close();
        }
        close();

        return result;
    }


    public synchronized Cursor getExerciseCursor(String language) {
        SQLiteDatabase database = getReadableDatabase();
        return database.rawQuery(buildQuery(0), new String[]{language});
    }

    public synchronized List<Exercise> getExerciseList(String language) {
        Cursor c = getExerciseCursor(language);
        return buildExerciseList(c);
    }

    public synchronized Cursor getExerciseCursorForSet(int setId, String language) {
        SQLiteDatabase database = getReadableDatabase();

        String sql = "SELECT *\n" +
                "FROM "+ExerciseSetColumns.TABLE_NAME+" ES LEFT OUTER JOIN exercise_set_exercises ESE\n" +
                "\tON ES."+ExerciseSetColumns._ID+" = ESE."+ExerciseSetColumns._ID+"\n" +
                "LEFT OUTER JOIN "+ExerciseColumns.TABLE_NAME+" E\n" +
                "\tON ESE."+ExerciseColumns._ID+" = E."+ExerciseColumns._ID+"\n" +
                "LEFT OUTER JOIN "+ExerciseLocalColumns.TABLE_NAME+" L\n" +
                "\tON E."+ExerciseColumns._ID+" = L."+ExerciseLocalColumns.EXERCISE_ID+"\n" +
                "WHERE ES."+ExerciseSetColumns._ID+" = ? AND L."+ExerciseLocalColumns.LANGUAGE+" = ?\n" +
                "ORDER BY ESE."+ExerciseColumns._ID+" ASC";

        return database.rawQuery(sql, new String[]{String.valueOf(setId), language});
    }

    public synchronized ExerciseSet getExerciseListForSet(int setId, String language) {
        Cursor c = getExerciseCursorForSet(setId, language);

        ExerciseSet result = null;

        if(c != null) {

            c.moveToFirst();

            if(!c.isAfterLast()) {
                result = ExerciseSetColumns.fromCursor(c);
            }

            while(!c.isAfterLast()) {
                result.add(ExerciseColumns.fromCursor(c));
                c.moveToNext();
            }

            c.close();
        }

        close();

        return result;
    }

    public synchronized List<ExerciseSet> getExerciseSets(boolean hideDefaults) {
        Cursor c = getExerciseSetsCursor();

        List<ExerciseSet> result = new ArrayList<>();

        if(c != null) {

            c.moveToFirst();

            while(!c.isAfterLast()) {
                ExerciseSet set = ExerciseSetColumns.fromCursor(c);
                if(!hideDefaults || !set.isDefaultSet()) {
                    result.add(set);
                }
                c.moveToNext();
            }

            c.close();
        }

        return result;
    }

    private List<Exercise> buildExerciseList(Cursor c) {
        List<Exercise> result = new ArrayList<>();

        if(c != null) {

            c.moveToFirst();

            while(!c.isAfterLast()) {
                result.add(ExerciseColumns.fromCursor(c));
                c.moveToNext();
            }

            c.close();
        }

        close();

        return result;
    }

    public synchronized List<Exercise> getExerciseListBySections(String language, @NonNull List<String> sections) {
        SQLiteDatabase database = getReadableDatabase();

        String[] argValues = new String[sections.size() + 1];

        argValues[0] = language;

        for(int i = 1; i < argValues.length; ++i) {
            argValues[i] = "%"+sections.get(i - 1)+"%";
        }

        Cursor c = database.rawQuery(buildQuery(sections.size()), argValues);

        return buildExerciseList(c);
    }

    /**
     * SELECT *
     * FROM exercises E LEFT OUTER JOIN exercises_local L
     * ON E._id = L.exercise_id
     * WHERE L.language = "de" [AND E.section LIKE ?]
     * ORDER BY E._id ASC
     *
     * @return the sql query without the ; at the end.
     */
    private String buildQuery(int sectionCheck) {
        StringBuilder sqlQuery = new StringBuilder();

        sqlQuery.append("SELECT ");

        for(String field : ExerciseColumns.PROJECTION) {
            sqlQuery.append("E.").append(field).append(", ");
        }
        for(String field : ExerciseLocalColumns.PROJECTION) {
            sqlQuery.append("L.").append(field).append(", ");
        }
        // delete the last comma
        sqlQuery.setLength(sqlQuery.length()-2);

        sqlQuery.append(" FROM ");
        sqlQuery.append(ExerciseColumns.TABLE_NAME);
        sqlQuery.append(" E LEFT OUTER JOIN ");
        sqlQuery.append(ExerciseLocalColumns.TABLE_NAME);
        sqlQuery.append(" L ");

        sqlQuery.append("ON E.");
        sqlQuery.append(ExerciseColumns._ID);
        sqlQuery.append(" = L.");
        sqlQuery.append(ExerciseLocalColumns.EXERCISE_ID);
        sqlQuery.append(" ");

        sqlQuery.append("WHERE ");
        sqlQuery.append("L.");
        sqlQuery.append(ExerciseLocalColumns.LANGUAGE);
        sqlQuery.append(" = ? ");

        if(sectionCheck > 0) {
            sqlQuery.append("AND ( ");
        }

        for(int i = 0; i < sectionCheck; ++i) {
            sqlQuery.append("E.");
            sqlQuery.append(ExerciseColumns.SECTION);
            sqlQuery.append(" LIKE ? ");

            if(i + 1 == sectionCheck) {
                sqlQuery.append(") ");
            } else {
                sqlQuery.append("OR ");
            }
        }


        sqlQuery.append("ORDER BY E.");
        sqlQuery.append(ExerciseColumns._ID);
        sqlQuery.append(" ASC");

        return sqlQuery.toString();
    }

    public long addDefaultExerciseSet(String name) {
        SQLiteDatabase database = getReadableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(ExerciseSetColumns.NAME, name);
        cv.put(ExerciseSetColumns.DEFAULT, 1);

        long id = database.insert(ExerciseSetColumns.TABLE_NAME, null, cv);
        database.close();

        return id;
    }
}
