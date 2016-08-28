package orgprivacy_friendly_apps.secuso.privacyfriendlybreakreminder;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.Random;

import java.util.ArrayList;
import java.util.List;

public class BreakActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView ct_text;
    private CountDownTimer ct;
    private String stopTime = "", sideRepetition = "";
    String image1, image2;
    private boolean isRunning = false;
    private List<Exercise> exerciseList;
    private SharedPreferences sharedPrefs;
    private TextView description, side_repetition, break_exercise_type, execution;
    private int currentExercise, breakTime = 0, currentExerciseSection;
    private ImageView image;
    private String[] exercises;
    private DBHandler dbHandler;
    private List<List<Exercise>> allAvailableExercises;
    private List<Integer> sections;
    private Random random;
    private boolean exerciseSide = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentExercise = 0;
        currentExerciseSection = 0;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int mins = sharedPrefs.getInt("break_value", 5);
        String bufferZeroMinute = "";

        if (mins < 10)
            bufferZeroMinute = "0";

        String[] allProfiles = sharedPrefs.getString("profiles", "").split(";");
        String currentProfile = sharedPrefs.getString("name_text", "");

        for (int i = 0; i < allProfiles.length; i++) {
            if (allProfiles[i].split(",")[0].equals(currentProfile) && !allProfiles[i].split(",")[4].equals("-1")) {
                exercises = allProfiles[i].split(",")[4].split("\\.");
            }
        }

        if (exercises == null) {
            setContentView(R.layout.activity_break_no_exercises);
            Button cancelButton = (Button) findViewById(R.id.button_cancel);
            cancelButton.setOnClickListener(this);
            ct_text = (TextView) findViewById(R.id.textViewBreak1);

            ct_text.setText(bufferZeroMinute + mins + ":00");
            ct_text.setOnClickListener(this);
        } else {
            setContentView(R.layout.activity_break);
            Button nextButton = (Button) findViewById(R.id.button_next);
            nextButton.setOnClickListener(this);
            ct_text = (TextView) findViewById(R.id.textViewBreak);
            ct_text.setText(bufferZeroMinute + mins + ":00");
            ct_text.setOnClickListener(this);

            dbHandler = new DBHandler(this);
            random = new Random();
            sections = new ArrayList<>();
            setRandomExercises();
        }

        //Keep screen on while on break
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void onClick(View v) {
        int mins = sharedPrefs.getInt("break_value", 10);
        String bufferZeroMinute = "";
        String bufferZeroSecond = "";
        int time = mins * 60 * 1000;

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

        switch (v.getId()) {

            case R.id.textViewBreak1:
            case R.id.textViewBreak:
                if (isRunning) {
                    ct.cancel();
                    stopTime = (String) ct_text.getText();
                    isRunning = false;
                } else {
                    startTimer(time);
                }
                break;


            case R.id.button_cancel:
                if (ct != null)
                    ct.cancel();
                finish();
                break;

            case R.id.button_next:

                // Next Exercise
                currentExercise++;
                side_repetition.setText(R.string.exercise_break);
                if (currentExercise > exerciseList.size() - 1) {
                    currentExercise = 0;
                    if (sections.size() == allAvailableExercises.size()) {
                        System.out.println("Did all exercises, restart!");
                        sections = new ArrayList<>();
                    }
                    while (true) {

                        currentExerciseSection = currentExerciseSection + 1 % allAvailableExercises.size();
                        if (!sections.contains(currentExerciseSection)) {
                            sections.add(currentExerciseSection);
                            exerciseList = allAvailableExercises.get(currentExerciseSection);
                            System.out.println("Random id for section election: " + currentExerciseSection);
                            break;
                        }

                    }
                }

                //Set description and execution text of current exercise
                description.setText(exerciseList.get(currentExercise).getDescription());
                execution.setText(exerciseList.get(currentExercise).getExecution());

                setExerciseImage();

                //Update Timer
                String[] currentTime = ((String) ct_text.getText()).split(":");
                int minute = Integer.parseInt(currentTime[0]);
                int second = Integer.parseInt(currentTime[1]);

                if (second != 0) {
                    ct.cancel();
                    breakTime = 0;
                    if (minute == 0 && second > 0) {
                        minute = 1;
                        second = 0;
                    } else if (minute > 0 && second > 0) {
                        minute++;
                        second = 0;
                    }
                    if (minute < 10)
                        bufferZeroMinute = "0";
                    if (second < 10)
                        bufferZeroSecond = "0";


                    System.out.println("New Time: " + bufferZeroMinute + minute + ":" + bufferZeroSecond + second);
                    if (isRunning) {
                        time = minute * 60 * 1000;
                        startTimer(time);
                    } else {
                        stopTime = bufferZeroMinute + minute + ":" + bufferZeroSecond + second;
                        ct_text.setText(stopTime);
                    }
                }

                break;
        }
    }


    private void setRandomExercises() {

        allAvailableExercises = new ArrayList<>();
        System.out.println("Number of sections: " + exercises.length);


        String usedSectionsString = sharedPrefs.getString("currently_done_exercises", "");
        System.out.println("Number of used sections " + usedSectionsString.split("\\.").length + "  " + usedSectionsString);
        SharedPreferences.Editor editor = sharedPrefs.edit();


        if(exercises.length <= usedSectionsString.split("\\.").length) {
            usedSectionsString = "";
        }

        //Selection of the Section
        boolean notFoundYet = true;

        while(notFoundYet){
            currentExerciseSection = random.nextInt(exercises.length);
            if(!usedSectionsString.contains(exercises[currentExerciseSection])){
                List<Exercise> list = dbHandler.getExercisesFromSection(exercises[currentExerciseSection]);
                allAvailableExercises.add(list);
                usedSectionsString += exercises[currentExerciseSection] + ".";
                editor.putString("currently_done_exercises", usedSectionsString);
                notFoundYet = false;
                System.out.println("Section: " + exercises[currentExerciseSection] + " and number of ex for it: " + list.size());
            }
        }

        editor.apply();

        currentExerciseSection = 0;
        Collections.shuffle(allAvailableExercises.get(currentExerciseSection));

        String allExe = "";
        for (int i = 0; i< allAvailableExercises.get(currentExerciseSection).size();i++)
            allExe += allAvailableExercises.get(currentExerciseSection).get(i).getImageID()+ " ";

        System.out.println("Random list for section election: " + allExe);

        // Set exercise list to current section
        exerciseList = allAvailableExercises.get(currentExerciseSection);
        sections.add(currentExerciseSection);

        description = (TextView) findViewById(R.id.textViewDescription);
        description.setText(exerciseList.get(currentExercise).getDescription());

        execution = (TextView) findViewById(R.id.textViewExecution);
        execution.setText(exerciseList.get(currentExercise).getExecution());

        side_repetition = (TextView) findViewById(R.id.textSideRepetition);
        side_repetition.setText(R.string.exercise_break);

        break_exercise_type = (TextView) findViewById(R.id.break_exercise_type);
        break_exercise_type.setText(exerciseList.get(currentExercise).getSection());

        //FIXME
        setExerciseImage();
    }

    private void setExerciseImage() {
        String imageID = exerciseList.get(currentExercise).getImageID();
        image = (ImageView) findViewById(R.id.imageMid);
        if (imageID.split(",").length == 1) {
            sideRepetition = getResources().getText(R.string.exercise_repetition).toString();

            image1 = imageID;
            exerciseSide = false;
            int imageResID = getResources().getIdentifier("exercise_"+image1, "drawable", getPackageName());
            image.setImageResource(imageResID);
        } else {
            // There are 2 sides for an exercise
            exerciseSide = true;
            sideRepetition = getResources().getText(R.string.exercise_side).toString();
            image1 = imageID.split(",")[0];
            image2 = imageID.split(",")[1];
            System.out.println("Id of first image: " + image1 + " , id of second: " + image2);

            //image ID from Resource
            int imageResID = getResources().getIdentifier("exercise_"+image1, "drawable", getPackageName());
            image.setImageResource(imageResID);

        }
    }

    //FIXME Change to the correct picture and whether its side or repetition
    private void update() {
        //After 10 seconds first side/repetition, then after 20 seconds break for 10 seconds, afterwards second side/repetition and after 20 seconds break and new exercise
        breakTime++;
        switch (breakTime) {
            case 10:
                System.out.println("Time for Exercise: Left!");
                side_repetition.setText(sideRepetition + " 1");
                break;
            case 30:
                System.out.println("Time for Break between sides!");
                side_repetition.setText(R.string.exercise_break);
                //If exercise contains 2 images, set ImageView to the second image
                if (exerciseSide) {
                    int imageResID = getResources().getIdentifier("exercise_"+image2, "drawable", getPackageName());
                    image.setImageResource(imageResID);
                }
                break;
            case 40:
                System.out.println("Time for Exercise: Right!");
                side_repetition.setText(sideRepetition + " 2");
                break;
            case 60:
                System.out.println("Next Exercise!");
                breakTime = 0;
                currentExercise++;
                if (currentExercise > exerciseList.size() - 1) {
                    currentExercise = 0;
                    if (sections.size() == allAvailableExercises.size()) {
                        System.out.println("Did all exercises, restart!");
                        sections = new ArrayList<>();
                    }
                    while (true) {

                        currentExerciseSection = currentExerciseSection + 1 % allAvailableExercises.size();
                        if (!sections.contains(currentExerciseSection)) {
                            sections.add(currentExerciseSection);
                            exerciseList = allAvailableExercises.get(currentExerciseSection);
                            System.out.println("Random id for section election: " + currentExerciseSection);
                            break;
                        }

                    }
                }
                description.setText(exerciseList.get(currentExercise).getDescription());
                execution.setText(exerciseList.get(currentExercise).getExecution());
                side_repetition.setText(R.string.exercise_break);

                setExerciseImage();
                break;
        }
    }

    private void startTimer(int time) {

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

                // Update image and description of the exercise
                update();
            }

            public void onFinish() {
                isRunning = false;
                ct_text.setText("00:00");
                //Trigger the alarm
                String ringPref = sharedPrefs.getString("notifications_new_message_ringtone", "");

                if (!ringPref.equals("")) {
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), Uri.parse(ringPref));
                    r.play();
                }

                //Vibration
                boolean vibrateChecked = sharedPrefs.getBoolean("notifications_new_message_vibrate", false);
                if (vibrateChecked) {
                    // Get instance of Vibrator from current Context
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                    if (v != null) {
                        // Vibrate for 1500 milliseconds
                        v.vibrate(1500);
                    }
                }

                //Cancel the notification
                if (timeLeft) {
                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.cancel(999);
                }
                //Remove lag to keep screen on when the break ends
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                //Close database connection
                dbHandler.close();
                finish();
            }
        }.start();
        isRunning = true;
    }
}
