package org.secuso.privacyfriendlybreakreminder.activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.util.SortedList;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import org.secuso.privacyfriendlybreakreminder.R;
import org.secuso.privacyfriendlybreakreminder.activities.adapter.ExerciseAdapter;
import org.secuso.privacyfriendlybreakreminder.activities.layout.FlowLayout;
import org.secuso.privacyfriendlybreakreminder.database.SQLiteHelper;
import org.secuso.privacyfriendlybreakreminder.database.data.Exercise;
import org.secuso.privacyfriendlybreakreminder.exercises.ExerciseLocale;
import org.secuso.privacyfriendlybreakreminder.exercises.ExerciseSections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.secuso.privacyfriendlybreakreminder.activities.adapter.ExerciseAdapter.ID_COMPARATOR;

public class ChooseExerciseActivity extends AppCompatActivity {

    private static final String TAG = ChooseExerciseActivity.class.getSimpleName();

    public static final String EXTRA_SELECTED_EXERCISES = TAG+".EXTRA_SELECTED_EXERCISES";

    FlowLayout filterButtonLayout;
    RecyclerView exerciseList;

    ExerciseAdapter exerciseAdapter;
    SQLiteHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_exercise);

        initResources();

        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        int[] chosenExercises = getIntent().getIntArrayExtra(EXTRA_SELECTED_EXERCISES);
        List<Integer> chosenExercisesList = new ArrayList<>();

        for(int i : chosenExercises) chosenExercisesList.add(i);

        exerciseAdapter.add(databaseHelper.getExerciseList(ExerciseLocale.getLocale()));
        exerciseAdapter.setCheckedItems(chosenExercisesList);
    }

    private void initResources() {
        databaseHelper = new SQLiteHelper(this);

        filterButtonLayout = (FlowLayout) findViewById(R.id.layout_filter_buttons);
        exerciseList = (RecyclerView) findViewById(R.id.exercise_list);
        exerciseAdapter = new ExerciseAdapter(this, ID_COMPARATOR);
        exerciseAdapter.showCheckboxes(true);

        GridLayoutManager gridLayout = new GridLayoutManager(this, 3);
        exerciseList.setLayoutManager(gridLayout);
        exerciseList.setAdapter(exerciseAdapter);

        filterButtonLayout.removeAllViews();

        for(ExerciseSections section : ExerciseSections.getSectionList()) {
            // TODO: Add Buttons for every section we have
            //View view = LayoutInflater.from(this).inflate(R.layout.layout_section_filter_button, null, false);
            //TextView image = (TextView) view.findViewById(R.id.button_text);
            //filterButtonLayout.addView(view);
        }
    }

    public void onClick(View v) {

        // TODO get onclicklistener to call this method so we can filter the list
        //exerciseAdapter.replaceAll(databaseHelper.getExerciseListBySections());
        exerciseList.scrollToPosition(0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        sendDataBack();
        super.onBackPressed();
    }

    private void sendDataBack() {
        Intent result = new Intent();

        List<Integer> selectedIdList = exerciseAdapter.getCheckedIds();
        int[] selectedIds = new int[selectedIdList.size()];

        for(int i = 0; i < selectedIds.length; ++i) {
            selectedIds[i] = selectedIdList.get(i);
        }

        result.putExtra(EXTRA_SELECTED_EXERCISES, selectedIds);
        setResult(RESULT_OK, result);
    }
}
