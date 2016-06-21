package com.syber.hypoxia;

import android.os.Bundle;

import com.syber.base.BaseActivity;

public class DeviceListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        initAppBar();
    }
}
