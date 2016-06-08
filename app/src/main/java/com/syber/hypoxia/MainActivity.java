package com.syber.hypoxia;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.pgyersdk.update.PgyUpdateManager;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.syber.base.BaseActivity;
import com.syber.base.BaseViewHolder;
import com.syber.hypoxia.data.IRequester;
import com.syber.hypoxia.data.SignInResponse;
import com.syber.hypoxia.data.User;
import com.syber.hypoxia.data.UserSummaryResponse;

import java.text.SimpleDateFormat;

public class MainActivity extends BaseActivity {
    private ViewHolder viewHolder;
    private Bus bus = new Bus();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startManageBus(bus, this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
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
        if (User.isSignIn()) IRequester.getInstance().getUserSummary(bus, User.getUserInfoExt().user_id);
    }

    @Subscribe
    public void withSummary(UserSummaryResponse event) {
        if (isFinishing()) return;
        if (event.isSuccess()) {
            viewHolder.updateSummary(event);
        } else {
            showToast("用户数据获取失败");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (R.id.setting == id) {
            gotoActivity(UpdateUserInfoActivity.class);
        } else if (R.id.add_training == id) {
            gotoActivity(AddTraingActivity.class);
            return true;
        } else if (R.id.add_bp == id) {
            gotoActivity(AddBPActivity.class);
            return true;
        } else if (R.id.add_spo == id) {
            gotoActivity(AddSPOActivity.class);
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
        TextView hypoxiaTime, hypoxiaMode, sys, dia, oxygen, oxygenRate;
        private ImageView userImage;
        private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        public ViewHolder() {
            super(findViewById(R.id.view_holder));
            get(R.id.hypoxia).setOnClickListener(this);
            get(R.id.ecg).setOnClickListener(this);
            get(R.id.blood).setOnClickListener(this);
            get(R.id.spo2).setOnClickListener(this);
            get(R.id.manage_info).setOnClickListener(this);
            get(R.id.doctor).setOnClickListener(this);
            userInfo = get(R.id.user_info);
            userName = get(R.id.user_name);
            userImage = get(R.id.user_image);
            hypoxiaTime = get(R.id.hypoxia_time);
            hypoxiaMode = get(R.id.hypoxia_mode);
            sys = get(R.id.sys);
            dia = get(R.id.dia);
            oxygen = get(R.id.oxygen);
            oxygenRate = get(R.id.oxygen_rate);
        }

        void updateUserInfo() {
            if (User.isSignIn()) {
                SignInResponse.UserInfoExt ext = User.getUserInfoExt();
                userInfo.setText(String.format("%s 身高%scm 体重%skg", ext.sexstring, ext.height, ext.weight));
                userName.setText(ext.fullname);
                Picasso.with(MainActivity.this).load(IRequester.SERVER + "user/getavatar?id=" + User.getUserInfoExt().user_id).placeholder(R.drawable.user).into(
                        userImage);
            }
        }

        private void updateSummary(UserSummaryResponse event) {
            try {
                long time = dateFormat.parse(event.training.timeEnd).getTime();
                time -= dateFormat.parse(event.training.timeStart).getTime();
                hypoxiaTime.setText(time / 1000 / 60 + "");
            } catch (Exception e) {
                Logger.e(e, "");
                hypoxiaTime.setText("0");
            }
            hypoxiaMode.setText(event.training.trainingMode + "");
            oxygen.setText(event.spo2.O2p + "");
            oxygenRate.setText(event.spo2.HeartRate + "");
            sys.setText(event.pressure.Systolic + "");
            dia.setText(event.pressure.Diastolic + "");
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (R.id.hypoxia == id) {
                gotoActivity(HypoxiaActivity.class);
            } else if (R.id.blood == id) {
                gotoActivity(BloodPressureActivity.class);
            } else if (R.id.spo2 == id) {
                gotoActivity(OxygenSaturationActivity.class);
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
            } else if (R.id.manage_info == id) {
                gotoActivity(UpdateUserInfoActivity.class);
            }
        }
    }


}
