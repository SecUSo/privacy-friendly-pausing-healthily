package org.secuso.privacyfriendlybreakreminder.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author Christopher Beckmann
 * @version 2.0
 * @since 03.11.2017
 * created 03.11.2017
 */
public class OnBootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if(!"android.intent.action.BOOT_COMPLETED".equals(intent.getAction()))
            return;

        TimerSchedulerReceiver.scheduleNextAlarm(context);
    }
}
