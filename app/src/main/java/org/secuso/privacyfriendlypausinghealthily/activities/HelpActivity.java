package org.secuso.privacyfriendlypausinghealthily.activities;

import android.os.Bundle;
import android.widget.ExpandableListView;

import org.secuso.privacyfriendlypausinghealthily.R;
import org.secuso.privacyfriendlypausinghealthily.activities.helper.BaseActivity;
import org.secuso.privacyfriendlypausinghealthily.activities.helper.ExpandableListAdapter;

import java.util.ArrayList;
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

        expandableListDetail.put(getString(R.string.help_create_sets), Collections.singletonList(getString(R.string.help_create_sets_answer)));

        expandableListDetail.put(getString(R.string.help_select_exercises), Collections.singletonList(getString(R.string.help_select_exercises_answer)));

        expandableListDetail.put(getString(R.string.help_permission), Collections.singletonList(getString(R.string.help_permission_answer)));

        return expandableListDetail;
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_help;
    }

}
