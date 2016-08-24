package orgprivacy_friendly_apps.secuso.privacyfriendlybreakreminder;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class BreakActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView ct_text;
    private CountDownTimer ct;
    private String stopTime = "";
    private boolean isRunning = false;
    private List<Exercise> exerciseList;
    private SharedPreferences sharedPrefs;
    private TextView description, side_repetition, break_exercise_type;
    private int currentExercise, breakTime = 0;
    private ImageView image, image_mid, image_right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_break);

        currentExercise = 0;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int mins = sharedPrefs.getInt("break_value", 5);
        String bufferZeroMinute = "";

        if (mins < 10)
            bufferZeroMinute = "0";

        ct_text = (TextView) findViewById(R.id.textViewBreak);
        ct_text.setText(bufferZeroMinute + mins + ":00");

        Button skipButton = (Button) findViewById(R.id.button_next);
        skipButton.setOnClickListener(this);
        ct_text.setOnClickListener(this);


//        LinearLayout ll = (LinearLayout) findViewById(R.id.layout_break);
//        // TODO Do it dynamically
//        for (int i = 0; i < 20; i++) {
//            ImageView image = new ImageView(this);
//            image.setLayoutParams(new android.view.ViewGroup.LayoutParams(80, 60));
//            image.setImageResource(R.drawable.statistic_logo);
//            ll.addView(image);
//        }

        DBHandler dbHandler = new DBHandler(this);
        String[] allProfiles = sharedPrefs.getString("profiles", "").split(";");
        String currentProfile = sharedPrefs.getString("name_text", "");
        String[] exercises = new String[8];
        for (int i = 0; i < allProfiles.length; i++) {
            if (allProfiles[i].split(",")[0].equals(currentProfile)) {
                System.out.println("Hi: " + allProfiles[i].split(",")[4]);
                exercises = allProfiles[i].split(",")[4].split("\\.");
            }
        }
        exerciseList = dbHandler.getExercisesFromSection(exercises[currentExercise]);
        description = (TextView) findViewById(R.id.textViewDescription);
        description.setText(exerciseList.get(currentExercise).getDescription());

        side_repetition = (TextView) findViewById(R.id.textSideRepetition);
        side_repetition.setText("Break");

        break_exercise_type = (TextView) findViewById(R.id.break_exercise_type);
        break_exercise_type.setText(exerciseList.get(currentExercise).getSection());

        image = (ImageView) findViewById(R.id.imageView);
        image_mid = (ImageView) findViewById(R.id.imageMid);
        image_right = (ImageView) findViewById(R.id.imageRight);
        image.setImageResource(R.drawable.train_left);
        image_right.setImageResource(R.drawable.train_right);
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

                            //FIXME 10 Sec break -> 10 + 10 exercise -> 10 sec break
                            update();

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

                            //FIXME Test Vibration
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

            case R.id.button_next:
                //TODO Update Timer
                currentExercise++;
                System.out.println("Current Ex#: " + currentExercise + " , All Ex#: " + exerciseList.size());
                side_repetition.setText("Break");
                if (currentExercise > exerciseList.size() - 1)
                    currentExercise = 0;

                switch (currentExercise % 3) {
                    case 0:
                        image.setImageResource(R.drawable.train_left);
                        image_mid.setImageResource(android.R.color.transparent);
                        image_right.setImageResource(R.drawable.train_right);
                        break;

                    case 1:
                        image.setImageResource(android.R.color.transparent);
                        image_mid.setImageResource(R.drawable.train_left);
                        image_right.setImageResource(android.R.color.transparent);
                        break;

                    case 2:
                        image.setImageResource(android.R.color.transparent);
                        image_mid.setImageResource(R.drawable.train_right);
                        image_right.setImageResource(android.R.color.transparent);
                        break;
                }

                description.setText(exerciseList.get(currentExercise).getDescription());

                //finish();
                break;
        }
    }

    private void update() {
        breakTime++;

        switch (breakTime) {
            case 10:
                System.out.println("Time for Exercise: Left!");
                image_mid.setImageResource(R.drawable.train_left);
                image_right.setImageResource(android.R.color.transparent);
                image.setImageResource(android.R.color.transparent);
                side_repetition.setText("Side/Repetition 1");
                break;
            case 20:
                System.out.println("Time for Exercise: Right!");
                image_mid.setImageResource(R.drawable.train_right);
                side_repetition.setText("Side/Repetition 2");
                break;
            case 30:
                System.out.println("Next Exercise!");
                image.setImageResource(R.drawable.train_left);
                image_mid.setImageResource(android.R.color.transparent);
                image_right.setImageResource(R.drawable.train_right);
                breakTime = 0;
                currentExercise++;
                description.setText(exerciseList.get(currentExercise).getDescription());
                side_repetition.setText("Break");

        }
    }
}
