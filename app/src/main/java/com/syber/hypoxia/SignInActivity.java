package com.syber.hypoxia;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.syber.base.BaseActivity;
import com.syber.base.BaseViewHolder;
import com.syber.base.util.MatchUtils;
import com.syber.base.view.ViewTouch;
import com.syber.hypoxia.data.IRequester;
import com.syber.hypoxia.data.SignInResponse;
import com.syber.hypoxia.data.User;
import com.transitionseverywhere.Scene;
import com.transitionseverywhere.SidePropagation;
import com.transitionseverywhere.Slide;
import com.transitionseverywhere.Transition;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionSet;

/**
 * Created by liangtg on 16-5-10.
 */
public class SignInActivity extends BaseActivity {
    private Bus bus = new Bus();
    private ViewHolder viewHolder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty);
        startTransition();
        viewHolder = new ViewHolder();
        startManageBus(bus, this);
    }

    @Subscribe
    public void withSignIn(SignInResponse event) {
        if (isFinishing()) return;
        if (event.isSuccess() && null != event.userinfoExt) {
            User.saveUser(event);
            finish();
        } else {
            showToast("登录失败:" + event.error);
        }
    }

    private void startTransition() {
        TransitionSet set = new TransitionSet();
        set.addTransition(new Slide(Gravity.BOTTOM).addTarget(TextInputLayout.class).addTarget(FrameLayout.class).setPropagation(new SidePropagation()));
        set.addListener(new Transition.TransitionListenerAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                if (null != viewHolder) {
                    ViewTouch.tap(viewHolder.inputPhone.getEditText());
                }
            }
        });
        set.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
        TransitionManager.go(Scene.getSceneForLayout((ViewGroup) findViewById(R.id.content), R.layout.activity_sign_in, this), set);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewHolder = null;
    }

    private class ViewHolder extends BaseViewHolder implements TextView.OnEditorActionListener {
        TextInputLayout inputPhone, inputPass;

        public ViewHolder() {
            super(findViewById(R.id.view_holder));
            inputPhone = get(R.id.input_phone);
            inputPass = get(R.id.input_pass);
            inputPass.getEditText().setOnEditorActionListener(this);
            get(R.id.signin).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (R.id.signin == id) {
                attemptSignIn();
            }
        }

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            attemptSignIn();
            return true;
        }

        private void attemptSignIn() {
            if (checkPhone() && checkPass()) {
                IRequester.getInstance().signIn(bus, inputPhone.getEditText().getText().toString(), inputPass.getEditText().getText().toString());
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

    }


}
