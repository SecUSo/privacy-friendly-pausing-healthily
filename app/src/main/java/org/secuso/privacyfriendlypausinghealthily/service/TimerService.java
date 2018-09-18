package org.secuso.privacyfriendlypausinghealthily.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import org.secuso.privacyfriendlypausinghealthily.R;
import org.secuso.privacyfriendlypausinghealthily.activities.ExerciseActivity;
import org.secuso.privacyfriendlypausinghealthily.activities.TimerActivity;
import org.secuso.privacyfriendlypausinghealthily.receivers.NotificationCancelReceiver;
import org.secuso.privacyfriendlypausinghealthily.receivers.NotificationDeletedReceiver;

import java.util.Locale;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static org.secuso.privacyfriendlypausinghealthily.activities.tutorial.FirstLaunchManager.PREF_EXERCISE_CONTINUOUS;
import static org.secuso.privacyfriendlypausinghealthily.activities.tutorial.FirstLaunchManager.WORK_TIME;
import static org.secuso.privacyfriendlypausinghealthily.receivers.NotificationCancelReceiver.ACTION_NOTIFICATION_CANCELED;
import static org.secuso.privacyfriendlypausinghealthily.receivers.NotificationDeletedReceiver.ACTION_NOTIFICATION_DELETED;

/**
 * The main timer service. It handles the work timer and sends updates to the notification and the {@link TimerActivity}.
 * When the work time is up, an alarm will fire to start the exercises.
 * @author Christopher Beckmann
 * @version 2.0
 * @see TimerActivity
 */
public class TimerService extends Service {

    public static final String TAG = TimerService.class.getSimpleName();
    public static final String TIMER_BROADCAST = TAG + ".TIMER_BROADCAST";

    public static final String ACTION_START_TIMER = TAG + "ACTION_START_TIMER";
    public static final String ACTION_PAUSE_TIMER = TAG + "ACTION_PAUSE_TIMER";
    public static final String ACTION_RESUME_TIMER = TAG + "ACTION_RESUME_TIMER";
    public static final String ACTION_STOP_TIMER = TAG + "ACTION_STOP_TIMER";
    public static final String ACTION_SNOOZE_TIMER = TAG + "ACTION_SNOOZE_TIMER";

    private static final int UPDATE_INTERVAL = 125;
    public static final int NOTIFICATION_ID = 31337;

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

            if(remainingSeconds != lastTime || isPaused()) {
                lastTime = remainingSeconds;
                updateNotification();
            }
        }
    };
    private BroadcastReceiver notificationDeletedReceiver = new NotificationDeletedReceiver();
    private BroadcastReceiver notificationPreferenceChangedReceiver = new NotificationCancelReceiver();

    private void onTimerDone() {

        // send a notification with sound and vibration
        stopForeground(false);

        Intent snoozeIntent = new Intent(this, TimerService.class);
        snoozeIntent.setAction(ACTION_SNOOZE_TIMER);

        PendingIntent startExercises = PendingIntent.getActivity(this, 0, new Intent(this, ExerciseActivity.class), FLAG_CANCEL_CURRENT);
        PendingIntent snoozeExercise = PendingIntent.getService(this, 0, snoozeIntent, FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "timer_done");
        builder.setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.take_a_break_now))
                .setContentIntent(startExercises)
                .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setWhen(0)
                .setOngoing(false)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_notification)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setVibrate(new long[] { 0, 1000, 1000, 1000, 1000, 1000, 1000 })
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setOnlyAlertOnce(false)
                .setDeleteIntent(PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(ACTION_NOTIFICATION_DELETED), FLAG_UPDATE_CURRENT));

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        if(pref.getBoolean(PREF_EXERCISE_CONTINUOUS, false)) {
            builder.addAction(0, getString(R.string.dismiss_and_dont_repeat), PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(ACTION_NOTIFICATION_CANCELED), FLAG_UPDATE_CURRENT));
        }

        builder.addAction(R.drawable.ic_replay_black_48dp, getString(R.string.snooze), snoozeExercise);
        builder.addAction(R.drawable.ic_play_arrow_black, getString(R.string.start), startExercises);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    @Override
    public void onCreate() {
        super.onCreate();

        registerReceiver(timerReceiver, new IntentFilter(TIMER_BROADCAST));
        registerReceiver(notificationDeletedReceiver, new IntentFilter(ACTION_NOTIFICATION_DELETED));
        registerReceiver(notificationPreferenceChangedReceiver, new IntentFilter(ACTION_NOTIFICATION_CANCELED));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(timerReceiver);
        unregisterReceiver(notificationDeletedReceiver);
        unregisterReceiver(notificationPreferenceChangedReceiver);
    }

    public synchronized void startTimer(final long duration) {
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
        if (isRunning) {
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
        if (isRunning) {
            mTimer.cancel();
            mTimer = createTimer(initialDuration);
            mTimer.start();
        }
        remainingDuration = initialDuration;

        sendBroadcast(buildBroadcast());
    }

    public synchronized void stopAndResetTimer() {
        if (isRunning) mTimer.cancel();
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

        if (intent != null) {

            final String action = intent.getAction();

            if      (ACTION_START_TIMER.equals(action))     handleRestartTimer();
            else if (ACTION_PAUSE_TIMER.equals(action))     pauseTimer();
            else if (ACTION_RESUME_TIMER.equals(action))    resumeTimer();
            else if (ACTION_STOP_TIMER.equals(action))      stopAndResetTimer();
            else if (ACTION_SNOOZE_TIMER.equals(action))    handleSnoozeTimer();
        }

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        return START_NOT_STICKY;
    }

    private void handleSnoozeTimer() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        long snoozeTime = (long)((float)pref.getLong(WORK_TIME, 1000 * 60 * 60) / 60f * 5f);

        startTimer(snoozeTime);
    }

    private void handleRestartTimer() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        if(pref.getBoolean(PREF_EXERCISE_CONTINUOUS, false)) {
            long duration = pref.getLong(WORK_TIME, 1000 * 60 * 60);
            startTimer(duration);
        }
    }


    private Notification buildNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "timer_running");
        builder.setContentTitle(getString(R.string.app_name));

        int secondsUntilFinished = (int) Math.ceil(remainingDuration / 1000.0);
        int minutesUntilFinished = secondsUntilFinished / 60;
        int hours = minutesUntilFinished / 60;
        int seconds = secondsUntilFinished % 60;
        int minutes = minutesUntilFinished % 60;

        String time = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);

        PendingIntent startExercises = PendingIntent.getActivity(this, 0, new Intent(this, ExerciseActivity.class), FLAG_CANCEL_CURRENT);

        builder.setContentText(time);
        builder.setColor(ContextCompat.getColor(this, R.color.colorAccent));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setWhen(0);
        builder.setProgress((int) initialDuration, (int) (initialDuration - remainingDuration), false);
        builder.setSmallIcon(R.mipmap.ic_notification);
        builder.setOngoing(isRunning() || isPaused());

        Intent intent = new Intent(this, TimerActivity.class);
        intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
        builder.setContentIntent(PendingIntent.getActivity(this, 0, intent, FLAG_UPDATE_CURRENT));

        builder.addAction(R.drawable.ic_play_arrow_black, getString(R.string.start_break), startExercises);

        Intent stopIntent = new Intent(this, TimerService.class);
        stopIntent.setAction(ACTION_STOP_TIMER);
        builder.addAction(R.drawable.ic_replay_black_48dp, getString(R.string.stop), PendingIntent.getService(this, 0, stopIntent, FLAG_UPDATE_CURRENT));

        Intent pauseIntent = new Intent(this, TimerService.class);
        if(!isPaused()) {
            pauseIntent.setAction(ACTION_PAUSE_TIMER);
            builder.addAction(R.drawable.ic_pause_black_48dp, getString(R.string.pause), PendingIntent.getService(this, 0, pauseIntent, FLAG_UPDATE_CURRENT));
        } else {
            pauseIntent.setAction(ACTION_RESUME_TIMER);
            builder.addAction(R.drawable.ic_play_arrow_black, getString(R.string.resume), PendingIntent.getService(this, 0, pauseIntent, FLAG_UPDATE_CURRENT));
        }

        return builder.build();
    }

    private void updateNotification() {
        if(isRunning() || isPaused()) {
            startForeground(NOTIFICATION_ID, buildNotification());
        } else {
            stopForeground(true);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if(isReset()){
            stopSelf();
        }
        return super.onUnbind(intent);
    }

    public static void startService(Context context) {
        context.startService(new Intent(context.getApplicationContext(), TimerService.class));
    }

    public synchronized long getRemainingDuration() {
        return remainingDuration;
    }

    public boolean isReset() {
        return !isRunning() && !isPaused();
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
