package com.syber.hypoxia;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.syber.base.BaseViewHolder;
import com.syber.base.util.Extra;

/**
 * Created by liangtg on 16-6-21.
 */
public class PassFragment extends AppCompatDialogFragment implements View.OnClickListener {
    EditText inputPass;
    private String fragment;

    public PassFragment() {
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Material_Light_Dialog_NoActionBar_MinWidth);
    }

    public static PassFragment from(Fragment fragment) {
        Bundle args = new Bundle();
        args.putString(Extra.FRAGMENT, fragment.getTag());
        PassFragment result = new PassFragment();
        result.setArguments(args);
        return result;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragment = getArguments().getString(Extra.FRAGMENT);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pass, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        inputPass = BaseViewHolder.get(view, R.id.input_pass);
        view.findViewById(R.id.ok).setOnClickListener(this);
        view.findViewById(R.id.cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.ok == id) {
            if (checkPass()) {
                dismiss();
                OnInputPassListener listener = null;
                try {
                    if (TextUtils.isEmpty(fragment)) {
                        listener = (OnInputPassListener) getActivity();
                    } else {
                        listener = (OnInputPassListener) getFragmentManager().findFragmentByTag(fragment);
                    }
                } catch (Exception e) {
                }
                if (listener != null) {
                    listener.onInputPass(inputPass.getText().toString());
                }
            }
        } else if (R.id.cancel == id) {
            dismiss();
        }
    }

    private boolean checkPass() {
        boolean result = false;
        Editable text = inputPass.getText();
        if (text.length() >= 6) {
            result = true;
        } else {
            Toast.makeText(getActivity(), R.string.prompt_input_correct_pass, Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    public interface OnInputPassListener {
        void onInputPass(String pass);
    }

}
