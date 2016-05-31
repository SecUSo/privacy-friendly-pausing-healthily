package orgprivacy_friendly_apps.secuso.privacyfriendlybreakreminder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

public class BreakReminder extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener{

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
        int mins = sharedPrefs.getInt("work_value", 50);
        String bufferZeroMinute = "";

        if(mins < 10)
            bufferZeroMinute = "0";

        ct_text =(TextView)findViewById(R.id.textView);
        ct_text.setText(bufferZeroMinute+mins+":00");

        Button playStopButton = (Button)findViewById(R.id.button_playStop);
        playStopButton.setOnClickListener(this);
        Button resetButton = (Button)findViewById(R.id.button_reset);
        resetButton.setOnClickListener(this);
        ct_text.setOnClickListener(this);

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
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int mins = sharedPrefs.getInt("work_value", 50);
        String bufferZeroMinute = "";
        String bufferZeroSecond = "";
        int time = mins * 60 * 1000;
        time = 5000;
        int oldTime = time;

        if(stopTime == "" && !isRunning) {
            if (time / 1000 / 60 < 10)
                bufferZeroMinute = "0";

            ct_text.setText(bufferZeroMinute + time / 1000 / 60 + ":00");
        } else if(!isRunning){
            ct_text.setText(stopTime);
            String stringTime = (String)ct_text.getText();
            String[] timef = stringTime.split(":");
            int minute = Integer.parseInt(timef[0]);
            int second = Integer.parseInt(timef[1]);
            System.out.println("Minute: "+ minute + "  Second: "+second);
            time = (1000 * (minute*60)) + (1000 * second);

            if(minute < 10)
                bufferZeroMinute = "0";
            if(second < 10)
                bufferZeroSecond = "0";

            ct_text.setText(bufferZeroMinute + minute + ":" + bufferZeroSecond + second);
        }


        System.out.println(time +" "+ ct_text.getText());



        switch (v.getId()) {

            case  R.id.textView:
            case  R.id.button_playStop:
                if (isRunning){
                    ct.cancel();
                    stopTime = (String)ct_text.getText();
                    isRunning = false;
                }else{
                    ct = new CountDownTimer(time, 1000) {

                        public void onTick(long millisUntilFinished) {
                            String bufferZeroMinute = "";
                            String bufferZeroSecond = "";

                            if ((millisUntilFinished / 1000)/60 < 10)
                                bufferZeroMinute = "0";

                            if (millisUntilFinished / 1000 % 60 < 10)
                                bufferZeroSecond = "0";

                            ct_text.setText(bufferZeroMinute+(millisUntilFinished / 1000)/60 + ":" + bufferZeroSecond + millisUntilFinished / 1000 % 60);
                        }

                        public void onFinish() {
                            isRunning = false;
                            ct_text.setText("00:00");
                            //TODO trigger the alarm

                            startBreak();
                        }
                    }.start();
                    isRunning = true;
                }
                break;


            case R.id.button_reset:
                if (ct != null)
                    ct.cancel();

                if(oldTime/1000/60 < 10)
                    bufferZeroMinute = "0";

                ct_text.setText(bufferZeroMinute + oldTime/1000/60+":00");
                stopTime = oldTime/1000/60+":00";
                isRunning = false;
                break;
        }
    }

    public void startBreak(){
        Intent intent = new Intent(this, BreakDeciderActivity.class);
        this.startActivity(intent);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int mins = sharedPrefs.getInt("work_value", 50);
        String bufferZeroMinute = "";
        int time = mins * 60 * 1000;

        if(time/1000/60 < 10)
            bufferZeroMinute = "0";

        ct_text.setText(bufferZeroMinute + time/1000/60+":00");
    }
}
