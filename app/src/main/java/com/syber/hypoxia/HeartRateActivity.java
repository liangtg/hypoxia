package com.syber.hypoxia;

import android.os.Bundle;

import com.syber.base.BaseActivity;

public class HeartRateActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate);
        initAppBar();
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new HeartRateChartFragment(), "heart_rate").commit();
        }
    }
}
