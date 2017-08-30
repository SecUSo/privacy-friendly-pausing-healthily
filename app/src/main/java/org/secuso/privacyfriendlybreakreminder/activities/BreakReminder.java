package org.secuso.privacyfriendlybreakreminder.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.secuso.privacyfriendlybreakreminder.R;
import org.secuso.privacyfriendlybreakreminder.widget.*;

import java.util.Locale;


public class BreakReminder extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private boolean isRunning = false;
    private TextView ct_text;
    private CountDownTimer ct;
    private String stopTime = "";
    private int oldTime = 0;
    private Spinner profileSpinner;
    private SharedPreferences sharedPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        setContentView(R.layout.activity_break_reminder);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String allProfiles = sharedPrefs.getString("profiles", "");
        if (allProfiles.equals("")) {
            allProfiles = this.getResources().getText(R.string.standard_profile).toString();
            String exercises = this.getResources().getText(R.string.all_exercises).toString();
            editor.putString("exercise_value", exercises);
            editor.putString("profiles", allProfiles);
            editor.putString("current_language", Locale.getDefault().getLanguage());
            editor.putBoolean("notifications_stayOn", true);
            editor.apply();

            WelcomeDialog welcomeDialog = new WelcomeDialog();
            welcomeDialog.show(getFragmentManager(), "WelcomeDialog");
        } else if (!sharedPrefs.getString("current_language", "en").equals(Locale.getDefault().getLanguage())) {
            //FIXME could be nice to translate the body parts hear... but it would be a complex function
            editor.putString("current_language", Locale.getDefault().getLanguage());
            String[] profiles = allProfiles.split(";");
            allProfiles = "";

            for (int j = 0; j < profiles.length; j++) {
                String[] profile = profiles[j].split(",");
                profile[4] = "-1";
                profiles[j] = "";
                for (int i = 0; i < profile.length; i++) {
                    profiles[j] += profile[i] + ",";
                }
                allProfiles += profiles[j] + ";";
            }

            editor.putString("profiles", allProfiles);
            editor.apply();
        }

        // If chosen, set screen to "stay on"
        boolean stayOn = sharedPrefs.getBoolean("notifications_stayOn", false);

        if (stayOn)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        int mins = sharedPrefs.getInt("work_value", 5) + 1;
        String bufferZeroMinute = "";

        if (mins < 10)
            bufferZeroMinute = "0";

        ct_text = (TextView)
                findViewById(R.id.textView);

        ct_text.setText(bufferZeroMinute + mins + ":00");

        Button playStopButton = (Button) findViewById(R.id.button_playStop);
        playStopButton.setOnClickListener(this);
        Button resetButton = (Button) findViewById(R.id.button_reset);
        resetButton.setOnClickListener(this);
        ct_text.setOnClickListener(this);

        profileSpinner = (Spinner) findViewById(R.id.spinner);

        String[] profileNames = new String[allProfiles.split(";").length + 1];
        String[] fillProfileNames = allProfiles.split(";");
        for (int i = 0; i < profileNames.length - 1; i++) {
            profileNames[i] = fillProfileNames[i].split(",")[0];
        }
        profileNames[profileNames.length - 1] = this.getResources().getText(R.string.new_profile).toString();
        ArrayAdapter<String> adapter = new
                ArrayAdapter<String>(this, R.layout.spinner_layout, profileNames);
        profileSpinner.setAdapter(adapter);

        //Set the ClickListener for Spinner
        profileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String profileSelected = (String) parent.getItemAtPosition(position);
                if (profileSelected.equals(getResources().getText(R.string.new_profile).toString())) {
                    createNewProfile();
                } else {
                    updatePreference(profileSelected);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    private void updatePreference(String profileSelected) {

        String allProfiles = sharedPrefs.getString("profiles", "");

        String currentProfile = sharedPrefs.getString("name_text", "") + "," + sharedPrefs.getInt("work_value", -1) + "," + sharedPrefs.getInt("break_value", -1) + "," + sharedPrefs.getBoolean("cont_value", false) + "," + sharedPrefs.getString("exercise_value", "-1");

        if (!(allProfiles.contains(currentProfile) && profileSelected.equals(sharedPrefs.getString("name_text", "")))) {
            if (ct != null) {
                ct.cancel();
                isRunning = false;
            }

            String[] profileNames = allProfiles.split(";");
            for (int i = 0; i < profileNames.length; i++) {
                String profileName = profileNames[i].split(",")[0];
                int interval = Integer.parseInt(profileNames[i].split(",")[1]);
                int break_time = Integer.parseInt(profileNames[i].split(",")[2]);
                boolean cont = Boolean.parseBoolean(profileNames[i].split(",")[3]);
                String exercises = profileNames[i].split(",")[4];
                if (profileName.equals(profileSelected)) {
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString("current_profile", "" + i);
                    editor.putString("name_text", profileName);
                    editor.putInt("work_value", interval - 1);
                    editor.putInt("break_value", break_time - 1);
                    editor.putBoolean("cont_value", cont);
                    editor.putString("exercise_value", exercises);
                    editor.apply();

                    //Update tutorial_clock
                    String bufferZeroMinute = "";
                    int time = interval * 60 * 1000;
                    if (time / 1000 / 60 < 10)
                        bufferZeroMinute = "0";

                    ct_text.setText(bufferZeroMinute + time / 1000 / 60 + ":00");

                    updateWidgets(bufferZeroMinute + time / 1000 / 60 + ":00");
                    break;
                }
            }
        }
    }

    private void fillProfiles() {

        if (ct != null) {
            ct.cancel();
            isRunning = false;
        }

        String allProfiles = sharedPrefs.getString("profiles", "");

        String[] profileNames = new String[allProfiles.split(";").length + 1];
        String[] fillProfileNames = allProfiles.split(";");
        for (int i = 0; i < profileNames.length - 1; i++) {
            profileNames[i] = fillProfileNames[i].split(",")[0];
        }
        profileNames[profileNames.length - 1] = getResources().getText(R.string.new_profile).toString();
        ArrayAdapter<String> adapter = new
                ArrayAdapter<String>(this, R.layout.spinner_layout, profileNames);
        profileSpinner.setAdapter(adapter);

        //Set Spinner on the current Profile
        //String currentProfile = sharedPrefs.getString("name_text", "Sport");
        int interval = sharedPrefs.getInt("work_value", 1);
        //profileSpinner.setSelection(Arrays.asList(profileNames).indexOf(currentProfile));
        profileSpinner.setSelection(Integer.parseInt(sharedPrefs.getString("current_profile", "-1")));

        //Update tutorial_clock
        String bufferZeroMinute = "";
        int time = interval * 60 * 1000;
        if (time / 1000 / 60 < 10)
            bufferZeroMinute = "0";

        ct_text.setText(bufferZeroMinute + time / 1000 / 60 + ":00");

        //Set the ClickListener for Spinner
        profileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String profileSelected = (String) parent.getItemAtPosition(position);
                if (profileSelected.equals(getResources().getText(R.string.new_profile).toString())) {
                    createNewProfile();
                } else {
                    updatePreference(profileSelected);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NotificationManager nManager = ((NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE));
        nManager.cancelAll();
    }


    @Override
    public void onResume() {
        super.onResume();

        // If chosen, set screen to "stay on"
        boolean stayOn = sharedPrefs.getBoolean("notifications_stayOn", false);

        if (stayOn)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //FIXME Add flag if New Profile or Resume
        if (sharedPrefs.getBoolean("change_profiles", false)) {
            fillProfiles();

            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putBoolean("change_profiles", false);
            editor.apply();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            // Handle the profile action
            Intent intent = new Intent(this, SettingsActivity.class);
            this.startActivity(intent);
            //} else if (id == R.id.nav_statistics) {
            // Show statistics
            //Intent intent = new Intent(this, StatisticsActivity.class);
            //this.startActivity(intent);
        } else if (id == R.id.nav_help) {
            // Show help
            Intent intent = new Intent(this, HelpActivity.class);
            this.startActivity(intent);
        } else if (id == R.id.nav_about) {
            // Show about page
            Intent intent = new Intent(this, AboutActivity.class);
            this.startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onClick(View v) {
        int mins = sharedPrefs.getInt("work_value", 50);
        String bufferZeroMinute = "";
        String bufferZeroSecond = "";
        int time = mins * 60 * 1000;

        stopTime = (String) ct_text.getText();
        oldTime = time;

        if (stopTime.equals("") && !isRunning) {
            if (time / 1000 / 60 < 10)
                bufferZeroMinute = "0";

            ct_text.setText(bufferZeroMinute + time / 1000 / 60 + ":00");
        } else if (!isRunning) {
            ct_text.setText(stopTime);
            String stringTime = (String) ct_text.getText();
            String[] timef = stringTime.split(":");
            int minute = Integer.parseInt(timef[0]);
            int second = Integer.parseInt(timef[1]);
            time = (1000 * (minute * 60)) + (1000 * second);

            if (minute < 10)
                bufferZeroMinute = "0";
            if (second < 10)
                bufferZeroSecond = "0";

            ct_text.setText(bufferZeroMinute + minute + ":" + bufferZeroSecond + second);
        }


        switch (v.getId()) {

            case R.id.textView:
            case R.id.button_playStop:
                if (isRunning) {
                    ct.cancel();
                    stopTime = (String) ct_text.getText();
                    isRunning = false;
                } else {
                    startTimer(time);
                }
                break;


            case R.id.button_reset:
                if (ct != null) {
                    //Reset tutorial_clock
                    ct.cancel();
                    int interval = sharedPrefs.getInt("work_value", 1) + 1;

                    bufferZeroMinute = "";
                    time = interval * 60 * 1000;
                    if (time / 1000 / 60 < 10)
                        bufferZeroMinute = "0";

                    ct_text.setText(bufferZeroMinute + time / 1000 / 60 + ":00");
                    stopTime = (String) ct_text.getText();
                    isRunning = false;

                    updateWidgets(stopTime);
                    break;
                }


                if (oldTime / 1000 / 60 < 10)
                    bufferZeroMinute = "0";

                ct_text.setText(bufferZeroMinute + oldTime / 1000 / 60 + ":00");
                stopTime = oldTime / 1000 / 60 + ":00";
                isRunning = false;
                break;
        }
    }


    private void startTimer(int time) {
        ct = new CountDownTimer(time, 1000) {
            boolean timeLeft = false;

            public void onTick(long millisUntilFinished) {
                String bufferZeroMinute = "";
                String bufferZeroSecond = "";

                if ((millisUntilFinished / 1000) / 60 < 10)
                    bufferZeroMinute = "0";

                if (millisUntilFinished / 1000 % 60 < 10)
                    bufferZeroSecond = "0";

                ct_text.setText(bufferZeroMinute + (millisUntilFinished / 1000) / 60 + ":" + bufferZeroSecond + millisUntilFinished / 1000 % 60);

                updateWidgets(bufferZeroMinute + (millisUntilFinished / 1000) / 60 + ":" + bufferZeroSecond + millisUntilFinished / 1000 % 60);

                //Show how much time is left

                timeLeft = sharedPrefs.getBoolean("notifications_new_message_timeLeft", false);
                if (timeLeft) {
                    Notification notification = new NotificationCompat.Builder(getApplicationContext()).setCategory(Notification.CATEGORY_MESSAGE)
                            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                            .setContentTitle("Break Reminder: ")
                            .setContentText("Take a break in " + ((millisUntilFinished / 1000) / 60) + " minutes and " + ((millisUntilFinished / 1000) % 60) + " seconds")
                            .setAutoCancel(true)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).build();
                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(1000, notification);
                }
            }

            public void onFinish() {
                isRunning = false;
                ct_text.setText("00:00");

                updateWidgets("00:00");

                //trigger the alarm
                String ringPref = sharedPrefs.getString("notifications_new_message_ringtone", "");

                if (!ringPref.equals("")) {
                    // Get the current ringtone
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), Uri.parse(ringPref));

                    // Play ringtone
                    r.play();
                }

                boolean vibrateChecked = sharedPrefs.getBoolean("notifications_new_message_vibrate", false);
                if (vibrateChecked) {
                    // Get instance of Vibrator from current Context
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                    if (v != null) {
                        // Vibrate for 1500 milliseconds
                        v.vibrate(1500);
                    }
                }

                //Cancel the notification
                if (timeLeft) {
                    Notification notification = new NotificationCompat.Builder(getApplicationContext()).setCategory(Notification.CATEGORY_MESSAGE)
                            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                            .setContentTitle("Break Reminder: ")
                            .setContentText("Take your break now!!")
                            .setAutoCancel(true)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).build();
                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(1000, notification);
                }
                startBreak();

                String workTime = "" + sharedPrefs.getInt("work_value", 0) + 1;
                if (workTime.length() == 1)
                    workTime = "0" + workTime;

                ct_text.setText(workTime + ":00");
                updateWidgets(workTime + ":00");
            }
        }.start();
        isRunning = true;
    }

    private void createNewProfile() {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean("change_profiles", true);
        editor.apply();
        Intent intent = new Intent(this, ProfileActivity.class);
        this.startActivity(intent);
    }

    private void updateWidgets(String time) {
        Intent intent = new Intent(this, AppWidget.class);
        intent.putExtra("time", time);
        int ids[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(this.getApplicationContext(), AppWidget.class));
        if (ids.length != 0) {
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            sendBroadcast(intent);
        }
    }

    public void startBreak() {
        String allProfiles = sharedPrefs.getString("profiles", "");
        String[] profiles = allProfiles.split(";");
        String currentProfile = sharedPrefs.getString("name_text", "");

        for (int i = 0; i < profiles.length; i++) {
            if (profiles[i].split(",")[0].equals(currentProfile) && profiles[i].split(",")[3].equals("true")) {
                Intent intent = new Intent(this, BreakActivity.class);
                this.startActivity(intent);
                return;
            }
        }
        Intent intent = new Intent(this, BreakDeciderActivity.class);
        this.startActivity(intent);
    }

    public static class WelcomeDialog extends DialogFragment {

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            LayoutInflater i = getActivity().getLayoutInflater();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setView(i.inflate(R.layout.first_dialog, null));
            builder.setIcon(R.mipmap.ic_drawer_logo);
            builder.setTitle(getActivity().getString(R.string.app_name_long));

            builder.setPositiveButton(getActivity().getString(R.string.dialog_positive), null);
            builder.setNegativeButton(getActivity().getString(R.string.tutorial_help), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // FIXME need api 21
                    // Context context = getContext();
                    Activity act = getActivity();
                    Intent intent = new Intent(act, HelpActivity.class);
                    act.startActivity(intent);
                }
            });

            return builder.create();
        }

    }
}
