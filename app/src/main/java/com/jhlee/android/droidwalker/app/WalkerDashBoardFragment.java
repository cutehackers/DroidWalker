package com.jhlee.android.droidwalker.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CheckableImageButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jhlee.android.droidwalker.R;
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
    private CheckableImageButton mStartButton;

    private Subscription mWalkDataEventListener;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_walker_dashboard, container, false);
        mStepView = (TextView) view.findViewById(R.id.step_text);
        mStartButton = (CheckableImageButton) view.findViewById(R.id.start_button);
        mStartButton.setOnClickListener(this);
        return view;
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
        boolean isChecked = mStartButton.isChecked();
        mStartButton.setChecked(!isChecked);

        // 만보기 시작 혹은 종료 이벤트를 MainActivity에게 보낸다.
        RxEventManager.instance().post(new DroidWalker(!isChecked));
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

    private void setStepText(int steps) {
        mStepView.setText(String.format(getString(R.string.dashboard_step_format), steps));
    }
}
