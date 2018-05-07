package org.secuso.privacyfriendlybreakreminder.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.secuso.privacyfriendlybreakreminder.activities.adapter.ExerciseAdapter;
import org.secuso.privacyfriendlybreakreminder.activities.helper.IExerciseTimeUpdateable;
import org.secuso.privacyfriendlybreakreminder.activities.tutorial.FirstLaunchManager;
import org.secuso.privacyfriendlybreakreminder.database.data.Exercise;
import org.secuso.privacyfriendlybreakreminder.exercises.ExerciseLocale;
import org.secuso.privacyfriendlybreakreminder.R;
import org.secuso.privacyfriendlybreakreminder.database.SQLiteHelper;
import org.secuso.privacyfriendlybreakreminder.database.data.ExerciseSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.Duration;

import static org.secuso.privacyfriendlybreakreminder.activities.adapter.ExerciseAdapter.ID_COMPARATOR;

/**
 * This activity lets you edit the name of an exercise set. To edit what exercises are in that set, {@link ChooseExerciseActivity} is called.
 * @author Christopher Beckmann
 * @version 2.0
 * @see ChooseExerciseActivity
 */
public class EditExerciseSetActivity extends AppCompatActivity implements android.support.v4.app.LoaderManager.LoaderCallbacks<ExerciseSet> {


    private static final String TAG = EditExerciseSetActivity.class.getSimpleName();
    // extras
    public static final String EXTRA_EXERCISE_SET_ID = "EXTRA_EXERCISE_SET_ID";
    public static final String EXTRA_EXERCISE_SET_NAME = "EXTRA_EXERCISE_SET_NAME";

    private static final int PICK_EXERCISE_REQUEST = 1;  // The request code

    // UI
    private TextView exerciseSetNameText;
    private RecyclerView exerciseList;
    private ProgressBar loadingSpinner;

    private ExerciseAdapter mAdapter;
    private ActionBar actionBar;
    private Toolbar toolbar;

    // exercise set information
    private long exerciseSetId = -1L;
    private String exerciseSetName = "";
    private boolean nameChanged = false;
    private boolean modificationsDone = false;
    private SQLiteHelper mDbHelper;
    private TextView exerciseSetTimeText;

    //methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_exercise_set);

        Intent i = getIntent();
        exerciseSetId = i.getLongExtra(EXTRA_EXERCISE_SET_ID, -1L);
        exerciseSetName = i.getStringExtra(EXTRA_EXERCISE_SET_NAME);

        if(exerciseSetId < 0L || TextUtils.isEmpty(exerciseSetName)) {
            // no valid exercise
            super.onBackPressed();
        }

        initResources();

        getSupportLoaderManager().initLoader(0, null, this);

        updateExerciseTime();
    }

    private void updateExerciseTime() {
        List<Integer> ids = new LinkedList<>();
        for(Exercise e : mAdapter.getExercises()) {
            ids.add(e.getId());
        }
        exerciseSetTimeText.setText(getString(R.string.exercise_time, mAdapter.getExerciseTimeString(ids)));
    }

    private void initResources() {
        mDbHelper = new SQLiteHelper(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        exerciseSetNameText = (TextView) findViewById(R.id.exercise_set_name);
        exerciseList = (RecyclerView) findViewById(R.id.exercise_list);
        mAdapter = new ExerciseAdapter(this, ID_COMPARATOR, null);
        exerciseList.setAdapter(mAdapter);
        exerciseList.setLayoutManager(new GridLayoutManager(this, 3));
        loadingSpinner = (ProgressBar) findViewById(R.id.loading_spinner);
        exerciseSetTimeText = (TextView) findViewById(R.id.exercise_set_time);

        exerciseSetNameText.setText(exerciseSetName);
        exerciseSetNameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                nameChanged = true;
            }
        });

        setupActionBar();
    }

    private void setupActionBar() {
        if (getSupportActionBar() == null) {
            setSupportActionBar(toolbar);
        }

        actionBar = getSupportActionBar();

        if(actionBar != null) {
            actionBar.setTitle(R.string.activity_title_edit_exercise_set);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_white);
        }
    }

    @Override
    public Loader<ExerciseSet> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<ExerciseSet>(this) {
            @Override
            public ExerciseSet loadInBackground() {
                return mDbHelper.getExerciseListForSet((int)exerciseSetId, ExerciseLocale.getLocale());
            }

            @Override
            protected void onStartLoading() {
                loadingSpinner.setVisibility(View.VISIBLE);
                forceLoad();
            }

            @Override
            protected void onReset() {}
        };
    }

    @Override
    public void onLoadFinished(Loader<ExerciseSet> loader, ExerciseSet set) {
        loadingSpinner.animate().alpha(0.0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                loadingSpinner.setVisibility(View.GONE);
            }
        });

        if(set != null) {
            mAdapter.replaceAll(set.getExercises());
        }
        updateExerciseTime();

        // load data only once
        getSupportLoaderManager().destroyLoader(0);
    }

    @Override
    public void onLoaderReset(Loader<ExerciseSet> loader) {}

//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        getSupportLoaderManager().restartLoader(0, null, this);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(modificationsDone()) {
                    showDiscardDialog();
                } else {
                    super.onBackPressed();
                }
                return true;
            case R.id.save:
                if(TextUtils.getTrimmedLength(exerciseSetNameText.getText()) == 0) {
                    Toast.makeText(this, R.string.activity_edit_no_empty_name, Toast.LENGTH_SHORT).show();
                } else {
                    saveChanges();
                    super.onBackPressed();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveChanges() {
        List<Exercise> set = mAdapter.getExercises();

        if(modificationsDone) {
            mDbHelper.clearExercisesFromSet((int) exerciseSetId);

            for (Exercise e : set) {
                mDbHelper.addExerciseToExerciseSet((int) exerciseSetId, e.getId());
            }
        }
        if(nameChanged) {
            ExerciseSet exerciseSet = new ExerciseSet();
            exerciseSet.setId(exerciseSetId);
            exerciseSet.setName(exerciseSetNameText.getText().toString());
            mDbHelper.updateExerciseSet(exerciseSet);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_exercise_sets, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if(modificationsDone()) {
            showDiscardDialog();
        } else {
            super.onBackPressed();
        }
    }

    private boolean modificationsDone() {
        return nameChanged || modificationsDone;
    }

    private void showDiscardDialog() {
        new AlertDialog.Builder(this)
            .setPositiveButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            })
            .setNegativeButton(R.string.discard, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    EditExerciseSetActivity.super.onBackPressed();
                }
            })
            .setMessage(R.string.dialog_discard_confirmation)
            .create().show();
    }

    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.add_button:

                Intent i = new Intent(this, ChooseExerciseActivity.class);
                i.putExtra(ChooseExerciseActivity.EXTRA_SELECTED_EXERCISES , getSelectedExerciseIds());
                startActivityForResult(i, PICK_EXERCISE_REQUEST);
                break;
            default:
                Log.d(TAG, "Unknown Button clicked.");
        }
    }

    private int[] getSelectedExerciseIds() {
        List<Exercise> set = mAdapter.getExercises();

        int[] result = new int[set.size()];

        for(int i = 0; i < set.size(); ++i) {
            result[i] = set.get(i).getId();
        }
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_EXERCISE_REQUEST) {
            if(resultCode == RESULT_OK) {

                int[] result = data.getIntArrayExtra(ChooseExerciseActivity.EXTRA_SELECTED_EXERCISES);
                List<Exercise> oldList = mAdapter.getExercises();
                // did we make any changes?

                boolean needToUpdate = false;

                if(result.length != oldList.size()) {
                    modificationsDone = true;
                    needToUpdate = true;
                }

                if(!needToUpdate) {
                    for (int id : result) {

                        boolean found = false;

                        for (Exercise e : oldList) {
                            if (e.getId() == id) {
                                found = true;
                                break;
                            }
                        }

                        if (!found) {
                            modificationsDone = true;
                            needToUpdate = true;
                            break;
                        }
                    }
                }

                if(needToUpdate) {
                    List<Exercise> allExercises = mDbHelper.getExerciseList(ExerciseLocale.getLocale());
                    List<Exercise> newList = new ArrayList<>();

                    for (int id : result) {
                        for (Exercise e : allExercises) {
                            if (e.getId() == id) {
                                newList.add(e);
                                break;
                            }
                        }
                    }

                    mAdapter.replaceAll(newList);
                    updateExerciseTime();
                }
            }
        }
    }
}
