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
import com.syber.hypoxia.data.BloodHistoryResponse;
import com.syber.hypoxia.data.IRequester;

import java.util.ArrayList;


public class BloodPressureHistoryFragment extends BaseFragment {
    private RecyclerView allHistory;
    private Bus bus = new Bus();
    private int page = 1;
    private ArrayList<BloodHistoryResponse.HistoryItem> data = new ArrayList<>();
    private HistoryAdapter historyAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bus.register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_blood_pressure_history, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        allHistory = get(R.id.all_history);
        allHistory.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        allHistory.setItemAnimator(new DefaultItemAnimator());
        historyAdapter = new HistoryAdapter();
        allHistory.setAdapter(historyAdapter);
        IRequester.getInstance().bloodData(bus, page);

    }

    @Subscribe
    public void withData(BloodHistoryResponse event) {
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
            return new AdapterHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bloodpressure_history, parent, false));
        }

        @Override
        public void onBindViewHolder(AdapterHolder holder, int position) {
            BloodHistoryResponse.HistoryItem item = data.get(position);
            holder.date.setText(item.pressure.Time_Test);
            holder.high.setText("收缩压" + item.pressure.Systolic);
            holder.low.setText("舒张压" + item.pressure.Diastolic);
            holder.rate.setText("心率" + item.pressure.HeartRate);
            holder.abnormal.setVisibility((item.pressure.Systolic < 130 && item.pressure.Diastolic < 85) ? View.GONE : View.VISIBLE);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    private class AdapterHolder extends RecyclerView.ViewHolder {
        TextView date, high, low, rate;
        View abnormal;

        public AdapterHolder(View itemView) {
            super(itemView);
            date = BaseViewHolder.get(itemView, R.id.date);
            high = BaseViewHolder.get(itemView, R.id.high);
            low = BaseViewHolder.get(itemView, R.id.low);
            rate = BaseViewHolder.get(itemView, R.id.rate);
            abnormal = BaseViewHolder.get(itemView, R.id.state);
        }
    }

}
