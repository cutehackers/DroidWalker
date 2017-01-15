package com.jhlee.android.droidwalker.network.endpoint;

import com.jhlee.android.droidwalker.model.Placemark;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Query;
import rx.Observable;

/**
 * DroidWalker
 * <p>
 *
 * header map
 *  X-Naver-Client-Id : AppCache.NMAP_CLIENT_ID
 *  X-Naver-Client-Secret : AppCache.NMAP_CLIENT_SECRET
 *
 * date 2017-01-15
 * author Jun-hyoung, Lee
 */

public interface NaverMapEndpoint {

    @GET("reversegeocode")
    Observable<Placemark> getReverseGeocode(
            @HeaderMap Map<String, String> headers, @Query("query") String location);
}
