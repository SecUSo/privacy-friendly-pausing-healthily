package org.secuso.privacyfriendlybreakreminder.activities;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.secuso.privacyfriendlybreakreminder.R;

import java.util.ArrayList;

public class ExerciseTypeActivity extends AppCompatActivity implements View.OnClickListener {

    Spinner typeSpinner;
    ListView listView;
    ArrayList<String> adapter;
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exercise_type);

        Button addButton = (Button) findViewById(R.id.button_et_add);
        addButton.setOnClickListener(this);

        Button saveButton = (Button) findViewById(R.id.button_et_save);
        saveButton.setOnClickListener(this);

        Button cancelButton = (Button) findViewById(R.id.button_et_cancel);
        cancelButton.setOnClickListener(this);

        typeSpinner = (Spinner) findViewById(R.id.type_spinner);
        listView = (ListView) findViewById(R.id.listView);

        adapter = new ArrayList<String>();
        listView.setAdapter(new ArrayAdapter<String>(this, R.layout.spinner_layout, adapter));
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.button_et_add:
                if(!adapter.contains((String)typeSpinner.getSelectedItem())){
                    adapter.add((String)typeSpinner.getSelectedItem());
                    listView.setAdapter(new ArrayAdapter<String>(this, R.layout.spinner_layout, adapter));
                }
                break;

            case R.id.button_et_save:
                sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPrefs.edit();

                String exerciseTypes = "";
                for (int i = 0;i<adapter.size();i++){
                    exerciseTypes += adapter.get(i) + ".";
                }
                editor.putString("exercise_value",exerciseTypes);
                editor.apply();

                finish();

                Toast.makeText(this, R.string.new_profile_success, Toast.LENGTH_LONG).show();

                break;

            case R.id.button_et_cancel:
                finish();
                break;
        }
    }
}
