package com.jhlee.android.droidwalker.network;

import com.nhn.android.maps.NMapActivity;

import java.util.HashMap;

/**
 * DroidWalker
 * <p>
 * date 2017-01-15
 * author Jun-hyoung, Lee
 */

public class RestApiFactory {
    private static RestApiFactory sInstance = new RestApiFactory();

    private HashMap<RestApiType, Object> mApiMap = new HashMap<>();

    private RestApiFactory() {
        // empty constructor
    }

    public static RestApiFactory instance() {
        return sInstance;
    }

    /**
     * REST api endpoint 생성한다.
     * @param type type of REST api
     * @param clazz endpoint class
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrCreate(RestApiType type, Class<T> clazz) {
        T endpoint = (T) mApiMap.get(type);
        if (endpoint == null) {

            switch (type) {
                case NAVER_MAP: {
                    endpoint = new NaverMapApiCreator().build(clazz);
                } break;

                default: return null;
            }

            mApiMap.put(type, endpoint);
        }

        return endpoint;
    }

}
