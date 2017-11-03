
package org.secuso.privacyfriendlybreakreminder.activities;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import org.secuso.privacyfriendlybreakreminder.R;
import org.secuso.privacyfriendlybreakreminder.activities.helper.AppCompatPreferenceActivity;
import org.secuso.privacyfriendlybreakreminder.activities.tutorial.PrefManager;
import org.secuso.privacyfriendlybreakreminder.receivers.TimerSchedulerReceiver;

import java.util.HashSet;
import java.util.List;

import static org.secuso.privacyfriendlybreakreminder.activities.tutorial.PrefManager.PREF_SCHEDULE_EXERCISE_ENABLED;

/**
 * @author Christopher Beckmann
 * @version 2.0
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    protected SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        overridePendingTransition(0, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return ExercisePreferenceFragment.class.getName().equals(fragmentName)
                || TimerSchedulePreferenceFragment.class.getName().equals(fragmentName);
    }


    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {

                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            } if(preference instanceof MultiSelectListPreference) {
                MultiSelectListPreference mslPreference = (MultiSelectListPreference) preference;

                if(stringValue.length() >= 2) {
                    stringValue = stringValue.substring(1, stringValue.length() - 1);
                }

                String[] setValues = stringValue.split(",");

                if(setValues.length == 7) {
                    mslPreference.setSummary(preference.getContext().getString(R.string.pref_schedule_exercise_days_allselectedsummary));
                    return true;
                }

                StringBuilder sb = new StringBuilder();
                for(int i = 0; i < mslPreference.getEntries().length; i++) {
                    String preferenceEntryString = mslPreference.getEntryValues()[i].toString();

                    for(String chosenValue : setValues) {
                        if (chosenValue.trim().equals(preferenceEntryString)) {
                            sb.append(mslPreference.getEntries()[i]);
                            sb.append(", ");
                            break;
                        }
                    }
                }

                if(sb.length() > 0) {
                    sb.setLength(sb.length() - 2);
                }

                if(sb.length() == 0) {
                    sb.append(preference.getContext().getString(R.string.pref_schedule_exercise_days_defaultsummary));
                }

                mslPreference.setSummary(sb.toString());

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        if(preference instanceof MultiSelectListPreference) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext())
                    .getStringSet(preference.getKey(), new HashSet<String>()));
        } else {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext())
                    .getString(preference.getKey(), ""));
        }
    }

    protected int getNavigationDrawerID() {
        return R.id.nav_settings;
    }

    public static class ExercisePreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_exercise);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("pref_exercise_time"));
        }
    }

    public static class TimerSchedulePreferenceFragment extends PreferenceFragment {

        private OnSharedPreferenceChangeListener listener = new OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                // if anything changed with this settings .. reset the alarm
                TimerSchedulerReceiver.scheduleNextAlarm(getActivity().getApplicationContext());
            }
        };

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_scheduler);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference("pref_schedule_exercise_days"));

            PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext())
                    .registerOnSharedPreferenceChangeListener(listener);
        }

        @Override
        public void onDetach() {
            PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext())
                    .unregisterOnSharedPreferenceChangeListener(listener);

            super.onDetach();
        }


    }
}