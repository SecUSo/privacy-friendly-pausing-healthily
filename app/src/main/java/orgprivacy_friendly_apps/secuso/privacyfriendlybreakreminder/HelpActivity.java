package orgprivacy_friendly_apps.secuso.privacyfriendlybreakreminder;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        setupActionBar();

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
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void clickHandler(View v){
        switch (v.getId()){
            case R.id.imageView:
                setContentView(R.layout.help2);
                ImageView img = (ImageView) findViewById(R.id.img_back);
                img.setImageResource(R.drawable.help_start_screen);
                break;
            case R.id.imageView1:
                setContentView(R.layout.help2);
                img = (ImageView) findViewById(R.id.img_back);
                img.setImageResource(R.drawable.help_settings_screen);
                break;
            case R.id.imageView2:
                setContentView(R.layout.help2);
                img = (ImageView) findViewById(R.id.img_back);
                img.setImageResource(R.drawable.help_create_profile_screen);
                break;
            case R.id.imageView3:
                setContentView(R.layout.help2);
                img = (ImageView) findViewById(R.id.img_back);
                img.setImageResource(R.drawable.help_break_screen);
                break;
            case R.id.imageView4:
                setContentView(R.layout.help2);
                img = (ImageView) findViewById(R.id.img_back);
                img.setImageResource(R.drawable.help_general_screen);
                break;
            case R.id.imageView5:
                setContentView(R.layout.help2);
                img = (ImageView) findViewById(R.id.img_back);
                img.setImageResource(R.drawable.help_notification_screen);
                break;
            case R.id.img_back:
                setContentView(R.layout.help);
                break;
            default:

        }
    }

}
