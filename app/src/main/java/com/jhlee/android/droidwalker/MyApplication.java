package com.jhlee.android.droidwalker;

import android.support.multidex.MultiDexApplication;

import com.jhlee.android.droidwalker.base.AndroidContext;

import timber.log.Timber;

/**
 * DroidWalker
 *
 * date 2017-01-13
 * author Jun-hyoung, Lee
 */

public class MyApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        AndroidContext.instance().init(this);
    }
}
