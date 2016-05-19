package com.syber.base.util;

/**
 * Created by dbx on 16/3/15.
 */
public class ByteUtil {

    public static String toHex(byte[] to) {
        if (null == to || to.length == 0) return "[]";
        StringBuffer sb = new StringBuffer("[" + Integer.toHexString(to[0] & 0xFF));
        for (int i = 1; i < to.length; i++) {
            sb.append(" " + Integer.toHexString(to[i] & 0xFF));
        }
        sb.append("]");
        return sb.toString();
    }

    public static int unsignedByteToInt(byte data) {
        return data & 0xFF;
    }
}
