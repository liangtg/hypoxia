package com.syber.hypoxia;

import android.content.Context;

import com.orhanobut.logger.Logger;
import com.pgyersdk.crash.PgyCrashManager;
import com.syber.base.BaseApplication;
import com.syber.hypoxia.data.User;

import java.text.SimpleDateFormat;

/**
 * Created by liangtg on 16-5-10.
 */
public class IApplication extends BaseApplication {
    public static Context context;
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        Logger.init("hypoxia").methodCount(1);
        User.init();
        PgyCrashManager.register(this);
    }
}
