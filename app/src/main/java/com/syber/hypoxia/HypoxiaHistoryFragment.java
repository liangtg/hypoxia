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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.syber.base.BaseFragment;
import com.syber.base.BaseViewHolder;
import com.syber.base.data.DataRequester;
import com.syber.base.data.PageDataProvider;
import com.syber.base.view.ViewPost;
import com.syber.hypoxia.data.HypoxiaHistoryResponse;
import com.syber.hypoxia.data.IRequester;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by liangtg on 16-6-6.
 */
public class HypoxiaHistoryFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView allHistory;
    private SwipeRefreshLayout swipeRefresh;
    private ArrayList<HypoxiaHistoryResponse.HistoryItem> data = new ArrayList<>();
    private HistoryAdapter historyAdapter;
    private Bus bus = new Bus();
    private DataProvicer dataProvicer = new DataProvicer();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBaseActivity().startManageBus(bus, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hypoxia_history, container, false);
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
        dataProvicer.refresh();
        ViewPost.postOnAnimation(view, new Runnable() {
            @Override
            public void run() {
                swipeRefresh.setRefreshing(!dataProvicer.onceWorked());
            }
        });
    }

    @Subscribe
    public void withData(HypoxiaHistoryResponse event) {
        if (null == getView() || getActivity().isFinishing()) return;
        swipeRefresh.setRefreshing(false);
        if (event.isSuccess()) {
            int old = data.size();
            data.addAll(event.list);
            historyAdapter.notifyItemRangeInserted(old, event.list.size());
            dataProvicer.endPage(true, !event.list.isEmpty());
        } else {
            dataProvicer.endPage(false, false);
        }
    }

    @Override
    public void onRefresh() {
        dataProvicer.refresh();
    }

    private class HistoryAdapter extends RecyclerView.Adapter<AdapterHolder> {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);

        @Override
        public AdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new AdapterHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hypoxia_history, parent, false));
        }

        @Override
        public void onBindViewHolder(AdapterHolder holder, int position) {
            if (position == getItemCount() - 1) dataProvicer.nextPage();
            HypoxiaHistoryResponse.HistoryItem item = data.get(position);
            holder.date.setText(dateFormat.format(new Date(item.time_start)));
            holder.time.setText(String.format("%s~%s", timeFormat.format(new Date(item.time_start)), timeFormat.format(new Date(item.time_end))));
            long minute = (item.time_end - item.time_start) / 1000 / 60;
            holder.minute.setText(String.format("%d分钟", minute));
            if (null != item.training) holder.mode.setText("模式" + item.training.trainingMode);
            holder.progressBar.setProgress((int) minute);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    private class AdapterHolder extends RecyclerView.ViewHolder {
        TextView date, time, minute, mode;
        ProgressBar progressBar;

        public AdapterHolder(View itemView) {
            super(itemView);
            date = BaseViewHolder.get(itemView, R.id.date);
            time = BaseViewHolder.get(itemView, R.id.time);
            minute = BaseViewHolder.get(itemView, R.id.minute);
            mode = BaseViewHolder.get(itemView, R.id.mode);
            progressBar = BaseViewHolder.get(itemView, R.id.progress);
        }
    }

    private class DataProvicer extends PageDataProvider {
        private String date;
        private DataRequester.DataRequest request;

        @Override
        public void doWork(int page) {
            request = IRequester.getInstance().hypoxiaData(bus, page, date);
        }

        @Override
        public void onResetData() {
            date = IApplication.dateFormat.format(new Date());
            data.clear();
            historyAdapter.notifyDataSetChanged();
        }
    }


}
