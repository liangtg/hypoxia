package com.syber.hypoxia;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.TextUtils;


/**
 * A simple {@link Fragment} subclass.
 */
public class StringPickerFragment extends AppCompatDialogFragment implements DialogInterface.OnClickListener {
    public static final String[] SEX = {"男", "女"};
    private static final String KEY_FRAGMENT = "FRAGMENT";
    private static final String KEY_ARRAY = "ARRAY";
    private static final String KEY_TITLE = "TITLE";
    private String fragment;
    private String title;
    private String[] items;

    public static StringPickerFragment from(Fragment fragment, String title, String[] array) {
        StringPickerFragment result = new StringPickerFragment();
        Bundle args = new Bundle();
        args.putString(KEY_FRAGMENT, fragment.getTag());
        args.putStringArray(KEY_ARRAY, array);
        args.putString(KEY_TITLE, title);
        result.setArguments(args);
        return result;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        title = getArguments().getString(KEY_TITLE);
        items = getArguments().getStringArray(KEY_ARRAY);
        fragment = getArguments().getString(KEY_FRAGMENT);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setItems(items, this);
        return builder.create();
    }
    
    @Override
    public void onClick(DialogInterface dialog, int which) {
        OnItemClickedListener listener = null;
        try {
            if (TextUtils.isEmpty(fragment)) {
                listener = (OnItemClickedListener) getActivity();
            } else {
                listener = (OnItemClickedListener) getFragmentManager().findFragmentByTag(fragment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != listener) listener.onItemClicked(which, items);
        dismiss();
    }

    public interface OnItemClickedListener {
        void onItemClicked(int which, String[] array);
    }

}
