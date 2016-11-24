package com.test;

import android.util.Log;

/**
 * Created by liangtg on 16-11-24.
 */

public class NLog {

    public static void log(byte[] data) {
        Log.d("bt", toHex(data));
    }

    public static String toHex(byte[] to) {
        if (null == to || to.length == 0) return "[]";
        StringBuffer sb = new StringBuffer("[" + Integer.toHexString(to[0] & 0xFF));
        for (int i = 1; i < to.length; i++) {
            sb.append(" " + Integer.toHexString(to[i] & 0xFF));
        }
        sb.append("]");
        return sb.toString();
    }

    public void write(byte[] log) {
        log(log);
    }

}
