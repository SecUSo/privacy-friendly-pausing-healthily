package org.secuso.privacyfriendlypausinghealthily.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.preference.PreferenceManager;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;

import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.secuso.privacyfriendlypausinghealthily.activities.tutorial.FirstLaunchManager;
import org.secuso.privacyfriendlypausinghealthily.exercises.ExerciseLocale;
import org.secuso.privacyfriendlypausinghealthily.R;
import org.secuso.privacyfriendlypausinghealthily.activities.adapter.ExerciseSetSpinnerAdapter;
import org.secuso.privacyfriendlypausinghealthily.activities.helper.BaseActivity;
import org.secuso.privacyfriendlypausinghealthily.database.SQLiteHelper;
import org.secuso.privacyfriendlypausinghealthily.database.data.ExerciseSet;
import org.secuso.privacyfriendlypausinghealthily.service.TimerService;

import com.shawnlin.numberpicker.NumberPicker;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static org.secuso.privacyfriendlypausinghealthily.activities.tutorial.FirstLaunchManager.DEFAULT_EXERCISE_SET;
import static org.secuso.privacyfriendlypausinghealthily.activities.tutorial.FirstLaunchManager.PAUSE_TIME;

/**
 * This is the main break reminder activity. It lets you choose exercise and work times, as well as the exercises you want to perform during the break.
 * When a time is chosen and the "play" buttin is pressed. It displays a big Countdown. The timing itself is handled by the {@link TimerService}.
 * @author Christopher Beckmann
 * @version 2.0
 * @see TimerService
 */
public class TimerActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<List<ExerciseSet>> {
    private static final String TAG = TimerActivity.class.getSimpleName();

    // UI
    private ConstraintLayout mainContent;
    private ProgressBar progressBar;
    private TextView timerText;
    private ImageButton playButton;
    private ImageButton resetButton;
    private ImageButton skipButton;
    private NumberPicker secondsPicker;
    private NumberPicker minutesPicker;
    private NumberPicker hoursPicker;
    private NumberPicker secondsBreakPicker;
    private NumberPicker minutesBreakPicker;
    private LinearLayout pickerLayout;
    private Spinner exerciseSetSpinner;
    private ExerciseSetSpinnerAdapter exerciseSetAdapter;

    private boolean isActivityVisible = false;

    // animation
    private boolean currentStatusIsPickerVisible = false;

    private ExerciseSet currentExerciseSet;
    private ConstraintSet constraintSetPicker;
    private ConstraintSet constraintSetRunning;

    // Service
    private TimerService mTimerService = null;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerService.TimerServiceBinder binder = (TimerService.TimerServiceBinder) service;
            mTimerService = binder.getService();

            TimerActivity.this.onServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mTimerService = null;
        }
    };

    private void onServiceConnected() {
        updateUI();
    }

    private final BroadcastReceiver timerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long millisUntilDone = intent.getLongExtra("onTickMillis", -1L);
            long initialDuration = intent.getLongExtra("initialMillis", 0L);
            boolean isRunning = intent.getBooleanExtra("isRunning", false);
            boolean isPaused = intent.getBooleanExtra("isPaused", false);

            //if(intent.getBooleanExtra("done" ,false)) {
                // TODO: show a dialog here to start the exercise?
            //}

            //Log.d(TAG, millisUntilDone + "/" + initialDuration + " (" + (isRunning ? "Running" : "") + (isPaused ? "Paused" : "") + (!isRunning && !isPaused ?  "Stopped" : "") + ")");

            updateUI(isRunning, isPaused, initialDuration, millisUntilDone);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        initResources();
        initAnimations();
        getSupportLoaderManager().initLoader(0, null, this);
    }

    /**
     * Must be called <b>after</b> {@link #initResources()}
     */
    private void initAnimations() {
        constraintSetPicker = new ConstraintSet();
        constraintSetPicker.clone(mainContent);

        constraintSetRunning = new ConstraintSet();
        constraintSetRunning.clone(mainContent);
        int[] chainViews = {R.id.button_reset, R.id.button_playPause, R.id.button_skip};
        float[] chainWeights = {0.5f, 0.5f, 0.5f};
        constraintSetRunning.createHorizontalChain(0, ConstraintSet.LEFT, 0, ConstraintSet.RIGHT, chainViews, chainWeights, ConstraintSet.CHAIN_PACKED);
        constraintSetRunning.setVisibility(R.id.button_reset, View.VISIBLE);
        constraintSetRunning.setVisibility(R.id.button_skip, View.VISIBLE);
        constraintSetRunning.setVisibility(R.id.picker_layout, View.INVISIBLE);
        constraintSetRunning.setVisibility(R.id.progressBar, View.VISIBLE);
        constraintSetRunning.setVisibility(R.id.timerText, View.VISIBLE);
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_timer;
    }

    @Override
    protected void onStop() {
        super.onStop();

        shutdownServiceBinding();
    }

    @Override
    protected void onPause() {
        super.onPause();

        isActivityVisible = false;

        unregisterReceiver(timerReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        isActivityVisible = true;

        registerReceiver(timerReceiver, new IntentFilter(TimerService.TIMER_BROADCAST));

        if(mTimerService != null && !mTimerService.isRunning()) {
            updateProgress(mTimerService.getInitialDuration());
        }
        updateUI();

        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        initServiceBinding();
    }

    private void initServiceBinding() {
        TimerService.startService(this);
        Intent intent = new Intent(this, TimerService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void shutdownServiceBinding() {
        if (mTimerService != null) {
            unbindService(mServiceConnection);
        }
    }

    private void initResources() {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        exerciseSetAdapter = new ExerciseSetSpinnerAdapter(this, R.layout.layout_exercise_set, new LinkedList<ExerciseSet>());

        mainContent = findViewById(R.id.main_content);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        timerText = (TextView) findViewById(R.id.timerText);
        playButton = (ImageButton) findViewById(R.id.button_playPause);
        resetButton = (ImageButton) findViewById(R.id.button_reset);
        skipButton = (ImageButton) findViewById(R.id.button_skip);
        exerciseSetSpinner = (Spinner) findViewById(R.id.spinner_choose_exercise_set);
        exerciseSetSpinner.setAdapter(exerciseSetAdapter);
        exerciseSetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentExerciseSet = (ExerciseSet) exerciseSetSpinner.getAdapter().getItem(position);
                pref.edit().putLong(DEFAULT_EXERCISE_SET, id).apply();

                if(mTimerService != null && mTimerService.isRunning()) {
                    if(currentExerciseSet.getExerciseSetTime(TimerActivity.this)*1000 > getCurrentSetBreakTime()) {
                        Toast toast = Toast.makeText(TimerActivity.this, R.string.toast_not_enough_exercise_time, Toast.LENGTH_SHORT);
                        toast.setGravity(toast.getGravity(), 0, 250);
                        toast.show();
                        saveCurrentSetBreakTime();
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        secondsPicker = (NumberPicker) findViewById(R.id.seconds_picker);
        minutesPicker = (NumberPicker) findViewById(R.id.minutes_picker);
        hoursPicker = (NumberPicker) findViewById(R.id.hours_picker);
        pickerLayout = (LinearLayout) findViewById(R.id.picker_layout);

        secondsBreakPicker = (NumberPicker) findViewById(R.id.seconds_break_picker);
        minutesBreakPicker = (NumberPicker) findViewById(R.id.minutes_break_picker);

        setPickerAttributes(secondsPicker);
        setPickerAttributes(minutesPicker);
        setPickerAttributes(hoursPicker);
        setPickerAttributes(secondsBreakPicker);
        setPickerAttributes(minutesBreakPicker);

        secondsPicker.setValue(pref.getInt(FirstLaunchManager.PREF_PICKER_SECONDS, 0));
        minutesPicker.setValue(pref.getInt(FirstLaunchManager.PREF_PICKER_MINUTES, 30));
        hoursPicker.setValue(pref.getInt(FirstLaunchManager.PREF_PICKER_HOURS, 1));
        secondsBreakPicker.setValue(pref.getInt(FirstLaunchManager.PREF_BREAK_PICKER_SECONDS, 0));
        minutesBreakPicker.setValue(pref.getInt(FirstLaunchManager.PREF_BREAK_PICKER_MINUTES, 5));
    }

    private void setPickerAttributes(NumberPicker np) {
        np.setTextColorResource(R.color.middlegrey);
        np.setSelectedTextColorResource(R.color.colorAccent);
        np.setDividerColorResource(R.color.transparent);
        np.setSelectedTextSize(R.dimen.picker_selected_text_size);
        np.setTextSize(R.dimen.picker_text_size);
        np.setFormatter(NumberPicker.getTwoDigitFormatter());
        np.setWheelItemCount(5);
    }

    private void updateProgress(long millisUntilFinished) {
        progressBar.setProgress(progressBar.getMax() - (int) millisUntilFinished);

        int secondsUntilFinished = (int) Math.ceil(millisUntilFinished / 1000.0);
        int minutesUntilFinished = secondsUntilFinished / 60;
        int hours = minutesUntilFinished / 60;
        int seconds = secondsUntilFinished % 60;
        int minutes = minutesUntilFinished % 60;

        String time = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
        timerText.setText(time);
    }


    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.button_playPause:
            case R.id.progressBar:
                handlePlayPressed();
                break;
            case R.id.button_reset:
                if(mTimerService != null)
                    mTimerService.stopAndResetTimer();
                break;
            case R.id.button_skip:
                if(mTimerService != null)
                    mTimerService.skipTimer();
                break;
            //case R.id.button_chooseExercise:
            //    startActivity(new Intent(this, ManageExerciseSetsActivity.class));
            //    break;
        }
    }

    private void handlePlayPressed() {
        if(mTimerService != null) {
            if(mTimerService.isPaused()) {
                mTimerService.resumeTimer();
            }
            else if(mTimerService.isRunning()){
                mTimerService.pauseTimer();

            } else {
                final long duration = getCurrentSetDuration();
                saveCurrentSetDuration();

                ExerciseSet set = getCurrentSelectedExerciseSet();

                long seconds = 0;
                if(set != null) seconds = set.getExerciseSetTime(this);

                if(seconds * 1000 > getCurrentSetBreakTime()) {

                    showTimeWarningDialog(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mTimerService.startTimer(duration);
                            progressBar.setMax((int) duration);
                            saveCurrentSetBreakTime();
                        }
                    });

                } else {
                    mTimerService.startTimer(duration);
                    progressBar.setMax((int) duration);

                    saveCurrentSetBreakTime();
                }
            }
        }
    }

    private void showTimeWarningDialog(DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.warning);
        builder.setMessage(R.string.dialog_warning_not_enough_exercise_time);
        Drawable icon = getDrawable(R.drawable.ic_about);
        if (icon != null) {
            icon.setTint(ActivityCompat.getColor(this, R.color.yellow));
            builder.setIcon(icon);
        }
        builder.setPositiveButton(R.string.start, listener);
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    private void saveCurrentSetBreakTime() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);


        SharedPreferences.Editor editor = pref.edit()
                .putInt(FirstLaunchManager.PREF_BREAK_PICKER_SECONDS, secondsBreakPicker.getValue())
                .putInt(FirstLaunchManager.PREF_BREAK_PICKER_MINUTES, minutesBreakPicker.getValue());

        ExerciseSet set = getCurrentSelectedExerciseSet();

        long seconds = 0;
        if(set != null) {
            seconds = set.getExerciseSetTime(this);
        }

        if(seconds * 1000 > getCurrentSetBreakTime()) {
            editor.putLong(PAUSE_TIME, seconds * 1000).apply();
        } else {
            editor.putLong(PAUSE_TIME, getCurrentSetBreakTime()).apply();
        }
    }

    private void saveCurrentSetDuration() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.edit().putInt(FirstLaunchManager.PREF_PICKER_SECONDS, secondsPicker.getValue())
                .putInt(FirstLaunchManager.PREF_PICKER_MINUTES, minutesPicker.getValue())
                .putInt(FirstLaunchManager.PREF_PICKER_HOURS, hoursPicker.getValue())
                .putLong(FirstLaunchManager.WORK_TIME, getCurrentSetDuration()).apply();
    }

    private long getCurrentSetDuration() {
        long duration = secondsPicker.getValue() * 1000;
        duration += minutesPicker.getValue() * 1000 * 60;
        duration += hoursPicker.getValue() * 1000 * 60 * 60;
        return duration;
    }

    private long getCurrentSetBreakTime() {
        long breakTime = secondsBreakPicker.getValue() * 1000;
        breakTime += minutesBreakPicker.getValue() * 1000 * 60;
        return breakTime;
    }

    private void updateUI() {
        if(mTimerService != null) {
            updateUI(mTimerService.isRunning(), mTimerService.isPaused(), mTimerService.getInitialDuration(), mTimerService.getRemainingDuration());
        } else {
            showPicker(true);
        }
    }

    private synchronized void updateUI(boolean running, boolean isPaused, long initialDuration, long remainingDuration) {
        updatePlayButton(running);
        progressBar.setMax((int) initialDuration);
        updateProgress(remainingDuration);
        showPicker(!running && !isPaused);
    }

    private synchronized void showPicker(final boolean showPicker) {
        if(showPicker != currentStatusIsPickerVisible) {

            TransitionManager.beginDelayedTransition(mainContent);
            ConstraintSet constraint = showPicker ? constraintSetPicker : constraintSetRunning;
            constraint.applyTo(mainContent);

            //pickerLayout.clearAnimation();
            //timerText.clearAnimation();
            //progressBar.clearAnimation();

            currentStatusIsPickerVisible = showPicker;

            /*
            if (showPicker) {
                pickerLayout.setAlpha(0f);
                pickerLayout.setVisibility(View.VISIBLE);
                pickerLayout.animate()
                        .alpha(1f)
                        //.setStartDelay(mShortAnimationDuration)
                        .setDuration(mShortAnimationDuration)
                        .setListener(null);

                timerText.animate()
                        .alpha(0f)
                        .setDuration(mShortAnimationDuration)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animator) {
                                if(currentStatusIsPickerVisible)
                                    timerText.setVisibility(View.INVISIBLE);
                            }
                        });

                progressBar.animate()
                        .alpha(0f)
                        .setDuration(mShortAnimationDuration)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animator) {
                                if(currentStatusIsPickerVisible)
                                    progressBar.setVisibility(View.INVISIBLE);
                            }
                        });

                resetButton.animate()
                        .alpha(0f)
                        .setDuration(mShortAnimationDuration)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                if(currentStatusIsPickerVisible)
                                    resetButton.setVisibility(View.GONE);
                            }
                        });

            } else {
                pickerLayout.animate()
                        .alpha(0f)
                        .setDuration(mShortAnimationDuration)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animator) {
                                if(!currentStatusIsPickerVisible)
                                    pickerLayout.setVisibility(View.INVISIBLE);
                            }
                        });

                timerText.setAlpha(0f);
                timerText.setVisibility(View.VISIBLE);
                timerText.animate()
                        .alpha(1f)
                        .setDuration(mShortAnimationDuration)
                        .setListener(null);

                progressBar.setAlpha(0f);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.animate()
                        .alpha(1f)
                        .setDuration(mShortAnimationDuration)
                        .setListener(null);

                resetButton.setAlpha(0f);
                resetButton.setVisibility(View.VISIBLE);
                resetButton.animate()
                        .alpha(1f)
                        .setDuration(mShortAnimationDuration)
                        .setListener(null);
            }
            */
        }
    }

    private void updatePlayButton(boolean isRunning) {
        if(isRunning) {
            playButton.setImageResource(R.drawable.ic_pause_black_48dp);
        } else {
            playButton.setImageResource(R.drawable.ic_play_arrow_black);
        }
    }

    @Override
    public Loader<List<ExerciseSet>> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<List<ExerciseSet>>(this) {
            @Override
            public List<ExerciseSet> loadInBackground() {
                SQLiteHelper helper = new SQLiteHelper(getContext());
                return helper.getExerciseSetsWithExercises(ExerciseLocale.getLocale());
            }

            @Override
            protected void onStartLoading() {
                forceLoad();
            }

            @Override
            protected void onReset() {}
        };
    }

    @Override
    public void onLoadFinished(Loader<List<ExerciseSet>> loader, List<ExerciseSet> data) {
        exerciseSetAdapter.updateData(data);

        long defaultId = PreferenceManager.getDefaultSharedPreferences(this).getLong("DEFAULT_EXERCISE_SET", 0L);

        for(int i = 0; i < data.size(); ++i) {
            ExerciseSet e = data.get(i);

            if(e.getId() == defaultId) {
                exerciseSetSpinner.setSelection(i);
                if(exerciseSetSpinner.getOnItemSelectedListener() != null)
                    exerciseSetSpinner.getOnItemSelectedListener().onItemSelected(null, null, i, e.getId());
                break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<ExerciseSet>> loader) {}

    public ExerciseSet getCurrentSelectedExerciseSet() {
        return currentExerciseSet;
    }
}
