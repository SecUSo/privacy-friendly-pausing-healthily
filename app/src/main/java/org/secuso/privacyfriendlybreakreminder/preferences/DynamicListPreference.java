package org.secuso.privacyfriendlybreakreminder.preferences;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.secuso.privacyfriendlybreakreminder.R;


public class DynamicListPreference extends ListPreference implements DialogInterface.OnClickListener {

    Context mContext;
    SharedPreferences sharedPreferences;

    public DynamicListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected View onCreateDialogView() {
        ListView view = new ListView(getContext());
        view.setAdapter(adapter());
        setEntries(entries());
        setEntryValues(entryValues());
        setValueIndex(Integer.parseInt(sharedPreferences.getString("current_profile", "1")));
        setPositiveButtonText(mContext.getString(R.string.dialog_positive));
        return view;
    }


    private ListAdapter adapter() {
        return new ArrayAdapter(getContext(), android.R.layout.select_dialog_singlechoice);
    }

    private CharSequence[] entries() {
        //action to provide entry data in char sequence array for list

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String[] allProfiles = sharedPreferences.getString("profiles", "").split(";");
        CharSequence[] entries = new CharSequence[allProfiles.length];

        for (int i = 0; i < allProfiles.length; i++) {
            String profileName = allProfiles[i].split(",")[0];
            entries[i] = profileName;
            if (profileName.equals(sharedPreferences.getString("name_text", ""))) {
                editor.putString("current_profile", "" + i);
                System.out.println("My current index: "+i);
            }
        }
        editor.apply();
        return entries;
    }

    private CharSequence[] entryValues() {
        //action to provide entry data in char sequence array for list

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String[] allProfiles = sharedPreferences.getString("profiles", "").split(";");
        CharSequence[] entries = new CharSequence[allProfiles.length];
        for (int i = 0; i < allProfiles.length; i++) {
            String profileName = allProfiles[i].split(",")[0];
            entries[i] = "" + i;
        }
        return entries;
    }



}
