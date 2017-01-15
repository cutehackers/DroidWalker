package com.jhlee.android.droidwalker.network;

/**
 * DroidWalker
 * <p>
 * date 2017-01-15
 * author Jun-hyoung, Lee
 */

public interface ApiCreator {

    RestApiType getApiType();

    <T> T build(Class<T> clazz);
}
