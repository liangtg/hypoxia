package com.syber.hypoxia.helo;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Intent;

import com.syber.hypoxia.bt.FlowExtra;

import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by liangtg on 16-9-30.
 */

public class HypoxiaBPFlow extends BleFlow {
    private byte[] cmd_get_state = {0x55, (byte) 0xAA, 0x06, (byte) 0xbc, 0x00, (byte) 0xC1};
    private byte[] cmd_get_time = {0x55, (byte) 0xAA, 0x06, (byte) 0xB4, 0x00, (byte) 0xB9};
    private byte[] cmd_set_time = {0x55, (byte) 0xaa, 0x0b, (byte) 0xb2, 0x10, 0x07, 0x05, 0x10, 0x06, 0x00, (byte) 0xee};
    private int step = 0;
    private boolean haveProgress = false;
    private boolean started = false;
    private boolean waitCmd = false;

    @Override
    public void onStart() {
        started = true;
        step = 0;
        haveProgress = false;
        enableNotify(Hypoxia.SERVICE_DATA, Hypoxia.C2, true);
        enableNotifyCCC(Hypoxia.SERVICE_DATA, Hypoxia.C2, true);
    }

    @Override
    protected void onExit() {
        if (started) {
            enableNotify(Hypoxia.SERVICE_DATA, Hypoxia.C2, false);
            enableNotify(Hypoxia.SERVICE_BP, Hypoxia.ICP, false);
            enableNotify(Hypoxia.SERVICE_BP, Hypoxia.BPM, false);
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//        if (!haveProgress) {
//            new CheckThread().start();
//        }
    }

    @Override
    protected void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Intent intent = new Intent();
        intent.putExtra(FlowExtra.KEY_PUL_ARRAY, characteristic.getValue());
        if (Hypoxia.ICP.equals(characteristic.getUuid())) {
            haveProgress = true;
            intent.putExtra(FlowExtra.KEY_SYS, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 1).intValue());
            manager.requestConfirm(FlowExtra.PROGRESS_BP, this, intent);
        } else if (Hypoxia.BPM.equals(characteristic.getUuid())) {
            if (haveProgress) {
                setHandleEnd(true);
                enableNotify(Hypoxia.SERVICE_BP, Hypoxia.ICP, false);
                enableNotify(Hypoxia.SERVICE_BP, Hypoxia.BPM, false);
            }
            intent.putExtra(FlowExtra.KEY_SYS, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 1).intValue());
            intent.putExtra(FlowExtra.KEY_DIA, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 3).intValue());
            intent.putExtra(FlowExtra.KEY_PUL, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 14).intValue());
            manager.requestConfirm(FlowExtra.RESULT_BP, this, intent);
        } else if (Hypoxia.C2.equals(characteristic.getUuid())) {
            if (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) == 0xAA) {
                step++;
                next();
            } else if (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) == 0x55) {
                if (!haveProgress) new CheckThread().start();
            }
        }
    }

    @Override
    protected void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        if (BleFlow.CCC.equals(descriptor.getUuid())) {
            if (Hypoxia.BPM.equals(descriptor.getCharacteristic().getUuid())) {
                if (enableNotify(Hypoxia.SERVICE_BP, Hypoxia.ICP, true)) {
                    BTManager.e("enable icp true");
                    enableNotifyCCC(Hypoxia.SERVICE_BP, Hypoxia.ICP, true);
                } else {
                    BTManager.e("enable icp fail");
                }
            } else if (Hypoxia.ICP.equals(descriptor.getCharacteristic().getUuid())) {
                if (Arrays.equals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE, descriptor.getValue())) {
                    enableNotify(Hypoxia.SERVICE_BP, Hypoxia.ICP, true);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    manager.getBlutoothGatt().writeDescriptor(descriptor);
                }
            } else if (Hypoxia.C2.equals(descriptor.getCharacteristic().getUuid())) {
                next();
            }
        }
    }

    private void next() {
        if (waitCmd) return;
        if (step == 0) {
            writeChara(Hypoxia.SERVICE_DATA, Hypoxia.C1, cmd_get_state);
        } else if (step == 1) {
            writeChara(Hypoxia.SERVICE_DATA, Hypoxia.C1, cmd_get_time);
        } else if (step == 2) {
            setTime();
        } else {
            enableNotify(Hypoxia.SERVICE_DATA, Hypoxia.C2, false);
            enableNotify(Hypoxia.SERVICE_BP, Hypoxia.BPM, true);
            enableNotifyCCC(Hypoxia.SERVICE_BP, Hypoxia.BPM, true);
        }
    }

    private void setTime() {
        Calendar cal = Calendar.getInstance();
        cmd_set_time[4] = (byte) (cal.get(Calendar.YEAR) % 100);
        cmd_set_time[5] = (byte) (cal.get(Calendar.MONTH) + 1);
        cmd_set_time[6] = (byte) cal.get(Calendar.DAY_OF_MONTH);
        cmd_set_time[7] = (byte) cal.get(Calendar.HOUR_OF_DAY);
        cmd_set_time[8] = (byte) cal.get(Calendar.MINUTE);
        cmd_set_time[9] = (byte) cal.get(Calendar.SECOND);
        int sum = 0;
        for (int i = 0; i < cmd_set_time.length - 1; i++) {
            sum += cmd_set_time[i];
        }
        cmd_set_time[10] = (byte) sum;
        writeChara(Hypoxia.SERVICE_DATA, Hypoxia.C1, cmd_set_time);
    }


    private class CheckThread extends Thread {
        @Override
        public void run() {
            if (isExit()) return;
            try {
                Thread.sleep(1000 * 50);
            } catch (InterruptedException e) {
            }
            if (!haveProgress) next();
        }
    }
}
