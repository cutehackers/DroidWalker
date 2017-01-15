package com.jhlee.android.droidwalker.network;

import com.jhlee.android.droidwalker.AppCache;
import com.jhlee.android.droidwalker.model.Placemark;
import com.jhlee.android.droidwalker.network.endpoint.NaverMapEndpoint;

import rx.Observable;

/**
 * DroidWalker
 * <p>
 * date 2017-01-15
 * author Jun-hyoung, Lee
 */

public class FetchAddressRestCommand extends RestCommand {

    private double mLatitude;
    private double mLongitude;

    public FetchAddressRestCommand latitude(double latitude) {
        mLatitude = latitude;
        return this;
    }

    public FetchAddressRestCommand longitude(double longitude) {
        mLongitude = longitude;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Observable<Placemark> build() {
        NaverMapEndpoint endpoint = RestApiFactory.instance().getOrCreate(RestApiType.NAVER_MAP,
                NaverMapEndpoint.class);

        String geocode = String.valueOf(mLongitude) + "," + String.valueOf(mLatitude);
        return endpoint.getReverseGeocode(AppCache.getNMapClient(), geocode);
    }

    @Override
    public void execute() {
        Observable<Placemark> runner = build();
        runner.subscribe();
    }
}
