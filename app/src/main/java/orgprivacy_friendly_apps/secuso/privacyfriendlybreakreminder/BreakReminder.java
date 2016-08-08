package orgprivacy_friendly_apps.secuso.privacyfriendlybreakreminder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
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
import android.preference.ListPreference;
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

public class BreakReminder extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private boolean isRunning = false;
    private TextView ct_text;
    private CountDownTimer ct;
    private String stopTime = "";
    private SeekBarPreference _seekBarWork;
    private SeekBarPreference _seekBarBreak;

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

        // If chosen, set screen to "stay on"
        boolean stayOn = sharedPrefs.getBoolean("notifications_stayOn", false);
        if(stayOn)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        int mins = sharedPrefs.getInt("work_value", 50);
        String bufferZeroMinute = "";

        if (mins < 10)
            bufferZeroMinute = "0";

        ct_text = (TextView) findViewById(R.id.textView);
        ct_text.setText(bufferZeroMinute + mins + ":00");

        Button playStopButton = (Button) findViewById(R.id.button_playStop);
        playStopButton.setOnClickListener(this);
        Button resetButton = (Button) findViewById(R.id.button_reset);
        resetButton.setOnClickListener(this);
        ct_text.setOnClickListener(this);

        Button profileButton = (Button) findViewById(R.id.button_profile);
        profileButton.setOnClickListener(this);

        Spinner profileSpinner = (Spinner) findViewById(R.id.spinner);
        String[] some_array = getResources().getStringArray(R.array.profile_entries);
        ArrayAdapter<String> adapter = new
                ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, some_array);
        profileSpinner.setAdapter(adapter);

        //Set the ClickListener for Spinner
        profileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("Selected item: " + parent.getItemAtPosition(position) + " with id: " + id);
                //TODO Match the work and break value to the assigned profile
                switch ((String) parent.getItemAtPosition(position)){
                    case "New Profile":
                        createNewProfile();
                        break;

                }
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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

            //FIXME
            case R.id.button_profile:
                FragmentManager fm = getFragmentManager();
                ProfileDialog pd = new ProfileDialog();
                pd.show(fm, "Profile Dialog");

                //createNewProfile();
                break;

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
                            //FIXME Vibrate
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
                if (ct != null)
                    ct.cancel();

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


    public static class ProfileDialog extends DialogFragment {

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle("")
                    .setItems(R.array.profile_entries, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // The 'which' argument contains the index position
                            // of the selected item

                        }
                    });
            return builder.create();
        }
    }

}
