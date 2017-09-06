package org.secuso.privacyfriendlybreakreminder.activities.old;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.secuso.privacyfriendlybreakreminder.R;
import org.secuso.privacyfriendlybreakreminder.activities.old.ExerciseTypeActivity;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private SeekBar interval_seekbar, break_seekbar;
    private TextView interval_text, break_text;
    private String oldExerciseValue = "";
    SharedPreferences sharedPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_profile);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        oldExerciseValue = sharedPrefs.getString("exercise_value", "-1");

        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("exercise_value", "-1");
        editor.apply();

        interval_seekbar = (SeekBar) findViewById(R.id.new_profile_interval);
        interval_seekbar.setProgress(0);
        break_seekbar = (SeekBar) findViewById(R.id.new_profile_break);
        break_seekbar.setProgress(0);

        interval_text = (TextView) findViewById(R.id.interval_text);
        interval_text.setText("1 " + getResources().getText(R.string.settings_unit));
        break_text = (TextView) findViewById(R.id.break_text);
        break_text.setText("1 " + getResources().getText(R.string.settings_unit));

        interval_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                interval_text.setText((progress + 1) + " " + getResources().getText(R.string.settings_unit));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        break_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                break_text.setText((progress + 1) + " " + getResources().getText(R.string.settings_unit));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        Button cancelButton = (Button) findViewById(R.id.button_profile_cancel);
        cancelButton.setOnClickListener(this);

        Button saveButton = (Button) findViewById(R.id.button_profile_save);
        saveButton.setOnClickListener(this);

        Button selectButton = (Button) findViewById(R.id.button_profile_select);
        selectButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_profile_save:
                EditText profileName =
                        (EditText) findViewById(R.id.editProfileName);
                String name = profileName.getText().toString();

                if (name.equals("")) {
                    Toast.makeText(this, R.string.new_profile_emptyName, Toast.LENGTH_SHORT).show();
                    return;
                } else if (prefContainsName(name)) {
                    Toast.makeText(this, R.string.new_profile_doubleName, Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    CheckBox cb_Cont = (CheckBox) findViewById(R.id.continuouslyCB);
                    Boolean cont = cb_Cont.isChecked();
                    // Add to preferences

                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString("name_text", name);
                    editor.putInt("work_value", interval_seekbar.getProgress());
                    editor.putInt("break_value", break_seekbar.getProgress());
                    editor.putString("current_profile", "" + (sharedPrefs.getString("profiles", "").split(";").length));
                    editor.putString("profiles", sharedPrefs.getString("profiles", "") + name + "," + (interval_seekbar.getProgress() + 1) + "," + (break_seekbar.getProgress() + 1) + "," + cont + "," + sharedPrefs.getString("exercise_value", "-1") + ";");
                    editor.apply();
                    finish();

                    break;
                }
            case R.id.button_profile_cancel:
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString("exercise_value", oldExerciseValue);
                editor.apply();
                finish();
                break;

            case R.id.button_profile_select:
                createExerciseType();
                break;
        }
    }


    private boolean prefContainsName(String profileName) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String allProfiles = sharedPrefs.getString("profiles", "");
        String[] profiles = allProfiles.split(";");
        for (String profile : profiles) {
            if (profile.split(",")[0].equalsIgnoreCase(profileName)) {
                return true;
            }
        }

        return false;
    }

    private void createExerciseType() {
        Intent intent = new Intent(this, ExerciseTypeActivity.class);
        this.startActivity(intent);
    }

}
