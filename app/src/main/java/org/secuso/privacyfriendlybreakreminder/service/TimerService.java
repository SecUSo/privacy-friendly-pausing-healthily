package org.secuso.privacyfriendlybreakreminder.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;

import org.secuso.privacyfriendlybreakreminder.R;
import org.secuso.privacyfriendlybreakreminder.activities.TimerActivity;

import java.io.FileDescriptor;
import java.util.Timer;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class TimerService extends Service {
    public static final String TAG = TimerService.class.getSimpleName();
    public static final String NOTIFICATION_BROADCAST = TAG + ".NOTIFICATION_BROADCAST";
    public static final String TIMER_BROADCAST = TAG + ".TIMER_BROADCAST";

    private TimerServiceBinder mBinder = new TimerServiceBinder();
    private CountDownTimer mTimer;

    private boolean isRunning = false;
    private long remainingDuration = 0;
    private long initialDuration = 0;

    private BroadcastReceiver timerReceiver = new BroadcastReceiver() {
        int lastTime = 0;
        @Override
        public void onReceive(Context context, Intent intent) {
            if((int) remainingDuration / 1000 != lastTime) {
                lastTime = (int) remainingDuration / 1000;
                updateNotification();
            }

            if(intent.getBooleanExtra("done" ,false)) {
                updateNotification();

            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        registerReceiver(timerReceiver, new IntentFilter(TIMER_BROADCAST));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(timerReceiver);
    }

    public synchronized void startTimer(long duration) {
        if(!isRunning) {
            initialDuration = duration;
            mTimer = createTimer(duration);
            mTimer.start();
            isRunning = true;
        }
    }

    public synchronized void pauseTimer() {
        if(isRunning) {
            mTimer.cancel();
            isRunning = false;
        }
    }

    public synchronized void resumeTimer() {
        if(!isRunning & remainingDuration > 0) {
            mTimer = createTimer(remainingDuration);
            mTimer.start();
            isRunning = true;
        }
    }

    public synchronized void resetTimer() {
        if(isRunning) {
            mTimer.cancel();
            isRunning = false;
            remainingDuration = 0;
        }
    }

    public synchronized boolean isRunning() {
        return isRunning;
    }

    private CountDownTimer createTimer(long duration) {
        remainingDuration = duration;

        return new CountDownTimer(duration, 10) {

            @Override
            public void onTick(long millisUntilFinished) {
                int secondsUntilFinished = (int) Math.ceil(millisUntilFinished / 1000.0);

                remainingDuration = millisUntilFinished;

                Intent broadcast = new Intent(TIMER_BROADCAST);
                broadcast.putExtra("onTickMillis", millisUntilFinished);
                broadcast.putExtra("countdown_seconds", secondsUntilFinished);
                sendBroadcast(broadcast);
            }

            @Override
            public void onFinish() {
                // TODO: finish broadcast
                Intent broadcast = new Intent(TIMER_BROADCAST);
                broadcast.putExtra("done", true);
                broadcast.putExtra("onTickMillis", 0);
                broadcast.putExtra("countdown_seconds", 0);
                sendBroadcast(broadcast);
                resetTimer();
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        setAsForegroundService();

        return START_STICKY;
    }

    private void setAsForegroundService() {
        startForeground(31337, buildNotification());
    }

    private Notification buildNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(getString(R.string.app_name));

        int secondsUntilFinished = (int) Math.ceil(remainingDuration / 1000.0);
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

        builder.setContentText(sb.toString());
        builder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, TimerActivity.class), FLAG_UPDATE_CURRENT));
        builder.setColor(ContextCompat.getColor(this, R.color.colorAccent));
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setWhen(0);
        builder.setSmallIcon(R.mipmap.ic_launcher);

        return builder.build();
    }

    private void updateNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(31337, buildNotification());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public static void startService(Context context) {
        context.startService(new Intent(context.getApplicationContext(), TimerService.class));
    }

    public class TimerServiceBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }

    public long getInitialDuration() {
        return initialDuration;
    }
}
