package com.jhlee.android.droidwalker.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jhlee.android.droidwalker.R;

/**
 * date     2017-01-14
 * author   Jun-hyoung, Lee
 * description
 * history
 */

public class WalkerRecordsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_walker_records, container, false);
        return view;
    }
}
