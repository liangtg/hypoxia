package com.syber.hypoxia;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.TextUtils;
import android.widget.DatePicker;


/**
 * A simple {@link Fragment} subclass.
 */
public class DatePickerFragment extends AppCompatDialogFragment implements DatePickerDialog.OnDateSetListener {
    private static final String KEY_YEAR = "YEAR";
    private static final String KEY_MONTH = "MONTH";
    private static final String KEY_DAY = "DAY";
    private static final String KEY_FRAGMENT = "FRAGMENT";
    private int year;
    private int month;
    private int day;
    private String tag;

    public static DatePickerFragment from(int year, int month, int day) {
        DatePickerFragment result = new DatePickerFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_YEAR, year);
        args.putInt(KEY_MONTH, month);
        args.putInt(KEY_DAY, day);
        result.setArguments(args);
        return result;
    }

    public static DatePickerFragment from(Fragment fragment, int year, int month, int day) {
        DatePickerFragment result = new DatePickerFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_YEAR, year);
        args.putInt(KEY_MONTH, month);
        args.putInt(KEY_DAY, day);
        args.putString(KEY_FRAGMENT, fragment.getTag());
        result.setArguments(args);
        return result;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        year = getArguments().getInt(KEY_YEAR);
        month = getArguments().getInt(KEY_MONTH);
        day = getArguments().getInt(KEY_DAY);
        tag = getArguments().getString(KEY_FRAGMENT);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
        dialog.setTitle("出生日期");
        return dialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        DatePickerDialog.OnDateSetListener listener = null;
        if (TextUtils.isEmpty(tag)) {
            try {
                listener = (DatePickerDialog.OnDateSetListener) getActivity();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                listener = (DatePickerDialog.OnDateSetListener) getFragmentManager().findFragmentByTag(tag);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (null != listener) listener.onDateSet(view, year, monthOfYear, dayOfMonth);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        dismiss();
    }
}
