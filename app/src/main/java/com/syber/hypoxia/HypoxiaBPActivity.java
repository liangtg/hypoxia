package com.syber.hypoxia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.syber.base.BaseActivity;
import com.syber.base.BaseViewHolder;
import com.syber.base.view.ViewPost;
import com.syber.hypoxia.helo.BPFlow;
import com.syber.hypoxia.helo.BTManager;
import com.syber.hypoxia.widget.HoloCircularProgressBar;

import java.util.Arrays;

public class HypoxiaBPActivity extends BaseActivity implements BTManager.RequestListener {
    private BTManager bleHelper;
    private ViewHolder viewHolder;
    private boolean inProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hypoxia_bp);
        initAppBar();
        bleHelper = new BTManager();
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
        if (bleHelper.handleEnableResult(requestCode, resultCode, data)) {
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
        if (BPFlow.PROGRESS_BP == request) {
            Log.e("flow", Arrays.toString(data.getByteArrayExtra(BPFlow.KEY_PUL_ARRAY)));
            Log.e("flow", "压力:" + data.getIntExtra(BPFlow.KEY_SYS, 0));
            inProgress = true;
            viewHolder.inProgress(data.getIntExtra(BPFlow.KEY_SYS, 0));
        } else if (BPFlow.RESULT_BP == request) {
            Log.e("flow", Arrays.toString(data.getByteArrayExtra(BPFlow.KEY_PUL_ARRAY)));
            Log.e("flow", String.format("结果:%d\t%d", data.getIntExtra(BPFlow.KEY_SYS, 0), data.getIntExtra(BPFlow.KEY_DIA, 0)));
            if (inProgress) {
                Intent intent = new Intent(data);
                intent.setClass(this, AddBPActivity.class);
                startActivity(intent);
                finish();
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
