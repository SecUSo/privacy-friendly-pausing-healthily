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
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;

import org.secuso.privacyfriendlybreakreminder.R;
import org.secuso.privacyfriendlybreakreminder.activities.ExerciseActivity;
import org.secuso.privacyfriendlybreakreminder.activities.TimerActivity;

import java.io.FileDescriptor;
import java.util.Locale;
import java.util.Timer;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class TimerService extends Service {

    public static final String TAG = TimerService.class.getSimpleName();
    public static final String NOTIFICATION_BROADCAST = TAG + ".NOTIFICATION_BROADCAST";
    public static final String TIMER_BROADCAST = TAG + ".TIMER_BROADCAST";

    private static final int UPDATE_INTERVAL = 25;
    private static final int NOTIFICATION_ID = 31337;

    private TimerServiceBinder mBinder = new TimerServiceBinder();
    private CountDownTimer mTimer;
    private NotificationManager notificationManager;

    private boolean isRunning = false;
    private long remainingDuration = 0;
    private long initialDuration = 0;

    private BroadcastReceiver timerReceiver = new BroadcastReceiver() {
        int lastTime = 0;
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getBooleanExtra("done" ,false)) {
                lastTime = 0;
                onTimerDone();
                return;
            }

            // reset lastTime if we are starting a new timer
            long initialMillis = intent.getLongExtra("initialMillis", 0);
            long remainingMillis = intent.getLongExtra("onTickMillis", 0);

            if(initialMillis == remainingMillis) {
                lastTime = 0;
            }

            // limit the notification updates
            int remainingSeconds = intent.getIntExtra("countdown_seconds", 0);

            if(remainingSeconds != lastTime) {
                lastTime = remainingSeconds;
                updateNotification();

            }
        }
    };

    private void onTimerDone() {

        // send a notification with sound and vibration
        stopForeground(false);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(getString(R.string.app_name))
                .setContentText("Take a break now! Click here to do your chosen exercises.")
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, ExerciseActivity.class), FLAG_UPDATE_CURRENT))
                .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setWhen(0)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setVibrate(new long[] { 0, 1000, 1000, 1000, 1000, 1000, 1000 })
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setOnlyAlertOnce(false);
        notificationManager.notify(NOTIFICATION_ID, builder.build());

        // TODO: show decider activity?!
        // maybe rather show a dialog
    }

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

            if(duration > 0) {
                mTimer = createTimer(duration);
                mTimer.start();
                isRunning = true;
                sendBroadcast(buildBroadcast());

            } else {
                remainingDuration = initialDuration;
                Intent broadcast = buildBroadcast();
                broadcast.putExtra("done", true);
                sendBroadcast(broadcast);
            }
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

    public synchronized boolean isPaused() { return !isRunning && initialDuration != 0 && remainingDuration > 0 && remainingDuration != initialDuration; }

    public synchronized boolean isRunning() {
        return isRunning;
    }

    private CountDownTimer createTimer(long duration) {
        remainingDuration = duration;

        return new CountDownTimer(duration, UPDATE_INTERVAL) {

            @Override
            public void onTick(long millisUntilFinished) {
                if(isRunning) {
                    remainingDuration = millisUntilFinished;
                    sendBroadcast(buildBroadcast());
                }
            }

            @Override
            public void onFinish() {
                isRunning = false;
                remainingDuration = 0;

                Intent broadcast = buildBroadcast();
                broadcast.putExtra("done", true);
                TimerService.this.sendBroadcast(broadcast);

                remainingDuration = initialDuration;

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

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        return START_STICKY;
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
        builder.setOngoing(isRunning() || isPaused());

        return builder.build();
    }

    private void updateNotification() {
        if(isRunning() || isPaused())
            startForeground(NOTIFICATION_ID, buildNotification());
        else
            stopForeground(false);

        notificationManager.notify(NOTIFICATION_ID, buildNotification());
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
