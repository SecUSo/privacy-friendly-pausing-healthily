package org.secuso.privacyfriendlybreakreminder.database;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.secuso.privacyfriendlybreakreminder.database.data.Exercise;
import org.secuso.privacyfriendlybreakreminder.database.columns.ExerciseColumns;
import org.secuso.privacyfriendlybreakreminder.database.columns.ExercisesLocalColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {

    private SQLiteDatabase dataBase;
    private static final String DATABASE_NAME = "exercises.sqlite";
    private static final String DATABASE_PATH = "/data/data/org.secuso.privacyfriendlybreakreminder/databases/";
    private static final int DATABASE_VERSION = 3;

    private static final String[] deleteQueryList = {
            ExerciseColumns.SQL_DELETE_ENTRIES,
            ExercisesLocalColumns.SQL_DELETE_ENTRIES};

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        //Check if database exists
        File databaseFile = context.getDatabasePath(DATABASE_NAME);
        if (!databaseFile.exists()) {
            this.getReadableDatabase();
            try {
                copyDataBase(context);
                this.close();
            } catch (Exception e) {
                Log.v("db log", "Copying data didn´t work!!");
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        execSQLList(db, deleteQueryList);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private void execSQLList(SQLiteDatabase db, String[] queryList) {
        for (String query : queryList) {
            db.execSQL(query);
        }
    }


    public Cursor getExerciseCursor(String language) {
        SQLiteDatabase database = getReadableDatabase();

        return database.rawQuery(buildQuery(false), new String[]{language});
    }

    public List<Exercise> getExerciseList(String language) {
        SQLiteDatabase database = getReadableDatabase();

        Cursor c = database.rawQuery(buildQuery(false), new String[]{language});

        List<Exercise> result = new ArrayList<>();

        if(c != null) {

            while(!c.isAfterLast()) {
                result.add(ExerciseColumns.getExercise(c));
                c.moveToNext();
            }

            c.close();
        }

        return result;
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
    private String buildQuery(boolean addSectionCheck) {
        StringBuilder sqlQuery = new StringBuilder();

        sqlQuery.append("SELECT ");

        for(String field : ExerciseColumns.PROJECTION) {
            sqlQuery.append("E.").append(field).append(", ");
        }
        for(String field : ExercisesLocalColumns.PROJECTION) {
            sqlQuery.append("L.").append(field).append(", ");
        }
        // delete the last comma
        sqlQuery.setLength(sqlQuery.length()-2);

        sqlQuery.append(" FROM ");
        sqlQuery.append(ExerciseColumns.TABLE_NAME);
        sqlQuery.append(" E LEFT OUTER JOIN ");
        sqlQuery.append(ExercisesLocalColumns.TABLE_NAME);
        sqlQuery.append(" L");

        sqlQuery.append("ON E.");
        sqlQuery.append(ExerciseColumns._ID);
        sqlQuery.append(" = L.");
        sqlQuery.append(ExercisesLocalColumns.EXERCISE_ID);

        sqlQuery.append("WHERE ");
        sqlQuery.append("L.");
        sqlQuery.append(ExercisesLocalColumns.LANGUAGE);
        sqlQuery.append("= ? ");

        if(addSectionCheck) {
            sqlQuery.append("AND E.");
            sqlQuery.append(ExerciseColumns.SECTION);
            sqlQuery.append("LIKE ? ");
        }

        sqlQuery.append("ORDER BY E.");
        sqlQuery.append(ExerciseColumns._ID);
        sqlQuery.append(" ASC");

        return sqlQuery.toString();
    }


    public List<Exercise> getExercisesFromSection(String language, String section) {
        SQLiteDatabase database = getReadableDatabase();

        Cursor c = database.rawQuery(buildQuery(true), new String[]{language, "%"+section+"%"});

        List<Exercise> result = new ArrayList<>();

        if(c != null) {

            while(!c.isAfterLast()) {
                result.add(ExerciseColumns.getExercise(c));
                c.moveToNext();
            }

            c.close();
        }

        return result;
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
    }
}
