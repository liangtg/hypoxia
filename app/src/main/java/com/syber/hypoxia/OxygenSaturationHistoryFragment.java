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
import com.syber.hypoxia.data.IRequester;
import com.syber.hypoxia.data.OxygenSaturationHistoryResponse;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by liangtg on 16-6-6.
 */
public class OxygenSaturationHistoryFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView allHistory;
    private HistoryAdapter historyAdapter;
    private ArrayList<OxygenSaturationHistoryResponse.HistoryItem> data = new ArrayList<>();
    private Bus bus = new Bus();
    private DataProvider dataProvider = new DataProvider();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBaseActivity().startManageBus(bus, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_oxygen_saturation_history, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        allHistory = get(R.id.all_history);
        allHistory.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        allHistory.setItemAnimator(new DefaultItemAnimator());
        historyAdapter = new HistoryAdapter();
        allHistory.setAdapter(historyAdapter);
        swipeRefresh = get(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(this);
        dataProvider.refresh();
        ViewPost.postOnAnimation(view, new Runnable() {
            @Override
            public void run() {
                swipeRefresh.setRefreshing(dataProvider.onceWorked());
            }
        });
    }

    @Subscribe
    public void withData(OxygenSaturationHistoryResponse event) {
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
    public void onRefresh() {
        dataProvider.refresh();
    }

    private class HistoryAdapter extends RecyclerView.Adapter<AdapterHolder> {

        @Override
        public AdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new AdapterHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_oxygen_history, parent, false));
        }

        @Override
        public void onBindViewHolder(AdapterHolder holder, int position) {
            if (position == getItemCount() - 1) dataProvider.nextPage();
            OxygenSaturationHistoryResponse.HistoryItem item = data.get(position);
            if (null == item.spo2) return;
            holder.date.setText(item.spo2.Time_Test);
            holder.spo.setText(String.format("血氧%d%%", item.spo2.O2p));
            holder.rate.setText("心率" + item.spo2.HeartRate);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    class AdapterHolder extends RecyclerView.ViewHolder {
        TextView date, spo, rate;

        public AdapterHolder(View itemView) {
            super(itemView);
            date = BaseViewHolder.get(itemView, R.id.date);
            spo = BaseViewHolder.get(itemView, R.id.high);
            rate = BaseViewHolder.get(itemView, R.id.rate);
        }
    }

    private class DataProvider extends PageDataProvider {
        private String date = IApplication.dateFormat.format(new Date());

        @Override
        public void doWork(int page) {
            IRequester.getInstance().spoData(bus, page, date);
        }

        @Override
        public void onResetData() {
            date = IApplication.dateFormat.format(new Date());
            data.clear();
            historyAdapter.notifyDataSetChanged();
        }
    }


}
