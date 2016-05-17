package com.syber.base.data;

import android.util.Log;
import android.util.Property;

import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.squareup.otto.Bus;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class GsonCallback<T extends BaseResponse> extends AbstractDataRequest {
    private static final String EMPTY_JSON = "{}";
    private Class<T> responseType;
    private Property property;
    private Object propertyObj;

    public GsonCallback(Bus bus, Class<T> responseType) {
        super(bus);
        this.responseType = responseType;
    }

    public GsonCallback setProperty(Property property, Object p) {
        this.property = property;
        propertyObj = p;
        return this;
    }

    @Override
    public void onFailure(Call call, IOException e) {
        DataRequester.logRequest(call.request(), e);
        T target = new Gson().fromJson(EMPTY_JSON, responseType);
        target.defaultError(e);
        withDataBackground(target);
        setResult(target);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        DataRequester.logRequest(call.request());
        DataRequester.logResponse(response);
        try {
            T result = processResponse(response);
            withDataBackground(result);
            setResult(result);
        } catch (Exception e) {
            BaseResponse target = new Gson().fromJson(EMPTY_JSON, this.responseType);
            Logger.e(e, "");
            setResult(target.error(-1));
        }
    }

    protected T processResponse(Response response) throws IOException {
        T target;
        String body = response.body().string();
        Log.d("response", body);
        target = new Gson().fromJson(body, this.responseType);
        if (!response.isSuccessful()) target.error(response.code());
        return target;
    }

    protected void withDataBackground(T response) {
        if (response.isSuccess() && null != property) {
            property.set(propertyObj, response.getData());
        }
    }

}
