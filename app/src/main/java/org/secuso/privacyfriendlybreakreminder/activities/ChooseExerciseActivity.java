package org.secuso.privacyfriendlybreakreminder.activities;

import android.content.Intent;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.secuso.privacyfriendlybreakreminder.R;
import org.secuso.privacyfriendlybreakreminder.activities.adapter.ExerciseAdapter;
import org.secuso.privacyfriendlybreakreminder.activities.helper.IExerciseTimeUpdateable;
import org.secuso.privacyfriendlybreakreminder.database.SQLiteHelper;
import org.secuso.privacyfriendlybreakreminder.exercises.ExerciseLocale;
import org.secuso.privacyfriendlybreakreminder.exercises.ExerciseSections;

import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static org.secuso.privacyfriendlybreakreminder.activities.adapter.ExerciseAdapter.ID_COMPARATOR;

/**
 * This activity lets you choose exercises. The result is then sent back to {@link EditExerciseSetActivity}
 * @author Christopher Beckmann
 * @version 2.0
 * @see EditExerciseSetActivity
 */
public class ChooseExerciseActivity extends AppCompatActivity implements IExerciseTimeUpdateable {

    private static final String TAG = ChooseExerciseActivity.class.getSimpleName();

    public static final String EXTRA_SELECTED_EXERCISES = TAG+".EXTRA_SELECTED_EXERCISES";

    ChipGroup filterButtonLayout;
    RecyclerView exerciseList;
    TextView exerciseSetTimeText;

    ExerciseAdapter exerciseAdapter;
    SQLiteHelper databaseHelper;

    List<Chip> buttons;
    boolean[] buttonStates;

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

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);

        update(0);
    }

    private void initResources() {
        databaseHelper = new SQLiteHelper(this);

        filterButtonLayout = (ChipGroup) findViewById(R.id.layout_filter_buttons);
        exerciseList = (RecyclerView) findViewById(R.id.exercise_list);
        exerciseAdapter = new ExerciseAdapter(this, ID_COMPARATOR, this);
        exerciseAdapter.showCheckboxes(true);

        GridLayoutManager gridLayout = new GridLayoutManager(this, 3);
        exerciseList.setLayoutManager(gridLayout);
        exerciseList.setAdapter(exerciseAdapter);

        final List<ExerciseSections> sections = ExerciseSections.getSectionList();
        buttonStates = new boolean[sections.size()];
        buttons = new ArrayList<Chip>(sections.size());

        for(int i = 0; i < sections.size(); ++i) {
            ExerciseSections section = sections.get(i);

            String sectionText = section.getLocalName(this);

            Chip button = new Chip(this);

            button.setCheckable(true);
            button.setClickable(true);
            button.setChecked(false);
            button.setElegantTextHeight(true);
            button.setChipText(sectionText);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<String> filterSections = new ArrayList<String>(sections.size());

                    for(int i = 0; i < buttons.size(); ++i) {
                        if(buttons.get(i).isChecked()) {
                            filterSections.add(sections.get(i).name());
                        }
                    }

                    exerciseAdapter.replaceAll(databaseHelper.getExerciseListBySections(ExerciseLocale.getLocale(), filterSections));
                    exerciseList.scrollToPosition(0);
                    update(0);
                }
            });

            buttons.add(button);
            filterButtonLayout.addView(button);
        }

        exerciseSetTimeText = (TextView) findViewById(R.id.exercise_set_time);
        update(0);
    }


    private void switchButton(View v) {
        for(int i = 0; i < buttons.size(); ++i) {
            if(v.equals(buttons.get(i))) {
                buttonStates[i] = !buttonStates[i];
                CardView b = (CardView) v;
                b.setBackgroundColor(buttonStates[i] ?
                ContextCompat.getColor(this, R.color.colorAccent) :
                ContextCompat.getColor(this, R.color.middlegrey));
            }
        }
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

    @Override
    public void update(int i) {
        exerciseSetTimeText.setText(getString(R.string.exercise_time, exerciseAdapter.getExerciseTimeString()));
    }
}
