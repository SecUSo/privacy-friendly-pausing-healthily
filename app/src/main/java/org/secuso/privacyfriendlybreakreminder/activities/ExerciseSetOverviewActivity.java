package org.secuso.privacyfriendlybreakreminder.activities;

import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import org.secuso.privacyfriendlybreakreminder.R;
import org.secuso.privacyfriendlybreakreminder.activities.adapter.ExerciseAdapter;
import org.secuso.privacyfriendlybreakreminder.database.SQLiteHelper;

public class ExerciseSetOverviewActivity extends AppCompatActivity implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>{

    private TextView exerciseSetName;
    private RecyclerView exerciseList;

    private ExerciseAdapter exerciseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_set);

        initResources();
    }

    private void initResources() {
        exerciseSetName = (TextView) findViewById(R.id.exercise_set_name);
        exerciseList = (RecyclerView) findViewById(R.id.exercise_list);
        exerciseAdapter = new ExerciseAdapter(this, null);
        exerciseList.setAdapter(exerciseAdapter);
        exerciseList.setLayoutManager(new GridLayoutManager(this, 2));

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {
            @Override
            public Cursor loadInBackground() {
                SQLiteHelper helper = new SQLiteHelper(getContext());
                return helper.getExerciseCursorForSet(2, "de"); // TODO; get correct subset list
            }

            @Override
            protected void onStartLoading() {
                forceLoad();
            }

            @Override
            protected void onReset() {}
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursor.moveToFirst();
        exerciseAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((ExerciseAdapter) exerciseList.getAdapter()).changeCursor(null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getSupportLoaderManager().restartLoader(0, null, this);
    }
}
