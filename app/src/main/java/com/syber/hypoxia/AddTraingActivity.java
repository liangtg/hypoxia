package com.syber.hypoxia;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
import com.syber.hypoxia.bt.FlowExtra;
import com.syber.hypoxia.data.IRequester;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddTraingActivity extends BaseActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    ProgressDialog progressDialog;
    Calendar start = Calendar.getInstance(), end = Calendar.getInstance();
    ViewHolder viewHolder;
    int traingMode = 0;
    private int[] modeTime = {35, 35, 45, 35, 45};
    private Bus bus = new Bus();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startManageBus(bus, this);
        setContentView(R.layout.activity_add_traing);
        initAppBar();
        boolean autoAdd = false;
        try {
            start.setTime(sdf.parse(getIntent().getStringExtra(FlowExtra.KEY_START_TIME)));
            end.setTime(sdf.parse(getIntent().getStringExtra(FlowExtra.KEY_END_TIME)));
            traingMode = getIntent().getIntExtra(FlowExtra.KEY_MODE, -1);
            autoAdd = traingMode >= 0;
        } catch (Exception e) {
        }
        viewHolder = new ViewHolder(findViewById(R.id.content));
        if (autoAdd) {
            ViewPost.postOnAnimation(viewHolder.getContainer(), new Runnable() {
                @Override
                public void run() {
                    tryAdd();
                }
            });
        } else {
            traingMode = 0;
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
            if (tryAdd()) return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean tryAdd() {
        if (viewHolder.allTime.getText().length() > 0) {
            int minute = Integer.parseInt(viewHolder.allTime.getText().toString());
            if (minute > 0) {
                end.setTimeInMillis(start.getTimeInMillis());
                end.add(Calendar.MINUTE, minute);
                progressDialog = ProgressDialog.show(this, null, "正在上传，请稍等", true, true);
                IRequester.getInstance().addTraing(bus, sdf.format(start.getTime()), sdf.format(end.getTime()), String.valueOf(traingMode));
                return true;
            }
        }
        return false;
    }

    @Subscribe
    public void withResponse(EmptyResponse event) {
        if (isFinishing()) return;
        progressDialog.dismiss();
        showToast("添加" + (event.isSuccess() ? "成功" : "失败"));
        if (event.isSuccess()) finish();
    }

    public void onStartTimeClicked(View view) {
        new DatePickerDialog(this, this, start.get(Calendar.YEAR), start.get(Calendar.MONTH), start.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void onEndTimeClicked(View view) {
        new TimePickerDialog(this, this, start.get(Calendar.HOUR_OF_DAY), start.get(Calendar.MINUTE), true).show();
    }

    public void onModeClicked(final View view) {
//        1，	35，	180
//        2，	45，	180
//        3，	35，	220
//        4，	45，	220
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("训练模式");
        builder.setItems(new String[]{"模式0", "模式1", "模式2", "模式3", "模式4"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                traingMode = which;
                viewHolder.mode.setText("模式" + which);
                viewHolder.allTime.setText(String.valueOf(modeTime[which]));
            }
        });
        builder.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        start.set(Calendar.HOUR_OF_DAY, hourOfDay);
        start.set(Calendar.MINUTE, minute);
        viewHolder.endTime.setText(sdf.format(start.getTime()).substring(11, 16));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        start.set(year, monthOfYear, dayOfMonth);
        viewHolder.startTime.setText(sdf.format(start.getTime()).substring(0, 10));
    }

    class ViewHolder extends BaseViewHolder {
        TextView startTime, endTime, allTime, mode;

        public ViewHolder(View view) {
            super(view);
            startTime = get(R.id.start_time);
            endTime = get(R.id.end_time);
            allTime = get(R.id.all_time);
            mode = get(R.id.mode);
            startTime.setText(sdf.format(start.getTime()).substring(0, 10));
            endTime.setText(sdf.format(start.getTime()).substring(11, 16));
        }
    }

}
