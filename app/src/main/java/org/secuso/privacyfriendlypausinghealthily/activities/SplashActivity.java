package org.secuso.privacyfriendlypausinghealthily.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.secuso.privacyfriendlypausinghealthily.activities.tutorial.FirstLaunchManager;
import org.secuso.privacyfriendlypausinghealthily.activities.tutorial.TutorialActivity;

/**
 * @author yonjuni
 * @version 1.0
 * @since 22.10.16
 * created 22.10.16
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent mainIntent = null;

        FirstLaunchManager firstStartPref = new FirstLaunchManager(this);

        if(firstStartPref.isFirstTimeLaunch()) {
            firstStartPref.initFirstTimeLaunch();
            mainIntent = new Intent(this, TutorialActivity.class);
        } else {
            mainIntent = new Intent(this, TimerActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }

        SplashActivity.this.startActivity(mainIntent);
        SplashActivity.this.finish();
    }

}
