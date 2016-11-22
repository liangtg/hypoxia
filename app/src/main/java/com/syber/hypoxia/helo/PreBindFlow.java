package com.syber.hypoxia.helo;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.syber.hypoxia.Helo;
import com.syber.hypoxia.HeloCMD;
import com.syber.hypoxia.HeloResponse;
import com.syber.hypoxia.bt.FlowExtra;

import java.util.Arrays;

/**
 * Created by liangtg on 16-7-15.
 */
public class PreBindFlow extends BleFlow {
    @Override
    public void onStart() {
        enableNotify(Helo.SERVICE1, Helo.S1N1, true);
        writeChara(HeloCMD.MATCH.service, HeloCMD.MATCH.cha, HeloCMD.MATCH.cmd);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (BluetoothGatt.GATT_SUCCESS == status) {
            if (Arrays.equals(characteristic.getValue(), HeloCMD.RESPONSE_BIND.cmd)) {
                setHandleEnd(true);
                manager.requestConfirm(FlowExtra.REQUEST_MATCHED, this, null);
            }
        }
    }

    @Override
    protected void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        HeloResponse response = new HeloResponse(characteristic.getValue());
        if (response.verify()) {
            if (HeloResponse.MATCH == response.cmd()) {
                setHandleEnd(true);
                enableNotify(Helo.SERVICE1, Helo.S1N1, false);
                if (response.byteValue() == 1) {
                    manager.requestConfirm(FlowExtra.REQUEST_MATCHED, this, null);
                } else {
                    manager.requestConfirm(FlowExtra.REQUEST_BINDED_OTHER, this, null);
                }
            } else if (HeloResponse.REQUEST_BIND == response.cmd()) {
                manager.requestConfirm(FlowExtra.REQUEST_BIND, this, null);
            }
        }
    }

    @Override
    protected void onRequestConfirmed(int request, int result) {
        if (FlowExtra.REQUEST_BIND == request) {
            if (FlowExtra.CONFIRM_OK == result) {
                writeChara(HeloCMD.RESPONSE_BIND.service, HeloCMD.RESPONSE_BIND.cha, HeloCMD.RESPONSE_BIND.cmd);
            }
        } else if (FlowExtra.REQUEST_MATCHED == request) {
            setHandleEnd(true);
            if (null != next) next.start();
        }
    }
}
