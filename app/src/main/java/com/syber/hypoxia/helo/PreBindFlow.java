package com.syber.hypoxia.helo;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.syber.hypoxia.Helo;

/**
 * Created by liangtg on 16-7-15.
 */
public class PreBindFlow extends BleFlow {
    @Override
    public void onStart() {
        enableNotify(Helo.SERVICE1, Helo.S1N1, true);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
    }

    @Override
    protected void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
    }
}
