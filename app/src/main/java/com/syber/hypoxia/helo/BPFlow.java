package com.syber.hypoxia.helo;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;

import com.syber.hypoxia.Helo;
import com.syber.hypoxia.HeloCMD;
import com.syber.hypoxia.HeloResponse;
import com.syber.hypoxia.bt.FlowExtra;

/**
 * Created by liangtg on 16-7-18.
 */
public class BPFlow extends BleFlow {

    public BPFlow() {
        dependency = new PreBindFlow();
        dependency.next = this;
    }

    @Override
    public void onStart() {
        enableNotify(Helo.SERVICE0, Helo.S0N2, true);
        writeChara(HeloCMD.GET_BP.service, HeloCMD.GET_BP.cha, HeloCMD.GET_BP.cmd);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
    }

    @Override
    protected void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        HeloResponse response = new HeloResponse(characteristic.getValue());
        if (response.verify()) {
            if (HeloResponse.BP == response.cmd()) {
                int sys = response.intValue(4);
                int dia = response.intValue(5);
                Intent intent = new Intent();
                intent.putExtra(FlowExtra.KEY_SYS, sys);
                intent.putExtra(FlowExtra.KEY_DIA, dia);
                manager.requestConfirm(FlowExtra.RESULT_BP, this, intent);
            }
        }
    }
}
