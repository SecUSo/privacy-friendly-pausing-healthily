package org.secuso.privacyfriendlypausinghealthily.database.data;

import android.content.Context;
import android.preference.PreferenceManager;

import org.secuso.privacyfriendlypausinghealthily.activities.tutorial.FirstLaunchManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Model for an exercise set. It holds {@link Exercise}s.
 * @author Christopher Beckmann
 * @version 2.0
 * @since 03.09.2017
 * created 03.09.2017
 */
public class ExerciseSet {
    private long id = -1L;
    private String name = null;
    private List<Exercise> exercises = new ArrayList<>();
    private boolean isDefaultSet = false;

    public ExerciseSet() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean add(Exercise exercise) {
        if(!exercises.contains(exercise)) {
            exercises.add(exercise);
            return true;
        }
        return false;
    }

    public boolean remove(Exercise exercise) {
        if(exercises.contains(exercise)) {
            exercises.remove(exercise);
            return true;
        }
        return false;
    }

    public int indexOf(Exercise e) { return exercises.indexOf(e); }

    public Exercise get(int index) {
        return exercises.get(index);
    }

    public int size() {
        return exercises.size();
    }

    public List<Exercise> getExercises() {
        return exercises;
    }

    public boolean isDefaultSet() {
        return isDefaultSet;
    }

    public void setDefaultSet(boolean defaultSet) {
        isDefaultSet = defaultSet;
    }

    public long getExerciseSetTime(Context context) {
        int result = 0;
        for(Exercise e : getExercises()) {
            result += e.getImageID().split(",").length;
        }
        long exerciseDuration = Long.parseLong(PreferenceManager.getDefaultSharedPreferences(context).getString(FirstLaunchManager.EXERCISE_DURATION, "30"));
        return (result * exerciseDuration);
    }
}
