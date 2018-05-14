package org.secuso.privacyfriendlybreakreminder.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.secuso.privacyfriendlybreakreminder.R;
import org.secuso.privacyfriendlybreakreminder.activities.tutorial.FirstLaunchManager;
import org.secuso.privacyfriendlybreakreminder.database.SQLiteHelper;
import org.secuso.privacyfriendlybreakreminder.database.data.Exercise;
import org.secuso.privacyfriendlybreakreminder.database.data.ExerciseSet;
import org.secuso.privacyfriendlybreakreminder.dialog.ExerciseDialog;
import org.secuso.privacyfriendlybreakreminder.exercises.ExerciseLocale;
import org.secuso.privacyfriendlybreakreminder.service.TimerService;

import java.util.Locale;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.view.Gravity.CENTER_HORIZONTAL;
import static org.secuso.privacyfriendlybreakreminder.service.TimerService.ACTION_STOP_TIMER;

/**
 * This activity handles showing the exercises and the exercise timer.
 *
 * @author Christopher Beckmann
 * @version 2.0
 */
public class ExerciseActivity extends AppCompatActivity implements android.support.v4.app.LoaderManager.LoaderCallbacks<ExerciseSet> {

    private static final String TAG = ExerciseActivity.class.getSimpleName();
    private static boolean confirmationDialogShown = false;
    private static boolean endDialogShown = false;

    // UI
    private TextView breakTimerText;
    private ProgressBar progressBar;
    private TextView timerText;
    private TextView executionText;
    private TextView descriptionText;
    private TextView sectionText;
    private ImageView exerciseImage;
    private ConstraintLayout exerciseContent;
    private ImageButton playButton;
    private ImageButton repeatButton;
    private ImageButton continuousButton;
    private ImageButton prevButton;
    private ImageButton nextButton;
    private ProgressBar progressBarBig;
    private TextView breakTimerTextBig;
    private ConstraintLayout bigProgressBarLayout;
    private ImageButton exerciseInfoButton;
    private Toast toast;

    private boolean isActivityVisible = false;
    private boolean isBreakFinished = false;

    private boolean repeatStatus;
    private boolean continuousStatus;
    private boolean showBigTimer = false;
    private boolean showControlButtons = true;
    private boolean keepScreenOn = true;

    // exerciseSet info
    private long exerciseSetId;
    private ExerciseSet set;
    private int currentExercise = 0;
    private int currentExercisePart = 0;

    // timer
    private long exerciseTime = 20 * 1000;
    private long pauseDuration;
    private CountDownTimer exerciseTimer;
    private CountDownTimer breakTimer;
    private boolean isBreakTimerRunning;
    private boolean isExerciseTimerRunning;
    private long remainingBreakDuration;
    private long remainingExerciseDuration;

    // database and utility
    private SQLiteHelper dbHelper;
    private SharedPreferences pref;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        Intent stopTimer = new Intent(this, TimerService.class);
        stopTimer.setAction(ACTION_STOP_TIMER);
        startService(stopTimer);

        mHandler = new Handler();

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        exerciseSetId = pref.getLong(FirstLaunchManager.DEFAULT_EXERCISE_SET, 0L);
        pauseDuration = pref.getLong(FirstLaunchManager.PAUSE_TIME, 5 * 60 * 1000);
        repeatStatus = pref.getBoolean(FirstLaunchManager.REPEAT_STATUS, false);
        continuousStatus = pref.getBoolean(FirstLaunchManager.REPEAT_EXERCISES, false);
        try {
            exerciseTime = Long.parseLong(pref.getString(FirstLaunchManager.EXERCISE_DURATION, "30")) * 1000;
        } catch (NumberFormatException e) {
            exerciseTime = 30L * 1000;
        }
        keepScreenOn = pref.getBoolean(FirstLaunchManager.KEEP_SCREEN_ON_DURING_EXERCISE, true);

        initResources();

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_close_white);
        }

        if (keepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        getSupportLoaderManager().initLoader(0, null, this);
    }

    private void initResources() {
        dbHelper = new SQLiteHelper(this);
        playButton = (ImageButton) findViewById(R.id.button_playPause);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        timerText = (TextView) findViewById(R.id.timerText);
        executionText = (TextView) findViewById(R.id.execution);
        descriptionText = (TextView) findViewById(R.id.description);
        exerciseImage = (ImageView) findViewById(R.id.exercise_image);
        sectionText = (TextView) findViewById(R.id.section);
        repeatButton = (ImageButton) findViewById(R.id.button_repeat);
        exerciseContent = (ConstraintLayout) findViewById(R.id.exercise_layout);
        continuousButton = (ImageButton) findViewById(R.id.button_continuous);
        prevButton = (ImageButton) findViewById(R.id.button_prev);
        nextButton = (ImageButton) findViewById(R.id.button_next);
        exerciseInfoButton = (ImageButton) findViewById(R.id.exercise_info_button);

        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        toast.setGravity(toast.getGravity(), 0, 250);

        progressBarBig = (ProgressBar) findViewById(R.id.progressBarBig);
        breakTimerTextBig = (TextView) findViewById(R.id.breakTimerTextBig);
        bigProgressBarLayout = (ConstraintLayout) findViewById(R.id.bigProgressBarLayout);

        setRepeatButtonStatus(repeatStatus);
        setContinuousButtonStatus(continuousStatus);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!isBreakFinished) {
                    showConfirmationDialog(this);
                } else {
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_exercise, menu);

        MenuItem timerItem = menu.findItem(R.id.break_timer);
        breakTimerText = (TextView) MenuItemCompat.getActionView(timerItem);
        breakTimerText.setTextColor(Color.WHITE);
        breakTimerText.setTextSize(20);
        breakTimerText.setGravity(CENTER_HORIZONTAL);
        breakTimerText.setPadding(16, 0, 16, 0);
        //breakTimerText.set(10, 0, 10, 0);

        return true;
    }

    @Override
    public void onBackPressed() {
        if (isBreakFinished) {
            showConfirmationDialog(this);
        } else {
            finish();
        }
    }

    @Override
    public void finish() {
        Intent intent = new Intent(ExerciseActivity.this, TimerActivity.class);
        intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);

        // start the next timer if continuous is activated
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        if(pref.getBoolean(FirstLaunchManager.PREF_EXERCISE_CONTINUOUS, false)) {
            Intent timerServiceIntent = new Intent(this.getApplicationContext(), TimerService.class);
            timerServiceIntent.setAction(TimerService.ACTION_START_TIMER);
            startService(timerServiceIntent);
        }

        if(breakTimer != null) breakTimer.cancel();
        if(exerciseTimer != null) exerciseTimer.cancel();

        super.finish();

        if(isActivityVisible) {
            ExerciseActivity.this.startActivity(intent);
            ExerciseActivity.this.overridePendingTransition(0, 0);
        }
    }

    private static void showConfirmationDialog(final ExerciseActivity activity) {
        if (activity.isActivityVisible && !confirmationDialogShown) {
            confirmationDialogShown = true;
            new AlertDialog.Builder(activity)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .setMessage(R.string.dialog_leave_break_confirmation)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            confirmationDialogShown = false;
                        }
                    })
                    .create().show();
        }
    }

    private static void showEndDialog(final ExerciseActivity activity) {
        if (activity.isActivityVisible && !endDialogShown) {
            endDialogShown = true;
            new AlertDialog.Builder(activity)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .setTitle(R.string.dialog_end_break_confirmation_title)
                    .setMessage(R.string.dialog_end_break_confirmation)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            endDialogShown = false;
                        }
                    })
                    .create().show();
        }
    }

    @Override
    public Loader<ExerciseSet> onCreateLoader(int id, final Bundle args) {
        return new ExerciseSetLoader(this, (int)exerciseSetId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityVisible = true;

        if (isBreakFinished) {
            showEndDialog(this);
        }

        if (keepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityVisible = false;

        if(isBreakFinished) {
            // TODO: Either start a short Timer to see if the user comes back - or start the next work time rand finish this activity
            // TODO: for now we just finish
            finish();
        }

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onLoadFinished(Loader<ExerciseSet> loader, ExerciseSet set) {
        if (set != null) {
            this.set = set;
        } else {
            this.set = new ExerciseSet();
        }

        if (this.set.size() > 0) {
            setExercise(0);
        } else {
            showBigTimer(true);
            showControlButtons(false);
        }
        // load data only once
        getSupportLoaderManager().destroyLoader(0);

        startBreakTimer();
    }

    @Override
    public void onLoaderReset(Loader<ExerciseSet> loader) {
    }


    private void updatePlayButton(boolean isRunning) {
        if (isRunning) {
            playButton.setImageResource(R.drawable.ic_pause_black_48dp);
        } else {
            playButton.setImageResource(R.drawable.ic_play_arrow_black);
        }
    }

    private void updateProgress(long remainingDuration) {
        progressBar.setMax((int) exerciseTime);
        progressBar.setProgress(progressBar.getMax() - (int) remainingDuration);

        int secondsUntilFinished = (int) Math.ceil(remainingDuration / 1000.0);
        int minutesUntilFinished = secondsUntilFinished / 60;
        int seconds = secondsUntilFinished % 60;
        int minutes = minutesUntilFinished % 60;

        String time = String.format(Locale.US, "%02d:%02d", minutes, seconds);
        timerText.setText(time);
    }

    private void updateBigProgress(long remainingDuration) {
        progressBarBig.setMax((int) pauseDuration);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progressBarBig.setProgress(progressBarBig.getMax() - (int) remainingDuration, true);
        } else {
            progressBarBig.setProgress(progressBarBig.getMax() - (int) remainingDuration);
        }

        int secondsUntilFinished = (int) Math.ceil(remainingDuration / 1000.0);
        int minutesUntilFinished = secondsUntilFinished / 60;
        int seconds = secondsUntilFinished % 60;
        int minutes = minutesUntilFinished % 60;

        String time = String.format(Locale.US, "%02d:%02d", minutes, seconds);
        breakTimerTextBig.setText(time);
    }

    private void showBigTimer(boolean show) {
        if (showBigTimer != show) {
            showBigTimer = show;

            if (show) {
                bigProgressBarLayout.setVisibility(View.VISIBLE);
                bigProgressBarLayout.animate().alpha(1.0f).setDuration(125).setListener(null);

                exerciseContent.animate().alpha(0.0f).setDuration(125).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (!showBigTimer)
                            exerciseContent.setVisibility(View.GONE);
                    }
                });
            } else {
                bigProgressBarLayout.animate().alpha(0.0f).setDuration(125).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (!showBigTimer)
                            bigProgressBarLayout.setVisibility(View.GONE);
                    }
                });

                exerciseContent.setVisibility(View.VISIBLE);
                exerciseContent.animate().alpha(1.0f).setDuration(125).setListener(null);
            }
        }
    }

    private void showControlButtons(boolean show) {
        if (show != showControlButtons) {
            showControlButtons = show;

            if (show) {
                playButton.setVisibility(View.VISIBLE);
                repeatButton.setVisibility(View.VISIBLE);
                continuousButton.setVisibility(View.VISIBLE);
                prevButton.setVisibility(View.VISIBLE);
                nextButton.setVisibility(View.VISIBLE);
            } else {
                playButton.setVisibility(View.GONE);
                repeatButton.setVisibility(View.GONE);
                continuousButton.setVisibility(View.GONE);
                prevButton.setVisibility(View.GONE);
                nextButton.setVisibility(View.GONE);
            }
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.progressBarLayout:
            case R.id.button_playPause:
                handlePlayPauseClicked();
                break;
            case R.id.button_next:
                handleSkipClicked();
                break;
            case R.id.button_prev:
                handlePrevClicked();
                break;
            case R.id.button_repeat:
                handleRepeatClicked();
                break;
            case R.id.button_continuous:
                handleContinuousClicked();
                break;
            default:
        }
    }

    private boolean next() {
        boolean result = nextExercisePart() || nextExercise();

        if(result) vibrate();

        return result;
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500,VibrationEffect.DEFAULT_AMPLITUDE));
        }else{
            v.vibrate(500);
        }
    }

    private boolean nextExercise() {
        if (set != null) {
            if (showBigTimer && repeatStatus && currentExercise == set.size()) {
                // repeat status is was turned back on.. and somebody presses next
                showBigTimer(false);
                currentExercise = set.size() - 1;
            }

            if (setExercise((currentExercise + 1))) {
                return true;
            } else {
                showBigTimer(true);
            }
        }
        return false;
    }

    private boolean previousExercise() {
        if (showBigTimer) {
            showBigTimer(false);
        }
        return set != null && setExercise(currentExercise - 1);
    }

    private boolean setExercise(int number) {
        if (set != null) {
            if (set.size() != 0) {

                if (number < 0) {
                    currentExercise = repeatStatus ?
                            (number + set.size()) :
                            0;

                } else if (number >= set.size()) {
                    currentExercise = repeatStatus ?
                            (number % set.size()) :
                            (set.size());

                    if (!repeatStatus) return false;

                } else {
                    currentExercise = number;
                }

                currentExercisePart = 0;
                showExercise(set.get(currentExercise), currentExercisePart);
                return true;
            }
        }
        return false;
    }

    private boolean nextExercisePart() {
        if (set != null) {
            if (set.size() != 0 && currentExercise < set.size()) {
                int[] images = set.get(currentExercise).getImageResIds(this);

                currentExercisePart = (currentExercisePart + 1);

                if (currentExercisePart >= images.length) {
                    currentExercisePart = 0;
                    return false;
                }

                showExercise(set.get(currentExercise), currentExercisePart);
                return true;
            }
        }
        return false;
    }

    private void showExercise(final Exercise e, int image) {
        int[] images = e.getImageResIds(this);

        if (image < 0 || image >= images.length) {
            image = 0;
        }

        View.OnClickListener infoClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseExerciseTimer();
                ExerciseDialog.showExerciseDialog(ExerciseActivity.this, e, new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        resumeExerciseTimer();
                    }
                });
            }
        };

        executionText.setText(e.getExecution());
        descriptionText.setText(e.getDescription());
        sectionText.setText(e.getSection(this));
        exerciseImage.setImageResource(e.getImageResIds(this)[image]);
        exerciseImage.setOnClickListener(infoClickListener);
        exerciseInfoButton.setOnClickListener(infoClickListener);

        if (continuousStatus)
            startExerciseTimer();
        else
            resetExerciseTimer();
    }

    private void handleRepeatClicked() {
        repeatStatus = !repeatStatus;

        pref.edit().putBoolean(FirstLaunchManager.REPEAT_STATUS, repeatStatus).apply();

        toast.setText(repeatStatus ? R.string.activity_exercise_button_repeat_on : R.string.activity_exercise_button_repeat_off);
        toast.show();

        setRepeatButtonStatus(repeatStatus);
    }

    private void setRepeatButtonStatus(boolean repeatStatus) {
        repeatButton.setColorFilter(
                repeatStatus ?
                        ActivityCompat.getColor(this, R.color.colorPrimary) :
                        ActivityCompat.getColor(this, R.color.middlegrey));
    }

    private void handleContinuousClicked() {
        continuousStatus = !continuousStatus;

        pref.edit().putBoolean(FirstLaunchManager.REPEAT_EXERCISES, continuousStatus).apply();

        toast.setText(continuousStatus ? R.string.activity_exercise_button_continuous_on : R.string.activity_exercise_button_continuous_off);
        toast.show();

        setContinuousButtonStatus(continuousStatus);
    }

    private void setContinuousButtonStatus(boolean continuousStatus) {
        continuousButton.setColorFilter(
                continuousStatus ?
                        ActivityCompat.getColor(this, R.color.colorPrimary) :
                        ActivityCompat.getColor(this, R.color.middlegrey));
    }

    private void handlePrevClicked() {
        toast.setText(R.string.activity_exercise_button_prev);
        toast.show();
        previousExercise();
    }

    private void handleSkipClicked() {
        toast.setText(R.string.activity_exercise_button_next);
        toast.show();
        nextExercise();
    }

    private void handlePlayPauseClicked() {
        if (isExercisePaused()) {
            resumeExerciseTimer();
            updatePlayButton(true);
        } else if (isExerciseTimerRunning) {
            pauseExerciseTimer();
            updatePlayButton(false);
        } else {
            startExerciseTimer();
            updatePlayButton(true);
        }
    }

    // timer
    public CountDownTimer createBreakTimer(long duration) {
        return new CountDownTimer(duration, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingBreakDuration = millisUntilFinished;
                updateBreakTimer(remainingBreakDuration);
                updateBigProgress(remainingBreakDuration);
            }

            @Override
            public void onFinish() {
                isBreakFinished = true;
                remainingBreakDuration = 0;
                isBreakTimerRunning = false;
                updateBreakTimer(remainingBreakDuration);
                updateBigProgress(remainingBreakDuration);

                if(isActivityVisible) {
                    showEndDialog(ExerciseActivity.this);
                } else {
                    finish();
                }

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                }, 1000 * 5);
            }
        };
    }

    private void updateBreakTimer(long remainingBreakDuration) {
        int secondsUntilFinished = (int) Math.ceil(remainingBreakDuration / 1000.0);
        int minutesUntilFinished = secondsUntilFinished / 60;
        int seconds = secondsUntilFinished % 60;
        int minutes = minutesUntilFinished % 60;

        String time = String.format(Locale.US, "%02d:%02d", minutes, seconds);
        time = getString(R.string.remaining_time) + " " + time;
        if (breakTimerText != null) {
            breakTimerText.setText(time);
        }
    }

    public CountDownTimer createExerciseTimer(long duration) {
        return new CountDownTimer(duration, 25) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingExerciseDuration = millisUntilFinished;
                updateProgress(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                remainingExerciseDuration = 0;
                isExerciseTimerRunning = false;
                updatePlayButton(false);
                updateProgress(0L);
                next();
            }
        };
    }

    public void startBreakTimer() {
        breakTimer = createBreakTimer(pauseDuration);
        breakTimer.start();
        isBreakTimerRunning = true;
    }

    public void startExerciseTimer() {
        if (exerciseTimer != null) {
            exerciseTimer.cancel();
        }
        exerciseTimer = createExerciseTimer(exerciseTime);
        exerciseTimer.start();
        isExerciseTimerRunning = true;
        updateProgress(exerciseTime);
        updatePlayButton(true);
    }

    private void pauseBreakTimer() {
        if (isBreakTimerRunning) {
            breakTimer.cancel();
            isBreakTimerRunning = false;
        }
    }

    private void pauseExerciseTimer() {
        if (isExerciseTimerRunning) {
            exerciseTimer.cancel();
            isExerciseTimerRunning = false;
        }
    }

    public void resumeBreakTimer() {
        if (!isBreakTimerRunning & remainingBreakDuration > 0) {
            breakTimer = createExerciseTimer(remainingBreakDuration);
            breakTimer.start();
            isBreakTimerRunning = true;
        }
    }

    public void resumeExerciseTimer() {
        if (!isExerciseTimerRunning & remainingExerciseDuration > 0) {
            exerciseTimer = createExerciseTimer(remainingExerciseDuration);
            exerciseTimer.start();
            isExerciseTimerRunning = true;
        }
    }

    public boolean isExercisePaused() {
        return !isExerciseTimerRunning && remainingExerciseDuration > 0;
        // return !isRunning && initialDuration != 0 && remainingDuration > 0 && remainingDuration != initialDuration;
    }

    private void resetExerciseTimer() {
        if (exerciseTimer != null) {
            exerciseTimer.cancel();
        }
        isExerciseTimerRunning = false;
        remainingExerciseDuration = 0;

        updatePlayButton(false);
        updateProgress(exerciseTime);
    }

    private static class ExerciseSetLoader extends AsyncTaskLoader<ExerciseSet> {
        int exerciseSetId;

        ExerciseSetLoader(Context context, int exerciseSetId) {
            super(context);
            this.exerciseSetId = exerciseSetId;
        }

        @Override
        public ExerciseSet loadInBackground() {
            return new SQLiteHelper(getContext()).getExerciseListForSet(exerciseSetId, ExerciseLocale.getLocale());
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        protected void onReset() {
        }
    };
}
