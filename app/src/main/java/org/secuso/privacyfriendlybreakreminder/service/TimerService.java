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
import java.util.Locale;
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
            sendBroadcast(buildBroadcast());
        }
    }

    public synchronized void pauseTimer() {
        if(isRunning) {
            mTimer.cancel();
            isRunning = false;
            sendBroadcast(buildBroadcast());
        }
    }

    public synchronized void resumeTimer() {
        if(!isRunning & remainingDuration > 0) {
            mTimer = createTimer(remainingDuration);
            mTimer.start();
            isRunning = true;
            sendBroadcast(buildBroadcast());
        }
    }

    public synchronized void resetTimer() {
        if(isRunning) {
            mTimer.cancel();
            mTimer = createTimer(initialDuration);
            mTimer.start();
        }
        remainingDuration = initialDuration;
        sendBroadcast(buildBroadcast());
    }

    public synchronized void stopAndResetTimer() {
        if(isRunning) mTimer.cancel();
        isRunning = false;
        remainingDuration = initialDuration;
        sendBroadcast(buildBroadcast());
    }

    public synchronized boolean isPaused() { return !isRunning && remainingDuration > 0 && remainingDuration != initialDuration; }

    public synchronized boolean isRunning() {
        return isRunning;
    }

    private CountDownTimer createTimer(long duration) {
        remainingDuration = duration;

        return new CountDownTimer(duration, 10) {

            @Override
            public void onTick(long millisUntilFinished) {
                synchronized (TimerService.this) {
                    remainingDuration = millisUntilFinished;
                }

                sendBroadcast(buildBroadcast());
            }

            @Override
            public void onFinish() {
                Intent broadcast = buildBroadcast();
                broadcast.putExtra("done", true);
                sendBroadcast(buildBroadcast());

                stopAndResetTimer();
            }
        };
    }

    private synchronized Intent buildBroadcast() {
        int secondsUntilFinished = (int) Math.ceil(remainingDuration / 1000.0);

        Intent broadcast = new Intent(TIMER_BROADCAST);
        broadcast.putExtra("onTickMillis", remainingDuration);
        broadcast.putExtra("initialMillis", initialDuration);
        broadcast.putExtra("countdown_seconds", secondsUntilFinished);
        broadcast.putExtra("isRunning", isRunning());
        broadcast.putExtra("isPaused", isPaused());
        return (broadcast);
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

        String time = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);

        builder.setContentText(time);
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

    public synchronized long getRemainingDuration() {
        return remainingDuration;
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
