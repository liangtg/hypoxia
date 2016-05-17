package com.syber.hypoxia;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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

public class AddTraingActivity extends BaseActivity implements TimePickerDialog.OnTimeSetListener {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    ProgressDialog progressDialog;

    Calendar calendar = Calendar.getInstance(Locale.CHINA);
    Calendar start = Calendar.getInstance(), end = Calendar.getInstance();
    ViewHolder viewHolder;
    int traingMode = 0;
    private boolean startTime = true;
    private Bus bus = new Bus();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startManageBus(bus, this);
        setTitle("");
        setContentView(R.layout.activity_add_traing);
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
            if (end.after(start)) {
                progressDialog = ProgressDialog.show(this, null, "正在上传，请稍等", true, true);
                IRequester.getInstance().addTraing(bus, sdf.format(start.getTime()), sdf.format(end.getTime()), String.valueOf(traingMode));
            } else {
                showToast("请选择正确的结束时间");
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
        startTime = true;
        new TimePickerDialog(this, this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    public void onEndTimeClicked(View view) {
        startTime = false;
        new TimePickerDialog(this, this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    public void onModeClicked(final View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("训练模式");
        builder.setItems(new String[]{"模式1", "模式2", "模式3", "模式4"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                traingMode = which;
                viewHolder.mode.setText("模式" + (which + 1));
            }
        });
        builder.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (startTime) {
            viewHolder.startTime.setText(String.format("%02d:%02d", hourOfDay, minute));
            start.set(Calendar.HOUR_OF_DAY, hourOfDay);
            start.set(Calendar.MINUTE, minute);
        } else {
            viewHolder.endTime.setText(String.format("%02d:%02d", hourOfDay, minute));
            end.set(Calendar.HOUR_OF_DAY, hourOfDay);
            end.set(Calendar.MINUTE, minute);
        }
        if (end.after(start)) {
            viewHolder.allTime.setText((end.getTimeInMillis() - start.getTimeInMillis()) / 1000 / 60 + "分钟");
        }
    }

    class ViewHolder extends BaseViewHolder {
        TextView startTime, endTime, allTime, mode;

        public ViewHolder(View view) {
            super(view);
            startTime = get(R.id.start_time);
            endTime = get(R.id.end_time);
            allTime = get(R.id.all_time);
            mode = get(R.id.mode);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            startTime.setText(sdf.format(calendar.getTime()));
            end.add(Calendar.MINUTE, 20);
            endTime.setText(sdf.format(end.getTime()));
            allTime.setText("20分钟");
        }
    }

}
