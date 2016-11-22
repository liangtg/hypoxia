package com.syber.hypoxia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.syber.base.BaseActivity;
import com.syber.base.BaseViewHolder;
import com.syber.base.view.ViewPost;
import com.syber.hypoxia.bt.FlowExtra;
import com.syber.hypoxia.helo.BTManager;
import com.syber.hypoxia.widget.HoloCircularProgressBar;

import java.util.Arrays;

public class HypoxiaBPActivity extends BaseActivity implements BTManager.RequestListener {
    private static final int REQUEST_ADD = 0x1000;
    private BTManager bleHelper;
    private ViewHolder viewHolder;
    private boolean inProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hypoxia_bp);
        initAppBar();
        bleHelper = BTManager.instance;
        bleHelper.setRequestListener(this);
        viewHolder = new ViewHolder(findViewById(R.id.view_holder));
        ViewPost.postOnAnimation(viewHolder.getContainer(), new Runnable() {
            @Override
            public void run() {
                startFlow();
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
        bleHelper.startHypoxiaBP(HypoxiaBPActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleHelper.stop();
    }

    @Override
    public void onRequestConfirm(int request, Intent data) {
        if (isFinishing()) return;
        if (FlowExtra.PROGRESS_BP == request) {
            Log.e("flow", Arrays.toString(data.getByteArrayExtra(FlowExtra.KEY_PUL_ARRAY)));
            Log.e("flow", "压力:" + data.getIntExtra(FlowExtra.KEY_SYS, 0));
            inProgress = true;
            viewHolder.inProgress(data.getIntExtra(FlowExtra.KEY_SYS, 0));
        } else if (FlowExtra.RESULT_BP == request) {
            Log.e("flow", Arrays.toString(data.getByteArrayExtra(FlowExtra.KEY_PUL_ARRAY)));
            Log.e("flow", String.format("结果:%d\t%d", data.getIntExtra(FlowExtra.KEY_SYS, 0), data.getIntExtra(FlowExtra.KEY_DIA, 0)));
            if (inProgress) {
                inProgress = false;
                Intent intent = new Intent(data);
                intent.setClass(this, AddBPActivity.class);
                startActivityForResult(intent, REQUEST_ADD);
            }
        }
    }

    private class ViewHolder extends BaseViewHolder {
        View scan, progress;
        TextView pressure;
        HoloCircularProgressBar progressBar;

        public ViewHolder(View view) {
            super(view);
            scan = get(R.id.step_scan);
            progress = get(R.id.progress);
            pressure = get(R.id.count_down);
            progressBar = get(R.id.start);
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
        }
    }


}
