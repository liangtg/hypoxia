package com.syber.hypoxia.helo;

import android.content.Intent;
import android.os.Bundle;

import com.syber.base.BaseActivity;
import com.syber.hypoxia.R;

public class HeloBpActivity extends BaseActivity {
    private BleHelper bleHelper = new BleHelper("");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helo_bp);
        initAppBar();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bleHelper.handleEnableResult(requestCode, resultCode, data)) {
            finish();
        }
    }
}
