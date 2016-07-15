package com.syber.hypoxia.helo;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import java.util.UUID;

/**
 * Created by liangtg on 16-7-15.
 */
public abstract class BleFlow {
    protected BleFlow dependency;
    private boolean handleEnd = false;
    private BluetoothGatt bluetoothGatt;

    public void attachBluetoothGatt(BluetoothGatt gatt) {
        bluetoothGatt = gatt;
    }

    public void start() {
        if (null != dependency) {
            dependency.start();
        } else {
            onStart();
        }
    }

    public abstract void onStart();

    public boolean handleEnd() {
        return handleEnd;
    }

    protected void setHandleEnd(boolean end) {
        handleEnd = end;
    }

    public void handleCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (null != dependency && !dependency.handleEnd()) {
            dependency.handleCharacteristicWrite(gatt, characteristic, status);
        } else {
            onCharacteristicWrite(gatt, characteristic, status);
        }
    }

    public abstract void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);

    public void handleCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (null != dependency && !dependency.handleEnd()) {
            dependency.handleCharacteristicChanged(gatt, characteristic);
        } else {
            onCharacteristicChanged(gatt, characteristic);
        }
    }

    protected abstract void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);

    protected boolean enableNotify(UUID service, UUID chara, boolean enable) {
        return bluetoothGatt.setCharacteristicNotification(bluetoothGatt.getService(service).getCharacteristic(chara), enable);
    }

}
