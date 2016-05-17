package com.syber.hypoxia.data;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.squareup.otto.Bus;
import com.syber.base.data.BaseResponse;
import com.syber.base.data.DataRequester;
import com.syber.base.data.GsonCallback;
import com.syber.hypoxia.BuildConfig;
import com.syber.hypoxia.IApplication;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.FormBody;

/**
 * Created by liangtg on 16-5-11.
 */
public class IRequester extends DataRequester {

    private static IRequester instance;


    private IRequester() {
        TelephonyManager tm = (TelephonyManager) IApplication.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        deviceId = tm.getDeviceId();
        userAgent = String.format("Hypoxia/%s (%s/%s; Android/%s)", BuildConfig.VERSION_NAME, Build.MODEL, Build.DEVICE, Build.VERSION.RELEASE);
    }

    public static IRequester getInstance() {
        if (null == instance) instance = new IRequester();
        return instance;
    }

    public DataRequest signIn(Bus bus, String phone, String pass) {
        GsonCallback callback = new GsonCallback(bus, SignInResponse.class);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("user_name", phone);
        builder.add("pswd", pass);
        enque(postBuilder("user/login?1", builder.build()).build(), callback);
        return callback;
    }

    public DataRequest hypoxiaData(Bus bus, int page) {
        GsonCallback callback = new GsonCallback(bus, HypoxiaHistoryResponse.class);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("pageno", String.valueOf(page));
        builder.add("user_id", User.getUserInfoExt().user_id);
        enque(postBuilder("user/trainingdata?1", builder.build()).build(), callback);
        return callback;
    }

    public DataRequest hypoxiaChartData(Bus bus, String start, String end) {
        GsonCallback callback = new GsonCallback(bus, HypoxiaChartResponse.class);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("user_id", User.getUserInfoExt().user_id);
        builder.add("starttime", start);
        builder.add("endtime", end);
        enque(postBuilder("user/trainingchart?1", builder.build()).build(), callback);
        return callback;
    }


    public DataRequest bloodData(Bus bus, int page) {
        GsonCallback callback = new GsonCallback(bus, BloodHistoryResponse.class);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("pageno", String.valueOf(page));
        builder.add("user_id", User.getUserInfoExt().user_id);
        enque(postBuilder("user/pressuredata?1", builder.build()).build(), callback);
        return callback;
    }

    public DataRequest bloodChartData(Bus bus, String start, String end) {
        GsonCallback callback = new GsonCallback(bus, BPChartResponse.class);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("user_id", User.getUserInfoExt().user_id);
        builder.add("starttime", start);
        builder.add("endtime", end);
        enque(postBuilder("user/pressurechart?1", builder.build()).build(), callback);
        return callback;
    }

    public DataRequest addTraing(Bus bus, String start, String end, String mode) {
        GsonCallback callback = new GsonCallback(bus, BaseResponse.class);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("user_id", User.getUserInfoExt().user_id);
        builder.add("time_start", start);
        builder.add("time_end", end);
        JSONObject obj = new JSONObject();
//        {"Time_Start":"2016-04-16 11:09:28","Time_End":"2016-04-16 11:39:28","TrainingMode":2}
        try {
            obj.putOpt("Time_Start", start);
            obj.putOpt("Time_End", end);
            obj.putOpt("TrainingMode", mode);
        } catch (JSONException e) {
        }
        builder.add("content", obj.toString());
        enque(postBuilder("user/uploadtraining?1", builder.build()).build(), callback);
        return callback;
    }

    public DataRequest addBP(Bus bus, String start, int sys, int dia, int pul) {
        GsonCallback callback = new GsonCallback(bus, BaseResponse.class);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("user_id", User.getUserInfoExt().user_id);
        builder.add("time_start", start);
        builder.add("time_end", start);
//        {"Time_Test":"2016-04-06 15:38:47","Diastolic":90,"Systolic":160,"HeartRate":176}
        JSONObject obj = new JSONObject();
        try {
            obj.putOpt("Time_Test", start);
            obj.putOpt("Diastolic", dia);
            obj.putOpt("Systolic", sys);
            obj.putOpt("HeartRate", pul);
        } catch (JSONException e) {
        }
        builder.add("content", obj.toString());
        enque(postBuilder("user/uploadpressure?1", builder.build()).build(), callback);
        return callback;
    }

}
