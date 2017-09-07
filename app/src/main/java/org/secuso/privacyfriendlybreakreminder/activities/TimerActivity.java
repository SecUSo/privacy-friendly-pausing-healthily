package org.secuso.privacyfriendlybreakreminder.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.ColorRes;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.secuso.privacyfriendlybreakreminder.exercises.ExerciseLocale;
import org.secuso.privacyfriendlybreakreminder.R;
import org.secuso.privacyfriendlybreakreminder.activities.adapter.ExerciseSetSpinnerAdapter;
import org.secuso.privacyfriendlybreakreminder.activities.helper.BaseActivity;
import org.secuso.privacyfriendlybreakreminder.database.SQLiteHelper;
import org.secuso.privacyfriendlybreakreminder.database.data.ExerciseSet;
import org.secuso.privacyfriendlybreakreminder.service.TimerService;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class TimerActivity extends BaseActivity implements android.support.v4.app.LoaderManager.LoaderCallbacks<List<ExerciseSet>> {
    private static final String TAG = TimerActivity.class.getSimpleName();
    private static final String PREF_PICKER_SECONDS = TAG + ".PREF_PICKER_SECONDS";
    private static final String PREF_PICKER_MINUTES = TAG + ".PREF_PICKER_MINUTES";
    private static final String PREF_PICKER_HOURS = TAG + ".PREF_PICKER_HOURS";

    // UI
    private ProgressBar progressBar;
    private TextView timerText;
    private ImageButton playButton;
    private ImageButton resetButton;
    private NumberPicker secondsPicker;
    private NumberPicker minutesPicker;
    private NumberPicker hoursPicker;
    private LinearLayout pickerLayout;
    private Spinner exerciseSetSpinner;
    private ExerciseSetSpinnerAdapter exerciseSetAdapter;

    // animation
    private int mShortAnimationDuration;
    private boolean currentStatusIsPickerVisible = false;

    private static final String[] SECONDS_MINUTES = new String[60];
    private static final String[] HOURS = new String[24];

    static {
        for(int i = 0; i < SECONDS_MINUTES.length; ++i) {
            SECONDS_MINUTES[i] = String.format(Locale.US, "%02d", i);
        }
        for(int i = 0; i < HOURS.length; ++i) {
            HOURS[i] = String.format(Locale.US, "%02d", i);
        }
    }

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

            Log.d(TAG, millisUntilDone + "/" + initialDuration + " (" + (isRunning ? "Running" : "") + (isPaused ? "Paused" : "") + (!isRunning && !isPaused ?  "Stopped" : "") + ")");

            updateUI(isRunning, isPaused, initialDuration, millisUntilDone);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        initResources();
        getSupportLoaderManager().initLoader(0, null, this);
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

        unregisterReceiver(timerReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        TimerService.startService(this);
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

        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        exerciseSetAdapter = new ExerciseSetSpinnerAdapter(this, R.layout.layout_exercise_set, new LinkedList<ExerciseSet>());

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        timerText = (TextView) findViewById(R.id.timerText);
        playButton = (ImageButton) findViewById(R.id.button_playPause);
        resetButton = (ImageButton) findViewById(R.id.button_reset);
        exerciseSetSpinner = (Spinner) findViewById(R.id.spinner_choose_exercise_set);
        exerciseSetSpinner.setAdapter(exerciseSetAdapter);
        exerciseSetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pref.edit().putLong("DEFAULT_EXERCISE_SET", id).apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        secondsPicker = (NumberPicker) findViewById(R.id.seconds_picker);
        minutesPicker = (NumberPicker) findViewById(R.id.minutes_picker);
        hoursPicker = (NumberPicker) findViewById(R.id.hours_picker);
        pickerLayout = (LinearLayout) findViewById(R.id.picker_layout);

        secondsPicker.setDisplayedValues(SECONDS_MINUTES);
        secondsPicker.setMinValue(0);
        secondsPicker.setMaxValue(SECONDS_MINUTES.length - 1);
        secondsPicker.setValue(pref.getInt(PREF_PICKER_SECONDS, 0));

        minutesPicker.setDisplayedValues(SECONDS_MINUTES);
        minutesPicker.setMinValue(0);
        minutesPicker.setMaxValue(SECONDS_MINUTES.length - 1);
        minutesPicker.setValue(pref.getInt(PREF_PICKER_MINUTES, 30));

        hoursPicker.setDisplayedValues(HOURS);
        hoursPicker.setMinValue(0);
        hoursPicker.setMaxValue(HOURS.length - 1);
        hoursPicker.setValue(pref.getInt(PREF_PICKER_HOURS, 1));

        setDividerColor(secondsPicker, R.color.transparent);
        setDividerColor(minutesPicker, R.color.transparent);
        setDividerColor(hoursPicker,   R.color.transparent);

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

        //progressBar.setMax(1000);
        //ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 0, 1000 * percentFinished); // see this max value coming back here, we animale towards that value

        //animation.setDuration(5000); //in milliseconds
        //animation.setInterpolator(new LinearInterpolator());
        //animation.start();
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
                long duration = getCurrentSetDuration();
                saveCurrentSetDuration();
                mTimerService.startTimer(duration);
                progressBar.setMax((int) duration);
            }
        }
    }

    private void saveCurrentSetDuration() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.edit().putInt(PREF_PICKER_SECONDS, secondsPicker.getValue())
                .putInt(PREF_PICKER_MINUTES, minutesPicker.getValue())
                .putInt(PREF_PICKER_HOURS, hoursPicker.getValue()).apply();
    }

    private long getCurrentSetDuration() {
        long duration = secondsPicker.getValue() * 1000;
        duration += minutesPicker.getValue() * 1000 * 60;
        duration += hoursPicker.getValue() * 1000 * 60 * 60;
        return duration;
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

            pickerLayout.clearAnimation();
            timerText.clearAnimation();
            progressBar.clearAnimation();

            currentStatusIsPickerVisible = showPicker;

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
                        //.setStartDelay(mShortAnimationDuration)
                        .setListener(null);
            }
        }
    }

    private void updatePlayButton(boolean isRunning) {
        if(isRunning) {
            playButton.setImageResource(R.drawable.ic_pause_black_48dp);
        } else {
            playButton.setImageResource(R.drawable.ic_play_arrow_black);
        }
    }

    private void setDividerColor(NumberPicker picker, @ColorRes int color) {
        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(ContextCompat.getColor(this, color));
                    pf.set(picker, colorDrawable);
                } catch (IllegalArgumentException | Resources.NotFoundException | IllegalAccessException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                break;
            }
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
                break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<ExerciseSet>> loader) {}
}
