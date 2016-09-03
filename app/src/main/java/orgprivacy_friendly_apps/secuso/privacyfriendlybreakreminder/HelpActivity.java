package orgprivacy_friendly_apps.secuso.privacyfriendlybreakreminder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class HelpActivity extends AppCompatActivity {

    boolean isInNextView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_overview);
        setupActionBar();

        final ListView lv = (ListView) findViewById(R.id.listView2);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
                String selectedFromList =(String) (lv.getItemAtPosition(myItemInt));
                isInNextView = true;

                setContentView(R.layout.help_content);
                TextView title = (TextView)  findViewById(R.id.titleText);
                TextView description = (TextView)  findViewById(R.id.descriptionText);
                ImageView img = (ImageView) findViewById(R.id.img);

                switch (myItemInt){
                    case 0:
                        img.setImageResource(R.drawable.help_start_screen);
                        title.setText(R.string.help_start_screen_title);
                        description.setText(R.string.help_start_screen);
                        break;
                    case 1:
                        img.setImageResource(R.drawable.help_settings_screen);
                        title.setText(R.string.help_settings_screen_title);
                        description.setText(R.string.help_settings_screen);
                        break;
                    case 2:
                        img.setImageResource(R.drawable.help_create_profile_screen);
                        title.setText(R.string.help_settings_screen_title);
                        description.setText(R.string.help_settings_screen1);
                        break;
                    case 3:
                        img.setImageResource(R.drawable.help_notification_screen);
                        title.setText(R.string.help_settings_screen_title);
                        description.setText(R.string.help_settings_screen2);
                        break;
                    case 4:
                        img.setImageResource(R.drawable.help_create_profile_screen);
                        title.setText(R.string.help_create_profile_screen_title);
                        description.setText(R.string.help_create_profile_screen);
                        break;
                    case 5:
                        img.setImageResource(R.drawable.help_break_screen);
                        title.setText(R.string.help_break_screen_title);
                        description.setText(R.string.help_break_screen);
                        break;
                    case 6:
                        title.setText(R.string.disclaimer);
                        img.setImageResource(R.drawable.ic_law);
                        description.setText(R.string.disclaimer);
                        break;
                    default:

                }
            }
        });
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.help);
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                if(isInNextView){
                    isInNextView = false;
                    finish();
                    Intent intent = new Intent(this, HelpActivity.class);
                    startActivity(intent);
                    return true;
                }

                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
