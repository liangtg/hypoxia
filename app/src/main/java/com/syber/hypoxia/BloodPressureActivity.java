package com.syber.hypoxia;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import com.syber.base.BaseActivity;

public class BloodPressureActivity extends BaseActivity implements FragmentManager.OnBackStackChangedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_pressure);
        initAppBar();

        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new BloodPressureChartFragment(), "bp_chart").commit();
        getSupportFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_bp, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.all_history:
                item.setVisible(false);
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                        new BloodPressureHistoryFragment(),
                        "bp_history").addToBackStack("bp_history").commit();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackStackChanged() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            supportInvalidateOptionsMenu();
        }
    }
}
