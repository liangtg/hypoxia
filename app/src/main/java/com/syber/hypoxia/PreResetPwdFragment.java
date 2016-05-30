package com.syber.hypoxia;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.syber.base.BaseFragment;
import com.syber.base.util.MatchUtils;
import com.syber.hypoxia.data.IRequester;
import com.syber.hypoxia.data.SignInResponse;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreResetPwdFragment extends BaseFragment implements View.OnClickListener, StringPickerFragment.OnItemClickedListener {
    private View preFirst, preSecond;
    private SignInResponse.UserInfoExt update = new SignInResponse.UserInfoExt();
    private SignInResponse.UserInfoExt pre;

    private EditText userPhone, userName, userHeight, userWeight;
    private TextView userSex;
    private Bus bus = new Bus();
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bus.register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pre_reset_pwd, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        preFirst = get(R.id.pre_first);
        preSecond = get(R.id.pre_second);
        userPhone = get(R.id.user_phone_text);
        userName = get(R.id.user_name_text);
        userSex = get(R.id.user_sex_text);
        userHeight = get(R.id.user_height_text);
        userWeight = get(R.id.user_weight_text);
        get(R.id.user_sex_container).setOnClickListener(this);
        get(R.id.next_verify).setOnClickListener(this);
        get(R.id.next).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.user_sex_container == id) {
            StringPickerFragment.from(this, "性别", StringPickerFragment.SEX).show(getFragmentManager().beginTransaction().addToBackStack("sex_picker"),
                    "sex_picker");
        } else if (R.id.next_verify == id) {
            if (checkPhone()) {
                showProgress("正在获取信息");
                IRequester.getInstance().preResetPwd(bus, userPhone.getText().toString());
            }
        } else if (R.id.next == id) {
            verify();
        }
    }

    private void showProgress(String msg) {
        if (null == progressDialog) {
            progressDialog = ProgressDialog.show(getActivity(), null, msg, true, false);
        } else {
            progressDialog.setMessage(msg);
            progressDialog.show();
        }
    }

    @Subscribe
    public void withPre(SignInResponse event) {
        if (null == getView() || getActivity().isFinishing()) return;
        if (null != progressDialog) progressDialog.dismiss();
        if (event.isSuccess()) {
            pre = event.userinfoExt;
            preFirst.setVisibility(View.GONE);
            preSecond.setVisibility(View.VISIBLE);
        } else {
            showToast("" + event.error);
        }
    }

    @Override
    public void onItemClicked(int which, String[] array) {
        userSex.setText(array[which]);
        update.sex = String.valueOf(which + 1);
    }

    private boolean checkPhone() {
        boolean result = false;
        Editable text = userPhone.getText();
        if (text.length() == 0) {
            showToast(R.string.prompt_input_phone);
        } else if (!MatchUtils.matchPhone(text)) {
            showToast(R.string.prompt_input_correct_phone);
        } else {
            result = true;
        }
        return result;
    }

    private void verify() {
        if (checkName() && checkSex() && checkHeight() && checkWeight()) {
            if (pre.equals(update)) {
                getFragmentManager().beginTransaction().hide(this).add(R.id.fragment_container, ResetPwdFragment.from(pre), "reset").commit();
            } else {
                showToast("信息不匹配");
            }
        }
    }

    private boolean checkName() {
        boolean result = false;
        if (userName.getText().length() == 0) {
            showToast("请输入您的名字");
            userName.requestFocus();
        } else {
            result = true;
            update.fullname = userName.getText().toString();
        }
        return result;
    }

    private boolean checkSex() {
        boolean result = false;
        if (userSex.getText().length() == 0) {
            showToast("请选择您的性别");
        } else {
            result = true;
        }
        return result;
    }

    private boolean checkHeight() {
        boolean result = false;
        if (userHeight.getText().length() == 0) {
            showToast("请输入您的身高");
        } else {
            update.height = userHeight.getText().toString();
            result = true;
        }
        return result;
    }


    private boolean checkWeight() {
        boolean result = false;
        if (userWeight.getText().length() == 0) {
            showToast("请输入您的体重");
        } else {
            update.weight = userWeight.getText().toString();
            result = true;
        }
        return result;
    }

}
