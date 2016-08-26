package orgprivacy_friendly_apps.secuso.privacyfriendlybreakreminder;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.widget.Toast;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
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

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

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
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if(isXLargeTablet(this)) {
                finish();
                return true;
            }
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName);
    }


    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        private SeekBarPreference _seekBarWork;
        private SeekBarPreference _seekBarBreak;

        private DynamicListPreference dlp;
        private ExerciseListPreference elp;

        private String currentProfile = "";
        private Bundle bundle;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            bundle = savedInstanceState;
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            // Get widgets :
            _seekBarWork = (SeekBarPreference) this.findPreference("work_value");
            _seekBarBreak = (SeekBarPreference) this.findPreference("break_value");

            dlp = (DynamicListPreference) this.findPreference("current_profile");
            elp = (ExerciseListPreference) this.findPreference("exercise");

            // Set listener :
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

            //Get profile name
            currentProfile = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString("name_text", "");

            // Set seekbar summary :
            int radius = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getInt("work_value", 50);
            _seekBarWork.setSummary(this.getString(R.string.settings_summary).replace("$1", "" + radius));

            radius = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getInt("break_value", 10);
            _seekBarBreak.setSummary(this.getString(R.string.settings_summary).replace("$1", "" + radius));


            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("name_text"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }


        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            if (key.equals("current_profile")) {
                ListPreference listPref = (ListPreference) findPreference("current_profile");
                int i = Integer.parseInt(listPref.getValue());


                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("current_profile", "" + i);
                editor.putBoolean("change_profiles", true);
                String[] allProfile = sharedPreferences.getString("profiles", "").split(";");

                // Deactivate the onPrefListener in SettingsActivity
                getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
                editor.putString("name_text", allProfile[i].split(",")[0]);
                editor.putInt("work_value", Integer.parseInt(allProfile[i].split(",")[1]));
                editor.putInt("break_value", Integer.parseInt(allProfile[i].split(",")[2]));
                editor.putBoolean("cont_value", Boolean.parseBoolean(allProfile[i].split(",")[3]));
                editor.putString("exercise_value", allProfile[i].split(",")[4]);
                editor.apply();
                getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

                // FIXME Has to be done because the summary of the name
                onDestroy();
                onCreate(bundle);
                return;
            }


            // Set seekbar summary :
            int radius = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getInt("work_value", 50);
            _seekBarWork.setSummary(this.getString(R.string.settings_summary).replace("$1", "" + radius));
            radius = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getInt("break_value", 10);
            _seekBarBreak.setSummary(this.getString(R.string.settings_summary).replace("$1", "" + radius));

            //FIXME Update the preferences of the selected profile

            if (!key.equals("profiles")) {
                getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
                updateProfilesPreference();
                getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
            }

        }


        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

            System.out.println("All Profiles" + PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString("profiles", ""));

            super.onPause();
        }

        private void updateProfilesPreference() {
            int work_radius = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getInt("work_value", 50);
            int break_radius = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getInt("break_value", 10);
            String newProfileName = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString("name_text", "");
            String allProfiles = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString("profiles", "");
            boolean cont = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getBoolean("cont_value", false);
            String exercises = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString("exercise_value", "-1");

            if (allProfiles.contains(newProfileName + "," + work_radius + "," + break_radius + "," + cont + "," + exercises) && newProfileName.equals(currentProfile)) {
                //Nothing changes
                System.out.println("No changes for a profile in general settings");
            } else {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).edit();

                System.out.println("SETTINGS ACTIVITY: " + newProfileName);
                if (newProfileName.equals("")) {
                    // Profile name empty
                    System.out.println("EMPTY NAME IN SETTINGS ACTIVITY");
                    Toast.makeText(this.getActivity(), R.string.settings_emptyName, Toast.LENGTH_LONG).show();
                    editor.putString("name_text", currentProfile);
                    editor.apply();
                    findPreference("name_text").setSummary(currentProfile);

                } else if (currentProfile != newProfileName && prefContainsName(newProfileName)) {
                    // Profile name exists already
                    Toast.makeText(this.getActivity(), R.string.settings_doubleName, Toast.LENGTH_LONG).show();
                    editor.putString("name_text", currentProfile);
                    editor.apply();
                    findPreference("name_text").setSummary(currentProfile);
                } else {

                    String[] profiles = allProfiles.split(";");

                    for (int i = 0; i < profiles.length; i++) {
                        if (profiles[i].split(",")[0].equals(currentProfile)) {
                            profiles[i] = newProfileName + "," + work_radius + "," + break_radius + "," + cont + "," + exercises;
                            break;
                        }
                    }
                    StringBuilder builder = new StringBuilder();
                    for (String s : profiles) {
                        builder.append(s + ";");
                    }

                    System.out.println("All Profiles: " + builder.toString());
                    editor.putBoolean("change_profiles", true);
                    editor.putString("profiles", builder.toString());
                    editor.apply();

                    currentProfile = newProfileName;
                    findPreference("name_text").setSummary(currentProfile);
                }
            }


        }

        private boolean prefContainsName(String profileName) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
            String allProfiles = sharedPrefs.getString("profiles", "");
            String[] profiles = allProfiles.split(";");
            for (String profile : profiles) {
                if (profile.split(",")[0].equalsIgnoreCase(profileName)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public void onResume() {
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
            currentProfile = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString("name_text", "");
            super.onResume();
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}