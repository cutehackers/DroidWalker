package com.jhlee.android.droidwalker.app;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jhlee.android.droidwalker.R;
import com.jhlee.android.droidwalker.base.AndroidContext;
import com.jhlee.android.droidwalker.database.DataBase;
import com.jhlee.android.droidwalker.model.DailyWalkSet;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * DroidWalker
 *
 * 만보기 기록
 *
 * date 2017-01-14
 * author Jun-hyoung, Lee
 */

public class WalkerRecordsFragment extends Fragment {

    private RecyclerView mRecordsView;
    private RecordsAdapter mAdapter;
    private boolean isFetching;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_walker_records, container, false);
        mRecordsView = (RecyclerView) view.findViewById(R.id.recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecordsView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecordsView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mAdapter = new RecordsAdapter();
        mRecordsView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetch();
    }

    private void fetch() {
        if (isFetching) {
            return;
        }

        isFetching = true;

        Observable<List<DailyWalkSet>> fetcher = Observable.create(new Observable.OnSubscribe<List<DailyWalkSet>>() {
            @Override
            public void call(Subscriber<? super List<DailyWalkSet>> subscriber) {
                try {
                    DataBase db = DataBase.instance();
                    subscriber.onNext(db.getRecords());
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });

        fetcher.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<DailyWalkSet>>() {
                    @Override
                    public void onNext(List<DailyWalkSet> dailyWalkSets) {
                        mAdapter.setDataList(dailyWalkSets);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("failed to fetch records. %s", e);
                        isFetching = false;
                    }

                    @Override
                    public void onCompleted() {
                        isFetching = false;
                    }
                });
    }


    private static class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.ViewHolder> {

        private Context context;
        private List<DailyWalkSet> dailyWalkSets;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView dateView;
            private final TextView stepView;

            public ViewHolder(View v) {
                super(v);
                dateView = (TextView) v.findViewById(R.id.date);
                stepView = (TextView) v.findViewById(R.id.step);
            }

            public void setDateView(long date) {
                dateView.setText(DataBase.getTimeString(date));
            }

            public void setStepView(String stepText) {
                stepView.setText(stepText);
            }
        }

        public RecordsAdapter() {
            context = AndroidContext.instance().get();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.record_item_layout, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            DailyWalkSet dataSet = dailyWalkSets.get(position);
            holder.setDateView(dataSet.getDate());
            holder.setStepView(String.format(context.getString(R.string.dashboard_step_format), dataSet.getSteps()));
        }

        @Override
        public int getItemCount() {
            return (dailyWalkSets == null) ? 0 : dailyWalkSets.size();
        }

        public void setDataList(List<DailyWalkSet> dailyWalkSets) {
            this.dailyWalkSets = dailyWalkSets;
            notifyDataSetChanged();
        }
    }

}
