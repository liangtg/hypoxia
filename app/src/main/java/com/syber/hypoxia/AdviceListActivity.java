package com.syber.hypoxia;

import android.os.Bundle;

import com.syber.base.BaseActivity;

public class AdviceListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advice_list);
        initAppBar();
    }
}
