package org.secuso.privacyfriendlybreakreminder.activities;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import org.secuso.privacyfriendlybreakreminder.R;
import org.secuso.privacyfriendlybreakreminder.activities.helper.BaseActivity;


public class HelpActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_help);
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_help;
    }

    public static class HelpFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref_help);
        }
    }

}
