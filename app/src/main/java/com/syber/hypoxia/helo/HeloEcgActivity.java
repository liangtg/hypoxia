package com.syber.hypoxia.helo;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.syber.base.BaseActivity;
import com.syber.base.BaseViewHolder;
import com.syber.base.view.ViewPost;
import com.syber.hypoxia.IApplication;
import com.syber.hypoxia.R;
import com.syber.hypoxia.data.BloodHistoryResponse;

import java.util.ArrayList;
import java.util.Date;

public class HeloEcgActivity extends BaseActivity implements BleHelper.RequestListener {
    private ArrayList<BloodHistoryResponse.HistoryItem> data = new ArrayList<>();

    private BleHelper bleHelper;
    private ConnectHeloFragment connectHeloFragment;
    private ViewHolder viewHolder;
    private Bus bus = new Bus();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helo_ecg);
        initAppBar();
        bleHelper = new BleHelper("HeloHL01", new PreBindFlow());
        bleHelper.setRequestListener(this);
        connectHeloFragment = new ConnectHeloFragment();
        connectHeloFragment.show(getSupportFragmentManager(), "connect");
        viewHolder = new ViewHolder();
        ViewPost.postOnAnimation(getWindow().getDecorView(), new Runnable() {
            @Override
            public void run() {
                bleHelper.startFlow(HeloEcgActivity.this);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bleHelper.handleEnableResult(requestCode, resultCode, data)) {
            finish();
        } else {
            bleHelper.startFlow(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleHelper.endFlow();
        bleHelper.setRequestListener(null);
        bleHelper = null;
    }

    @Override
    public void onRequestConfirm(int request, Intent data) {
        if (BleFlow.REQUEST_MATCHED == request) {
            connectHeloFragment.dismiss();
            bleHelper.setRequestConfirmed(request, BleFlow.CONFIRM_OK);
            new Timer().start();
        } else if (BleFlow.REQUEST_BIND == request) {
            showToast("发现设备,准备绑定");
            bleHelper.setRequestConfirmed(request, BleFlow.CONFIRM_OK);
        } else if (BleFlow.REQUEST_BINDED_OTHER == request) {
            viewHolder.ecgView.setClickable(false);
            connectHeloFragment.dismiss();
            new HeloBindedOtherFragment().show(getSupportFragmentManager(), "connected_other");
        }
    }

    private class ViewHolder extends BaseViewHolder {
        EcgView ecgView;
        RecyclerView recyclerView;

        public ViewHolder() {
            super(findViewById(R.id.view_holder));
            recyclerView = get(R.id.list);
            recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext(), LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(new HistoryAdapter());
            ecgView = get(R.id.ecg_view);
            ecgView.setClickable(false);
            ecgView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (R.id.ecg_view == id) {
                connectHeloFragment.show(getSupportFragmentManager(), "connect");
                bleHelper.startFlow(HeloEcgActivity.this);
                v.setClickable(false);
            }
        }
    }

    private class Timer extends CountDownTimer {
        public Timer() {
            super(40 * 1000, 500);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            viewHolder.ecgView.start(true);
        }

        @Override
        public void onFinish() {
            viewHolder.ecgView.start(false);
            BloodHistoryResponse.HistoryItem item = new BloodHistoryResponse.HistoryItem();
            String start = IApplication.dateFormat.format(new Date());
            item.pressure = new BloodHistoryResponse.Pressure();
            item.pressure.Time_Test = start;
            data.add(0, item);
            viewHolder.recyclerView.getAdapter().notifyItemInserted(0);
        }
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
            holder.high.setText("正常");
            holder.low.setText("舒张压" + item.pressure.Diastolic);
            holder.low.setVisibility(View.GONE);
            holder.rate.setText("心率" + item.pressure.HeartRate);
            holder.rate.setVisibility(View.GONE);
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
