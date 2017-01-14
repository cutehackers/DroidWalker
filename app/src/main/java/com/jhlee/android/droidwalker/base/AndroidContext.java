package com.jhlee.android.droidwalker.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;

import java.lang.ref.WeakReference;

/**
 * DroidWalker
 *
 * date 2017-01-13
 * author Jun-hyoung, Lee
 */

public class AndroidContext {

    private static AndroidContext sInstance = new AndroidContext();

    private WeakReference<Context> mRefContext;

    private AndroidContext() {

    }

    public static AndroidContext instance() {
        return sInstance;
    }

    public void init(Context applicationContext) {
        mRefContext = new WeakReference<>(applicationContext);
    }

    public Context get() {
        return mRefContext.get();
    }

    public SharedPreferences getSharedPreferences() {
        return get().getSharedPreferences("walker_prefs", Context.MODE_PRIVATE);
    }
}
