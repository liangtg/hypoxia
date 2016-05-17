package com.syber.hypoxia;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

public class AddBPActivity extends BaseActivity implements TimePickerDialog.OnTimeSetListener {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    ProgressDialog progressDialog;

    Calendar calendar = Calendar.getInstance(Locale.CHINA);
    ViewHolder viewHolder;
    private int sys, dia, pul;
    private Bus bus = new Bus();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bp);
        startManageBus(bus, this);
        setTitle("");
        initAppBar();
        viewHolder = new ViewHolder(findViewById(R.id.content));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.save == item.getItemId()) {
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
        new TimePickerDialog(this, this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        viewHolder.startTime.setText(String.format("%02d:%02d", hourOfDay, minute));
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
    }

    public void onDiaClicked(View view) {
    }

    public void onSysClicked(View view) {
    }

    public void onPulClicked(View view) {
    }

    class ViewHolder extends BaseViewHolder {
        TextView startTime, sysText, diaText, pulText;

        public ViewHolder(View view) {
            super(view);
            startTime = get(R.id.start_time);
            sysText = get(R.id.sys);
            diaText = get(R.id.dia);
            pulText = get(R.id.pul);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            startTime.setText(sdf.format(calendar.getTime()));
            Random random = new Random();
            sys = random.nextInt(10) + 120;
            dia = random.nextInt(10) + 80;
            pul = random.nextInt(20) + 70;
            sysText.setText("" + sys);
            diaText.setText("" + dia);
            pulText.setText("" + pul);
        }
    }

}
