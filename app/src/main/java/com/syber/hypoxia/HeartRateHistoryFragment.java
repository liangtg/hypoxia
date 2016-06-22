package com.syber.hypoxia;

import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.syber.hypoxia.data.HeartHistoryResponse;
import com.syber.hypoxia.data.IRequester;

import java.util.ArrayList;

/**
 * Created by liangtg on 16-6-22.
 */
public class HeartRateHistoryFragment extends BaseFragment {
    private RecyclerView allHistory;
    private Bus bus = new Bus();
    private int page = 1;
    private ArrayList<HeartHistoryResponse.HistoryItem> data = new ArrayList<>();
    private HistoryAdapter historyAdapter;

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
        allHistory = get(R.id.all_history);
        allHistory.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        allHistory.setItemAnimator(new DefaultItemAnimator());
        historyAdapter = new HistoryAdapter();
        allHistory.setAdapter(historyAdapter);
        IRequester.getInstance().heartData(bus, page);
    }

    @Subscribe
    public void withData(HeartHistoryResponse event) {
        if (null == getView() || getActivity().isFinishing()) return;
        if (event.isSuccess()) {
            data.addAll(event.list);
            historyAdapter.notifyDataSetChanged();
            page++;
            if (!event.list.isEmpty()) nextDelayRequest();
        } else {
            nextDelayRequest();
        }
    }

    private void nextDelayRequest() {
        allHistory.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (null == getView() || getActivity().isFinishing()) return;
                IRequester.getInstance().bloodData(bus, page);
            }
        }, 500);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }


    private class HistoryAdapter extends RecyclerView.Adapter<AdapterHolder> {

        @Override
        public AdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new AdapterHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_heart_rate_history, parent, false));
        }

        @Override
        public void onBindViewHolder(AdapterHolder holder, int position) {
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

}
