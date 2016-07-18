package com.syber.hypoxia.helo;

import android.bluetooth.BluetoothGatt;
import android.content.Intent;

/**
 * Created by liangtg on 16-7-18.
 */
public interface IBleManager {
    BluetoothGatt getBlutoothGatt();

    void requestConfirm(int request, BleFlow flow, Intent data);

    void setRequestConfirmed(int request, int result);

}
