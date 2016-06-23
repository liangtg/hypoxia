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

public class HeloDeviceActivity extends BaseActivity implements BluetoothAdapter.LeScanCallback {
    public static final int SCAN_TIME = 1000 * 60;
    public static final UUID CCC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final UUID S0N1 = UUID.fromString("facebead-ffff-eeee-0001-facebeadaaaa");
    private static final UUID S0N2 = UUID.fromString("facebead-ffff-eeee-0002-facebeadaaaa");
    private static final UUID S0N3 = UUID.fromString("facebead-ffff-eeee-0003-facebeadaaaa");
    private static final UUID S0N4 = UUID.fromString("facebead-ffff-eeee-0004-facebeadaaaa");
    private static final UUID S0N5 = UUID.fromString("facebead-ffff-eeee-0005-facebeadaaaa");
    private static final UUID S1N1 = UUID.fromString("facebead-ffff-eeee-0010-facebeadaaaa");
    private static final UUID S1N2 = UUID.fromString("facebead-ffff-eeee-0020-facebeadaaaa");
    private static final UUID S2N1 = UUID.fromString("facebead-ffff-eeee-0100-facebeadaaaa");
    private static final UUID S2N2 = UUID.fromString("facebead-ffff-eeee-0200-facebeadaaaa");

    private static final int OPEN_BLUETOOTH = 100;
    public static UUID SERVICE0 = UUID.fromString("0aabcdef-1111-2222-0000-facebeadaaaa");
    public static UUID SERVICE1 = UUID.fromString("1aabcdef-1111-2222-0000-facebeadaaaa");
    public static UUID SERVICE2 = UUID.fromString("2aabcdef-1111-2222-0000-facebeadaaaa");
    private static final UUID[] ALL = {SERVICE0, S0N1, SERVICE0, S0N2, SERVICE0, S0N3, SERVICE0, S0N4, SERVICE0, S0N5, SERVICE1, S1N1, SERVICE1, S1N2, SERVICE2, S2N1, SERVICE2, S2N2};
    private boolean blueEanbled;
    private ViewHolder viewHolder;
    private AHandler handler;
    private Runnable stopRunnable = new Runnable() {
        @Override
        public void run() {
            BluetoothAdapter.getDefaultAdapter().stopLeScan(HeloDeviceActivity.this);
        }
    };
    private BluetoothGattCallback callback = new GattCallback();
    private BluetoothGatt gatt;
    private int position = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helo_device);
        initAppBar();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        blueEanbled = bluetoothAdapter.isEnabled();
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
        if ("HeloHL01".equals(device.getName())) {
            scan(false);
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
        private WeakReference<HeloDeviceActivity> reference;

        public AHandler(HeloDeviceActivity activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            HeloDeviceActivity activity = reference.get();
            if (null == activity) return;
            switch (msg.what) {
                case MSG_DISCORRY:
                    activity.viewHolder.switcher.showNext();
                    activity.gatt.discoverServices();
                    break;
                case MSG_ENABLE:
                    BluetoothGattCharacteristic characteristic = activity.gatt.getService(ALL[msg.arg1 * 2]).getCharacteristic(ALL[msg.arg1 * 2 + 1]);
                    activity.gatt.setCharacteristicNotification(characteristic, true);
//                    BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(CCC);
//                    clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                    activity.gatt.writeDescriptor(clientConfig);
                    break;
                case MSG_PAIR:
//                    characteristic = activity.gatt.getService(SERVICE).getCharacteristic(WRITE);
//                    characteristic.setValue(CMD.PAIR);
//                    activity.gatt.writeCharacteristic(characteristic);
                    break;
                case MSG_READ_CHARA:
//                    activity.gatt.readCharacteristic(activity.gatt.getService(SERVICE).getCharacteristic(NOTIFY[msg.arg1]));
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
            for (int i = 0; i < ALL.length / 2; i++) {
                handler.sendEmptyMessage(AHandler.MSG_ENABLE);
                Message.obtain(handler, AHandler.MSG_ENABLE, i, 0).sendToTarget();
                position++;
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Logger.d("onDescriptorWrite:" + status + "\t" + descriptor.getUuid());
            if (position < ALL.length / 2) {
                Message.obtain(handler, AHandler.MSG_ENABLE, 0, 0).sendToTarget();
                position++;
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
        }
    }


}
