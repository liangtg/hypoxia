package com.syber.hypoxia;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.gson.Gson;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.syber.base.BaseFragment;
import com.syber.hypoxia.data.IRequester;
import com.syber.hypoxia.data.SignInResponse;


/**
 * A simple {@link Fragment} subclass.
 */
public class ResetPwdFragment extends BaseFragment implements View.OnClickListener {
    public static final String KEY_EXT = "EXT";
    private Bus bus = new Bus();
    private SignInResponse.UserInfoExt userInfoExt;
    private EditText inputPass, inputPassAgain;
    private ProgressDialog progressDialog;

    public static ResetPwdFragment from(SignInResponse.UserInfoExt ext) {
        ResetPwdFragment fragment = new ResetPwdFragment();
        Bundle args = new Bundle();
        args.putString(KEY_EXT, new Gson().toJson(ext));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String info = getArguments().getString(KEY_EXT);
        if (TextUtils.isEmpty(info)) {
            getActivity().finish();
            return;
        }
        userInfoExt = new Gson().fromJson(info, SignInResponse.UserInfoExt.class);
        getBaseActivity().startManageBus(bus, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reset_pwd, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        inputPass = get(R.id.input_pass);
        inputPassAgain = get(R.id.input_pass_again);
        get(R.id.commit).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.commit == id) {
            attemptCommit();
        }
    }

    private void attemptCommit() {
        if (checkPass() && checkPassAgain()) {
            showProgress("");
            IRequester.getInstance().resetPwd(bus, userInfoExt, inputPass.getText().toString());
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

    private void dismissProgress() {
        if (null != progressDialog) progressDialog.dismiss();
    }

    private boolean checkPass() {
        boolean result = false;
        Editable text = inputPass.getText();
        if (text.length() == 0) {
            showToast(R.string.prompt_input_pass);
        } else if (text.length() < 6) {
            showToast(R.string.prompt_input_correct_pass);
        } else {
            result = true;
        }
        return result;
    }

    private boolean checkPassAgain() {
        boolean result = false;
        Editable again = inputPassAgain.getText();
        if (again.length() == 0) {
            showToast(R.string.prompt_input_pass_again);
        } else if (!again.toString().equals(inputPass.getText().toString())) {
            showToast(R.string.prompt_again_pwd_mismatch);
        } else {
            result = true;
        }
        return result;
    }

    @Subscribe
    public void withResult(SignInResponse event) {
        if (null == getView() || getActivity().isFinishing()) return;
        dismissProgress();
        if (event.isSuccess()) {
            getActivity().finish();
        } else {
            showToast("修改失败:" + event.error);
        }
    }

}
