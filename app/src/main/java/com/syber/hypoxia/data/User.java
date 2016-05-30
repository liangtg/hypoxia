package com.syber.hypoxia.data;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.syber.hypoxia.BuildConfig;

/**
 * Created by liangtg on 16-5-10.
 */
public class User {
    private static final String KEY_USER = BuildConfig.APPLICATION_ID + ".USER1";
    private static SignInResponse userInfoExt;

    public static void init() {
        String data = PreferenceData.getInstance().getString(KEY_USER, "");
        if (!TextUtils.isEmpty(data)) {
            userInfoExt = new Gson().fromJson(data, SignInResponse.class);
        }
    }

    public static void saveUser(SignInResponse response) {
        if (null == response.userinfoExt) {
            response.userinfoExt = new SignInResponse.UserInfoExt();
            response.userinfoExt.user_id = response.userinfo.id;
        }
        userInfoExt = response;
        PreferenceData.getInstance().edit().putString(KEY_USER, new Gson().toJson(response)).commit();
    }

    public static boolean isSignIn() {
        return null != userInfoExt;
    }

    public static SignInResponse.UserInfoExt getUserInfoExt() {
        return userInfoExt.userinfoExt;
    }

    public static String getPhone() {
        return userInfoExt.userinfo.user_name;
    }

    public static void signOut() {
        PreferenceData.getInstance().edit().remove(KEY_USER).commit();
        userInfoExt = null;
    }

}
