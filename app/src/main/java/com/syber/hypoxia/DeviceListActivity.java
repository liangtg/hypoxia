package com.syber.hypoxia;

import android.os.Bundle;
import android.view.View;

import com.syber.base.BaseActivity;

public class DeviceListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        initAppBar();
        findViewById(R.id.add_device).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity(HeloDeviceActivity.class);
            }
        });
    }
}
