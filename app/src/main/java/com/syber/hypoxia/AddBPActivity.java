package com.syber.hypoxia;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import com.syber.base.view.ViewPost;
import com.syber.hypoxia.data.IRequester;
import com.syber.hypoxia.helo.BPFlow;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class AddBPActivity extends BaseActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    ProgressDialog progressDialog;
    Calendar calendar = Calendar.getInstance(Locale.CHINA);
    Calendar selectedDate = Calendar.getInstance(Locale.CHINA);
    ViewHolder viewHolder;
    private Snackbar snackbar;
    private int sys, dia, pul;
    private Bus bus = new Bus();
    private boolean autoAdd = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bp);
        startManageBus(bus, this);
        initAppBar();
        sys = getIntent().getIntExtra(BPFlow.KEY_SYS, 0);
        dia = getIntent().getIntExtra(BPFlow.KEY_DIA, 0);
        pul = getIntent().getIntExtra(BPFlow.KEY_PUL, 0);
        autoAdd = sys > 0;
        viewHolder = new ViewHolder(findViewById(R.id.content));
        if (autoAdd) {
            ViewPost.postOnAnimation(viewHolder.getContainer(), new Runnable() {
                @Override
                public void run() {
                    attemptAdd();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.save == item.getItemId()) {
            attemptAdd();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void attemptAdd() {
        if (viewHolder.sysText.getText().length() > 0 && viewHolder.diaText.getText().length() > 0 && viewHolder.pulText.getText().length() > 0) {
            sys = Integer.valueOf(viewHolder.sysText.getText().toString());
            dia = Integer.valueOf(viewHolder.diaText.getText().toString());
            pul = Integer.valueOf(viewHolder.pulText.getText().toString());
            if (sys > 0 && dia > 0 && pul > 0) {
                progressDialog = ProgressDialog.show(this, null, "正在上传，请稍等", true, true);
                IRequester.getInstance().addBP(bus, sdf.format(calendar.getTime()), sys, dia, pul);
            } else {
                showToast("请输入大于0的值");
            }
        } else {
            showToast("请输入大于0的值");
        }
    }

    @Subscribe
    public void withResponse(EmptyResponse event) {
        if (isFinishing()) return;
        progressDialog.dismiss();
        String msg = "添加" + (event.isSuccess() ? "成功" : "失败");
        snackbar = Snackbar.make(viewHolder.getContainer(), msg, Snackbar.LENGTH_LONG);
        if (event.isSuccess()) {
            setResult(RESULT_OK);
            snackbar.setCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    finish();
                }
            });
        }
        snackbar.show();
    }

    public void onStartTimeClicked(View view) {
        new TimePickerDialog(this, this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
        selectedDate.set(Calendar.MINUTE, minute);
        calendar.setTimeInMillis(selectedDate.getTimeInMillis());
        viewHolder.startTime.setText(sdf.format(calendar.getTime()).substring(11, 16));
    }

    public void onDiaClicked(View view) {
    }

    public void onSysClicked(View view) {
    }

    public void onPulClicked(View view) {
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        selectedDate.set(year, monthOfYear, dayOfMonth);
        calendar.set(year, monthOfYear, dayOfMonth);
        viewHolder.startDate.setText(sdf.format(calendar.getTime()).substring(0, 10));
    }

    public void onStartDateClicked(View view) {
        new DatePickerDialog(this, this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    class ViewHolder extends BaseViewHolder {
        TextView startTime, sysText, diaText, pulText, startDate;

        public ViewHolder(View view) {
            super(view);
            startTime = get(R.id.start_time);
            startDate = get(R.id.start_date);
            sysText = get(R.id.sys);
            diaText = get(R.id.dia);
            pulText = get(R.id.pul);
            startDate.setText(sdf.format(calendar.getTime()).substring(0, 10));
            startTime.setText(sdf.format(calendar.getTime()).substring(11, 16));
            if (sys == 0) {
                Random random = new Random();
                sys = random.nextInt(10) + 120;
                dia = random.nextInt(10) + 80;
                pul = random.nextInt(20) + 70;
            }
            sysText.setText("" + sys);
            diaText.setText("" + dia);
            pulText.setText("" + pul);
        }
    }

}
