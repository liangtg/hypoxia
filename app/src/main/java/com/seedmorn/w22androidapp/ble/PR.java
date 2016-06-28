package com.seedmorn.w22androidapp.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

/**
 * Created by liangtg on 16-6-28.
 */
public class PR {
    public static void log(BluetoothGattCharacteristic chara) {
        Log.e("cmd", toHex(chara.getValue()));
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

}
