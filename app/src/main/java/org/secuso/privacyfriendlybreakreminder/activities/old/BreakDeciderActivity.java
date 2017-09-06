package org.secuso.privacyfriendlybreakreminder.activities.old;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.secuso.privacyfriendlybreakreminder.R;
import org.secuso.privacyfriendlybreakreminder.activities.old.BreakActivity;

public class BreakDeciderActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_break_decider);

        Button skipButton = (Button)findViewById(R.id.button_skip);
        skipButton.setOnClickListener(this);
        Button breakButton = (Button)findViewById(R.id.button_break);
        breakButton.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_skip:
                finish();
                break;
            case R.id.button_break:
                finish();
                Intent intent = new Intent(this, BreakActivity.class);
                this.startActivity(intent);
                break;
        }
    }
}
