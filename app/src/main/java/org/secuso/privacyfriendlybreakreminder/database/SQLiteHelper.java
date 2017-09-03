package org.secuso.privacyfriendlybreakreminder.database;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.secuso.privacyfriendlybreakreminder.database.columns.ExerciseSetColumns;
import org.secuso.privacyfriendlybreakreminder.database.data.Exercise;
import org.secuso.privacyfriendlybreakreminder.database.columns.ExerciseColumns;
import org.secuso.privacyfriendlybreakreminder.database.columns.ExerciseLocalColumns;
import org.secuso.privacyfriendlybreakreminder.database.data.ExerciseSet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHelper.class.getSimpleName();

    private Context mContext;
    private static final String DATABASE_NAME = "exercises.sqlite";
    private static final String DATABASE_PATH = "/data/data/org.secuso.privacyfriendlybreakreminder/databases/";
    private static final int DATABASE_VERSION = 4;

    private static final String[] deleteQueryList = {
            ExerciseColumns.SQL_DELETE_ENTRIES,
            ExerciseLocalColumns.SQL_DELETE_ENTRIES,
            ExerciseSetColumns.SQL_DELETE_ENTRIES};

    private boolean onCreate;
    private boolean onUpgrade;

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        if (onCreate || onUpgrade) {
            onCreate = onUpgrade = false;
            copyDatabaseFromAssets(db);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        onCreate = true;
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade = true;
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    public synchronized Cursor getExerciseCursor(String language) {
        SQLiteDatabase database = getReadableDatabase();
        return database.rawQuery(buildQuery(false), new String[]{language});
    }

    public synchronized List<Exercise> getExerciseList(String language) {
        Cursor c = getExerciseCursor(language);
        return buildExerciseList(c);
    }

    public synchronized Cursor getExerciseCursorForSet(int setId, String language) {
        SQLiteDatabase database = getReadableDatabase();

        String sql = "SELECT *\n" +
                "FROM exercise_set ES LEFT OUTER JOIN exercise_set_exercises ESE\n" +
                "\tON ES.exercise_set_id = ESE.exercise_set_id\n" +
                "LEFT OUTER JOIN exercises E\n" +
                "\tON ESE.exercise_id = E.exercise_id\n" +
                "LEFT OUTER JOIN exercises_local L\n" +
                "\tON E.exercise_id = L.exercise_id\n" +
                "WHERE ES.exercise_set_id = ? AND L.language = ?\n" +
                "ORDER BY ESE.exercise_id ASC";

        String sql2 = "SELECT *\n" +
                "\tFROM (SELECT * \n" +
                "\t\t\tFROM (SELECT *\n" +
                "\t\t\t\tFROM "+ExerciseSetColumns.TABLE_NAME+" ES LEFT OUTER JOIN exercise_set_exercises ESE\n" +
                "\t\t\t\tON ES."+ExerciseSetColumns._ID+" = ESE."+ExerciseSetColumns._ID+"\n" +
                "\t\t\t\tWHERE ES."+ExerciseSetColumns._ID+" = ?\n" +
                "\t\t\t\tORDER BY ESE."+ExerciseColumns._ID+" ASC) ES_ESE \n" +
                "\t\t\tLEFT OUTER JOIN "+ExerciseColumns.TABLE_NAME+" E\n" +
                "\t\t\tON ES_ESE."+ExerciseColumns._ID+" = E."+ExerciseColumns._ID+") ES_ESE_E \n" +
                "\t\tLEFT OUTER JOIN "+ExerciseLocalColumns.TABLE_NAME+" L\n" +
                "\t\tON ES_ESE_E."+ExerciseColumns._ID+" = L."+ExerciseLocalColumns.EXERCISE_ID+"\n" +
                "\t\tWHERE L."+ExerciseLocalColumns.LANGUAGE+" = ?";

        return database.rawQuery(sql, new String[]{String.valueOf(setId), language});
    }

    public synchronized ExerciseSet getExerciseListForSet(int setId, String language) {
        Cursor c = getExerciseCursorForSet(setId, language);

        ExerciseSet result = null;

        if(c != null) {

            c.moveToFirst();

            result = ExerciseSetColumns.fromCursor(c);

            while(!c.isAfterLast()) {
                result.add(ExerciseColumns.fromCursor(c));
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

        return result;
    }


    private String buildQuery(boolean addSectionCheck) {
        return buildQuery(addSectionCheck, "");
    }

    public synchronized List<Exercise> getExercisesFromSection(String language, String section) { // TODO: Rename after old activities are deleted
        SQLiteDatabase database = getReadableDatabase();

        Cursor c = database.rawQuery(buildQuery(true), new String[]{language, "%"+section+"%"});

        return buildExerciseList(c);
    }

    /**
     * SELECT
     *  E._id,
     *  E.section,
     *  E.image_id,
     *  L.local_id,
     *  L.language,
     *  L.exercise_id,
     *  L.name,
     *  L.description,
     *  L.execution
     * FROM exercises E LEFT OUTER JOIN exercises_local L
     * ON E._id = L.exercise_id
     * WHERE L.language = "de" [AND E.section LIKE %?%]
     * ORDER BY E._id ASC
     *
     * @return the sql query without the ; at the end.
     */
    private String buildQuery(boolean addSectionCheck, String customWhereClause) {
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

        if(addSectionCheck) {
            sqlQuery.append("AND E.");
            sqlQuery.append(ExerciseColumns.SECTION);
            sqlQuery.append(" LIKE ? ");
        }

        sqlQuery.append("ORDER BY E.");
        sqlQuery.append(ExerciseColumns._ID);
        sqlQuery.append(" ASC");

        return sqlQuery.toString();
    }

    private void copyDataBase(Context context) throws IOException {
        InputStream myInput = context.getAssets().open(DATABASE_NAME);
        String outFileName = DATABASE_PATH + DATABASE_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        myOutput.flush();
        myOutput.close();
        myInput.close();

        SQLiteDatabase copiedDb = context.openOrCreateDatabase(DATABASE_NAME, 0, null);
        copiedDb.execSQL("PRAGMA user_version = " + DATABASE_VERSION);
        copiedDb.close();
    }

    /**
     * Copy packaged database from assets folder to the database created in the
     * application package context.
     *
     * @param db
     *            The target database in the application package context.
     */
    private void copyDatabaseFromAssets(SQLiteDatabase db) {
        Log.i(TAG, "copyDatabase");
        InputStream myInput = null;
        OutputStream myOutput = null;
        try {
            // Open db packaged as asset as the input stream
            myInput = mContext.getAssets().open(DATABASE_NAME);

            // Open the db in the application package context:
            myOutput = new FileOutputStream(db.getPath());

            // Transfer db file contents:
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
            myOutput.flush();

            // Set the version of the copied database to the current
            // version:
            SQLiteDatabase copiedDb = mContext.openOrCreateDatabase(DATABASE_NAME, 0, null);
            copiedDb.execSQL("PRAGMA user_version = " + DATABASE_VERSION);
            copiedDb.close();

        } catch (IOException e) {
            e.printStackTrace();
            throw new Error(TAG + " Error copying database");
        } finally {
            // Close the streams
            try {
                if (myOutput != null) {
                    myOutput.close();
                }
                if (myInput != null) {
                    myInput.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new Error(TAG + " Error closing streams");
            }
        }
    }
}
