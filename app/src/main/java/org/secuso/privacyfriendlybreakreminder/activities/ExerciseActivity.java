package org.secuso.privacyfriendlybreakreminder.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import org.secuso.privacyfriendlybreakreminder.R;

public class ExerciseActivity extends AppCompatActivity {

    private ImageView playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        initResources();
    }

    private void initResources() {
        playButton = (ImageView) findViewById(R.id.button_playPause);
    }

    private void updatePlayButton(boolean isRunning) {
        if(isRunning) {
            playButton.setImageResource(R.drawable.ic_pause_black_48dp);
        } else {
            playButton.setImageResource(R.drawable.ic_play_arrow_black_48dp);
        }
    }
}
