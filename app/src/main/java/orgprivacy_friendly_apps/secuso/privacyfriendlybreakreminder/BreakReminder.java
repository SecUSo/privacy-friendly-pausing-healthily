package orgprivacy_friendly_apps.secuso.privacyfriendlybreakreminder;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
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
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;

public class BreakReminder extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private boolean isRunning = false;
    private TextView ct_text;
    private CountDownTimer ct;
    private String stopTime = "";

    private Spinner profileSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_break_reminder);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String allProfiles = sharedPrefs.getString("profiles", "");
        if (allProfiles.equals("")) {
            System.out.println("Es gibt noch keine Profile!!");
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString("profiles", "Sport,5,1;Exams,90,15;Pomodoro,30,5;");
            editor.apply();
        }

        System.out.println("Alle Profile: " + sharedPrefs.getString("profiles", "FAIL"));

        // If chosen, set screen to "stay on"
        boolean stayOn = sharedPrefs.getBoolean("notifications_stayOn", false);

        if (stayOn)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        int mins = sharedPrefs.getInt("work_value", 50);
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
        profileNames[profileNames.length - 1] = "New Profile...";
        ArrayAdapter<String> adapter = new
                ArrayAdapter<String>(this, R.layout.spinner_layout, profileNames);
        profileSpinner.setAdapter(adapter);

        //Set the ClickListener for Spinner
        profileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()

        {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("Selected item: " + parent.getItemAtPosition(position) + " with id: " + id);

                String profileSelected = (String) parent.getItemAtPosition(position);
                if (profileSelected.equals("New Profile...")) {
                    createNewProfile();
                } else {
                    updatePreference(profileSelected);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void updatePreference(String profileSelected) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String allProfiles = sharedPrefs.getString("profiles", "");

        String currentProfile = sharedPrefs.getString("name_text", "") + "," + sharedPrefs.getInt("work_value", -1) + "," + sharedPrefs.getInt("break_value", -1);

        System.out.println("Current PROFILE: " + currentProfile + " , PROFILE SELECTED: " + profileSelected);
        if (allProfiles.contains(currentProfile) && profileSelected.equals(sharedPrefs.getString("name_text", ""))) {
            System.out.println("Profile didn´t change");
        } else {
            String[] profileNames = allProfiles.split(";");
            for (int i = 0; i < profileNames.length; i++) {
                String profileName = profileNames[i].split(",")[0];
                int interval = Integer.parseInt(profileNames[i].split(",")[1]);
                int break_time = Integer.parseInt(profileNames[i].split(",")[2]);
                if (profileName.equals(profileSelected)) {
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString("name_text", profileName);
                    editor.putInt("work_value", interval);
                    editor.putInt("break_value", break_time);
                    editor.apply();

                    //Update clock
                    String bufferZeroMinute = "";
                    int time = interval * 60 * 1000;
                    if (time / 1000 / 60 < 10)
                        bufferZeroMinute = "0";

                    ct_text.setText(bufferZeroMinute + time / 1000 / 60 + ":00");
                    break;
                }
            }
        }
    }

    private void fillProfiles() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String allProfiles = sharedPrefs.getString("profiles", "");

        String[] profileNames = new String[allProfiles.split(";").length + 1];
        String[] fillProfileNames = allProfiles.split(";");
        for (int i = 0; i < profileNames.length - 1; i++) {
            profileNames[i] = fillProfileNames[i].split(",")[0];
        }
        profileNames[profileNames.length - 1] = "New Profile...";
        ArrayAdapter<String> adapter = new
                ArrayAdapter<String>(this, R.layout.spinner_layout, profileNames);
        profileSpinner.setAdapter(adapter);

        //Set Spinner on the current Profile
        String currentProfile = sharedPrefs.getString("name_text", "Sport");
        int interval = sharedPrefs.getInt("work_value", 1);
        profileSpinner.setSelection(Arrays.asList(profileNames).indexOf(currentProfile));

        //Update clock
        String bufferZeroMinute = "";
        int time = interval * 60 * 1000;
        if (time / 1000 / 60 < 10)
            bufferZeroMinute = "0";

        ct_text.setText(bufferZeroMinute + time / 1000 / 60 + ":00");

        //Set the ClickListener for Spinner
        profileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()

        {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("Selected item: " + parent.getItemAtPosition(position) + " with id: " + id);

                String profileSelected = (String) parent.getItemAtPosition(position);
                if (profileSelected.equals("New Profile...")) {
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
    public void onResume() {
        super.onResume();
        fillProfiles();
        profileSpinner = (Spinner) findViewById(R.id.spinner);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.break_reminder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        } else if (id == R.id.nav_statistics) {

        } else if (id == R.id.nav_help) {

        } else if (id == R.id.nav_about) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onClick(View v) {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int mins = sharedPrefs.getInt("work_value", 50);
        String bufferZeroMinute = "";
        String bufferZeroSecond = "";
        int time = mins * 60 * 1000;
        time = 5000;
        int oldTime = time;

        if (stopTime == "" && !isRunning) {
            if (time / 1000 / 60 < 10)
                bufferZeroMinute = "0";

            ct_text.setText(bufferZeroMinute + time / 1000 / 60 + ":00");
        } else if (!isRunning) {
            ct_text.setText(stopTime);
            String stringTime = (String) ct_text.getText();
            String[] timef = stringTime.split(":");
            int minute = Integer.parseInt(timef[0]);
            int second = Integer.parseInt(timef[1]);
            System.out.println("Minute: " + minute + "  Second: " + second);
            time = (1000 * (minute * 60)) + (1000 * second);

            if (minute < 10)
                bufferZeroMinute = "0";
            if (second < 10)
                bufferZeroSecond = "0";

            ct_text.setText(bufferZeroMinute + minute + ":" + bufferZeroSecond + second);
        }


        System.out.println(time + " " + ct_text.getText());


        switch (v.getId()) {

            case R.id.textView:
            case R.id.button_playStop:
                if (isRunning) {
                    ct.cancel();
                    stopTime = (String) ct_text.getText();
                    isRunning = false;
                } else {
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

                            //Show how much time is left
                            //String timeLeft = bufferZeroMinute + (millisUntilFinished / 1000) / 60 + ":" + bufferZeroSecond + millisUntilFinished / 1000 % 60;
                            //System.out.println("Time left: " + timeLeft);
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
                            //trigger the alarm
                            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            String ringPref = sharedPrefs.getString("notifications_new_message_ringtone", "");
                            System.out.println("Sound: " + ringPref);
                            if (!ringPref.equals("")) {
                                System.out.println("-----------------PLAY NOTIFICATION SOUND----------------");
                                // Get the current ringtone
                                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), Uri.parse(ringPref));

                                // Play ringtone
                                r.play();
                            }
                            //FIXME Test Vibration
                            boolean vibrateChecked = sharedPrefs.getBoolean("notifications_new_message_vibrate", false);
                            System.out.println("Vibrate is : " + vibrateChecked);
                            if (vibrateChecked) {
                                // Get instance of Vibrator from current Context
                                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                                if (v == null) {
                                    System.out.println("No vibrator! :D");
                                } else {
                                    // Vibrate for 3000 milliseconds
                                    System.out.println("Vibrate for 3000 ms");
                                    v.vibrate(3000);
                                }
                            }

                            //Cancel the notification
                            if (timeLeft) {
                                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                notificationManager.cancel(1000);
                            }
                            startBreak();
                        }
                    }.start();
                    isRunning = true;
                }
                break;


            case R.id.button_reset:
                if (ct != null) {
                    //Reset clock
                    int interval = sharedPrefs.getInt("work_value", 1);

                    bufferZeroMinute = "";
                    time = interval * 60 * 1000;
                    if (time / 1000 / 60 < 10)
                        bufferZeroMinute = "0";

                    ct_text.setText(bufferZeroMinute + time / 1000 / 60 + ":00");


                }


                if (oldTime / 1000 / 60 < 10)
                    bufferZeroMinute = "0";

                ct_text.setText(bufferZeroMinute + oldTime / 1000 / 60 + ":00");
                stopTime = oldTime / 1000 / 60 + ":00";
                isRunning = false;
                break;
        }
    }

    private void createNewProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        this.startActivity(intent);
    }

    public void startBreak() {
        Intent intent = new Intent(this, BreakDeciderActivity.class);
        this.startActivity(intent);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int mins = sharedPrefs.getInt("work_value", 50);
        String bufferZeroMinute = "";
        int time = mins * 60 * 1000;

        if (time / 1000 / 60 < 10)
            bufferZeroMinute = "0";

        ct_text.setText(bufferZeroMinute + time / 1000 / 60 + ":00");
    }


}
