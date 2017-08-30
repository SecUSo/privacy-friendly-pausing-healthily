package org.secuso.privacyfriendlybreakreminder.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created by badri_000 on 23.08.2016.
 */
public class ExerciseListPreference extends ListPreference implements DialogInterface.OnClickListener {

    Context mContext;
    SharedPreferences sharedPreferences;
    private boolean[] mClickedDialogEntryIndices;
    String[] exercises;

    public ExerciseListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mClickedDialogEntryIndices = new boolean[getEntries().length];
    }

    @Override
    protected View onCreateDialogView() {

        ListView view = new ListView(getContext());
        view.setAdapter(adapter());

        sharedPreferences = getSharedPreferences();
        String exercise = sharedPreferences.getString("exercise_value", "-1");

        exercises = exercise.split("\\.");

        return view;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        CharSequence[] entries = getEntries();
        CharSequence[] entryValues = getEntryValues();

        if (entries == null || entryValues == null || entries.length != entryValues.length) {
            throw new IllegalStateException(
                    "ListPreference requires an entries array and an entryValues array which are both the same length");
        }

        restoreCheckedEntries();
        builder.setMultiChoiceItems(entries, mClickedDialogEntryIndices,
                new DialogInterface.OnMultiChoiceClickListener() {
                    public void onClick(DialogInterface dialog, int which, boolean val) {
                        mClickedDialogEntryIndices[which] = val;
                    }
                });
    }

    private void restoreCheckedEntries() {
        CharSequence[] entryValues = getEntries();

        for (int j = 0; j < exercises.length; j++) {
            String val = exercises[j].trim();
            for (int i = 0; i < entryValues.length; i++) {
                CharSequence entry = entryValues[i];
                if (entry.equals(val)) {
                    mClickedDialogEntryIndices[i] = true;
                    break;
                }
            }
        }

    }

    private ListAdapter adapter() {
        return new ArrayAdapter(getContext(), android.R.layout.select_dialog_multichoice);
    }


    @Override
    protected void onDialogClosed(boolean positiveResult) {
        CharSequence[] entryValues = getEntries();
        String exs = "";
        if (positiveResult && entryValues != null) {
            for (int i = 0; i < entryValues.length; i++) {
                if (mClickedDialogEntryIndices[i]) {
                    exs += entryValues[i] + ".";
                }
            }

        }

        if (exs.equals("") && positiveResult)
            exs = "-1";

        if(positiveResult) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("exercise_value", exs);
            editor.apply();
        }
    }

}
