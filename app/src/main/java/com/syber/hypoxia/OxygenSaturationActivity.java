package com.syber.hypoxia;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.syber.base.BaseActivity;

public class OxygenSaturationActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oxygen_saturation);
        setTitle("");
        initAppBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.measure, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (R.id.measure == id) {
            gotoActivity(MeasureOxygenActivity.class);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
