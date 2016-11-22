package com.syber.hypoxia.helo;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.syber.hypoxia.Helo;
import com.syber.hypoxia.HeloCMD;
import com.syber.hypoxia.HeloResponse;
import com.syber.hypoxia.bt.FlowExtra;

import java.util.Arrays;

/**
 * Created by liangtg on 16-8-3.
 */
public class ECGFlow extends BleFlow {
    private Handler handler = new Handler(Looper.getMainLooper());

    public ECGFlow() {
        dependency = new PreBindFlow();
        dependency.next = this;
    }

    @Override
    public void onStart() {
        enableNotify(Helo.SERVICE0, Helo.S0N5, true);
        enableNotify(Helo.SERVICE0, Helo.S0N4, true);
        enableNotify(HeloCMD.GET_HR.service, HeloCMD.GET_HR.cha, true);
        enableNotify(Helo.SERVICE0, Helo.S0N2, true);
        writeChara(HeloCMD.GET_RAW_PULSE.service, HeloCMD.GET_RAW_PULSE.cha, HeloCMD.GET_RAW_PULSE.cmd);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (Arrays.equals(HeloCMD.GET_RAW_PULSE.cmd, characteristic.getValue())) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    writeChara(HeloCMD.GET_RAW_ECG.service, HeloCMD.GET_RAW_ECG.cha, HeloCMD.GET_RAW_ECG.cmd);
                }
            }, 40 * 1000);
        }
    }

    @Override
    protected void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (Helo.S0N5.equals(characteristic.getUuid())) {
            int p1 = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 0);
            int p2 = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 4);
            int p3 = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 8);
            int p4 = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 12);
            int p5 = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 16);
            BTManager.e(String.format("PUL :%d,%d,%d,%d,%d", p1, p2, p3, p4, p5));
            int[] pul = new int[]{p1, p2, p3, p4, p5};
            Intent data = new Intent();
            data.putExtra(FlowExtra.KEY_PUL_ARRAY, pul);
            manager.requestConfirm(FlowExtra.RESULT_RAW_PUL, this, data);
        } else if (Helo.S0N4.equals(characteristic.getUuid())) {
            int p1 = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 0);
            int p2 = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 4);
            int p3 = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 8);
            int p4 = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 12);
            int p5 = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 16);
            BTManager.e(String.format("ecg :%d,%d,%d,%d,%d,", p1, p2, p3, p4, p5));
            int[] pul = new int[]{p1, p2, p3, p4, p5};
            Intent data = new Intent();
            data.putExtra(FlowExtra.KEY_ECG_ARRAY, pul);
            manager.requestConfirm(FlowExtra.RESULT_RAW_ECG, this, data);
        } else if (Helo.S0N2.equals(characteristic.getUuid())) {
            HeloResponse response = new HeloResponse(characteristic.getValue());
            if (response.verify()) {
                if (response.cmd() == HeloResponse.ECG) {
                    int r = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 9);
                    Intent data = new Intent();
                    data.putExtra(FlowExtra.KEY_ECG, r);
                    manager.requestConfirm(FlowExtra.RESULT_ECG, this, data);
                } else if (HeloResponse.BP == response.cmd()) {
                    int sys = response.intValue(4);
                    int dia = response.intValue(5);
                    Intent intent = new Intent();
                    intent.putExtra(FlowExtra.KEY_SYS, sys);
                    intent.putExtra(FlowExtra.KEY_DIA, dia);
                    manager.requestConfirm(FlowExtra.RESULT_BP, this, intent);
                } else if (HeloResponse.HR == response.cmd()) {
                    int pul = response.intValue(4);
                    Intent intent = new Intent();
                    intent.putExtra(FlowExtra.KEY_PUL, pul);
                    manager.requestConfirm(FlowExtra.RESULT_HR, this, intent);
                }
            }
        }
    }
}
