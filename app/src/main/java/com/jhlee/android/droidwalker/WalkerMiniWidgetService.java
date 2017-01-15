package com.jhlee.android.droidwalker;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.jhlee.android.droidwalker.database.DataBase;
import com.jhlee.android.droidwalker.model.WalkSet;
import com.jhlee.android.droidwalker.ui.event.PermissionGrantedEvent;
import com.jhlee.android.droidwalker.ui.event.RxEventManager;

import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * DroidWalker
 * <p>
 * date 2017-01-15
 * author Jun-hyoung, Lee
 */

public class WalkerMiniWidgetService extends Service implements View.OnClickListener {

    public final static String ACTION_PAUSE = "pause";

    private WindowManager mWindowManager;
    private View mWidget;
    private TextView mStepView;
    private TextView mDistanceView;

    private CompositeSubscription mSubscription;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setUpEventListener();

        if (canDrawOverlays()) {
            View widget = LayoutInflater.from(this).inflate(R.layout.walker_mini_widget, null);
            setUpWidgetView(widget);
            fetch();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_PAUSE.equals(intent.getStringExtra("action"))) {
            stopSelf();
            return START_NOT_STICKY;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        releaseEventListener();
        if (mWidget != null) {
            mWindowManager.removeView(mWidget);
            mWidget = null;
        }

        Timber.d("WalkerMiniWidgetService onDestory()");
    }

    @Override
    public void onClick(View view) {
        final int id = view.getId();

        switch (id) {
            case R.id.close_button: {
                //close the service and remove view from the view hierarchy
                stopSelf();
                break;
            }
        }
    }


    //
    //----------------------------------------------------------------------------------------------
    //-- methods
    //

    private void setUpWidgetView(View widget) {
        mWidget = widget;

        //Add the view to the window.
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 200;

        //Add the view to the window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mWidget, params);

        mWidget.findViewById(R.id.close_button).setOnClickListener(this);

        mStepView = (TextView) widget.findViewById(R.id.step_text);
        mDistanceView = (TextView) widget.findViewById(R.id.distance_text);

        mWidget.setOnTouchListener(new View.OnTouchListener() {
            private int prevWidgetX;
            private int prevWidgetY;
            float startWidgetX;
            float startWidgetY;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        prevWidgetX = params.x;
                        prevWidgetY = params.y;
                        startWidgetX = event.getRawX();
                        startWidgetY = event.getRawY();
                        return true;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        params.x = prevWidgetX + (int) (event.getRawX() - startWidgetX);
                        params.y = prevWidgetY + (int) (event.getRawY() - startWidgetY);

                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(mWidget, params);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private boolean canDrawOverlays() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        } else {
            return Settings.canDrawOverlays(this);
        }
    }

    private void setUpEventListener() {
        releaseEventListener();

        mSubscription = new CompositeSubscription();
        mSubscription.add(RxEventManager.instance().subscribe(WalkSet.class, new Action1<WalkSet>() {
            @Override
            public void call(WalkSet walkSet) {
                setStepText(walkSet.getSteps());
                setDistanceText(walkSet.getDistance());
            }
        }));
    }

    private void releaseEventListener() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    /**
     * 현재 걸음 수 텍스트로 표시
     */
    private void setStepText(int steps) {
        mStepView.setText(String.format(getString(R.string.dashboard_step_format), steps));
    }

    /**
     * 현재 이동 거리 표시
     * @param distance 이동 거리 미터.
     */
    private void setDistanceText(int distance) {
        String text = "0 m";
        if (distance < 1000) {
            text = String.format(getString(R.string.dashboard_distance_m_format), distance);
        } else if (distance >= 1000) {
            text = String.format(getString(R.string.dashboard_distance_km_format), (float)distance/1000);
        }
        mDistanceView.setText(text);
    }

    /**
     * 데이터 베이스에 저장된 걸음 수를 표시한다.
     */
    private void fetch() {
        DataBase db = DataBase.instance();
        long today = DataBase.getToday();

        WalkSet walkSet = db.getWalkSet(today);
        setStepText((walkSet == null) ? 0 : walkSet.getSteps());
        setDistanceText((walkSet == null) ? 0 : walkSet.getDistance());

        db.close();
    }

}
