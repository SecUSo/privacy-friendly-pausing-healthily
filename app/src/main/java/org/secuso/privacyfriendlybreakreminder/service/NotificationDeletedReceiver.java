package org.secuso.privacyfriendlybreakreminder.service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import org.secuso.privacyfriendlybreakreminder.activities.TimerActivity;

import static org.secuso.privacyfriendlybreakreminder.activities.tutorial.PrefManager.PREF_EXERCISE_CONTINUOUS;

/**
 * @author Christopher Beckmann
 * @version 2.0
 * @since 26.10.2017
 * created 26.10.2017
 */
public class NotificationDeletedReceiver extends BroadcastReceiver {

    private static final String TAG = NotificationDeletedReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        if(pref.getBoolean(PREF_EXERCISE_CONTINUOUS, false)) {
            Intent serviceIntent = new Intent(context, TimerService.class);
            serviceIntent.setAction(TimerService.ACTION_START_TIMER);
            context.startService(serviceIntent);
        }
    }
}
