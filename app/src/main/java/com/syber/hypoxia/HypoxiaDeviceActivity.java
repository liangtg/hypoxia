package com.syber.hypoxia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.syber.base.BaseActivity;
import com.syber.base.BaseViewHolder;
import com.syber.hypoxia.helo.BPFlow;
import com.syber.hypoxia.helo.BTManager;

import java.util.Arrays;

public class HypoxiaDeviceActivity extends BaseActivity implements BTManager.RequestListener {
    private BTManager bleHelper;
    private ViewHolder viewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hypoxia_device);
        initAppBar();
        bleHelper = new BTManager();
        bleHelper.setRequestListener(this);
        viewHolder = new ViewHolder(findViewById(R.id.view_holder));
    }

    private void startFlow() {
        bleHelper.startHypoxiaBP(HypoxiaDeviceActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleHelper.stop();
    }

    @Override
    public void onRequestConfirm(int request, Intent data) {
        if (BPFlow.PROGRESS_BP == request) {
            Log.e("flow", Arrays.toString(data.getByteArrayExtra(BPFlow.KEY_PUL_ARRAY)));
            Log.e("flow", "压力:" + data.getIntExtra(BPFlow.KEY_SYS, 0));
        } else if (BPFlow.RESULT_BP == request) {
            Log.e("flow", Arrays.toString(data.getByteArrayExtra(BPFlow.KEY_PUL_ARRAY)));
            Log.e("flow", String.format("结果:%d\t%d", data.getIntExtra(BPFlow.KEY_SYS, 0), data.getIntExtra(BPFlow.KEY_DIA, 0)));
        }
    }

    private class ViewHolder extends BaseViewHolder {
        private final ArrayAdapter<String> adapter;
        ListView listView;


        public ViewHolder(View view) {
            super(view);
            get(R.id.hypoxia_bp).setOnClickListener(this);
            listView = get(R.id.list);
            adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
            listView.setAdapter(adapter);
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (R.id.hypoxia_bp == id) {
                startFlow();
            }
        }
    }


}
