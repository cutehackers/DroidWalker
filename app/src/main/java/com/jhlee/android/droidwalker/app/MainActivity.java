package com.jhlee.android.droidwalker.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.jhlee.android.droidwalker.DroidWalkerService;
import com.jhlee.android.droidwalker.R;
import com.jhlee.android.droidwalker.model.DroidWalkerPower;
import com.jhlee.android.droidwalker.ui.event.PermissionGrantedEvent;
import com.jhlee.android.droidwalker.ui.event.RxEventManager;

import rx.Subscription;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * DroidWalker
 *
 * date 2017-01-13
 * author Jun-hyoung, Lee
 */

public class MainActivity extends AppCompatActivity {

    private static final int RC_HANDLE_PERMISSION_LOCATION = 0X0101;
    private static final int RC_HANDLE_PERMISSION_DRAW_OVER = 0X0102;

    private View mRoot;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private PageCreator[] mPageCreators;

    private Intent mDroidWalkerService;

    private Subscription mDroidWalkerEventListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRoot = findViewById(R.id.activity_main);

        mPageCreators = new PageCreator[] {
                new PageCreator(getString(R.string.app_main_dashboard), PageCreator.TYPE_DASHBOARD),
                new PageCreator(getString(R.string.app_main_records), PageCreator.TYPE_RECORD)
        };

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

        mTabLayout = (TabLayout) findViewById(R.id.tablayout);
        mTabLayout.setupWithViewPager(mViewPager);

        mDroidWalkerService = new Intent(this, DroidWalkerService.class);
        setUpEventListener();

        Timber.d("MainActivity onCreate()");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseEventListener();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_HANDLE_PERMISSION_DRAW_OVER) {

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RC_HANDLE_PERMISSION_LOCATION) {
            if (verifyPermissions(grantResults)) {
                // Camera permission has been granted, preview can be displayed
                RxEventManager.instance().post(new PermissionGrantedEvent(Manifest.permission.ACCESS_FINE_LOCATION));

            } else {
                Snackbar.make(mRoot, R.string.permissions_not_granted, Snackbar.LENGTH_SHORT).show();
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //
    //----------------------------------------------------------------------------------------------
    // -- methods
    //

    private void setUpEventListener() {
        releaseEventListener();

        // 만보기 시작 멈춤 이벤트 핸들러
        mDroidWalkerEventListener = RxEventManager.instance().subscribe(DroidWalkerPower.class,
                new Action1<DroidWalkerPower>() {
                    @Override
                    public void call(DroidWalkerPower walker) {
                        if (walker.enable()) {
                            startService(mDroidWalkerService);
                        } else {
                            stopService(mDroidWalkerService);
                        }
                    }
                });
    }

    private void releaseEventListener() {
        if (mDroidWalkerEventListener != null && !mDroidWalkerEventListener.isUnsubscribed()) {
            mDroidWalkerEventListener.unsubscribe();
        }
    }

    boolean checkGpsReady() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // location permission has not been granted.
            requestGpsPermission();
            return false;

        } else {

            return true;
        }
    }

    void checkDrawOverReady() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, RC_HANDLE_PERMISSION_DRAW_OVER);
        }
    }

    private void requestGpsPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Snackbar.make(mRoot, R.string.app_main_permission, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.app_ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION},
                                    RC_HANDLE_PERMISSION_LOCATION);
                        }
                    }).show();
        } else {

            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    RC_HANDLE_PERMISSION_LOCATION);
        }
    }

    public static boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if(grantResults.length < 1){
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    /**
     * 메인 페이지 프라그먼트 어댑터
     */
    private class MyPagerAdapter extends FragmentStatePagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mPageCreators.length;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case PageCreator.TYPE_DASHBOARD:
                case PageCreator.TYPE_RECORD: {
                    return mPageCreators[position].create();
                }
                default: return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mPageCreators[position].title;
        }
    }


    /**
     * 페이지 프라그먼트 인스턴스를 생성하는 헬퍼
     *  1. 대시보드
     *  2. 내 기록
     */
    private static class PageCreator {
        static final int TYPE_DASHBOARD = 0;
        static final int TYPE_RECORD = 1;

        final String title;
        final int type;

        PageCreator(String title, int type) {
            this.title = title;
            this.type = type;
        }

        final Fragment create() {
            switch (type) {
                case TYPE_DASHBOARD: {
                    return new WalkerDashBoardFragment();
                }
                case TYPE_RECORD: {
                    return new WalkerRecordsFragment();
                }
                default: return null;
            }
        }
    }
}
