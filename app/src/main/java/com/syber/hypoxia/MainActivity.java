package com.syber.hypoxia;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.pgyersdk.update.PgyUpdateManager;
import com.syber.base.BaseActivity;
import com.syber.base.BaseViewHolder;
import com.syber.hypoxia.data.SignInResponse;
import com.syber.hypoxia.data.User;

public class MainActivity extends BaseActivity {
    private ViewHolder viewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        PgyUpdateManager.register(this);
        if (!User.isSignIn()) {
            gotoActivity(SignInActivity.class);
        }
        viewHolder = new ViewHolder();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!User.isSignIn()) finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewHolder.updateUserInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (R.id.add_training == id) {
            gotoActivity(AddTraingActivity.class);
            return true;
        } else if (R.id.add_bp == id) {
            gotoActivity(AddBPActivity.class);
            return true;
        } else if (R.id.sign_out == id) {
            User.signOut();
            recreate();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ViewHolder extends BaseViewHolder {
        TextView userInfo, userName;

        public ViewHolder() {
            super(findViewById(R.id.view_holder));
            get(R.id.hypoxia).setOnClickListener(this);
            get(R.id.ecg).setOnClickListener(this);
            get(R.id.blood).setOnClickListener(this);
            userInfo = get(R.id.user_info);
            userName = get(R.id.user_name);
        }

        void updateUserInfo() {
            if (User.isSignIn()) {
                SignInResponse.UserInfoExt ext = User.getUserInfoExt();
                userInfo.setText(String.format("%s 身高%scm 体重%skg", ext.sexstring, ext.height, ext.weight));
                userName.setText(ext.fullname);
            }
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (R.id.hypoxia == id) {
                gotoActivity(HypoxiaActivity.class);
            } else if (R.id.blood == id) {
                gotoActivity(BloodPressureActivity.class);
            } else if (R.id.ecg == id) {
                try {
                    PackageInfo info = getPackageManager().getPackageInfo("com.hes.hpmobile", PackageManager.GET_ACTIVITIES);
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setClassName("com.hes.hpmobile", "com.hes.hpmobile.UI.Activities.SplashScreen");
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (PackageManager.NameNotFoundException e) {
                    showToast("您没有安装该程序");
                }
            }
        }
    }


}
