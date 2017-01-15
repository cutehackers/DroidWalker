package com.jhlee.android.droidwalker.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CheckableImageButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jhlee.android.droidwalker.AppCache;
import com.jhlee.android.droidwalker.R;
import com.jhlee.android.droidwalker.base.AndroidContext;
import com.jhlee.android.droidwalker.database.DataBase;
import com.jhlee.android.droidwalker.model.DroidWalker;
import com.jhlee.android.droidwalker.model.WalkData;
import com.jhlee.android.droidwalker.ui.event.RxEventManager;

import rx.Subscription;
import rx.functions.Action1;

/**
 * DroidWalker
 *
 * 만보기 대쉬보드 화면
 *
 * date 2017-01-14
 * author Jun-hyoung, Lee
 */

public class WalkerDashBoardFragment extends Fragment implements View.OnClickListener {

    private TextView mStepView;
    private CheckableImageButton mPowerButton;
    private TextView mPowerText;

    private Subscription mWalkDataEventListener;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_walker_dashboard, container, false);
        mStepView = (TextView) view.findViewById(R.id.step_text);
        setUpPowerView(view);

        fetchSteps();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 액티비티가 생성되고 난후 이벤트 리스닝이 준비되면, 현재 상태에 따라 워커 서비스를 실행한다.
        if (isWalkerEnabled()) {
            RxEventManager.instance().post(new DroidWalker(true));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpEventListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseEventListener();
    }


    //
    //----------------------------------------------------------------------------------------------
    // -- implements View.OnClickListener
    //

    @Override
    public void onClick(View view) {
        boolean enabled = !isWalkerEnabled();

        // 만보기 시작 혹은 종료 이벤트를 MainActivity에게 보낸다.
        RxEventManager.instance().post(new DroidWalker(enabled));
        setWalkerEnabled(enabled);
        setPowerChecked(enabled);
    }


    //
    //----------------------------------------------------------------------------------------------
    // -- methods
    //

    private void setUpEventListener() {
        releaseEventListener();

        mWalkDataEventListener = RxEventManager.instance().subscribe(WalkData.class, new Action1<WalkData>() {
            @Override
            public void call(WalkData walkData) {
                setStepText(walkData.getSteps());
            }
        });
    }

    private void releaseEventListener() {
        if (mWalkDataEventListener != null && !mWalkDataEventListener.isUnsubscribed()) {
            mWalkDataEventListener.unsubscribe();
        }
    }

    /**
     * 현재 걸음 수 텍스트로 표시
     */
    private void setStepText(int steps) {
        mStepView.setText(String.format(getString(R.string.dashboard_step_format), steps));
    }

    /**
     * 만보기 진행상태에 따른 버튼 상태 반영
     */
    private void setUpPowerView(View view) {
        boolean enabled = isWalkerEnabled();

        mPowerButton = (CheckableImageButton) view.findViewById(R.id.power_button);
        mPowerButton.setOnClickListener(this);

        mPowerText = (TextView) view.findViewById(R.id.power_text);
        setPowerChecked(enabled);
    }

    private void setPowerChecked(boolean enabled) {
        mPowerText.setText(enabled ? getString(R.string.dashboard_power_off) : getString(R.string.dashboard_power_on));
        mPowerButton.setChecked(enabled);
    }

    /**
     * 만보기가 동작중인지 여부s
     */
    private boolean isWalkerEnabled() {
        return AndroidContext.instance().getSharedPreferences().getBoolean(AppCache.PREFS_WALKER_ENABLED, false);
    }

    /**
     * 만보기가 진행중인지 멈추었는지 값을 설정한다.
     */
    private void setWalkerEnabled(boolean enabled) {
        AndroidContext.instance().getSharedPreferences().edit()
                .putBoolean(AppCache.PREFS_WALKER_ENABLED, enabled)
                .apply();
    }

    /**
     * 데이터 베이스에 저장된 걸음 수를 표시한다.
     */
    private void fetchSteps() {
        DataBase db = DataBase.instance();
        long today = DataBase.getToday();

        int todaySteps = db.getSteps(today);
        setStepText((todaySteps == -1) ? 0 : todaySteps);
        db.close();
    }
}
