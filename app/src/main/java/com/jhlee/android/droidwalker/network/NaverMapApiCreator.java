package com.jhlee.android.droidwalker.network;

import com.jhlee.android.droidwalker.AppCache;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * DroidWalker
 * <p>
 * date 2017-01-15
 * author Jun-hyoung, Lee
 */

public class NaverMapApiCreator implements ApiCreator {
    @Override
    public RestApiType getApiType() {
        return RestApiType.NAVER_MAP;
    }

    @Override
    public <T> T build(Class<T> clazz) {
        OkHttpClient okClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Interceptor.Chain chain) throws IOException {
                        return chain.proceed(chain.request());
                    }
                })
                .build();

        Retrofit client = new Retrofit.Builder()
                .baseUrl(AppCache.NMAP_BASE_URL)
                .client(okClient)
                .addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return client.create(clazz);
    }
}
