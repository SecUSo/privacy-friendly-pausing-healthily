package org.secuso.privacyfriendlybreakreminder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.secuso.privacyfriendlybreakreminder.activities.tutorial.PrefManager;
import org.secuso.privacyfriendlybreakreminder.activities.tutorial.TutorialActivity;

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

        PrefManager firstStartPref = new PrefManager(this);

        if(firstStartPref.isFirstTimeLaunch()) {
            mainIntent = new Intent(this, TutorialActivity.class);
        } else {
            mainIntent = new Intent(this, TimerActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }

        SplashActivity.this.startActivity(mainIntent);
        SplashActivity.this.finish();
    }

}
