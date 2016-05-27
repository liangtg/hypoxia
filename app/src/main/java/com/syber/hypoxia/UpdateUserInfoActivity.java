package com.syber.hypoxia;

import android.os.Bundle;

import com.syber.base.BaseActivity;

public class UpdateUserInfoActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user_info);
        initAppBar();
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new UpdateUserInfoFragment(), "update_user_info").commit();
        }
    }


}
