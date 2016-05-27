package com.syber.hypoxia;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.syber.base.BaseFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreResetPwdFragment extends BaseFragment implements View.OnClickListener {

    public PreResetPwdFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pre_reset_pwd, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        get(R.id.user_name_container).setOnClickListener(this);
        get(R.id.user_sex_container).setOnClickListener(this);
        get(R.id.user_weight_container).setOnClickListener(this);
        get(R.id.user_height_container).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
    }
}
