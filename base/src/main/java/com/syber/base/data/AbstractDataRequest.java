package com.syber.base.data;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

import okhttp3.Callback;

public abstract class AbstractDataRequest implements DataRequester.DataRequest, Callback, Runnable {
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean canceled = false;
    private Bus bus;
    private Object result;

    public AbstractDataRequest(Bus bus) {
        this.bus = bus;
    }

    @Override
    public void cancel() {
        canceled = true;
    }

    protected boolean isCanceled() {
        return canceled;
    }

    protected void setResult(Object result) {
        this.result = result;
        handler.post(this);
    }

    @Override
    public void run() {
        if (!isCanceled()) {
            bus.post(result);
        }
    }

}
