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

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.syber.base.BaseFragment;
import com.syber.base.util.MatchUtils;
import com.syber.hypoxia.data.IRequester;
import com.syber.hypoxia.data.SignInResponse;


/**
 * A simple {@link Fragment} subclass.
 */
public class ResetPwdFragment extends BaseFragment implements View.OnClickListener {
    private Bus bus = new Bus();
    private EditText inputPhone, inputID, inputPass, inputPassAgain;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBaseActivity().startManageBus(bus, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reset_pwd, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        inputPhone = get(R.id.input_phone);
        inputID = get(R.id.input_id);
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
        if (checkPhone() && checkID() && checkPass() && checkPassAgain()) {
            showProgress("");
            IRequester.getInstance().resetPwd(bus, inputPhone.getText().toString(), inputID.getText().toString(), inputPass.getText().toString());
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

    private boolean checkPhone() {
        boolean result = false;
        Editable text = inputPhone.getText();
        if (text.length() == 0) {
            showToast(R.string.prompt_input_phone);
        } else if (MatchUtils.matchPhone(text)) {
            showToast(R.string.prompt_input_correct_phone);
        } else {
            result = true;
        }
        return result;
    }

    private boolean checkID() {
        boolean result = false;
        Editable text = inputID.getText();
        if (text.length() == 0) {
            showToast(R.string.prompt_input_id);
        } else if (text.length() != 15 && text.length() != 18) {
            showToast(R.string.prompt_input_correct_id);
        } else {
            result = true;
        }
        return result;
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
