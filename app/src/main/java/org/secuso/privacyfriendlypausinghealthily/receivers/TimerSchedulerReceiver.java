package org.secuso.privacyfriendlypausinghealthily.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.WakefulBroadcastReceiver;

import org.secuso.privacyfriendlypausinghealthily.service.TimerService;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import static org.secuso.privacyfriendlypausinghealthily.activities.tutorial.FirstLaunchManager.PREF_SCHEDULE_EXERCISE_DAYS;
import static org.secuso.privacyfriendlypausinghealthily.activities.tutorial.FirstLaunchManager.PREF_SCHEDULE_EXERCISE_DAYS_ENABLED;
import static org.secuso.privacyfriendlypausinghealthily.activities.tutorial.FirstLaunchManager.PREF_SCHEDULE_EXERCISE_ENABLED;
import static org.secuso.privacyfriendlypausinghealthily.activities.tutorial.FirstLaunchManager.PREF_SCHEDULE_EXERCISE_TIME;
import static org.secuso.privacyfriendlypausinghealthily.activities.tutorial.FirstLaunchManager.WORK_TIME;

/**
 * @author Christopher Beckmann
 * @version 2.0
 * @since 03.11.2017
 * created 03.11.2017
 */
public class TimerSchedulerReceiver extends WakefulBroadcastReceiver {

    private SharedPreferences mPref;
    private TimerService mTimerService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerService.TimerServiceBinder binder = (TimerService.TimerServiceBinder) service;
            mTimerService = binder.getService();
            TimerSchedulerReceiver.this.startTimer();
            TimerSchedulerReceiver.scheduleNextAlarm(mTimerService.getApplicationContext());
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: automatically set continuous mode?
        Intent timerIntent = new Intent(context, TimerService.class);
        context.getApplicationContext().startService(timerIntent);
        context.getApplicationContext().bindService(timerIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        mPref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private void startTimer() {
        mTimerService.startTimer(mPref.getLong(WORK_TIME, 1000L * 60L * 60L));
    }

    public static void scheduleNextAlarm(@NonNull Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        // delete any previous set alarm
        deleteScheduledAlarm(context);

        if(!pref.getBoolean(PREF_SCHEDULE_EXERCISE_ENABLED, false)) {
            return;
        }

        long timestamp = pref.getLong(PREF_SCHEDULE_EXERCISE_TIME, 32400000);


        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent automaticTimerIntent = new Intent(context, TimerSchedulerReceiver.class);
        PendingIntent automaticTimerPending = PendingIntent.getBroadcast(context, 0, automaticTimerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
        calendar.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH));
        calendar.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if(calendar.before(Calendar.getInstance())){
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        boolean done = false;
        boolean scheduleExerciseDaysEnabled = pref.getBoolean(PREF_SCHEDULE_EXERCISE_DAYS_ENABLED, false);

        if(scheduleExerciseDaysEnabled) {

            Set<String> daySet = pref.getStringSet(PREF_SCHEDULE_EXERCISE_DAYS, new HashSet<String>(Arrays.asList("Mo","Di","Mi","Do","Fr","Sa","So")));

            for(int i = 0; i < 7; i++) {
                String currentDay;
                // skip days that are not selected
                switch (calendar.get(Calendar.DAY_OF_WEEK)) {
                    case Calendar.MONDAY:
                        currentDay = "Mo";
                        break;
                    case Calendar.TUESDAY:
                        currentDay = "Di";
                        break;
                    case Calendar.WEDNESDAY:
                        currentDay = "Mi";
                        break;
                    case Calendar.THURSDAY:
                        currentDay = "Do";
                        break;
                    case Calendar.FRIDAY:
                        currentDay = "Fr";
                        break;
                    case Calendar.SATURDAY:
                        currentDay = "Sa";
                        break;
                    case Calendar.SUNDAY:
                        currentDay = "So";
                        break;
                    default:
                        currentDay = "None";
                }

                for(String day : daySet) {
                    if(currentDay.equals(day)) {
                        done = true;
                        break;
                    }
                }

                if(done) {
                    break;
                }

                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        if(done || !scheduleExerciseDaysEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), automaticTimerPending);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), automaticTimerPending);
            }
        }
    }

    private static void deleteScheduledAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent automaticTimerIntent = new Intent(context, TimerSchedulerReceiver.class);
        PendingIntent automaticTimerPending = PendingIntent.getBroadcast(context, 0, automaticTimerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.cancel(automaticTimerPending);
    }
}
