package com.syber.hypoxia;

import android.os.Bundle;

import com.syber.base.BaseActivity;

public class BloodPressureActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_pressure);
        initAppBar();

        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new BloodPressureChartFragment(), "bp_chart").commit();
    }

}
