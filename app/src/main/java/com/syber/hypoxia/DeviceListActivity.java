package com.syber.hypoxia;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.syber.base.BaseActivity;
import com.syber.base.BaseViewHolder;
import com.syber.hypoxia.helo.HeloBpActivity;
import com.syber.hypoxia.helo.HeloHrActivity;

public class DeviceListActivity extends BaseActivity {
    private ViewHolder viewHolder;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        initAppBar();
        viewHolder = new ViewHolder(findViewById(R.id.view_holder));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewHolder = null;
    }

    private void showWristbandDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.dialog_helo_option);
        builder.setTitle("健康手环");
        dialog = builder.show();
        dialog.findViewById(R.id.helo_option_bp).setOnClickListener(viewHolder);
        dialog.findViewById(R.id.helo_option_hr).setOnClickListener(viewHolder);
        dialog.findViewById(R.id.helo_option_ecg).setOnClickListener(viewHolder);
    }

    private class ViewHolder extends BaseViewHolder {
        private View wristbandCard;

        public ViewHolder(View view) {
            super(view);
            wristbandCard = get(R.id.wristband_card);
            get(R.id.start_wristband).setOnClickListener(this);
            get(R.id.start_oxygen).setOnClickListener(this);
            get(R.id.wristband_setting).setOnClickListener(this);
            get(R.id.add_device).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (R.id.start_wristband == id) {
                showWristbandDialog();
            } else if (R.id.wristband_setting == id) {
            } else if (R.id.start_oxygen == id) {
                gotoActivity(MeasureOxygenActivity.class);
            } else if (R.id.add_device == id) {
                gotoActivity(HeloDeviceActivity.class);
            } else if (R.id.helo_option_bp == id) {
                dialog.dismiss();
                gotoActivity(HeloBpActivity.class);
            } else if (R.id.helo_option_hr == id) {
                dialog.dismiss();
                gotoActivity(HeloHrActivity.class);
            } else if (R.id.helo_option_ecg == id) {
                dialog.dismiss();
            }
        }
    }

}
