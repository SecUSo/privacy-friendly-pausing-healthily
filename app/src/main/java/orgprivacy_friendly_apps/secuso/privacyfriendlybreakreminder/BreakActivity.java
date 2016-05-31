package orgprivacy_friendly_apps.secuso.privacyfriendlybreakreminder;

import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BreakActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView ct_text;
    private CountDownTimer ct;
    private String stopTime = "";
    private boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_break);



        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int mins = sharedPrefs.getInt("break_value", 5);
        String bufferZeroMinute = "";

        if(mins < 10)
            bufferZeroMinute = "0";

        ct_text =(TextView)findViewById(R.id.textViewBreak);
        ct_text.setText(bufferZeroMinute+mins+":00");

        Button playStopButton = (Button)findViewById(R.id.button_playStopBreak);
        playStopButton.setOnClickListener(this);
        Button resetButton = (Button)findViewById(R.id.button_cancel);
        resetButton.setOnClickListener(this);
        ct_text.setOnClickListener(this);


        LinearLayout ll = (LinearLayout)findViewById(R.id.layout_break);
        // TODO Do it dynamically
        for (int i = 0; i < 20; i++){
            ImageView image = new ImageView(this);
            image.setLayoutParams(new android.view.ViewGroup.LayoutParams(80,60));
            image.setImageResource(R.drawable.statistic_logo);
            ll.addView(image);
        }


    }

    public void onClick(View v) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int mins = sharedPrefs.getInt("break_value", 10);
        String bufferZeroMinute = "";
        String bufferZeroSecond = "";
        int time = mins * 60 * 1000;
        int oldTime = time;

        if(stopTime == "" && !isRunning) {
            if (time / 1000 / 60 < 10)
                bufferZeroMinute = "0";

            ct_text.setText(bufferZeroMinute + time / 1000 / 60 + ":00");
        } else if(!isRunning){
            ct_text.setText(stopTime);
            String stringTime = (String)ct_text.getText();
            String[] timef = stringTime.split(":");
            int minute = Integer.parseInt(timef[0]);
            int second = Integer.parseInt(timef[1]);
            System.out.println("Minute: "+ minute + "  Second: "+second);
            time = (1000 * (minute*60)) + (1000 * second);

            if(minute < 10)
                bufferZeroMinute = "0";
            if(second < 10)
                bufferZeroSecond = "0";

            ct_text.setText(bufferZeroMinute + minute + ":" + bufferZeroSecond + second);
        }


        System.out.println(time +" "+ ct_text.getText());



        switch (v.getId()) {

            case R.id.textViewBreak:
            case R.id.button_playStopBreak:
                if (isRunning) {
                    ct.cancel();
                    stopTime = (String) ct_text.getText();
                    isRunning = false;
                } else {
                    ct = new CountDownTimer(time, 1000) {

                        public void onTick(long millisUntilFinished) {
                            String bufferZeroMinute = "";
                            String bufferZeroSecond = "";

                            if ((millisUntilFinished / 1000) / 60 < 10)
                                bufferZeroMinute = "0";

                            if (millisUntilFinished / 1000 % 60 < 10)
                                bufferZeroSecond = "0";

                            ct_text.setText(bufferZeroMinute + (millisUntilFinished / 1000) / 60 + ":" + bufferZeroSecond + millisUntilFinished / 1000 % 60);
                        }

                        public void onFinish() {
                            isRunning = false;
                            ct_text.setText("00:00");
                            //TODO trigger the alarm
                            finish();
                            //startBreak();
                        }
                    }.start();
                    isRunning = true;
                }
                break;

            case R.id.button_cancel:
                finish();
                break;
        }
    }
}
