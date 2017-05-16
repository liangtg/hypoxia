package com.syber.base.data;

import android.text.TextUtils;

import com.orhanobut.logger.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

/**
 * Created by liangtg on 16-5-10.
 */
public class DataRequester {
    public static String SERVER = "http://hilo.syberos.com:17202/";
//    public static String SERVER = "http://172.16.22.101:34376/";

    protected static MediaType FORM = MediaType.parse("application/x-www-form-urlencoded");
    protected static String deviceId;
    protected static String userAgent = "syber";
    protected RequestBody EMPTY = RequestBody.create(FORM, "");
    protected OkHttpClient httpClient = new OkHttpClient();

    protected static void logRequest(Request request) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (null != request.body()) {
            try {
                BufferedSink buffer = Okio.buffer(Okio.sink(out));
                request.body().writeTo(buffer);
                buffer.flush();
            } catch (IOException ignored) {
            }
        }
        Logger.d(request.toString() + "\n" + new String(out.toByteArray()));
    }

    protected static void logRequest(Request request, Exception e) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (null != request.body()) {
            try {
                BufferedSink buffer = Okio.buffer(Okio.sink(out));
                request.body().writeTo(buffer);
                buffer.flush();
            } catch (IOException ignore) {
            }
        }
        Logger.e(e, request.toString() + "\n" + new String(out.toByteArray()));
    }

    protected static void logResponse(Response response) {
        Logger.d(response.toString());
    }

    protected Request.Builder postBuilder(String path, RequestBody body) {
        Request.Builder builder = new Request.Builder();
        builder.url(SERVER + path);
        builder.header("User-Agent", userAgent);
        builder.header("X-Device-Id", deviceId);
        builder.post(body);
        return builder;
    }

    protected Request.Builder patchBuilder(String path, RequestBody body) {
        Request.Builder builder = new Request.Builder();
        builder.url(SERVER + path);
        builder.header("User-Agent", userAgent);
        builder.header("X-Device-Id", deviceId);
        builder.patch(body);
        return builder;
    }

    protected Request.Builder getBuilder(String path) {
        Request.Builder builder = new Request.Builder();
        builder.url(SERVER + path);
        builder.header("User-Agent", userAgent);
        builder.header("X-Device-Id", deviceId);
        return builder;
    }

    protected void enque(Request request, Callback callback) {
        httpClient.newCall(request).enqueue(callback);
    }

    protected FormBody.Builder formAdd(FormBody.Builder builder, String name, String value) {
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(value)) builder.add(name, value);
        return builder;
    }


    public interface DataRequest {
        void cancel();
    }

}
