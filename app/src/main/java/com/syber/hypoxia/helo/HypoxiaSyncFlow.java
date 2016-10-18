package com.syber.hypoxia.helo;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Intent;

/**
 * Created by liangtg on 16-10-11.
 */

public class HypoxiaSyncFlow extends BleFlow {
    private byte[] CMD_SYNC_BP = {0x55, (byte) 0xAA, 0x06, (byte) 0xBA, 0x00, (byte) 0xBF};
    private byte[] CMD_SYNC_HYPOXIA = {0x55, (byte) 0xAA, 0x06, (byte) 0xBA, 0x01, (byte) 0xC0};
    private byte[] CMD_ACK = {0x55, (byte) 0xaa, 0x08, (byte) 0xb5, (byte) 0xba, 0x00, 0x00, 0x76};
    private boolean end = false;

    @Override
    public void onStart() {
        enableNotify(Hypoxia.SERVICE_DATA, Hypoxia.C2, true);
        enableNotifyCCC(Hypoxia.SERVICE_DATA, Hypoxia.C2, true);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (characteristic.getValue().length == 8 && !end) {
            end = true;
            writeChara(Hypoxia.SERVICE_DATA, Hypoxia.C1, CMD_SYNC_HYPOXIA);
        } else {
            new CheckThread().start();
        }
    }

    @Override
    protected void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) == 0x55) {
            new CheckThread().start();
        } else {
//            [aa 55 14 1 10 a 12 e 36 2c 0 73 41 4c 0 0 0 0 0 b0]
//            [aa 55 14 6 e a 11 a 31 e a 11 a 3a 0 dc 55 5a 0 75]
            Integer mode = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 3);
            Intent data = new Intent();
            if (mode > 2 && mode < 7) {
                data.putExtra(KEY_START_TIME,
                        String.format("20%02d-%02d-%02d %02d:%02d:00",
                                getUInt8(characteristic, 4),
                                getUInt8(characteristic, 5),
                                getUInt8(characteristic, 6),
                                getUInt8(characteristic, 7),
                                getUInt8(characteristic, 8)));
                data.putExtra(KEY_END_TIME,
                        String.format("20%02d-%02d-%02d %02d:%02d:00",
                                getUInt8(characteristic, 9),
                                getUInt8(characteristic, 10),
                                getUInt8(characteristic, 11),
                                getUInt8(characteristic, 12),
                                getUInt8(characteristic, 13)));
                data.putExtra(KEY_MODE, getUInt8(characteristic, 3) - 2);
                if (end) {
                    sendACK(0);
                    manager.requestConfirm(RESULT_HYPOXIA, this, data);
                    setHandleEnd(true);
                } else {
                    writeChara(Hypoxia.SERVICE_DATA, Hypoxia.C1, CMD_SYNC_BP);
                }
            } else {
                data.putExtra(KEY_TIME,
                        String.format("20%02d-%02d-%02d %02d:%02d:%02d",
                                getUInt8(characteristic, 4),
                                getUInt8(characteristic, 5),
                                getUInt8(characteristic, 6),
                                getUInt8(characteristic, 7),
                                getUInt8(characteristic, 8),
                                getUInt8(characteristic, 9)));
                data.putExtra(KEY_SYS, getUInt8(characteristic, 11));
                data.putExtra(KEY_DIA, getUInt8(characteristic, 12));
                data.putExtra(KEY_PUL, getUInt8(characteristic, 13));
                manager.requestConfirm(RESULT_BP, this, data);
                sendACK(0);
            }
        }
    }

    @Override
    protected void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        writeChara(Hypoxia.SERVICE_DATA, Hypoxia.C1, CMD_SYNC_BP);
    }

    private void sendACK(int num) {
        CMD_ACK[5] = (byte) num;
        CMD_ACK[7] = (byte) (0x76 + num);
        writeChara(Hypoxia.SERVICE_DATA, Hypoxia.C1, CMD_ACK);
    }

    private class CheckThread extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(5 * 1000);
            } catch (InterruptedException e) {
            }
            if (!isExit() && !handleEnd()) {
                writeChara(Hypoxia.SERVICE_DATA, Hypoxia.C1, CMD_SYNC_BP);
            }
        }
    }


}
