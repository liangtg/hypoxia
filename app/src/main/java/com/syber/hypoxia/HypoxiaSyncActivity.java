package com.syber.hypoxia;

import android.os.Bundle;
import android.view.View;

import com.syber.base.BaseActivity;
import com.syber.base.BaseViewHolder;

public class HypoxiaSyncActivity extends BaseActivity {
    private ViewHolder viewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hypoxia_sync);
        initAppBar();
        viewHolder = new ViewHolder(findViewById(R.id.view_holder));
    }

    private class ViewHolder extends BaseViewHolder {
        public ViewHolder(View view) {
            super(view);
        }
    }


}
