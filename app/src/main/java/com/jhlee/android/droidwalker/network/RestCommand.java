package com.jhlee.android.droidwalker.network;

import com.jhlee.android.droidwalker.base.Command;

import rx.Observable;

/**
 * DroidWalker
 * <p>
 * date 2017-01-15
 * author Jun-hyoung, Lee
 */

public abstract class RestCommand implements Command {

    public abstract <T> Observable<T> build();
}
