package org.secuso.privacyfriendlybreakreminder.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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

        // TODO: Notification was swiped away.
        Log.d(TAG, "Notification swiped away");

    }
}
