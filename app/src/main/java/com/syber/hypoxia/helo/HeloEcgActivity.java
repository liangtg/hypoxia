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
import com.syber.hypoxia.bt.FlowExtra;
import com.syber.hypoxia.data.BloodHistoryResponse;

import java.util.ArrayList;
import java.util.Date;

public class HeloEcgActivity extends BaseActivity implements BTManager.RequestListener {
    int ecgPosition = 0;
    private int sys, dia, pul, ecg = -1;
    private ArrayList<BloodHistoryResponse.HistoryItem> data = new ArrayList<>();
    private BTManager bleHelper;
    private ConnectHeloFragment connectHeloFragment;
    private ViewHolder viewHolder;
    private Bus bus = new Bus();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helo_ecg);
        initAppBar();
        bleHelper = new BTManager();
        bleHelper.setRequestListener(this);
        connectHeloFragment = new ConnectHeloFragment();
        connectHeloFragment.show(getSupportFragmentManager(), "connect");
        viewHolder = new ViewHolder();
        ViewPost.postOnAnimation(getWindow().getDecorView(), new Runnable() {
            @Override
            public void run() {
                bleHelper.startHeloECG(HeloEcgActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bleHelper.handleEnableResult(requestCode, resultCode, data)) {
            finish();
        } else {
            bleHelper.start(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleHelper.stop();
        bleHelper.setRequestListener(null);
        bleHelper = null;
    }

    @Override
    public void onRequestConfirm(int request, Intent data) {
        if (FlowExtra.REQUEST_MATCHED == request) {
            connectHeloFragment.dismiss();
            bleHelper.setRequestConfirmed(request, FlowExtra.CONFIRM_OK);
            new Timer().start();
        } else if (FlowExtra.REQUEST_BIND == request) {
            showToast("发现设备,准备绑定");
            bleHelper.setRequestConfirmed(request, FlowExtra.CONFIRM_OK);
        } else if (FlowExtra.REQUEST_BINDED_OTHER == request) {
            viewHolder.ecgView.setClickable(false);
            connectHeloFragment.dismiss();
            new HeloBindedOtherFragment().show(getSupportFragmentManager(), "connected_other");
        } else if (FlowExtra.RESULT_RAW_PUL == request) {
            int[] extra = data.getIntArrayExtra(FlowExtra.KEY_PUL_ARRAY);
            for (int i = 0; i < extra.length; i++) {
                viewHolder.ecgView.addPoint(extra[i]);
            }
        } else if (FlowExtra.RESULT_RAW_ECG == request) {
            int[] extra = data.getIntArrayExtra(FlowExtra.KEY_ECG_ARRAY);
            viewHolder.ecgView.addPoint((extra[0] - 400) / 200f * 1200);
//            if (ecgPosition == 0) {
//                for (int i = 0; i < extra.length; i++) {
//                }
//            }
            ecgPosition++;
            ecgPosition %= 4;
        } else if (FlowExtra.RESULT_BP == request) {
            sys = data.getIntExtra(FlowExtra.KEY_SYS, 0);
            dia = data.getIntExtra(FlowExtra.KEY_DIA, 0);
        } else if (FlowExtra.RESULT_HR == request) {
            pul = data.getIntExtra(FlowExtra.KEY_PUL, 0);
        } else if (FlowExtra.RESULT_ECG == request) {
            ecg = data.getIntExtra(FlowExtra.KEY_ECG, 0);
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
                bleHelper.start(HeloEcgActivity.this);
                v.setClickable(false);
            }
        }
    }

    private class Timer extends CountDownTimer {
        public Timer() {
            super(120 * 1000, 500);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (isFinishing()) {
                cancel();
                return;
            }
            viewHolder.ecgView.start(true);
        }

        @Override
        public void onFinish() {
            if (isFinishing()) return;
            viewHolder.ecgView.start(false);
            bleHelper.stop();
            BloodHistoryResponse.HistoryItem item = new BloodHistoryResponse.HistoryItem();
            String start = IApplication.dateFormat.format(new Date());
            item.pressure = new BloodHistoryResponse.Pressure();
            item.pressure.Time_Test = start;
            item.pressure.Systolic = sys;
            item.pressure.Diastolic = dia;
            item.pressure.HeartRate = pul;
            item.training = String.valueOf(ecg);
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
            holder.high.setText("收缩压" + item.pressure.Systolic);
            holder.low.setText("舒张压" + item.pressure.Diastolic);
//            holder.low.setVisibility(View.GONE);
            holder.rate.setText("心率" + item.pressure.HeartRate + ", " + item.training);
//            holder.rate.setVisibility(View.GONE);
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
