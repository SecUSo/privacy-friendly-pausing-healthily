package org.secuso.privacyfriendlybreakreminder.activities;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import org.secuso.privacyfriendlybreakreminder.R;
import org.secuso.privacyfriendlybreakreminder.activities.helper.BaseActivity;
import org.secuso.privacyfriendlybreakreminder.activities.helper.ExpandableListAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Christopher Beckmann
 * @version 2.0
 */
public class HelpActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_help);

        LinkedHashMap<String, List<String>> expandableListDetail = buildData();
        List<String> expandableListTitleGeneral = new ArrayList<String>(expandableListDetail.keySet());

        ExpandableListView generalExpandableListView = (ExpandableListView) findViewById(R.id.generalExpandableListView);
        generalExpandableListView.setAdapter(new ExpandableListAdapter(this, expandableListTitleGeneral, expandableListDetail));

    }

    private LinkedHashMap<String, List<String>> buildData() {
        LinkedHashMap<String, List<String>> expandableListDetail = new LinkedHashMap<>();

        expandableListDetail.put(getString(R.string.help_whatis), Collections.singletonList(getString(R.string.help_whatis_answer)));

        //expandableListDetail.put(getString(R.string.help_feature_one), Collections.singletonList(getString(R.string.help_feature_one_answer)));

        //expandableListDetail.put(getString(R.string.help_privacy), Collections.singletonList(getString(R.string.help_privacy_answer)));

        expandableListDetail.put(getString(R.string.help_permission), Collections.singletonList(getString(R.string.help_permission_answer)));

        return expandableListDetail;
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_help;
    }

}
