package org.secuso.privacyfriendlybreakreminder.exercises;

import android.content.Context;
import android.support.annotation.StringRes;

import org.secuso.privacyfriendlybreakreminder.R;

import java.util.Arrays;
import java.util.List;

/**
 * All available sections.
 * @author Christopher Beckmann
 * @version 2.0
 * @since 06.09.2017
 * created 06.09.2017
 */
public enum ExerciseSections {
    Head(R.string.exercise_section_head),
    Neck(R.string.exercise_section_neck),
    Arms(R.string.exercise_section_arms),
    Torso(R.string.exercise_section_torso),
    Spinal(R.string.exercise_section_spinal),
    Pelvis(R.string.exercise_section_pelvis),
    Legs(R.string.exercise_section_legs);


    private final @StringRes int nameResId;

    ExerciseSections(@StringRes int resId) {
        this.nameResId = resId;
    }

    public String getLocalName(Context context) {
        return context.getString(nameResId);
    }
    public static List<ExerciseSections> getSectionList() { return Arrays.asList( Head, Neck, Arms, Torso, Spinal, Pelvis, Legs ); }
}
