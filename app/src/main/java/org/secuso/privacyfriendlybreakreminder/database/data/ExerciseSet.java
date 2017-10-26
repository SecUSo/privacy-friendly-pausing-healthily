package org.secuso.privacyfriendlybreakreminder.database.data;

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
}
