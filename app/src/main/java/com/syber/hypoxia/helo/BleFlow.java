package com.syber.hypoxia.helo;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import com.syber.base.util.ByteUtil;

import java.util.UUID;

/**
 * Created by liangtg on 16-7-15.
 */
public abstract class BleFlow implements IBleManager.BTFlow {
    public static final UUID CCC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    protected BleFlow dependency;
    protected BleFlow next;
    protected IBleManager manager;
    private boolean handleEnd = false;
    private boolean exit = false;

    public void setBleManager(IBleManager manager) {
        if (null != dependency) dependency.setBleManager(manager);
        this.manager = manager;
    }

    public void start() {
        BTManager.e("start:\t" + getClass().getSimpleName());
        if (null != dependency && !dependency.handleEnd()) {
            dependency.start();
        } else {
            exit = false;
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

    protected void exit() {
        exit = true;
        onExit();
    }

    protected boolean isExit() {
        return exit;
    }

    protected void onExit() {
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

    protected void enableNotifyCCC(UUID service, UUID chara, boolean enable) {
        BluetoothGattCharacteristic characteristic = manager.getBlutoothGatt().getService(service).getCharacteristic(chara);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CCC);
        if ((BluetoothGattCharacteristic.PROPERTY_INDICATE & characteristic.getProperties()) != 0) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            manager.getBlutoothGatt().writeDescriptor(descriptor);
        } else if ((BluetoothGattCharacteristic.PROPERTY_NOTIFY & characteristic.getProperties()) != 0) {
            if (enable) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            } else {
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            }
            manager.getBlutoothGatt().writeDescriptor(descriptor);
        }
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

    protected int getUInt8(BluetoothGattCharacteristic characteristic, int offset) {
        return characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
    }

    protected int getUInt16(BluetoothGattCharacteristic characteristic, int offset) {
        return characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
    }

}
