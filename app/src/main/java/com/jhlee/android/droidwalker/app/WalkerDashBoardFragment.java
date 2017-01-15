package com.jhlee.android.droidwalker.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.CheckableImageButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jhlee.android.droidwalker.AppCache;
import com.jhlee.android.droidwalker.R;
import com.jhlee.android.droidwalker.WalkerMiniWidgetService;
import com.jhlee.android.droidwalker.base.AndroidContext;
import com.jhlee.android.droidwalker.database.DataBase;
import com.jhlee.android.droidwalker.model.DroidWalkerPower;
import com.jhlee.android.droidwalker.model.Item;
import com.jhlee.android.droidwalker.model.Placemark;
import com.jhlee.android.droidwalker.model.WalkSet;
import com.jhlee.android.droidwalker.network.FetchAddressRestCommand;
import com.jhlee.android.droidwalker.ui.event.PermissionGrantedEvent;
import com.jhlee.android.droidwalker.ui.event.RxEventManager;
import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.maplib.NGeoPoint;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * DroidWalker
 *
 * 만보기 대쉬보드 화면
 *
 * date 2017-01-14
 * author Jun-hyoung, Lee
 */

public class WalkerDashBoardFragment extends Fragment implements View.OnClickListener,
        NMapLocationManager.OnLocationChangeListener {

    private TextView mDistanceView;
    private TextView mStepView;
    private TextView mAddressView;
    private CheckableImageButton mPowerButton;
    private View mAllowMiniContainer;
    private CheckableImageButton mAllowMiniButton;

    private TextView mPowerText;

    private NMapLocationManager mLocationManager;
    private CompositeSubscription mSubscription;

    private boolean isFetchingAddress;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpEventListener();
        mLocationManager = new NMapLocationManager(getContext());
        mLocationManager.setOnLocationChangeListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_walker_dashboard, container, false);
        mStepView = (TextView) view.findViewById(R.id.step_text);
        mDistanceView = (TextView) view.findViewById(R.id.distance_text);
        mAddressView = (TextView) view.findViewById(R.id.address_text);
        setUpPowerView(view);
        setUpAllowMiniView(view);

        fetch();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 액티비티가 생성되고 난후 이벤트 리스닝이 준비되면, 현재 상태에 따라 워커 서비스를 실행한다.
        if (isWalkerEnabled()) {
            RxEventManager.instance().post(new DroidWalkerPower(true));
        }

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.enableMyLocation(true);
            setAddressText(getString(R.string.dashboard_fetching_address));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            setMiniViewChecked(false);
        } else {
            setMiniViewChecked(!Settings.canDrawOverlays(getActivity()));
        }

        // close mini view if floating.
        Activity activity = getActivity();
        Intent service = new Intent(activity, WalkerMiniWidgetService.class);
        service.putExtra("action", WalkerMiniWidgetService.ACTION_PAUSE);
        activity.startService(service);
    }

    @Override
    public void onPause() {
        super.onPause();

        // 미니뷰 보여준다.
        if (isWalkerEnabled() && canDrawOverlays()) {
            Activity activity = getActivity();
            activity.startService(new Intent(activity, WalkerMiniWidgetService.class));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseEventListener();
        mLocationManager.removeOnLocationChangeListener(this);
    }

    //
    //----------------------------------------------------------------------------------------------
    // -- implements View.OnClickListener
    //

    @Override
    public void onClick(View view) {
        Timber.d("WalkDashBoard checkSelfPermission() ");

        final int id = view.getId();
        switch (id) {
            case R.id.power_button: {
                if (((MainActivity) getActivity()).checkGpsReady()) {
                    toggleWalker();
                }
                break;
            }

            case R.id.allow_mini_button: {
                ((MainActivity) getActivity()).checkDrawOverReady();
                break;
            }
        }


    }


    //
    //----------------------------------------------------------------------------------------------
    // -- implements NMapLocationManager.OnLocationChangeListener
    //

    @Override
    public boolean onLocationChanged(NMapLocationManager nMapLocationManager, NGeoPoint nGeoPoint) {
        // 현재 위치 변경 시 호출된다. myLocation 객체에 변경된 좌표가 전달된다.
        // 현재 위치를 계속 탐색하려면 true를 반환한다.

        new FetchAddressRestCommand()
                .longitude(nGeoPoint.getLongitude())
                .latitude(nGeoPoint.getLatitude())
                .build()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Placemark>() {
                    @Override
                    public void onStart() {
                        isFetchingAddress = true;
                    }

                    @Override
                    public void onCompleted() {
                        isFetchingAddress = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("Naver map error fetching address. %s", e.getMessage());
                        isFetchingAddress = false;
                    }

                    @Override
                    public void onNext(Placemark placemark) {
                        Timber.d("Naver map success code. %d", placemark.getCode());
                        if (placemark.getResult().getTotal() > 0) {
                            Item item = placemark.getResult().getItems().get(0);
                            Timber.d("Naver map success fetching address: %s", item.getAddress());
                            setAddressText(item.getAddress());
                        } else {
                            Timber.d("Naver map success but no address.");
                        }
                    }
                });

        Timber.d("Naver map location changed %s", nGeoPoint.toString());
        return false;
    }

    private void fetchAddress(NGeoPoint nGeoPoint) {
        new FetchAddressRestCommand()
                .longitude(nGeoPoint.getLongitude())
                .latitude(nGeoPoint.getLatitude())
                .build()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Placemark>() {
                    @Override
                    public void onStart() {
                        isFetchingAddress = true;
                    }

                    @Override
                    public void onCompleted() {
                        isFetchingAddress = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("Naver map error fetching address. %s", e.getMessage());
                        isFetchingAddress = false;
                    }

                    @Override
                    public void onNext(Placemark placemark) {

                        Timber.d("Naver map success code. %d", placemark.getCode());
                        if (placemark.getResult().getTotal() > 0) {
                            Item item = placemark.getResult().getItems().get(0);
                            Timber.d("Naver map success fetching address: %s", item.getAddress());
                            setAddressText(item.getAddress());
                        } else {
                            Timber.d("Naver map success but no address.");
                        }
                    }
                });
    }

    @Override
    public void onLocationUpdateTimeout(NMapLocationManager nMapLocationManager) {
        Timber.e("Naver map location updateSteps timeout.");
    }

    @Override
    public void onLocationUnavailableArea(NMapLocationManager nMapLocationManager, NGeoPoint nGeoPoint) {
        Timber.e("Naver map location unavailable area.");
    }


    //
    //----------------------------------------------------------------------------------------------
    //-- methods
    //

    private void setUpEventListener() {
        releaseEventListener();

        mSubscription = new CompositeSubscription();
        mSubscription.add(RxEventManager.instance().subscribe(WalkSet.class, new Action1<WalkSet>() {
            @Override
            public void call(WalkSet walkSet) {
                setStepText(walkSet.getSteps());
                setDistanceText(walkSet.getDistance());

                if (!isFetchingAddress && walkSet.getLocation() != null) {
                    fetchAddress(walkSet.getLocation());
                }
            }
        }));

        mSubscription.add(RxEventManager.instance().subscribe(PermissionGrantedEvent.class,
                new Action1<PermissionGrantedEvent>() {
                    @Override
                    public void call(PermissionGrantedEvent event) {
                        if (Manifest.permission.ACCESS_FINE_LOCATION.equals(event.getPermissions())) {
                            toggleWalker();
                        }
                    }
                }));
    }

    private void releaseEventListener() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    private void toggleWalker() {
        boolean enabled = !isWalkerEnabled();

        // 만보기 시작 혹은 종료 이벤트를 MainActivity에게 보낸다.
        RxEventManager.instance().post(new DroidWalkerPower(enabled));
        setWalkerEnabled(enabled);
        setPowerChecked(enabled);
    }

    /**
     * 현재 걸음 수 텍스트로 표시
     */
    private void setStepText(int steps) {
        mStepView.setText(String.format(getString(R.string.dashboard_step_format), steps));
    }

    /**
     * 현재 이동 거리 표시
     * @param distance 이동 거리 미터.
     */
    private void setDistanceText(int distance) {
        String text = "0 m";
        if (distance < 1000) {
            text = String.format(getString(R.string.dashboard_distance_m_format), distance);
        } else if (distance >= 1000) {
            text = String.format(getString(R.string.dashboard_distance_km_format), (float)distance/1000);
        }
        mDistanceView.setText(text);
    }

    private void setAddressText(String address) {
        mAddressView.setText(address);
    }

    /**
     * 만보기 진행상태에 따른 버튼 파워 상태 반영
     */
    private void setUpPowerView(View view) {
        boolean enabled = isWalkerEnabled();

        mPowerButton = (CheckableImageButton) view.findViewById(R.id.power_button);
        mPowerButton.setOnClickListener(this);

        mPowerText = (TextView) view.findViewById(R.id.power_text);
        setPowerChecked(enabled);
    }

    private void setPowerChecked(boolean enabled) {
        mPowerText.setText(enabled ? getString(R.string.dashboard_power_off) : getString(R.string.dashboard_power_on));
        mPowerButton.setChecked(enabled);
    }

    private void setUpAllowMiniView(View view) {
        mAllowMiniContainer = view.findViewById(R.id.allow_mini_container);
        mAllowMiniButton = (CheckableImageButton) view.findViewById(R.id.allow_mini_button);
        mAllowMiniButton.setOnClickListener(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mAllowMiniContainer.setEnabled(false);
        } else {
            mAllowMiniContainer.setEnabled(!Settings.canDrawOverlays(getActivity()));
        }
    }

    private void setMiniViewChecked(boolean enabled) {
        mAllowMiniContainer.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    /**
     * 만보기가 동작중인지 여부s
     */
    private boolean isWalkerEnabled() {
        return AndroidContext.instance().getSharedPreferences().getBoolean(AppCache.PREFS_WALKER_ENABLED, false);
    }

    /**
     * 만보기가 진행중인지 멈추었는지 값을 설정한다.
     */
    private void setWalkerEnabled(boolean enabled) {
        AndroidContext.instance().getSharedPreferences().edit()
                .putBoolean(AppCache.PREFS_WALKER_ENABLED, enabled)
                .apply();
    }

    /**
     * 데이터 베이스에 저장된 걸음 수를 표시한다.
     */
    private void fetch() {
        DataBase db = DataBase.instance();
        long today = DataBase.getToday();

        WalkSet walkSet = db.getWalkSet(today);
        setStepText((walkSet == null) ? 0 : walkSet.getSteps());
        setDistanceText((walkSet == null) ? 0 : walkSet.getDistance());

        db.close();
    }

    private boolean canDrawOverlays() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        } else {
            return Settings.canDrawOverlays(getActivity());
        }
    }

}
