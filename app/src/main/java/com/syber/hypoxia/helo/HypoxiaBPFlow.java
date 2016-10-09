package com.syber.hypoxia.helo;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Intent;

/**
 * Created by liangtg on 16-9-30.
 */

public class HypoxiaBPFlow extends BleFlow {

    @Override
    public void onStart() {
        if (enableNotify(Hypoxia.SERVICE_BP, Hypoxia.BPM, true)) {
            BluetoothGattDescriptor descriptor = manager.getBlutoothGatt().getService(Hypoxia.SERVICE_BP).getCharacteristic(Hypoxia.BPM).getDescriptor(
                    BleFlow.CCC);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            manager.getBlutoothGatt().writeDescriptor(descriptor);
            BTManager.e("enable bpm true");
        } else {
            BTManager.e("enable bpm fail");
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
    }

    @Override
    protected void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Intent intent = new Intent();
        intent.putExtra(KEY_PUL_ARRAY, characteristic.getValue());
        if (Hypoxia.ICP.equals(characteristic.getUuid())) {
            intent.putExtra(KEY_SYS, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 1).intValue());
            manager.requestConfirm(PROGRESS_BP, this, intent);
        } else if (Hypoxia.BPM.equals(characteristic.getUuid())) {
            intent.putExtra(KEY_SYS, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 1).intValue());
            intent.putExtra(KEY_DIA, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 3).intValue());
            manager.requestConfirm(RESULT_BP, this, intent);
        }
    }

    @Override
    protected void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        if (BleFlow.CCC.equals(descriptor.getUuid())) {
            if (Hypoxia.BPM.equals(descriptor.getCharacteristic().getUuid())) {
                if (enableNotify(Hypoxia.SERVICE_BP, Hypoxia.ICP, true)) {
                    BTManager.e("enable icp true");
                    descriptor = manager.getBlutoothGatt().getService(Hypoxia.SERVICE_BP).getCharacteristic(Hypoxia.ICP).getDescriptor(BleFlow.CCC);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    manager.getBlutoothGatt().writeDescriptor(descriptor);
                } else {
                    BTManager.e("enable icp fail");
                }
            }
        }
    }
}
