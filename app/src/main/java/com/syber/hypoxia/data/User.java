package com.syber.hypoxia.data;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.syber.hypoxia.BuildConfig;

/**
 * Created by liangtg on 16-5-10.
 */
public class User {
    private static final String KEY_USER = BuildConfig.APPLICATION_ID + ".USER";
    private static SignInResponse.UserInfoExt userInfoExt;

    public static void init() {
        String data = PreferenceData.getInstance().getString(KEY_USER, "");
        if (!TextUtils.isEmpty(data)) {
            userInfoExt = new Gson().fromJson(data, SignInResponse.UserInfoExt.class);
        }
    }

    public static void saveUser(SignInResponse response) {
        PreferenceData.getInstance().edit().putString(KEY_USER, new Gson().toJson(response.userinfoExt)).commit();
        userInfoExt = response.userinfoExt;
    }

    public static boolean isSignIn() {
        return null != userInfoExt;
    }

    public static SignInResponse.UserInfoExt getUserInfoExt() {
        return userInfoExt;
    }

    public static void signOut() {
        PreferenceData.getInstance().edit().remove(KEY_USER).commit();
        userInfoExt = null;
    }

}
