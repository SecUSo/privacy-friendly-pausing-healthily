package orgprivacy_friendly_apps.secuso.privacyfriendlybreakreminder;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class ExerciseTypeActivity extends AppCompatActivity implements View.OnClickListener {

    // FIXME provisorische Form des Strings:
    // "NAME_DES_PROFILS,LAENGE_DER_ARBEITSZEIT,LAENGE_DER_PAUSE,CONTINIOUSLY"

    // FIXME HashSet of Exercises
    // NAME_DES_PROFILS - "UEBUNG1,UE1_LAENGE,UE1_WIEDERHOLUNG,UE1_IMG_NR,UE1_DESCR_NR#UE2....,SEQUENTIAL"

    // FIXME HashSet of Statistical Values
    // NAME_DES_PROFILS - DATUM_DES_ERSTELLENS,ANGEWANDTE_ZEITNAHMEN,UE_ID#ANZAHL_DERANWENDUNGEN,UE_ID2#ANZAHL_DERANWENDUNGEN,...


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exercise_type);

        Button addButton = (Button) findViewById(R.id.button_et_add);
        addButton.setOnClickListener(this);

        Button editButton = (Button) findViewById(R.id.button_et_edit);
        editButton.setOnClickListener(this);

        Button saveButton = (Button) findViewById(R.id.button_et_save);
        saveButton.setOnClickListener(this);

        Button cancelButton = (Button) findViewById(R.id.button_et_cancel);
        cancelButton.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        //TODO
        switch (v.getId()) {
            case R.id.button_et_add:
                System.out.println("Add new Exercise Type!");

                break;

            case R.id.button_et_edit:
                System.out.println("Edit new Exercise Type!");

                break;

            case R.id.button_et_save:
                System.out.println("Save new Exercise Type!");

                break;

            case R.id.button_et_cancel:
                System.out.println("Cancel!");
                finish();
                break;
        }
    }
}
