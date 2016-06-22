package com.syber.hypoxia;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ProgressBar;
import android.widget.ViewSwitcher;

import com.orhanobut.logger.Logger;
import com.syber.base.BaseActivity;
import com.syber.base.BaseViewHolder;
import com.syber.base.util.ByteUtil;
import com.syber.base.view.ViewPost;

import java.lang.ref.WeakReference;
import java.util.UUID;

public class MeasureOxygenActivity extends BaseActivity implements BluetoothAdapter.LeScanCallback {
    public static final int SCAN_TIME = 1000 * 60;
    public static final UUID CCC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final UUID WRITE = UUID.fromString("0000cd20-0000-1000-8000-00805f9b34fb");
    private static final UUID NOTIFY1 = UUID.fromString("0000cd01-0000-1000-8000-00805f9b34fb");
    private static final UUID NOTIFY2 = UUID.fromString("0000cd02-0000-1000-8000-00805f9b34fb");
    private static final UUID NOTIFY3 = UUID.fromString("0000cd03-0000-1000-8000-00805f9b34fb");
    private static final UUID NOTIFY4 = UUID.fromString("0000cd04-0000-1000-8000-00805f9b34fb");
    private static final UUID[] NOTIFY = {NOTIFY1, NOTIFY2, NOTIFY3, NOTIFY4};
    private static final int OPEN_BLUETOOTH = 100;
    public static UUID SERVICE = UUID.fromString("ba11f08c-5f14-0b0d-1080-007cbe422c76");
    private boolean blueEanbled;
    private ViewHolder viewHolder;
    private AHandler handler;
    private Runnable stopRunnable = new Runnable() {
        @Override
        public void run() {
            BluetoothAdapter.getDefaultAdapter().stopLeScan(MeasureOxygenActivity.this);
        }
    };
    private BluetoothGattCallback callback = new GattCallback();
    private BluetoothGatt gatt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure_oxygen);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        blueEanbled = bluetoothAdapter.isEnabled();
        initAppBar();
        handler = new AHandler(this);
        viewHolder = new ViewHolder();
        if (!blueEanbled) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), OPEN_BLUETOOTH);
        } else {
            ViewPost.postOnAnimation(viewHolder.indeterminate, new Runnable() {
                @Override
                public void run() {
                    scan(true);
                }
            });
        }
    }

    private void scan(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        handler.removeCallbacks(stopRunnable);
        if (enable) {
            bluetoothAdapter.startLeScan(this);
            handler.postDelayed(stopRunnable, SCAN_TIME);
        } else {
            stopRunnable.run();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (OPEN_BLUETOOTH == requestCode) {
            if (RESULT_OK == resultCode) {
                scan(true);
            } else {
                showToast("没有打开蓝牙");
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scan(false);
        viewHolder = null;
        if (null != gatt) {
            gatt.disconnect();
            gatt = null;
        }
        if (!blueEanbled) {
            BluetoothAdapter.getDefaultAdapter().disable();
        }
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Logger.d(device.getName() + "\t" + device.getAddress() + "\t" + ByteUtil.toHex(scanRecord));
        if ("iChoice".equals(device.getName())) {
            scan(false);
            String uuid = "ba11f08c-5f14-0b0d-1080-00" + device.getAddress().replaceAll(":", "").substring(2);
            Logger.d("new uuid:" + uuid);
            SERVICE = UUID.fromString(uuid);
            gatt = device.connectGatt(this, false, callback);
        }
    }

    private static class CMD {
        //        public static final byte[] PAIR = {(byte) 0xB5, 0, 0, (byte) 0xB1, 0x04, 0x55, (byte) 0xAA};
        public static final byte[] PAIR = {(byte) 0xAA, 0x55, 0x04, (byte) 0xB1, 0, 0, (byte) 0xB5,};
    }

    private static class AHandler extends Handler {
        public static final int MSG_DISCORRY = 1;
        public static final int MSG_ENABLE = 2;
        public static final int MSG_PAIR = 3;
        public static final int MSG_READ_CHARA = 4;
        private WeakReference<MeasureOxygenActivity> reference;

        public AHandler(MeasureOxygenActivity activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MeasureOxygenActivity activity = reference.get();
            if (null == activity) return;
            switch (msg.what) {
                case MSG_DISCORRY:
                    activity.viewHolder.switcher.showNext();
                    activity.gatt.discoverServices();
                    break;
                case MSG_ENABLE:
                    BluetoothGattCharacteristic characteristic = activity.gatt.getService(SERVICE).getCharacteristic(NOTIFY[msg.arg1]);
                    activity.gatt.setCharacteristicNotification(characteristic, true);
                    BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(CCC);
                    clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    activity.gatt.writeDescriptor(clientConfig);
                    break;
                case MSG_PAIR:
//                    BluetoothGattDescriptor desc = activity.gatt.getService(SERVICE).getCharacteristic(WRITE).getDescriptor(WRITE_CCC);
//                    desc.setValue(CMD.PAIR);
//                    activity.gatt.writeDescriptor(desc);
                    characteristic = activity.gatt.getService(SERVICE).getCharacteristic(WRITE);
                    characteristic.setValue(CMD.PAIR);
                    activity.gatt.writeCharacteristic(characteristic);
                    break;
                case MSG_READ_CHARA:
                    activity.gatt.readCharacteristic(activity.gatt.getService(SERVICE).getCharacteristic(NOTIFY[msg.arg1]));
                    break;
            }
        }
    }

    private class ViewHolder extends BaseViewHolder {
        ProgressBar indeterminate;
        ViewSwitcher switcher;

        public ViewHolder() {
            super(findViewById(R.id.view_holder));
            indeterminate = get(R.id.indeterminate);
            switcher = get(R.id.switcher);
        }
    }

    private class GattCallback extends BluetoothGattCallback {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Logger.d("onConnection:" + newState);
            if (null == viewHolder) return;
            if (BluetoothProfile.STATE_CONNECTED == newState) {
                handler.sendEmptyMessage(AHandler.MSG_DISCORRY);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Logger.d("onServicesDiscovered:" + status);
            handler.sendEmptyMessage(AHandler.MSG_ENABLE);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Logger.d("onDescriptorWrite:" + status + "\t" + descriptor.getUuid());
            for (int i = 0; i < NOTIFY.length; i++) {
                if (NOTIFY[i].equals(descriptor.getCharacteristic().getUuid())) {
                    if (i + 1 < NOTIFY.length) {
                        Message.obtain(handler, AHandler.MSG_ENABLE, i + 1, 0).sendToTarget();
                    } else {
                        handler.sendEmptyMessage(AHandler.MSG_PAIR);
                    }
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Logger.d("onCharacteristicRead");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Logger.d("onCharacteristicWrite:" + ByteUtil.toHex(characteristic.getValue()));
            handler.sendEmptyMessage(AHandler.MSG_READ_CHARA);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Logger.d("onCharacteristicChanged" + ByteUtil.toHex(characteristic.getValue()));
            if (isFinishing()) return;
            byte[] value = characteristic.getValue();
            if (value.length == 6 && value[2] == 3 && value[4] != 0) {
                AddSPOActivity.fromMeasure(MeasureOxygenActivity.this, value[3] & 0xFF, value[4] & 0xFF);
                finish();
            }
        }
    }

}
