package com.jhlee.android.droidwalker;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;

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
    private int mCounterSteps = 0;
    // Steps counted by the step counter previously. Used to keep counter consistent across rotation
    // changes
    private int mPreviousCounterSteps = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SensorManager sensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        sensorManager.registerListener(this,
                mSensor, SensorManager.SENSOR_DELAY_NORMAL, BATCH_LATENCY_0);
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

    @Override
    public void onSensorChanged(SensorEvent event) {

        /**
         * A step counter event contains the total number of steps since the listener
         * was first registered. We need to keep track of this initial value to calculate the
         * number of steps taken, as the first value a listener receives is undefined.
         */
        if (mCounterSteps < 1) {
            // initial value
            mCounterSteps = (int) event.values[0];
        }

        // Calculate steps taken based on first counter value received.
        mSteps = (int) event.values[0] - mCounterSteps;

        // Add the number of steps previously taken, otherwise the counter would start at 0.
        // This is needed to keep the counter consistent across rotation changes.
        mSteps = mSteps + mPreviousCounterSteps;

        // post step information eventx
        RxEventManager.instance().post(new WalkData(mCounterSteps + mSteps));

        Timber.d("DroidWalkerService onSensorChanged() step: %d", mSteps);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void setUpSensorEventListener() {
        SensorManager sensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        mCounterSteps = 0;

        sensorManager.registerListener(this,
                mSensor, SensorManager.SENSOR_DELAY_NORMAL, BATCH_LATENCY_0);
    }

    private void disposeSensorEventListener() {
        SensorManager sensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
        sensorManager.unregisterListener(this);
    }
}
