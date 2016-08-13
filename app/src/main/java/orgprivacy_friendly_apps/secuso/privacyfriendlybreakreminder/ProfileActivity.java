package orgprivacy_friendly_apps.secuso.privacyfriendlybreakreminder;


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

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private SeekBar interval_seekbar, break_seekbar;
    private TextView interval_text, break_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_profile);

        interval_seekbar = (SeekBar) findViewById(R.id.new_profile_interval);
        interval_seekbar.setProgress(1);
        break_seekbar = (SeekBar) findViewById(R.id.new_profile_break);
        break_seekbar.setProgress(1);

        interval_text = (TextView) findViewById(R.id.interval_text);
        interval_text.setText("1 Minutes");
        break_text = (TextView) findViewById(R.id.break_text);
        break_text.setText("1 Minutes");

        interval_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                interval_text.setText(progress + " Minutes");
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
                break_text.setText(progress + " Minutes");
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
                System.out.println("Save new profile!");
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
                    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString("name_text", name);
                    editor.putInt("work_value", interval_seekbar.getProgress());
                    editor.putInt("break_value", break_seekbar.getProgress());
                    System.out.println("BoolValue: "+cont);
                    editor.putString("profiles", sharedPrefs.getString("profiles", "") + name + "," + interval_seekbar.getProgress() + "," + break_seekbar.getProgress() + "," + cont + ";");
                    editor.apply();
                    finish();
                    break;
                }
            case R.id.button_profile_cancel:
                System.out.println("New profile canceled!");
                finish();
                break;

            case R.id.button_profile_select:
                System.out.println("Select Exercise Type!");
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
        System.out.println("Exercise Type Activity!");
    }

}
