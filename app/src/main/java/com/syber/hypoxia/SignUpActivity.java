package com.syber.hypoxia;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.syber.base.BaseActivity;
import com.syber.base.BaseViewHolder;
import com.syber.base.util.MatchUtils;
import com.syber.hypoxia.data.IRequester;
import com.syber.hypoxia.data.SignInResponse;
import com.syber.hypoxia.data.User;

public class SignUpActivity extends BaseActivity {

    private Bus bus = new Bus();
    private ViewHolder viewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        viewHolder = new ViewHolder();
        startManageBus(bus, this);
    }

    @Subscribe
    public void withSignup(SignInResponse event) {
        if (isFinishing()) return;
        if (event.isSuccess()) {
            User.saveUser(event);
            Intent intent = new Intent(this, UpdateUserInfoActivity.class);
            intent.putExtra(UpdateUserInfoActivity.KEY_EDIT, true);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        } else {
            showToast("注册失败:" + event.error);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewHolder = null;
    }

    private class ViewHolder extends BaseViewHolder implements TextView.OnEditorActionListener {
        TextInputLayout inputPhone, inputPass, inputPassAgain;

        public ViewHolder() {
            super(findViewById(R.id.view_holder));
            inputPhone = get(R.id.input_phone);
            inputPass = get(R.id.input_pass);
            inputPassAgain = get(R.id.input_pass_again);
            inputPassAgain.getEditText().setOnEditorActionListener(this);
            get(R.id.signup).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (R.id.signup == id) {
                attemptSignup();
            }
        }

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            attemptSignup();
            return true;
        }

        private void attemptSignup() {
            if (checkPhone() && checkPass() && checkPassAgain()) {
                IRequester.getInstance().signUp(bus, inputPhone.getEditText().getText().toString(), inputPass.getEditText().getText().toString());
            }
        }

        private boolean checkPhone() {
            boolean result = false;
            Editable text = inputPhone.getEditText().getText();
            if (text.length() == 0) {
                inputPhone.setError(getString(R.string.prompt_input_phone));
            } else if (!MatchUtils.matchPhone(text)) {
                inputPhone.setError(getString(R.string.prompt_input_correct_phone));
            } else {
                result = true;
            }
            inputPhone.setErrorEnabled(!result);
            return result;
        }

        private boolean checkPass() {
            boolean result = false;
            Editable text = inputPass.getEditText().getText();
            if (text.length() == 0) {
                inputPass.setError(getString(R.string.prompt_input_pass));
            } else if (text.length() < 6) {
                inputPass.setError(getString(R.string.prompt_input_correct_pass));
            } else {
                result = true;
            }
            inputPass.setErrorEnabled(!result);
            return result;
        }

        private boolean checkPassAgain() {
            boolean result = false;
            Editable again = inputPassAgain.getEditText().getText();
            if (again.length() == 0) {
                inputPassAgain.setError(getString(R.string.prompt_input_pass_again));
            } else if (!again.toString().equals(inputPass.getEditText().getText().toString())) {
                inputPassAgain.setError(getString(R.string.prompt_again_pwd_mismatch));
            } else {
                result = true;
            }
            inputPassAgain.setErrorEnabled(!result);
            return result;
        }

    }


}
