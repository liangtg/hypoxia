package com.syber.hypoxia;

import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.PopupWindowCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.pgyersdk.update.PgyUpdateManager;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.syber.base.BaseActivity;
import com.syber.base.BaseViewHolder;
import com.syber.base.view.ViewPost;
import com.syber.hypoxia.data.IRequester;
import com.syber.hypoxia.data.SignInResponse;
import com.syber.hypoxia.data.User;
import com.syber.hypoxia.data.UserSummaryResponse;

import java.text.SimpleDateFormat;

public class MainActivity extends BaseActivity {
    private PopupWindow guideWindow;
    private ViewHolder viewHolder;
    private Bus bus = new Bus();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        startService(new Intent(this, HeloService.class));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startManageBus(bus, this);
        viewHolder = new ViewHolder();
        setSupportActionBar(viewHolder.toolbar);
        PgyUpdateManager.register(this);
        if (!User.isSignIn()) {
            gotoActivity(SignInActivity.class);
        } else {
            ViewPost.postOnAnimation(viewHolder.getContainer(), new Runnable() {
                @Override
                public void run() {
                    viewHolder.toolbar.showOverflowMenu();
                    viewHolder.getContainer().postOnAnimationDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showGuide1();
                        }
                    }, 50);
                }
            });
        }
    }

    @NonNull
    private PopupWindow ensurePopupWindow() {
        if (null == guideWindow) {
            guideWindow = new PopupWindow(MainActivity.this);
            guideWindow.setBackgroundDrawable(new ColorDrawable(0x00FFFFFF));
            guideWindow.setWidth(-1);
            guideWindow.setFocusable(true);
            guideWindow.setHeight(-1);
            guideWindow.setOnDismissListener(new PopupDismissListener());
            PopupWindowCompat.setWindowLayoutType(guideWindow, WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL);
        }
        return guideWindow;
    }

    private void showGuide1() {
        PopupWindow window = ensurePopupWindow();
        View view = getLayoutInflater().inflate(R.layout.guide1, null);
        View space = view.findViewById(R.id.space);
        ViewGroup.LayoutParams params = space.getLayoutParams();
        TypedArray attrs = getTheme().obtainStyledAttributes(new int[]{R.attr.dropdownListPreferredItemHeight});
        float iheight = attrs.getDimension(0, 10);
        attrs.recycle();
        params.height = (int) (viewHolder.toolbar.getMenu().size() * iheight + iheight / 4);
        window.setContentView(view);
        window.showAtLocation(viewHolder.getContainer(), Gravity.FILL, 0, 0);
        view.setOnClickListener(new DismissClickListener());
    }

    private void showGuide2() {
        PopupWindow window = ensurePopupWindow();
        View view = getLayoutInflater().inflate(R.layout.guide2, null);
        window.setContentView(view);
        view.setOnClickListener(new DismissClickListener());
        View space = view.findViewById(R.id.space);
        int[] p = getLocationInActivity(viewHolder.level);
        ViewGroup.LayoutParams params = space.getLayoutParams();
        params.width = (int) (p[0] - (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                120,
                getResources().getDisplayMetrics()) - viewHolder.level.getWidth()) / 2);
        params.height = (int) (p[1] - (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                50,
                getResources().getDisplayMetrics()) - viewHolder.level.getHeight()) / 2);
        window.showAtLocation(viewHolder.getContainer(), Gravity.FILL, 0, 0);
    }

    private int[] getLocationInActivity(View target) {
        int[] position = new int[2];
        int[] windowPosition = new int[2];
        target.getLocationOnScreen(position);
        viewHolder.getContainer().getLocationOnScreen(windowPosition);
        position[0] -= windowPosition[0];
        position[1] -= windowPosition[1];
        return position;
    }

    private void showGuide3() {
        PopupWindow window = ensurePopupWindow();
        View view = getLayoutInflater().inflate(R.layout.guide3, null);
        window.setContentView(view);
        View device = viewHolder.get(R.id.device);
        int[] point = new int[2];
        device.getLocationInWindow(point);
        ViewGroup.LayoutParams params = view.findViewById(R.id.space).getLayoutParams();
        params.height = point[1];
        viewHolder.getContainer().getLocationInWindow(point);
        params.height -= point[1] + (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                64,
                getResources().getDisplayMetrics()) - device.getHeight()) / 2;
        view.setOnClickListener(new DismissClickListener());
        window.showAtLocation(viewHolder.getContainer(), Gravity.LEFT, 0, 0);
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
//        stopService(new Intent(this, HeloService.class));
        if (null != guideWindow) guideWindow.dismiss();
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
            get(R.id.manage_info).setOnClickListener(this);
            bloodLipid = get(R.id.blood_lipid);
            bloodLipid.setOnClickListener(this);
            get(R.id.doctor).setOnClickListener(this);
            get(R.id.device).setOnClickListener(this);
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
            } else if (R.id.manage_info == id) {
                gotoActivity(UpdateUserInfoActivity.class);
            } else if (R.id.doctor == id) {
                gotoActivity(AdviceListActivity.class);
            } else if (R.id.device == id) {
                gotoActivity(DeviceListActivity.class);
            } else if (R.id.blood_lipid == id) {
                gotoActivity(BloodLipidActivity.class);
            }
        }
    }

    private class PopupDismissListener implements PopupWindow.OnDismissListener, Runnable {
        int step = 0;

        @Override
        public void onDismiss() {
            if (step == 0) {
                viewHolder.toolbar.dismissPopupMenus();
            }
            viewHolder.getContainer().postOnAnimationDelayed(this, 50);
        }

        @Override
        public void run() {
            if (step == 0) {
                showGuide2();
            } else if (step == 1) {
                showGuide3();
            }
            step++;
        }
    }

    private class DismissClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            guideWindow.dismiss();
        }
    }

}
