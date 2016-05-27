package com.syber.hypoxia;

import android.os.Bundle;

import com.syber.base.BaseActivity;

public class ResetPwdActivity extends BaseActivity {
    public static final String FRAGMENT_PRE = "FRAGMENT_PRE";
    private PreResetPwdFragment preResetPwdFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pwd);
        initAppBar();
        if (null == savedInstanceState) {
            preResetPwdFragment = new PreResetPwdFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, preResetPwdFragment, FRAGMENT_PRE).commit();
        }
    }
}
