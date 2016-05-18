package com.syber.hypoxia;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
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
import com.syber.base.data.BaseResponse;
import com.syber.hypoxia.data.IRequester;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class AddSPOActivity extends BaseActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    ProgressDialog progressDialog;

    Calendar calendar = Calendar.getInstance(Locale.CHINA);
    Calendar selectedDate = Calendar.getInstance(Locale.CHINA);
    ViewHolder viewHolder;
    private int spo, pul;
    private Bus bus = new Bus();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_spo);
        startManageBus(bus, this);
        setTitle("");
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
    public void withResponse(BaseResponse event) {
        if (isFinishing()) return;
        progressDialog.dismiss();
        showToast("添加" + (event.isSuccess() ? "成功" : "失败"));
    }

    public void onStartTimeClicked(View view) {
        new DatePickerDialog(this, this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        selectedDate.set(year, monthOfYear, dayOfMonth);
        new TimePickerDialog(this, this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
        selectedDate.set(Calendar.MINUTE, minute);
        calendar.setTimeInMillis(selectedDate.getTimeInMillis());
        viewHolder.startTime.setText(viewHolder.sdf.format(calendar.getTime()));
    }


    class ViewHolder extends BaseViewHolder {
        TextView startTime, spoText, pulText;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        public ViewHolder(View view) {
            super(view);
            startTime = get(R.id.start_time);
            spoText = get(R.id.spo2);
            pulText = get(R.id.pul);
            startTime.setText(sdf.format(calendar.getTime()));
            Random random = new Random();
            spo = random.nextInt(10) + 80;
            pul = random.nextInt(20) + 70;
            spoText.setText("" + spo);
            pulText.setText("" + pul);
        }
    }

}
