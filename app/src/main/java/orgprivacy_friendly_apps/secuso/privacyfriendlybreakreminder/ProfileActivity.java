package orgprivacy_friendly_apps.secuso.privacyfriendlybreakreminder;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_profile);

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
                //Fixme Add new Profile to the Array of Profiles (doesn´t work with xml because it is not possible to add values dynamically to any part of resources)
                System.out.println("Save new profile!");
//                Spinner profileSpinner = (Spinner) findViewById(R.id.spinner);
//                String[] array = getResources().getStringArray(R.array.profile_entries);
//                List<String> stringList = new ArrayList<String>(Arrays.asList(array));
//                EditText profileName =
//                        (EditText) findViewById(R.id.editProfileName);
//                stringList.add(4, profileName.getText().toString());
//                ArrayAdapter<String> adapter = new
//                        ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, stringList);
//                profileSpinner.setAdapter(adapter);

                finish();
                break;

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


    private void createExerciseType() {
        Intent intent = new Intent(this, ExerciseTypeActivity.class);
        this.startActivity(intent);
        System.out.println("Exercise Type Activity!");
    }
}
