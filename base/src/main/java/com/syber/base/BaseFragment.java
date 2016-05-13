package com.syber.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Created by dbx on 15/12/2.
 */
public class BaseFragment extends Fragment {

    protected final String TAG = getClass().getSimpleName() + "\t";
    private static final String LIFE = "life";
    protected Toast toast;


    @SuppressWarnings("unchecked")
    protected <T extends View> T get(int id) {
        View view = getView();
        if (null != view) {
            return (T) view.findViewById(id);
        }
        return null;
    }

    protected void gotoActivity(Class<? extends Activity> cls) {
        Intent i = new Intent(getActivity(), cls);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(LIFE, TAG + "onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LIFE, TAG + "onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(LIFE, TAG + "onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(LIFE, TAG + "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LIFE, TAG + "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LIFE, TAG + "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(LIFE, TAG + "onStop");
    }

    @Override
    public void onDestroy() {
        BaseApplication.getRefWatcher(getActivity()).watch(this);
        super.onDestroy();
        Log.d(LIFE, TAG + "onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(LIFE, TAG + "onDetach");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(LIFE, TAG + "onDestroyView");
    }

    public void showToast(String msg) {
        if (!getActivity().isFinishing()) {
            if (null == toast) {
                toast = Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG);
            }
            toast.setText(msg);
            toast.show();
        }
    }

    public void showToast(int resId) {
        showToast(getString(resId));
    }
}
