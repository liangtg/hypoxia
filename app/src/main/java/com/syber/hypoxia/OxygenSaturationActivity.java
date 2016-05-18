package com.syber.hypoxia;

import android.os.Bundle;

import com.syber.base.BaseActivity;

public class OxygenSaturationActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oxygen_saturation);
        setTitle("");
        initAppBar();
    }
}
