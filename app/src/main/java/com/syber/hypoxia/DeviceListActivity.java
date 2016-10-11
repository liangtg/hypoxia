package com.syber.hypoxia;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.syber.base.BaseActivity;
import com.syber.base.BaseViewHolder;
import com.syber.hypoxia.helo.HeloBpActivity;
import com.syber.hypoxia.helo.HeloEcgActivity;
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
        try {
            Log.d("dialog", getResources().getResourceName(0x106011e));
            dialog = builder.show();
        } catch (Exception e) {
            Throwable t = e;
            while (null != t) {
                Log.e("dialog", t.toString());
                StackTraceElement[] s = t.getStackTrace();
                for (int i = 0; i < s.length; i++) {
                    Log.e("dialog", "\t" + s[i]);
                }
                t = t.getCause();
            }
            if (null != e) throw new RuntimeException(e);
        }
        dialog.findViewById(R.id.helo_option_bp).setOnClickListener(viewHolder);
        dialog.findViewById(R.id.helo_option_hr).setOnClickListener(viewHolder);
        dialog.findViewById(R.id.helo_option_ecg).setOnClickListener(viewHolder);
    }

    private class ViewHolder extends BaseViewHolder {
        private View wristbandCard;

        public ViewHolder(View view) {
            super(view);
            get(R.id.start_wristband).setOnClickListener(this);
            get(R.id.start_oxygen).setOnClickListener(this);
            get(R.id.wristband_setting).setOnClickListener(this);
            get(R.id.start_ecg_app).setOnClickListener(this);
            get(R.id.hypoxia_bp).setOnClickListener(this);
            get(R.id.hypoxia_sync).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (R.id.start_wristband == id) {
                showWristbandDialog();
            } else if (R.id.wristband_setting == id) {
            } else if (R.id.start_oxygen == id) {
                gotoActivity(MeasureOxygenActivity.class);
            } else if (R.id.helo_option_bp == id) {
                dialog.dismiss();
                gotoActivity(HeloBpActivity.class);
            } else if (R.id.helo_option_hr == id) {
                dialog.dismiss();
                gotoActivity(HeloHrActivity.class);
            } else if (R.id.helo_option_ecg == id) {
                dialog.dismiss();
                gotoActivity(HeloEcgActivity.class);
            } else if (R.id.start_ecg_app == id) {
                startEcgApp();
            } else if (R.id.hypoxia_bp == id) {
                gotoActivity(HypoxiaBPActivity.class);
            } else if (R.id.hypoxia_sync == id) {
                gotoActivity(HypoxiaSyncActivity.class);
            }
        }

        private void startEcgApp() {
            try {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setClassName("com.hes.hpmobile", "com.hes.hpmobile.UI.Activities.SplashScreen");
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                showToast("start fail:" + e.getMessage());
            }
        }
    }

}
