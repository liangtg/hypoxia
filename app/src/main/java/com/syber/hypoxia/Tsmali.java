package com.syber.hypoxia;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import com.seedmorn.w22androidapp.ble.PR;

import java.util.Arrays;

/**
 * Created by liangtg on 16-6-24.
 */
public class Tsmali {
    private BluetoothGatt mBluetoothGatt;

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        Log.e("cmd", Arrays.toString(characteristic.getValue()));
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        PR.log(characteristic);
    }


}
