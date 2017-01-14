package com.jhlee.android.droidwalker.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.jhlee.android.droidwalker.DroidWalkerService;
import com.jhlee.android.droidwalker.R;
import com.jhlee.android.droidwalker.model.DroidWalker;
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

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

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

        mPageCreators = new PageCreator[] {
                new PageCreator(getString(R.string.app_main_dashboard), PageCreator.TYPE_DASHBOARD),
                new PageCreator(getString(R.string.app_main_records), PageCreator.TYPE_RECORD)
        };

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        mViewPager.addOnPageChangeListener(this);

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


    //
    //----------------------------------------------------------------------------------------------
    // -- implements ViewPager.OnPageChangeListener
    //

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    //
    //----------------------------------------------------------------------------------------------
    // -- methods
    //

    private void setUpEventListener() {
        releaseEventListener();

        mDroidWalkerEventListener = RxEventManager.instance().subscribe(DroidWalker.class,
                new Action1<DroidWalker>() {
                    @Override
                    public void call(DroidWalker walker) {
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

        String title;
        int type;

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
