package com.syber.hypoxia;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.syber.base.BaseFragment;
import com.syber.base.BaseViewHolder;
import com.syber.base.data.PageDataProvider;
import com.syber.base.view.ViewPost;
import com.syber.hypoxia.data.HeartHistoryResponse;
import com.syber.hypoxia.data.IRequester;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by liangtg on 16-6-22.
 */
public class HeartRateHistoryFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView allHistory;
    private Bus bus = new Bus();
    private ArrayList<HeartHistoryResponse.HistoryItem> data = new ArrayList<>();
    private HistoryAdapter historyAdapter;
    private DataProvider dataProvider = new DataProvider();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bus.register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_heart_rate_history, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        swipeRefresh = get(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(this);
        allHistory = get(R.id.all_history);
        allHistory.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        allHistory.setItemAnimator(new DefaultItemAnimator());
        historyAdapter = new HistoryAdapter();
        allHistory.setAdapter(historyAdapter);
        dataProvider.refresh();
        ViewPost.postOnAnimation(view, new Runnable() {
            @Override
            public void run() {
                swipeRefresh.setRefreshing(!dataProvider.onceWorked());
            }
        });
    }

    @Subscribe
    public void withData(HeartHistoryResponse event) {
        if (null == getView() || getActivity().isFinishing()) return;
        swipeRefresh.setRefreshing(false);
        if (event.isSuccess()) {
            data.addAll(event.list);
            historyAdapter.notifyDataSetChanged();
            dataProvider.endPage(true, !event.list.isEmpty());
        } else {
            dataProvider.endPage(false, false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }

    @Override
    public void onRefresh() {
        dataProvider.refresh();
    }


    private class HistoryAdapter extends RecyclerView.Adapter<AdapterHolder> {

        @Override
        public AdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new AdapterHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_heart_rate_history, parent, false));
        }

        @Override
        public void onBindViewHolder(AdapterHolder holder, int position) {
            if (position == getItemCount() - 1) dataProvider.nextPage();
            HeartHistoryResponse.HistoryItem item = data.get(position);
            holder.date.setText(item.time_test);
            holder.rate.setText("心率" + item.heartrate);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    private class AdapterHolder extends RecyclerView.ViewHolder {
        TextView date, rate;
        View abnormal;

        public AdapterHolder(View itemView) {
            super(itemView);
            date = BaseViewHolder.get(itemView, R.id.date);
            rate = BaseViewHolder.get(itemView, R.id.rate);
            abnormal = BaseViewHolder.get(itemView, R.id.state);
        }
    }

    private class DataProvider extends PageDataProvider {
        private String date = IApplication.dateFormat.format(new Date());

        @Override
        public void doWork(int page) {
            IRequester.getInstance().heartData(bus, page, date);
        }

        @Override
        public void onResetData() {
            date = IApplication.dateFormat.format(new Date());
            data.clear();
            historyAdapter.notifyDataSetChanged();
        }
    }


}
