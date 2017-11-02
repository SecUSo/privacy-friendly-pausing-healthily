package org.secuso.privacyfriendlybreakreminder.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.secuso.privacyfriendlybreakreminder.service.TimerService;

import static org.secuso.privacyfriendlybreakreminder.activities.tutorial.PrefManager.PREF_EXERCISE_CONTINUOUS;

/**
 * @author Christopher Beckmann
 * @version 2.0
 * @since 02.11.2017
 * created 02.11.2017
 */
public class NotificationDeletedReceiver extends BroadcastReceiver {

    public static final String ACTION_NOTIFICATION_DELETED = "org.secuso.privacyfriendlybreakreminder.NotificationDeleted";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        if(pref.getBoolean(PREF_EXERCISE_CONTINUOUS, false)) {
            Intent serviceIntent = new Intent(context, TimerService.class);
            serviceIntent.setAction(TimerService.ACTION_START_TIMER);
            context.startService(serviceIntent);
        }
    }
};
