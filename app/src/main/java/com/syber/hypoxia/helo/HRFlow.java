package com.syber.hypoxia.helo;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;

import com.syber.hypoxia.HeloCMD;
import com.syber.hypoxia.HeloResponse;

/**
 * Created by liangtg on 16-7-18.
 */
public class HRFlow extends BleFlow {

    public HRFlow() {
        dependency = new PreBindFlow();
        dependency.next = this;
    }

    @Override
    public void onStart() {
        enableNotify(HeloCMD.GET_HR.service, HeloCMD.GET_HR.cha, true);
        writeChara(HeloCMD.GET_HR.service, HeloCMD.GET_HR.cha, HeloCMD.GET_HR.cmd);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
    }

    @Override
    protected void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        HeloResponse response = new HeloResponse(characteristic.getValue());
        if (response.verify()) {
            if (HeloResponse.HR == response.cmd()) {
                int pul = response.intValue(4);
                Intent intent = new Intent();
                intent.putExtra(KEY_PUL, pul);
                manager.requestConfirm(RESULT_HR, this, intent);
            }
        }
    }
}
