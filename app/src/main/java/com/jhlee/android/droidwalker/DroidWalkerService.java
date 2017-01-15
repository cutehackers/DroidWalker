package com.jhlee.android.droidwalker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.jhlee.android.droidwalker.database.DataBase;
import com.jhlee.android.droidwalker.model.WalkSet;
import com.jhlee.android.droidwalker.ui.event.RxEventManager;
import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.maplib.NGeoPoint;

import timber.log.Timber;

/**
 * DroidWalker
 *
 * date 2017-01-13
 * author Jun-hyoung, Lee
 */

public class DroidWalkerService extends Service implements SensorEventListener,
        NMapLocationManager.OnLocationChangeListener {

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
    private int mTodayDistance;
    private NMapLocationManager mLocationManager;
    private NGeoPoint mCurrentLocation;

    private static final int SHAKE_THRESHOLD = 800;
    private long prevTime;
    private float speed;
    private float prevX;
    private float prevY;
    private float prevZ;
    private float x, y, z;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setUpLocationManager();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setUpSensorEventListener();
        if (!mLocationManager.isMyLocationEnabled()) {
            mLocationManager.enableMyLocation(true);
        }

        Timber.i("DroidWalkerService onStartCommand() with id: %d", startId);

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        disposeSensorEventListener();
        disposeLocationManager();

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
        int todaySteps;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            todaySteps = getTodayStepBelowKitKat(event);
            if (todaySteps < 0) {
                return;
            }
        } else {
            todaySteps = getTodayStepOverKitKat(event);
            mCurrentSteps = (int) event.values[0];
        }

        // post step information event to MainActivity listener
        RxEventManager.instance().post(new WalkSet(todaySteps, mTodayDistance, mCurrentLocation));

        Timber.d("DroidWalkerService onSensorChanged() step: %d, today: %s", todaySteps, DataBase.getTimeString(DataBase.getToday()));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    //
    //----------------------------------------------------------------------------------------------
    // -- implements NMapLocationManager.OnLocationChangeListener
    //

    @Override
    public boolean onLocationChanged(NMapLocationManager nMapLocationManager, NGeoPoint nGeoPoint) {
        if (nGeoPoint == null) {
            return true;
        }

        if (mCurrentLocation != null) {
            double distance = NGeoPoint.getDistance(mCurrentLocation, nGeoPoint);
            if (distance > 0) {
                // update distance from database.
                DataBase db = DataBase.instance();
                long today = DataBase.getToday();

                int todayDistance = db.getDistance(today);
                if (todayDistance == -1) {
                    mTodayDistance = 0;
                    db.add(today, 0, 0);
                } else {
                    /**
                     * mLocationManager.isMyLocationFixed() 를 고려 해야하나?
                     */
                    todayDistance += distance;
                    mTodayDistance = todayDistance;
                    db.updateDistance(today, todayDistance);
                }
                db.close();
            }
        }
        mCurrentLocation = nGeoPoint;

        // 현재 위치 변경 시 호출된다. myLocation 객체에 변경된 좌표가 전달된다.
        // 현재 위치를 계속 탐색하려면 true를 반환한다.
        Timber.e("Naver map service location changed.");
        return true;
    }

    @Override
    public void onLocationUpdateTimeout(NMapLocationManager nMapLocationManager) {
        Timber.e("Naver map service  location updateSteps timeout.");
    }

    @Override
    public void onLocationUnavailableArea(NMapLocationManager nMapLocationManager, NGeoPoint nGeoPoint) {
        Timber.e("Naver map service  location unavailable area.");
    }


    /**
     * 만보기 기록 시작
     */
    private void setUpSensorEventListener() {
        mCurrentSteps = 0;

        SensorManager sensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
        } else {
            mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            sensorManager.registerListener(this,
                    mSensor, SensorManager.SENSOR_DELAY_NORMAL, BATCH_LATENCY_0);
        }
    }

    private void disposeSensorEventListener() {
        SensorManager sensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
        sensorManager.unregisterListener(this);
    }

    private void setUpLocationManager() {
        mLocationManager = new NMapLocationManager(this);
        mLocationManager.setOnLocationChangeListener(this);

        DataBase db = DataBase.instance();
        long today = DataBase.getToday();
        int distance = db.getDistance(today);
        mTodayDistance = (distance == -1) ? 0 : distance;
        db.close();
    }

    private void disposeLocationManager() {
        if (mLocationManager.isMyLocationEnabled()) {
            mLocationManager.disableMyLocation();
        }
        mLocationManager.removeOnLocationChangeListener(this);
    }

    private int getTodayStepOverKitKat(SensorEvent event) {
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

        // updateSteps step from database
        DataBase db = DataBase.instance();
        long today = DataBase.getToday();

        int todaySteps = db.getSteps(today);
        if (todaySteps == -1) {
            todaySteps = mSteps;
            db.add(today, mSteps, 0);
        } else {
            todaySteps += mSteps;
            db.updateSteps(today, todaySteps);
        }
        db.close();

        return todaySteps;
    }

    private int getTodayStepBelowKitKat(SensorEvent event) {
        long currentTime = System.currentTimeMillis();
        long interval = (currentTime - prevTime);

        if (interval > 100) {
            prevTime = currentTime;

            x = event.values[SensorManager.DATA_X];
            y = event.values[SensorManager.DATA_Y];
            z = event.values[SensorManager.DATA_Z];

            speed = Math.abs(x + y + z - prevX - prevY - prevZ) / interval * 10000;
            int todaySteps = -1;

            if (speed > SHAKE_THRESHOLD) {
                // updateSteps step from database
                DataBase db = DataBase.instance();
                long today = DataBase.getToday();

                todaySteps = db.getSteps(today);
                if (todaySteps == -1) {
                    todaySteps = 1;
                    db.add(today, todaySteps, 0);
                } else {
                    todaySteps++;
                    db.updateSteps(today, todaySteps);
                }
                db.close();
            }
            prevX = event.values[SensorManager.DATA_X];
            prevY = event.values[SensorManager.DATA_Y];
            prevZ = event.values[SensorManager.DATA_Z];
            return todaySteps;
        }
        return -1;
    }

}
