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
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.orhanobut.logger.Logger;
import com.syber.base.BaseActivity;
import com.syber.base.BaseViewHolder;
import com.syber.base.util.ByteUtil;
import com.syber.base.util.Extra;
import com.syber.base.view.ViewPost;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.UUID;

public class HeloDeviceActivity extends BaseActivity implements BluetoothAdapter.LeScanCallback {
    public static final int SCAN_TIME = 1000 * 60;
    public static final int OP_BP = 1;
    public static final int OP_HR = 2;
    public static final int OP_ECG = 3;
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int op = intent.getIntExtra(Extra.OPERATION, -1);
        if (OP_BP == op) {
            Message.obtain(handler, AHandler.MSG_WRITE, 0, 0, CMD.GET_BP).sendToTarget();
        } else if (OP_HR == op) {
            Message.obtain(handler, AHandler.MSG_WRITE, 0, 0, CMD.GET_HR).sendToTarget();
        } else if (OP_ECG == op) {
            Message.obtain(handler, AHandler.MSG_WRITE, 0, 0, CMD.GET_ECG).sendToTarget();
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

    public void visibleOp(int op) {
        if (op == 1) {
            viewHolder.switcher.setVisibility(View.GONE);
            if (null == getSupportFragmentManager().findFragmentByTag("helo_op")) {
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new HeloOpFragment(), "helo_op").commit();
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getBaseContext());
            builder.setTitle("手环已绑定在其他设备");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.show();
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

    private enum CMD {
        MATCH(new byte[]{0x12, 0x34, 0x0B, 0x11, 0x04, 0x11, 0x11, 0x11, 0x11, 0x64, 0, 0, 0, 0x43, 0x21}, SERVICE1, S1N1),
        RESPONSE_BIND(new byte[]{0x12, 0x34, 0x0B, 0x13, 0x04, 0x11, 0x11, 0x11, 0x11, 0x66, 0, 0, 0, 0x43, 0x21}, SERVICE1, S1N1),
        GET_BP(new byte[]{0x12, 0x34, 0x0A, 0x0C, 0x16, 0, 0, 0, 0x43, 0x21}, SERVICE0, S0N2),
        GET_HR(new byte[]{0x12, 0x34, 0x0A, 0x02, 0x0C, 0, 0, 0, 0x43, 0x21}, SERVICE0, S0N2),
        GET_ECG(new byte[]{0x12, 0x34, 0x0A, 0x0D, 0x17, 0, 0, 0, 0x43, 0x21}, SERVICE0, S0N2),
        REQUEST_UNBIND(new byte[]{0x12, 0x34, 0x0A, 0x9, 0x13, 0, 0, 0, 0x43, 0x21}, SERVICE1, S1N1),;
        private byte[] cmd;
        private UUID service;
        private UUID cha;

        CMD(byte[] cmd, UUID service, UUID cha) {
            this.cmd = cmd;
            this.service = service;
            this.cha = cha;
        }

        public byte cmdByte() {
            return cmd[3];
        }

        @Override
        public String toString() {
            return "CMD{" +
                    "cmd=" + Arrays.toString(cmd) +
                    ", service=" + service +
                    ", cha=" + cha +
                    '}';
        }
    }

    private static class AHandler extends Handler {
        public static final int MSG_DISCORRY = 1;
        public static final int MSG_ENABLE = 2;
        public static final int MSG_WRITE = 3;
        public static final int MSG_READ_CHARA = 4;
        public static final int MSG_DEVICE_MATCHED = 100;
        public static final int MSG_BP_RESULT = 101;
        public static final int MSG_HR_RESULT = 102;
        public static final int MSG_ECG_RESULT = 103;
        private WeakReference<HeloDeviceActivity> reference;

        public AHandler(HeloDeviceActivity activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            HeloDeviceActivity activity = reference.get();
            if (null == activity) return;
            CMD cmd;
            switch (msg.what) {
                case MSG_DISCORRY:
                    activity.viewHolder.switcher.showNext();
                    activity.gatt.discoverServices();
                    break;
                case MSG_ENABLE:
                    cmd = (CMD) msg.obj;
                    BluetoothGattCharacteristic characteristic = activity.gatt.getService(cmd.service).getCharacteristic(cmd.cha);
                    boolean e = activity.gatt.setCharacteristicNotification(characteristic, true);
                    Logger.d("enable:" + cmd.cha + "\t" + e);
                    break;
                case MSG_WRITE:
                    cmd = (CMD) msg.obj;
                    Logger.d("ready write:" + cmd);
                    characteristic = activity.gatt.getService(cmd.service).getCharacteristic(cmd.cha);
                    characteristic.setValue(cmd.cmd);
                    activity.gatt.writeCharacteristic(characteristic);
                    break;
                case MSG_READ_CHARA:
                    cmd = (CMD) msg.obj;
                    Logger.d("read:" + cmd);
                    activity.gatt.readCharacteristic(activity.gatt.getService(cmd.service).getCharacteristic(cmd.cha));
                    break;
                case MSG_DEVICE_MATCHED:
                    activity.visibleOp(msg.arg1);
                    break;
                case MSG_BP_RESULT:
                    HeloResponse response = (HeloResponse) msg.obj;
                    activity.showToast(String.format("血压:%d-%d", response.intValue(4), response.intValue(5)));
                    break;
                case MSG_HR_RESULT:
                    response = (HeloResponse) msg.obj;
                    activity.showToast(String.format("心率:%d", response.intValue(4)));
                    break;
                case MSG_ECG_RESULT:
                    response = (HeloResponse) msg.obj;
                    Toast.makeText(activity, "心电:" + response.byteValue(), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    private static class HeloResponse {
        public static final byte MATCH = 0x37;
        public static final byte REQUEST_BIND = 0x23;
        public static final byte BP = 0x41;
        public static final byte HR = 0x32;
        public static final byte ECG = 0x42;
        private byte[] array;

        public HeloResponse(byte[] array) {
            this.array = array;
        }

        public boolean verify() {
            boolean result = false;
            byte[] cmd = array;
            if (cmd.length >= 10) {
                if (cmd[0] == 0x12 && cmd[1] == 0x34 && cmd[cmd.length - 2] == 0x43 && cmd[cmd.length - 1] == 0x21) {
                    result = true;
                }
            }
            return result;
        }

        public byte cmd() {
            return array[3];
        }

        public byte byteValue() {
            return byteValue(0);
        }

        public byte byteValue(int offset) {
            return array[5 + offset];
        }

        public int intValue(int offset) {
            return byteValue(offset) & 0xFF;
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
            Logger.d("onConnection:" + newState + "\tgatt:" + Integer.toHexString(status));
            if (null == viewHolder) return;
            if (BluetoothProfile.STATE_CONNECTED == newState) {
                handler.sendEmptyMessage(AHandler.MSG_DISCORRY);
            } else if (BluetoothProfile.STATE_DISCONNECTED == newState) {
                gatt.connect();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Logger.d("onServicesDiscovered:" + status);
            handler.sendMessageDelayed(Message.obtain(handler, AHandler.MSG_ENABLE, CMD.MATCH), 0);
            handler.sendMessageDelayed(Message.obtain(handler, AHandler.MSG_ENABLE, CMD.GET_BP), 100);
            handler.sendMessageDelayed(Message.obtain(handler, AHandler.MSG_WRITE, CMD.MATCH), 500);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Logger.d("onDescriptorWrite:" + status + "\t" + descriptor.getUuid());
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Logger.d("onCharacteristicRead" + ByteUtil.toHex(characteristic.getValue()));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Logger.d("onCharacteristicWrite:" + ByteUtil.toHex(characteristic.getValue()));
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Logger.d("onCharacteristicChanged" + ByteUtil.toHex(characteristic.getValue()));
            if (isFinishing()) return;
            HeloResponse response = new HeloResponse(characteristic.getValue());
            if (response.verify()) {
                if (HeloResponse.REQUEST_BIND == response.cmd()) {
                    handler.obtainMessage(AHandler.MSG_WRITE, CMD.RESPONSE_BIND).sendToTarget();
                } else if (HeloResponse.MATCH == response.cmd()) {
                    Message.obtain(handler, AHandler.MSG_DEVICE_MATCHED, response.byteValue(), 0).sendToTarget();
//                    Message.obtain(handler, AHandler.MSG_WRITE, 0, 0, CMD.REQUEST_UNBIND).sendToTarget();
                } else if (HeloResponse.BP == response.cmd()) {
                    Message.obtain(handler, AHandler.MSG_BP_RESULT, 0, 0, response).sendToTarget();
                } else if (HeloResponse.HR == response.cmd()) {
                    Message.obtain(handler, AHandler.MSG_HR_RESULT, 0, 0, response).sendToTarget();
                } else if (HeloResponse.ECG == response.cmd()) {
                    Message.obtain(handler, AHandler.MSG_ECG_RESULT, 0, 0, response).sendToTarget();
                }
            }
        }

    }


}
