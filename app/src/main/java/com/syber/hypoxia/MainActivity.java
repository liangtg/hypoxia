package com.syber.hypoxia;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.Calendar;

public class MainActivity extends BaseActivity {
    private ViewHolder viewHolder;
    private Bus bus = new Bus();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startManageBus(bus, this);
        viewHolder = new ViewHolder();
        setSupportActionBar(viewHolder.toolbar);
        PgyUpdateManager.register(this);
        if (!User.isSignIn()) {
            gotoActivity(SignInActivity.class);
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        } else if (R.id.reset_pwd == id) {
            gotoActivity(ResetPwdActivity.class);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ViewHolder extends BaseViewHolder {
        TextView userInfo, userName;
        TextView hypoxiaTime, hypoxiaMode, sys, dia, oxygen, heartRate, lipidType, lipidValue, lipidUnit;
        Toolbar toolbar;
        TextView level;
        private ImageView userImage;
        private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        private View hypoxia, ecg, blood, spo2, bloodLipid;
        private TextView info1, info2, info3;

        public ViewHolder() {
            super(findViewById(R.id.view_holder));
            level = get(R.id.level);
            toolbar = get(R.id.app_bar);
            hypoxia = get(R.id.hypoxia);
            hypoxia.setOnClickListener(this);
            ecg = get(R.id.ecg);
            ecg.setOnClickListener(this);
            blood = get(R.id.blood);
            blood.setOnClickListener(this);
            spo2 = get(R.id.spo2);
            spo2.setOnClickListener(this);
            bloodLipid = get(R.id.blood_lipid);
            bloodLipid.setOnClickListener(this);
            userInfo = get(R.id.user_info);
            userName = get(R.id.user_name);
            userImage = get(R.id.user_image);
            hypoxiaTime = get(R.id.hypoxia_time);
            hypoxiaMode = get(R.id.hypoxia_mode);
            sys = get(R.id.sys);
            dia = get(R.id.dia);
            oxygen = get(R.id.oxygen);
            heartRate = get(R.id.heart_rate);
            lipidType = get(R.id.lipid_type);
            lipidValue = get(R.id.lipid_value);
            lipidUnit = get(R.id.lipid_unit);
            get(R.id.action1).setOnClickListener(this);
            get(R.id.action2).setOnClickListener(this);
            get(R.id.action3).setOnClickListener(this);
            info1 = get(R.id.info1);
            info2 = get(R.id.info2);
            info3 = get(R.id.info3);
        }

        void updateUserInfo() {
            if (User.isSignIn()) {
                SignInResponse.UserInfoExt ext = User.getUserInfoExt();
//                userInfo.setText(String.format("%s 身高%scm 体重%skg", ext.sexstring, ext.height, ext.weight));
                userName.setText(ext.fullname);
                Picasso.with(MainActivity.this).load(IRequester.SERVER + "user/getavatar?id=" + User.getUserInfoExt().user_id).placeholder(R.drawable.user).into(
                        userImage);
                Calendar user = Calendar.getInstance();
                user.setTimeInMillis(Long.parseLong(ext.birthday));
                Calendar now = Calendar.getInstance();
                int year = now.get(Calendar.YEAR) - user.get(Calendar.YEAR);
                if (year > 0) {
                    now.set(Calendar.YEAR, user.get(Calendar.YEAR));
                    if (now.after(user)) year--;
                }
                info1.setText(String.format("%02d岁", year));
                info2.setText(String.format("身高%scm", ext.height));
                info3.setText(String.format("体重%skg", ext.weight));
            }
        }

        private void updateSummary(UserSummaryResponse event) {
            if (null != event.training) {
                long time = Long.parseLong(event.training.time_end);
                time -= Long.parseLong(event.training.time_start);
                hypoxiaTime.setText(time / 1000 / 60 + "");
                hypoxiaMode.setText(event.training.mode + "");
                hypoxia.setVisibility(View.VISIBLE);
            } else {
                hypoxia.setVisibility(View.GONE);
            }
            if (null != event.spo2) {
                oxygen.setText(event.spo2.o2p + "");
                spo2.setVisibility(View.VISIBLE);
            } else {
                spo2.setVisibility(View.GONE);
            }
            if (null != event.pressure) {
                sys.setText(event.pressure.systolic + "");
                dia.setText(event.pressure.diastolic + "");
                blood.setVisibility(View.VISIBLE);
            } else {
                blood.setVisibility(View.GONE);
            }
            if (null != event.heartrate) {
                heartRate.setText(event.heartrate.heartrate + "");
                ecg.setVisibility(View.VISIBLE);
            } else {
                ecg.setVisibility(View.GONE);
            }
            bloodLipid.setVisibility(View.GONE);
            if (null != event.bloodfat) {
                if (!TextUtils.isEmpty(event.bloodfat.cholvalue)) {
                    lipidType.setText("血脂");
                    lipidValue.setText(event.bloodfat.cholvalue);
                    lipidUnit.setText("mmol/L");
                    bloodLipid.setVisibility(View.VISIBLE);
                }
            }
            if (null != event.bloodsugar && !TextUtils.isEmpty(event.bloodsugar.sugarvalue)) {
                lipidType.setText("血糖");
                lipidValue.setText(event.bloodsugar.sugarvalue);
                lipidUnit.setText("mmol/L");
                bloodLipid.setVisibility(View.VISIBLE);
            }
            if (null != event.uricacid && !TextUtils.isEmpty(event.uricacid.uricacidvalue)) {
                lipidType.setText("血尿酸");
                lipidValue.setText(event.uricacid.uricacidvalue);
                lipidUnit.setText("mmol/L");
                bloodLipid.setVisibility(View.VISIBLE);
            }
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
                gotoActivity(HeartRateActivity.class);
            } else if (R.id.blood_lipid == id) {
                gotoActivity(BloodLipidActivity.class);
            } else if (R.id.action1 == id) {
                gotoActivity(HypoxiaTrainingActivity.class);
            } else if (R.id.action2 == id) {
                gotoActivity(HypoxiaBPActivity.class);
            } else if (R.id.action3 == id) {
                gotoActivity(HypoxiaSyncActivity.class);
            }
        }
    }

}
