package com.syber.hypoxia.helo;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import com.syber.base.util.ByteUtil;

import java.util.UUID;

/**
 * Created by liangtg on 16-7-15.
 */
public abstract class BleFlow {
    public static final UUID CCC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static final String KEY_SYS = "sys";
    public static final String KEY_DIA = "dia";
    public static final String KEY_PUL = "pul";
    public static final String KEY_ECG = "ecg";
    public static final String KEY_PUL_ARRAY = "pul_array";
    public static final String KEY_ECG_ARRAY = "ecg_array";
    public static final int CONFIRM_OK = 1;
    public static final int CONFIRM_CANCEL = 2;

    public static final int REQUEST_BIND = 1;
    public static final int REQUEST_BINDED_OTHER = 2;
    public static final int REQUEST_MATCHED = 3;
    public static final int RESULT_BP = 4;
    public static final int RESULT_HR = 5;
    public static final int RESULT_ECG = 6;
    public static final int RESULT_RAW_PUL = 7;
    public static final int RESULT_RAW_ECG = 8;
    public static final int REQUEST_CONFIRM_DISCONNECT = 9;
    public static final int PROGRESS_BP = 10;

    protected BleFlow dependency;
    protected BleFlow next;
    protected IBleManager manager;
    private boolean handleEnd = false;

    public void setBleManager(IBleManager manager) {
        if (null != dependency) dependency.setBleManager(manager);
        this.manager = manager;
    }

    public void start() {
        BTManager.e("start:\t" + getClass().getSimpleName());
        if (null != dependency && !dependency.handleEnd()) {
            dependency.start();
        } else {
            onStart();
        }
    }

    public void reset() {
        setHandleEnd(false);
        if (null != dependency) {
            dependency.reset();
        }
    }

    public abstract void onStart();

    public boolean handleEnd() {
        return handleEnd;
    }

    protected void setHandleEnd(boolean end) {
        handleEnd = end;
    }

    protected void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
    }

    public void handleDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        if (null != dependency && !dependency.handleEnd()) {
            dependency.handleDescriptorWrite(gatt, descriptor, status);
        } else {
            onDescriptorWrite(gatt, descriptor, status);
        }
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
        return manager.getBlutoothGatt().setCharacteristicNotification(manager.getBlutoothGatt().getService(service).getCharacteristic(chara),
                enable);
    }

    protected void writeChara(UUID service, UUID chara, byte[] value) {
        BTManager.e(String.format("ready write:%s\t%s", chara, ByteUtil.toHex(value)));
        if (null == manager.getBlutoothGatt()) return;
        BluetoothGattCharacteristic characteristic = manager.getBlutoothGatt().getService(service).getCharacteristic(chara);
        characteristic.setValue(value);
        manager.getBlutoothGatt().writeCharacteristic(characteristic);
    }

    protected void onRequestConfirmed(int request, int result) {
    }

}
