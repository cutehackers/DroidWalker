package com.jhlee.android.droidwalker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.jhlee.android.droidwalker.database.DataBase;
import com.jhlee.android.droidwalker.model.WalkData;
import com.jhlee.android.droidwalker.ui.event.RxEventManager;

import timber.log.Timber;

/**
 * DroidWalker
 *
 * date 2017-01-13
 * author Jun-hyoung, Lee
 */

public class DroidWalkerService extends Service implements SensorEventListener {

    Sensor mSensor;

    // max batch latency is specified in microseconds
    private static final int BATCH_LATENCY_0 = 0; // no batching
    private static final int BATCH_LATENCY_5s = 5000000;
    private static final int BATCH_LATENCY_10s = 10000000;

    // Steps counted in current session
    private int mSteps = 0;
    // Value of the step counter sensor when the listener was registered.
    // (Total steps are calculated from this value.)
    private int mCurrentSteps = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setUpSensorEventListener();
        Timber.i("DroidWalkerService onStartCommand() with id: %d", startId);

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposeSensorEventListener();
        Timber.i("DroidWalkerService onDestroy()");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void onSensorChanged(SensorEvent event) {
        /**
         * A step counter event contains the total number of steps since the listener
         * was first registered. We need to keep track of this initial value to calculate the
         * number of steps taken, as the first value a listener receives is undefined.
         */
        if (mCurrentSteps < 1) {
            // initial value
            mCurrentSteps = (int) event.values[0];
        }

        // Calculate steps taken based on first counter value received.
        mSteps = (int) event.values[0] - mCurrentSteps;

        // update database
        DataBase db = DataBase.instance();
        long today = DataBase.getToday();

        int todaySteps = db.getSteps(today);
        if (todaySteps == -1) {
            todaySteps = mSteps;
            db.add(today, mSteps);
        } else {
            todaySteps += mSteps;
            db.update(today, todaySteps);
        }
        db.close();

        // post step information event
        RxEventManager.instance().post(new WalkData(todaySteps));

        mCurrentSteps = (int) event.values[0];

        Timber.d("DroidWalkerService onSensorChanged() step: %d, today: %s", todaySteps, DataBase.getTimeString(today));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * 만보기 기록 시작
     */
    private void setUpSensorEventListener() {
        mCurrentSteps = 0;

        SensorManager sensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        sensorManager.registerListener(this,
                mSensor, SensorManager.SENSOR_DELAY_NORMAL, BATCH_LATENCY_0);
    }

    private void disposeSensorEventListener() {
        SensorManager sensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
        sensorManager.unregisterListener(this);
    }
}
