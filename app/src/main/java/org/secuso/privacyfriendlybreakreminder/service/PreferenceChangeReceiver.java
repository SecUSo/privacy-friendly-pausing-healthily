package org.secuso.privacyfriendlybreakreminder.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Map;
import java.util.Set;

import static org.secuso.privacyfriendlybreakreminder.activities.tutorial.PrefManager.PREF_EXERCISE_CONTINUOUS;

/**
 * @author Christopher Beckmann
 * @version 1.0
 * @since 02.11.2017
 * created 02.11.2017
 */

public class PreferenceChangeReceiver extends BroadcastReceiver {

    private static final String TAG = PreferenceManager.class.getSimpleName();

    public static final String ACTION_PREF_CHANGE = "org.secuso.privacyfriendlybreakreminder.ACTION_PREF_CHANGE";
    public static final String EXTRA_DISABLE_CONTINUOUS = "EXTRA_DISABLE_CONTINUOUS";

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent == null) return;

        Bundle bundle = intent.getExtras();

        if(bundle == null) return;

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
//        Map<String, ?> prefMap = pref.getAll();

        for(String key : intent.getExtras().keySet()) {

            if(EXTRA_DISABLE_CONTINUOUS.equals(key)) {
                pref.edit().putBoolean(PREF_EXERCISE_CONTINUOUS, false).apply();
            }

//            if(prefMap.containsKey(key)) {
//
//                Object bundleValue = bundle.get(key);
//
//                if(prefMap.get(key).getClass().isInstance(bundleValue)) {
//                    if(bundleValue instanceof String) {
//                        pref.edit().putString(key, (String)bundleValue).apply();
//                    } else if(bundleValue instanceof Boolean) {
//                        pref.edit().putBoolean(key, (Boolean)bundleValue).apply();
//                    }
//                }
//            }
        }

    }
}
