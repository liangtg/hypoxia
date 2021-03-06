package com.syber.hypoxia.data;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.squareup.otto.Bus;
import com.syber.base.data.DataRequester;
import com.syber.base.data.EmptyResponse;
import com.syber.base.data.GsonCallback;
import com.syber.hypoxia.BuildConfig;
import com.syber.hypoxia.IApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by liangtg on 16-5-11.
 */
public class IRequester extends DataRequester {

    private static IRequester instance;


    private IRequester() {
        TelephonyManager tm = (TelephonyManager) IApplication.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        deviceId = tm.getDeviceId();
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = Settings.Secure.getString(IApplication.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        if (TextUtils.isEmpty(deviceId)) deviceId = Build.SERIAL;
        userAgent = String.format("Hypoxia/%s (%s/%s; Android/%s)", BuildConfig.VERSION_NAME, Build.MODEL, Build.DEVICE, Build.VERSION.RELEASE);
//        SERVER = "http://192.168.123.210:34376/";
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

    public DataRequest signUp(Bus bus, String phone, String pass, String id) {
        GsonCallback callback = new GsonCallback(bus, SignInResponse.class);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("user_name", phone);
        builder.add("pswd", pass);
        builder.add("identitycard", id);
        enque(postBuilder("user/register?1", builder.build()).build(), callback);
        return callback;
    }

//    user_id		(必须)	int		用户编号
//    fullname	(必须)	string		名字
//    sex		(必须)	int		性别
//    birthday	(必须)	Datetime	生日
//    height		(必须)	double		身高
//    weight		(必须)	double		体重
//    blood

    public DataRequest updateUserInfo(Bus bus, String pass, SignInResponse.UserInfoExt ext) {
        GsonCallback<SignInResponse> callback = new GsonCallback<>(bus, SignInResponse.class);
        FormBody.Builder builder = new FormBody.Builder();
        formAdd(builder, "user_id", ext.user_id);
        formAdd(builder, "pswd", pass);
        formAdd(builder, "fullname", ext.fullname);
        formAdd(builder, "sex", ext.sex);
        formAdd(builder, "birthday", ext.birthday);
        formAdd(builder, "height", ext.height);
        formAdd(builder, "weight", ext.weight);
        formAdd(builder, "blood", ext.blood);
        formAdd(builder, "identitycard", ext.identitycard);
        enque(postBuilder("user/modifyuserinfo?1", builder.build()).build(), callback);
        return callback;
    }

    public DataRequest preResetPwd(Bus bus, String phone) {
        GsonCallback callback = new GsonCallback(bus, SignInResponse.class);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("phone", phone);
        enque(postBuilder("user/needresetpassword?1", builder.build()).build(), callback);
        return callback;
    }

    public DataRequest resetPwd(Bus bus, String phone, String id, String pswd) {
        GsonCallback callback = new GsonCallback(bus, SignInResponse.class);
        FormBody.Builder builder = new FormBody.Builder();
        formAdd(builder, "pswd", pswd);
        formAdd(builder, "phone", phone);
        formAdd(builder, "identitycard", id);
        enque(postBuilder("user/resetpassword?1", builder.build()).build(), callback);
        return callback;
    }

    public DataRequest uploadImage(Bus bus, File image) {
        GsonCallback callback = new GsonCallback(bus, EmptyResponse.class);
        enque(postBuilder("user/uploadavatar?id=" + User.getUserInfoExt().user_id, RequestBody.create(MediaType.parse("image/jpeg"), image)).build(),
                callback);
        return callback;
    }

    public DataRequest hypoxiaData(Bus bus, int page, String date) {
        GsonCallback callback = new GsonCallback(bus, HypoxiaHistoryResponse.class);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("pageno", String.valueOf(page));
        builder.add("lasttime", date);
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
//        builder.add("orderby", "time_report asc");
        enque(postBuilder("user/trainingchart?1", builder.build()).build(), callback);
        return callback;
    }


    public DataRequest bloodData(Bus bus, int page, String date) {
        GsonCallback callback = new GsonCallback(bus, BloodHistoryResponse.class);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("pageno", String.valueOf(page));
        builder.add("lasttime", date);
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
//        builder.add("orderby", "time_report asc");
        enque(postBuilder("user/pressurechart?1", builder.build()).build(), callback);
        return callback;
    }

    public DataRequest addTraing(Bus bus, String start, String end, String mode) {
        GsonCallback callback = new GsonCallback(bus, EmptyResponse.class);
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
        GsonCallback callback = new GsonCallback(bus, EmptyResponse.class);
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

    public DataRequest spoChartData(Bus bus, String start, String end) {
        GsonCallback callback = new GsonCallback(bus, OxygenSaturationChartResponse.class);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("user_id", User.getUserInfoExt().user_id);
        builder.add("starttime", start);
        builder.add("endtime", end);
//        builder.add("orderby", "time_report asc");
        enque(postBuilder("user/SpO2Chart?1", builder.build()).build(), callback);
        return callback;
    }

    public DataRequest spoData(Bus bus, int page, String date) {
        GsonCallback callback = new GsonCallback(bus, OxygenSaturationHistoryResponse.class);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("pageno", String.valueOf(page));
        builder.add("lasttime", date);
        builder.add("user_id", User.getUserInfoExt().user_id);
        enque(postBuilder("user/SpO2Data?1", builder.build()).build(), callback);
        return callback;
    }

    public DataRequest addSPO(Bus bus, String start, int spo, int pul) {
        GsonCallback callback = new GsonCallback(bus, EmptyResponse.class);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("user_id", User.getUserInfoExt().user_id);
        builder.add("time_start", start);
        builder.add("time_end", start);
//        {"Time_Test":"2016-04-06 15:38:47","O2p":97,HeartRate":64}
        JSONObject obj = new JSONObject();
        try {
            obj.putOpt("Time_Test", start);
            obj.putOpt("O2p", spo);
            obj.putOpt("HeartRate", pul);
        } catch (JSONException e) {
        }
        builder.add("content", obj.toString());
        enque(postBuilder("user/uploadspo2?1", builder.build()).build(), callback);
        return callback;
    }

    public DataRequest getUserSummary(Bus bus, String uid) {
        GsonCallback callback = new GsonCallback(bus, UserSummaryResponse.class);
        enque(getBuilder("user/userdashboard?id=" + uid).build(), callback);
        return callback;
    }

    public DataRequest heartChartData(Bus bus, String start, String end) {
        GsonCallback callback = new GsonCallback(bus, HeartChartResponse.class);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("user_id", User.getUserInfoExt().user_id);
        builder.add("starttime", start);
        builder.add("endtime", end);
        enque(postBuilder("user/heartratechart?1", builder.build()).build(), callback);
        return callback;
    }


    public DataRequest heartData(Bus bus, int page, String date) {
        GsonCallback callback = new GsonCallback(bus, HeartHistoryResponse.class);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("pageno", String.valueOf(page));
        builder.add("lasttime", date);
        builder.add("user_id", User.getUserInfoExt().user_id);
        enque(postBuilder("user/heartratedata?1", builder.build()).build(), callback);
        return callback;
    }

    public DataRequest adviceList(Bus bus, String uid, String startDate, int page) {
        GsonCallback callback = new GsonCallback(bus, AdviceResponse.class);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("pageno", String.valueOf(page));
        builder.add("user_id", uid);
        builder.add("starttime", startDate);
        builder.add("pagesize", "20");
        builder.add("byPage", "true");
        enque(postBuilder("apidoctor/doctoradvices?1", builder.build()).build(), callback);
        return callback;
    }

    public DataRequest adviceReaded(Bus bus, String id) {
        GsonCallback callback = new GsonCallback(bus, EmptyResponse.class);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("id", id);
        enque(postBuilder("apidoctor/advicereaded?1", builder.build()).build(), callback);
        return callback;
    }

    public DataRequest bloodLipidList(Bus bus, String uid, String lasttime, int page) {
        GsonCallback callback = new GsonCallback(bus, BloodLipidResponse.class);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("pageno", String.valueOf(page));
        builder.add("user_id", uid);
        builder.add("lasttime", lasttime);
        enque(postBuilder("user/blood6itemsdata?1", builder.build()).build(), callback);
        return callback;
    }

}
