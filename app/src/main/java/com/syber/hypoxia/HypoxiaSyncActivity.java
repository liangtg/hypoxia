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
import com.syber.hypoxia.bt.FlowExtra;
import com.syber.hypoxia.bt.HypoxiaSPPFlow;
import com.syber.hypoxia.bt.SPPManager;
import com.syber.hypoxia.data.IRequester;
import com.syber.hypoxia.helo.BTManager;

import java.util.ArrayList;

public class HypoxiaSyncActivity extends BaseActivity implements BTManager.RequestListener {
    private ViewHolder viewHolder;
    //    private BTManager btManager = BTManager.instance;
    private SPPManager btManager;
    private ArrayList<Data> bpData = new ArrayList<>();
    private ArrayList<Data> hypoxiaData = new ArrayList<>();
    private Bus bus = new Bus();
    private int bpCount, hypoxiaCount;

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
        viewHolder.stateText.setText("正在交换数据");
        Log.d("flow", "" + request);
        if (request == FlowExtra.RESULT_BP) {
            Data upload = new Data();
            upload.time = data.getStringExtra(FlowExtra.KEY_TIME);
            upload.sys = data.getIntExtra(FlowExtra.KEY_SYS, 0);
            upload.dia = data.getIntExtra(FlowExtra.KEY_DIA, 0);
            upload.pul = data.getIntExtra(FlowExtra.KEY_PUL, 0);

            String time = String.format("%s\t%d\t%d\t%d",
                    data.getStringExtra(FlowExtra.KEY_TIME),
                    data.getIntExtra(FlowExtra.KEY_SYS, 0),
                    data.getIntExtra(FlowExtra.KEY_DIA, 0),
                    data.getIntExtra(FlowExtra.KEY_PUL, 0));
            bpData.add(upload);
        } else if (request == FlowExtra.RESULT_HYPOXIA) {
            Data upload = new Data();
            upload.startTime = data.getStringExtra(FlowExtra.KEY_START_TIME);
            upload.endTime = data.getStringExtra(FlowExtra.KEY_END_TIME);
            upload.mode = data.getIntExtra(FlowExtra.KEY_MODE, 0);
            hypoxiaData.add(upload);
        } else if (request == FlowExtra.REQUEST_END) {
            viewHolder.stateText.setText("数据交换完成");
            if (bpData.isEmpty() && hypoxiaData.isEmpty()) {
                Snackbar.make(viewHolder.getContainer(), "没有发现记录", Snackbar.LENGTH_SHORT).setCallback(new SnackbarCallback()).show();
            } else {
                viewHolder.stateText.append(",准备上传数据...");
                withResponse(new EmptyResponse());
            }
        }
    }

    @Subscribe
    public void withResponse(EmptyResponse event) {
        if (isFinishing()) return;
        if (!bpData.isEmpty()) {
            Data upload = bpData.remove(0);
            IRequester.getInstance().addBP(bus, upload.time, upload.sys, upload.dia, upload.pul);
            if (event.isSuccess()) bpCount++;
        } else if (!hypoxiaData.isEmpty()) {
            Data upload = hypoxiaData.remove(0);
            IRequester.getInstance().addTraing(bus, upload.startTime, upload.endTime, String.valueOf(upload.mode));
            if (event.isSuccess()) hypoxiaCount++;
        } else {
            Snackbar.make(viewHolder.getContainer(),
                    String.format("同步血压记录%d条,训练记录%d条", bpCount, hypoxiaCount),
                    Snackbar.LENGTH_SHORT).setCallback(new SnackbarCallback()).show();
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
