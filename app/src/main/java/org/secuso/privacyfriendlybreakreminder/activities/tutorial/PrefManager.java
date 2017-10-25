package org.secuso.privacyfriendlybreakreminder.activities.tutorial;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Class structure taken from tutorial at http://www.androidhive.info/2016/05/android-build-intro-slider-app/
 */
public class PrefManager {
    private static final String TAG = PrefManager.class.getSimpleName();

    public static final String PREF_PICKER_SECONDS          = TAG + ".PREF_PICKER_SECONDS";
    public static final String PREF_PICKER_MINUTES          = TAG + ".PREF_PICKER_MINUTES";
    public static final String PREF_PICKER_HOURS            = TAG + ".PREF_PICKER_HOURS";
    public static final String PREF_BREAK_PICKER_SECONDS    = TAG + ".PREF_BREAK_PICKER_SECONDS";
    public static final String PREF_BREAK_PICKER_MINUTES    = TAG + ".PREF_BREAK_PICKER_MINUTES";

    public static final String DEFAULT_EXERCISE_SET         = "DEFAULT_EXERCISE_SET";
    public static final String PAUSE_TIME                   = "PAUSE TIME";
    public static final String REPEAT_STATUS                = "REPEAT_STATUS";
    public static final String CONTINUOUS_STATUS            = "CONTINUOUS_STATUS";
    public static final String EXERCISE_DURATION            = "pref_exercise_time";
    public static final String KEEP_SCREEN_ON_DURING_EXERCISE = "pref_keep_screen_on_during_exercise";


    private SharedPreferences pref;

    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    public PrefManager(Context context) {
        pref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        pref.edit().putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime).apply();
    }

    public boolean isFirstTimeLaunch() {
        boolean isFirstTimeLaunch = pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);

        if(isFirstTimeLaunch)
            pref.edit()
            .putLong(DEFAULT_EXERCISE_SET, 0L)
            .putLong(PAUSE_TIME, 5 * 60 * 1000)
            .putBoolean(REPEAT_STATUS, false)
            .putBoolean(CONTINUOUS_STATUS, false)
            .putInt(PREF_BREAK_PICKER_SECONDS, 0)
            .putInt(PREF_BREAK_PICKER_MINUTES, 5)
            .putInt(PREF_PICKER_SECONDS, 0)
            .putInt(PREF_PICKER_MINUTES, 0)
            .putInt(PREF_PICKER_HOURS, 1)
            .putString(EXERCISE_DURATION, "30")
            .putBoolean(KEEP_SCREEN_ON_DURING_EXERCISE, true)
            .apply();

        return isFirstTimeLaunch;
    }

}
