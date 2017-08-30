package org.secuso.privacyfriendlybreakreminder.activities;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.secuso.privacyfriendlybreakreminder.R;
import org.secuso.privacyfriendlybreakreminder.service.TimerService;

public class TimerActivity extends AppCompatActivity {

    // UI
    private ProgressBar progressBar;
    private TextView timerText;

    // Service
    private TimerService mTimerService = null;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerService.TimerServiceBinder binder = (TimerService.TimerServiceBinder) service;
            mTimerService = binder.getService();

            TimerActivity.this.onServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mTimerService = null;
        }
    };

    private void onServiceConnected() {
        if(mTimerService.isRunning()) {
            progressBar.setMax((int) mTimerService.getInitialDuration());
        }
    }

    private final BroadcastReceiver timerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long millisUntilDone = intent.getLongExtra("onTickMillis", -1L);

            updateProgress(millisUntilDone);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        initResources();
    }

    @Override
    protected void onStop() {
        super.onStop();

        shutdownServiceBinding();
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(timerReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        TimerService.startService(this);
        registerReceiver(timerReceiver, new IntentFilter(TimerService.TIMER_BROADCAST));

        if(mTimerService != null && !mTimerService.isRunning()) {
            updateProgress(mTimerService.getInitialDuration());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        initServiceBinding();
    }

    private void initServiceBinding() {
        Intent intent = new Intent(this, TimerService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void shutdownServiceBinding() {
        if (mTimerService != null) {
            unbindService(mServiceConnection);
        }
    }

    private void initResources() {
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        timerText = (TextView) findViewById(R.id.timerText);

        timerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int duration = 1000 * 60 * 1; // 1 minutes
                if(mTimerService != null) {
                    mTimerService.startTimer(duration);
                    progressBar.setMax(duration);
                }
            }
        });
    }

    private void updateProgress(long millisUntilFinished) {
        progressBar.setProgress(progressBar.getMax() - (int) millisUntilFinished);

        int secondsUntilFinished = (int) Math.ceil(millisUntilFinished / 1000.0);
        int minutesUntilFinished = secondsUntilFinished / 60;
        int hours = minutesUntilFinished / 60;
        int seconds = secondsUntilFinished % 60;
        int minutes = minutesUntilFinished % 60;

        StringBuilder sb = new StringBuilder();

        if(hours > 0)       sb.append(hours).append(":");
        if(minutes < 10)    sb.append(0);
                            sb.append(minutes).append(":");
        if(seconds < 10)    sb.append(0);
                            sb.append(seconds);

        timerText.setText(sb.toString());

        //progressBar.setMax(1000);
        //ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 0, 1000 * percentFinished); // see this max value coming back here, we animale towards that value

        //animation.setDuration(5000); //in milliseconds
        //animation.setInterpolator(new LinearInterpolator());
        //animation.start();
    }






}
