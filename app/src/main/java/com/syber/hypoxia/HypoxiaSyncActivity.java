package com.syber.hypoxia;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.syber.base.BaseActivity;
import com.syber.base.BaseViewHolder;
import com.syber.base.data.EmptyResponse;
import com.syber.base.view.ViewPost;
import com.syber.hypoxia.bt.HypoxiaSPPFlow;
import com.syber.hypoxia.bt.SPPManager;
import com.syber.hypoxia.data.IRequester;
import com.syber.hypoxia.helo.BPFlow;
import com.syber.hypoxia.helo.BTManager;
import com.syber.hypoxia.helo.BleFlow;

public class HypoxiaSyncActivity extends BaseActivity implements BTManager.RequestListener {
    private ViewHolder viewHolder;
    //    private BTManager btManager = BTManager.instance;
    private SPPManager btManager;
    private Data upload = new Data();
    private Bus bus = new Bus();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hypoxia_sync);
        initAppBar();
        startManageBus(bus, this);
        btManager = new SPPManager(this);
        btManager.setFlow(new HypoxiaSPPFlow(btManager));
        viewHolder = new ViewHolder(findViewById(R.id.view_holder));
        ViewPost.postOnAnimation(viewHolder.getContainer(), new Runnable() {
            @Override
            public void run() {
                start();
            }
        });
    }

    private void start() {
        btManager.setRequestListener(this);
//        btManager.startHypoxiaSync(this);
        btManager.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (btManager.handleEnableResult(requestCode, resultCode, data)) {
            start();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        btManager.stop();
    }

    @Override
    public void onRequestConfirm(int request, Intent data) {
        if (isFinishing()) return;
        viewHolder.inProgress();
        Log.d("flow", "" + request);
        if (request == BleFlow.RESULT_BP) {
            upload.time = data.getStringExtra(BPFlow.KEY_TIME);
            upload.sys = data.getIntExtra(BPFlow.KEY_SYS, 0);
            upload.dia = data.getIntExtra(BPFlow.KEY_DIA, 0);
            upload.pul = data.getIntExtra(BPFlow.KEY_PUL, 0);

            String time = String.format("%s\t%d\t%d\t%d",
                    data.getStringExtra(BPFlow.KEY_TIME),
                    data.getIntExtra(BPFlow.KEY_SYS, 0),
                    data.getIntExtra(BPFlow.KEY_DIA, 0),
                    data.getIntExtra(BPFlow.KEY_PUL, 0));
            Log.d("flow", time);
        } else if (request == BleFlow.RESULT_HYPOXIA) {
            upload.startTime = data.getStringExtra(BPFlow.KEY_START_TIME);
            upload.endTime = data.getStringExtra(BPFlow.KEY_END_TIME);
            upload.mode = data.getIntExtra(BleFlow.KEY_MODE, 0);
            Log.d("flow",
                    String.format("%s\t%s\t%d",
                            data.getStringExtra(BPFlow.KEY_START_TIME),
                            data.getStringExtra(BPFlow.KEY_END_TIME),
                            data.getIntExtra(BPFlow.KEY_MODE, 0)));
            viewHolder.stateText.setText("正在上传数据");
            IRequester.getInstance().addBP(bus, upload.time, upload.sys, upload.dia, upload.pul);
        }
    }

    @Subscribe
    public void withResponse(EmptyResponse event) {
        if (isFinishing()) return;
        if (upload.bpAdded) {
            Snackbar.make(viewHolder.getContainer(),
                    "上传训练数据" + (event.isSuccess() ? "成功" : "失败"),
                    Snackbar.LENGTH_SHORT).setCallback(new SnackbarCallback()).show();
        } else {
            upload.bpAdded = true;
            viewHolder.stateText.setText("正在上传训练数据");
            Snackbar.make(viewHolder.getContainer(), "上传血压数据" + (event.isSuccess() ? "成功" : "失败"), Snackbar.LENGTH_SHORT).show();
            IRequester.getInstance().addTraing(bus, upload.startTime, upload.endTime, String.valueOf(upload.mode));
        }
    }

    private static class Data {
        private boolean bpAdded = false;
        private int sys, dia, pul;
        private String time;
        private int mode;
        private String startTime, endTime;
    }

    private class ViewHolder extends BaseViewHolder {
        private View stepScan, stepProgress;
        private TextView stateText;

        public ViewHolder(View view) {
            super(view);
            stepScan = get(R.id.step_scan);
            stepProgress = get(R.id.step_progress);
            stepScan.setVisibility(View.VISIBLE);
            stepProgress.setVisibility(View.GONE);
            stateText = get(R.id.state_text);
        }

        private void inProgress() {
            stepScan.setVisibility(View.GONE);
            stepProgress.setVisibility(View.VISIBLE);
        }
    }

    private class SnackbarCallback extends Snackbar.Callback {
        @Override
        public void onDismissed(Snackbar snackbar, int event) {
            finish();
        }
    }


}
