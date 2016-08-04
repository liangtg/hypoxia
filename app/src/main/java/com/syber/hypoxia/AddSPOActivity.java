package com.syber.hypoxia;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.syber.base.BaseActivity;
import com.syber.base.BaseViewHolder;
import com.syber.base.data.EmptyResponse;
import com.syber.hypoxia.data.IRequester;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class AddSPOActivity extends BaseActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private static final String KEY_SPO = "SPO";
    private static final String KEY_RATE = "RATE";
    private static final String KEY_EDITABLE = "EDITABLE";
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    ProgressDialog progressDialog;
    Calendar calendar = Calendar.getInstance(Locale.CHINA);
    //    Calendar selectedDate = Calendar.getInstance(Locale.CHINA);
    ViewHolder viewHolder;
    private int spo, pul;
    private Bus bus = new Bus();

    public static void fromMeasure(Activity activity, int spo, int rate) {
        Intent intent = new Intent(activity, AddSPOActivity.class);
        intent.putExtra(KEY_EDITABLE, false);
        intent.putExtra(KEY_SPO, spo);
        intent.putExtra(KEY_RATE, rate);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_spo);
        startManageBus(bus, this);
        initAppBar();
        viewHolder = new ViewHolder(findViewById(R.id.view_holder));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.save == item.getItemId()) {
            if (viewHolder.spoText.getText().length() > 0 && viewHolder.pulText.getText().length() > 0) {
                spo = Integer.valueOf(viewHolder.spoText.getText().toString());
                pul = Integer.valueOf(viewHolder.pulText.getText().toString());
                if (spo > 0 && pul > 0) {
                    progressDialog = ProgressDialog.show(this, null, "正在上传，请稍等", true, true);
                    IRequester.getInstance().addSPO(bus, sdf.format(calendar.getTime()), spo, pul);
                } else {
                    showToast("请输入大于0的值");
                }
            } else {
                showToast("请输入大于0的值");
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void withResponse(EmptyResponse event) {
        if (isFinishing()) return;
        progressDialog.dismiss();
        showToast("添加" + (event.isSuccess() ? "成功" : "失败"));
        if (event.isSuccess()) finish();
    }

    public void onStartTimeClicked(View view) {
        if (viewHolder.edit) {
            new TimePickerDialog(this, this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        calendar.set(year, monthOfYear, dayOfMonth);
        viewHolder.startDate.setText(sdf.format(calendar.getTime()).substring(0, 10));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        viewHolder.startTime.setText(sdf.format(calendar.getTime()).substring(11, 16));
    }

    public void onStartDateClicked(View view) {
        if (viewHolder.edit) {
            new DatePickerDialog(this, this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        }
    }


    class ViewHolder extends BaseViewHolder {
        TextView startTime, spoText, pulText, startDate;
        boolean edit = getIntent().getBooleanExtra(KEY_EDITABLE, true);

        public ViewHolder(View view) {
            super(view);
            startDate = get(R.id.start_date);
            startTime = get(R.id.start_time);
            spoText = get(R.id.spo2);
            pulText = get(R.id.pul);
            startDate.setText(sdf.format(calendar.getTime()).substring(0, 10));
            startTime.setText(sdf.format(calendar.getTime()).substring(11, 16));
            Random random = new Random();
            if (edit) {
                spo = random.nextInt(10) + 80;
                pul = random.nextInt(20) + 70;
            } else {
                spo = getIntent().getIntExtra(KEY_SPO, 0);
                pul = getIntent().getIntExtra(KEY_RATE, 0);
                spoText.setEnabled(false);
                pulText.setEnabled(false);
            }
            spoText.setText("" + spo);
            pulText.setText("" + pul);
        }
    }

}
