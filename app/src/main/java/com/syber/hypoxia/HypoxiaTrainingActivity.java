package com.syber.hypoxia;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.syber.base.BaseActivity;
import com.syber.base.BaseViewHolder;
import com.syber.base.view.ViewPost;
import com.syber.hypoxia.bt.FlowExtra;
import com.syber.hypoxia.bt.HypoxiaSPPTrainingFlow;
import com.syber.hypoxia.bt.SPPManager;
import com.syber.hypoxia.helo.BTManager;
import com.syber.hypoxia.widget.HoloCircularProgressBar;

public class HypoxiaTrainingActivity extends BaseActivity implements BTManager.RequestListener {
    private static final int REQUEST_ADD = 0x1000;
    private SPPManager bleHelper;
    private ViewHolder viewHolder;
    private boolean inProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hypoxia_training);
        initAppBar();
        bleHelper = new SPPManager(this);
        bleHelper.setRequestListener(this);
        viewHolder = new ViewHolder(findViewById(R.id.view_holder));
        ViewPost.postOnAnimation(viewHolder.getContainer(), new Runnable() {
            @Override
            public void run() {
//                startFlow();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_ADD == requestCode) {
            if (RESULT_OK == resultCode) gotoActivity(BloodPressureActivity.class);
            finish();
        } else if (bleHelper.handleEnableResult(requestCode, resultCode, data)) {
            startFlow();
        } else {
            finish();
        }
    }

    private void startFlow() {
        HypoxiaSPPTrainingFlow flow = new HypoxiaSPPTrainingFlow(bleHelper);
        flow.setMode(viewHolder.mode);
        bleHelper.setFlow(flow);
        bleHelper.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleHelper.stop();
    }

    @Override
    public void onRequestConfirm(int request, Intent data) {
        if (isFinishing()) return;
        if (FlowExtra.REPORT_STATE_CONNECTED == request) {
            inProgress = true;
            viewHolder.inProgress(0);
        } else if (FlowExtra.REPORT_STATE_CONNECT_FAILED == request) {
            showToast("设备连接失败,重新扫描设备...");
        } else if (FlowExtra.PROGRESS_BP == request) {
            Log.e("flow", "压力:" + data.getIntExtra(FlowExtra.KEY_SYS, 0));
            inProgress = true;
            viewHolder.inProgress(data.getIntExtra(FlowExtra.KEY_SYS, 0));
        } else if (FlowExtra.REPORT_STATE_INFO == request) {
            showAlert(data.getIntExtra(FlowExtra.KEY_ERROR, 0));
        } else if (FlowExtra.RESULT_HYPOXIA == request) {
            if (inProgress) {
                inProgress = false;
                Intent intent = new Intent(data);
                intent.setClass(this, AddTraingActivity.class);
                startActivityForResult(intent, REQUEST_ADD);
            }
        }
    }

    private void showAlert(int error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("发生错误");
        builder.setMessage(String.format("错误代码:[%d]", error));
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setPositiveButton("重试", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                bleHelper.setRequestConfirmed(FlowExtra.REPORT_STATE_INFO, 0);
            }
        });
        builder.show();
    }

    private class ViewHolder extends BaseViewHolder {
        View scan, progress, selector;
        TextView pressure;
        HoloCircularProgressBar progressBar;
        private int mode = -1;

        public ViewHolder(View view) {
            super(view);
            scan = get(R.id.step_scan);
            progress = get(R.id.progress);
            pressure = get(R.id.count_down);
            progressBar = get(R.id.start);
            selector = get(R.id.step_selector);
            get(R.id.mode0).setOnClickListener(this);
            get(R.id.mode1).setOnClickListener(this);
            get(R.id.mode2).setOnClickListener(this);
            get(R.id.mode3).setOnClickListener(this);
            get(R.id.mode4).setOnClickListener(this);
        }

        private void inProgress(int p) {
            scan.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            progressBar.setStart(true);
            pressure.setText(String.valueOf(p));
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (R.id.mode0 == id) {
                gotoScan(255);
            } else if (R.id.mode1 == id) {
                gotoScan(1);
            } else if (R.id.mode2 == id) {
                gotoScan(2);
            } else if (R.id.mode3 == id) {
                gotoScan(3);
            } else if (R.id.mode4 == id) {
                gotoScan(4);
            }
        }

        private void gotoScan(int mode) {
            if (this.mode == mode) return;
            this.mode = mode;
            TransitionManager.beginDelayedTransition((ViewGroup) getContainer());
            selector.setVisibility(View.GONE);
            scan.setVisibility(View.VISIBLE);
            startFlow();
        }

    }

}
