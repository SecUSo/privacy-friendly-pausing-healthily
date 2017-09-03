package org.secuso.privacyfriendlybreakreminder.database.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christopher Beckmann on 03.09.2017.
 */

public class ExerciseSet {
    private int id = -1;
    private String name = null;
    private List<Exercise> exercises = new ArrayList<>();

    public ExerciseSet() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void add(Exercise exercise) {
        if(!exercises.contains(exercise)) {
            exercises.add(exercise);
        }
    }

    public void remove(Exercise exercise) {
        if(exercises.contains(exercise)) {
            exercises.remove(exercise);
        }
    }

    public void get(int index) {
        exercises.get(index);
    }

    public int size() {
        return exercises.size();
    }

}
