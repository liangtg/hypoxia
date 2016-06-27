package com.syber.hypoxia;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.syber.base.BaseFragment;
import com.syber.base.util.Extra;

/**
 * Created by liangtg on 16-6-27.
 */
public class HeloOpFragment extends BaseFragment implements View.OnClickListener {
    private ProgressBar indeterminate;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_helo_op, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        get(R.id.helo_bp).setOnClickListener(this);
        get(R.id.helo_heart_rate).setOnClickListener(this);
        get(R.id.helo_ecg).setOnClickListener(this);
        indeterminate = get(R.id.indeterminate);
        indeterminate.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.helo_bp == id) {
            startHeloOp(HeloDeviceActivity.OP_BP);
        } else if (R.id.helo_heart_rate == id) {
            startHeloOp(HeloDeviceActivity.OP_HR);
        } else if (R.id.helo_ecg == id) {
            startHeloOp(HeloDeviceActivity.OP_ECG);
        }
    }

    private void startHeloOp(int op) {
        indeterminate.setVisibility(View.VISIBLE);
        Intent intent = new Intent(getActivity(), HeloDeviceActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(Extra.OPERATION, op);
        startActivity(intent);
    }

}
