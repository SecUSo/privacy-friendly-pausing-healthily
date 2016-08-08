package orgprivacy_friendly_apps.secuso.privacyfriendlybreakreminder;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

        if (mins < 10)
            bufferZeroMinute = "0";

        ct_text = (TextView) findViewById(R.id.textViewBreak);
        ct_text.setText(bufferZeroMinute + mins + ":00");

        Button playStopButton = (Button) findViewById(R.id.button_playStopBreak);
        playStopButton.setOnClickListener(this);
        Button resetButton = (Button) findViewById(R.id.button_cancel);
        resetButton.setOnClickListener(this);
        ct_text.setOnClickListener(this);


        LinearLayout ll = (LinearLayout) findViewById(R.id.layout_break);
        // TODO Do it dynamically
        for (int i = 0; i < 20; i++) {
            ImageView image = new ImageView(this);
            image.setLayoutParams(new android.view.ViewGroup.LayoutParams(80, 60));
            image.setImageResource(R.drawable.statistic_logo);
            ll.addView(image);
        }


    }

    public void onClick(View v) {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int mins = sharedPrefs.getInt("break_value", 10);
        String bufferZeroMinute = "";
        String bufferZeroSecond = "";
        int time = mins * 60 * 1000;
        int oldTime = time;

        if (stopTime == "" && !isRunning) {
            if (time / 1000 / 60 < 10)
                bufferZeroMinute = "0";

            ct_text.setText(bufferZeroMinute + time / 1000 / 60 + ":00");
        } else if (!isRunning) {
            ct_text.setText(stopTime);
            String stringTime = (String) ct_text.getText();
            String[] timef = stringTime.split(":");
            int minute = Integer.parseInt(timef[0]);
            int second = Integer.parseInt(timef[1]);
            System.out.println("Minute: " + minute + "  Second: " + second);
            time = (1000 * (minute * 60)) + (1000 * second);

            if (minute < 10)
                bufferZeroMinute = "0";
            if (second < 10)
                bufferZeroSecond = "0";

            ct_text.setText(bufferZeroMinute + minute + ":" + bufferZeroSecond + second);

        }


        System.out.println(time + " " + ct_text.getText());


        switch (v.getId()) {

            case R.id.textViewBreak:
            case R.id.button_playStopBreak:
                if (isRunning) {
                    ct.cancel();
                    stopTime = (String) ct_text.getText();
                    isRunning = false;
                } else {
                    ct = new CountDownTimer(time, 1000) {
                        boolean timeLeft = false;

                        public void onTick(long millisUntilFinished) {
                            String bufferZeroMinute = "";
                            String bufferZeroSecond = "";

                            if ((millisUntilFinished / 1000) / 60 < 10)
                                bufferZeroMinute = "0";

                            if (millisUntilFinished / 1000 % 60 < 10)
                                bufferZeroSecond = "0";

                            ct_text.setText(bufferZeroMinute + (millisUntilFinished / 1000) / 60 + ":" + bufferZeroSecond + millisUntilFinished / 1000 % 60);

                            //Show how much time is left
                            timeLeft = sharedPrefs.getBoolean("notifications_new_message_timeLeft", false);
                            if (timeLeft) {
                                Notification notification = new NotificationCompat.Builder(getApplicationContext()).setCategory(Notification.CATEGORY_MESSAGE)
                                        .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                                        .setContentTitle("Break Activity Reminder: ")
                                        .setContentText(((millisUntilFinished / 1000) / 60) + "Minutes and " + (millisUntilFinished / 1000 % 60) + " seconds")
                                        .setAutoCancel(true)
                                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).build();
                                NotificationManager notificationManager =
                                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                notificationManager.notify(999, notification);
                            }
                        }

                        public void onFinish() {
                            isRunning = false;
                            ct_text.setText("00:00");
                            //Trigger the alarm
                            String ringPref = sharedPrefs.getString("notifications_new_message_ringtone", "");
                            System.out.println("Sound: " + ringPref);
                            if (!ringPref.equals("")) {
                                System.out.println("-----------------PLAY NOTIFICATION SOUND----------------");
                                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), Uri.parse(ringPref));
                                r.play();
                            }

                            //FIXME Vibrate
                            boolean vibrateChecked = sharedPrefs.getBoolean("notifications_new_message_vibrate", false);
                            System.out.println("Vibrate is : " + vibrateChecked);
                            if (vibrateChecked) {
                                // Get instance of Vibrator from current Context
                                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                                if (v == null) {
                                    System.out.println("No vibrator! :D");
                                } else {
                                    // Vibrate for 3000 milliseconds
                                    System.out.println("Vibrate for 3000 ms");
                                    v.vibrate(3000);
                                }
                            }

                            //Cancel the notification
                            if (timeLeft) {
                                NotificationManager notificationManager =
                                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                notificationManager.cancel(999);
                            }
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
