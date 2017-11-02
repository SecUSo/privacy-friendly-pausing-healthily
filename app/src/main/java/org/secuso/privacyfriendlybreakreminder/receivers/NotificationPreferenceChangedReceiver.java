package org.secuso.privacyfriendlybreakreminder.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import org.secuso.privacyfriendlybreakreminder.service.TimerService;

import static org.secuso.privacyfriendlybreakreminder.activities.tutorial.PrefManager.PREF_EXERCISE_CONTINUOUS;

/**
 * @author Christopher Beckmann
 * @version 2.0
 * @since 02.11.2017
 * created 02.11.2017
 */

public class NotificationPreferenceChangedReceiver extends BroadcastReceiver {

    public static final String ACTION_PREF_CHANGE = "org.secuso.privacyfriendlybreakreminder.ACTION_PREF_CHANGE";
    public static final String EXTRA_DISABLE_CONTINUOUS = "EXTRA_DISABLE_CONTINUOUS";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        Bundle bundle = intent.getExtras();

        if (bundle == null) return;

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        for (String key : intent.getExtras().keySet()) {
            if (EXTRA_DISABLE_CONTINUOUS.equals(key)) {
                pref.edit().putBoolean(PREF_EXERCISE_CONTINUOUS, false).apply();
            }
        }

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(TimerService.NOTIFICATION_ID);
        }
    }
};
