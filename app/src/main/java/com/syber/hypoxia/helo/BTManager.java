package com.syber.hypoxia.helo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;

import com.syber.base.util.ByteUtil;
import com.syber.hypoxia.HeloCMD;
import com.syber.hypoxia.IApplication;
import com.syber.hypoxia.bt.FlowExtra;

import java.util.List;

/**
 * Created by liangtg on 16-8-2.
 */
public class BTManager implements IBleManager {
    public static final String DEVICE_HELO = "HeloHL01";
    public static final String DEVICE_IPC_906 = "IPC-906";
    public static final int OPEN_BLUETOOTH = 1366;
    public static BTManager instance = new BTManager();
    private static int gattId = 0;
    private Handler handler = new Handler(Looper.getMainLooper());
    private String deviceName;
    private BleFlow bleFlow;
    private boolean exit = false;
    private StartScanRunnable startScanRunnable = new StartScanRunnable();
    private StopScanRunnable stopScanRunnable = new StopScanRunnable();
    private GattCallback gattCallback = new GattCallback();
    private BluetoothGatt bluetoothGatt;
    private SparseArray<BleFlow> requestArray = new SparseArray<>();
    private RequestListener listener;

    private ScanCallback scanCallback = new ScanCallback();

    public static void e(Object msg) {
        Log.e("flow", "" + msg);
    }

    public static void initCmd() {
        String mac = BluetoothAdapter.getDefaultAdapter().getAddress();
        String[] value = mac.split(":");
        int sum = 0, b = 0;
        for (int i = 2; i < value.length; i++) {
            b = Integer.parseInt(value[i], 16);
            HeloCMD.MATCH.cmd[i + 3] = (byte) b;
//            HeloCMD.RESPONSE_BIND.cmd[i + 3] = (byte) b;
            e("" + value[i]);
            sum += b;
        }
        sum += HeloCMD.MATCH.cmd[2] + HeloCMD.MATCH.cmd[3] + HeloCMD.MATCH.cmd[4];
        updateSum(sum, HeloCMD.MATCH.cmd);
//        sum += 2;
//        updateSum(sum, HeloCMD.RESPONSE_BIND.cmd);
    }

    private static void updateSum(int sum, byte[] cmd) {
        String s = Integer.toHexString(sum);
        StringBuffer sb = new StringBuffer(s);
        if (sb.length() % 2 == 1) sb.insert(0, "0");
        Log.e("mac", "sum" + sb.toString());
        for (int i = 0; i < sb.length() / 2; i++) {
            int start = sb.length() - (i + 1) * 2;
            Log.e("mac", "start" + start);
            String substring = sb.substring(start, start + 2);
            Log.e("mac", "start" + substring);
            cmd[9 + i] = (byte) Integer.parseInt(substring, 16);
        }
    }

    @Override
    public BluetoothGatt getBlutoothGatt() {
        return bluetoothGatt;
    }

    @Override
    public void requestConfirm(int request, BTFlow flow, Intent data) {
        if (!exit && null != listener) {
            this.requestArray.put(request, (BleFlow) flow);
            handler.post(new RequestRunnable(request, data));
        }
    }

    @Override
    public void setRequestConfirmed(int request, int result) {
        if (exit || !gattCallback.connected || null == listener) return;
        this.requestArray.get(request).onRequestConfirmed(request, result);
        requestArray.remove(request);
    }

    public void startHeloBP(Activity activity) {
        initCmd();
        deviceName = DEVICE_HELO;
        bleFlow = new BPFlow();
        start(activity);
    }

    public void startHeloHR(Activity activity) {
        initCmd();
        deviceName = DEVICE_HELO;
        bleFlow = new HRFlow();
        start(activity);
    }

    public void startHeloECG(Activity activity) {
        initCmd();
        deviceName = DEVICE_HELO;
        bleFlow = new ECGFlow();
        start(activity);
    }

    public void startHypoxiaBP(Activity activity) {
        if (deviceName != DEVICE_IPC_906) resetState();
        deviceName = DEVICE_IPC_906;
        bleFlow = new HypoxiaBPFlow();
        bleFlow.setBleManager(this);
        start(activity);
    }

    public void startHypoxiaSync(Activity activity) {
        if (deviceName != DEVICE_IPC_906) resetState();
        deviceName = DEVICE_IPC_906;
        bleFlow = new HypoxiaSyncFlow();
        bleFlow.setBleManager(this);
        start(activity);
    }

    public void setRequestListener(RequestListener listener) {
        this.listener = listener;
    }

    public void start(Activity activity) {
        exit = false;
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            enableBluetooth(activity);
        } else if (null != bluetoothGatt) {
            bleFlow.start();
        } else {
            startScan();
        }
    }

    private void startScan() {
        handler.removeCallbacks(startScanRunnable);
        handler.removeCallbacks(stopScanRunnable);
        scanCallback.inScan = true;
        startScanRunnable.run();
    }

    public void enableBluetooth(Activity activity) {
        activity.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), OPEN_BLUETOOTH);
    }

    public boolean handleEnableResult(int requestCode, int resultCode, Intent data) {
        return OPEN_BLUETOOTH == requestCode && resultCode == Activity.RESULT_OK;
    }

    public void stop() {
        stop(false);
    }

    public void stop(boolean real) {
        exit = true;
        stopScan();
        if (!real) return;
        if (null != bleFlow) {
            bleFlow.exit();
            bleFlow = null;
        }
        resetState();
    }

    private void resetState() {
        gattCallback.inConnect = false;
        if (null != bluetoothGatt) {
            bluetoothGatt.disconnect();
            bluetoothGatt = null;
        }
    }

    private void stopScan() {
        scanCallback.inScan = false;
        handler.removeCallbacks(startScanRunnable);
        handler.removeCallbacks(stopScanRunnable);
        stopScanRunnable.run();
        handler.removeCallbacks(startScanRunnable);
    }

    public interface RequestListener {
        void onRequestConfirm(int request, Intent data);
    }

    private class ScanCallback implements BluetoothAdapter.LeScanCallback {
        private boolean inScan = true;

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (exit || !inScan) return;
            e(String.format("scan:||%s||----%s", device.getName(), device.getAddress()));
            if (deviceName.equals(device.getName())) {
                stopScan();
                e("connecting...");
                gattCallback = new GattCallback();
                gattCallback.reset();
                bluetoothGatt = device.connectGatt(IApplication.getContext(), false, gattCallback);
                bleFlow.setBleManager(BTManager.this);
            }
        }
    }

    private class StartScanRunnable implements Runnable {
        @Override
        public void run() {
            e("start le scan");
            BluetoothAdapter.getDefaultAdapter().startLeScan(scanCallback);
            handler.postDelayed(stopScanRunnable, 10 * 1000);
        }
    }

    private class StopScanRunnable implements Runnable {
        @Override
        public void run() {
            e("stop scan!");
            BluetoothAdapter.getDefaultAdapter().stopLeScan(scanCallback);
            handler.postDelayed(startScanRunnable, 500);
        }
    }

    private class GattCallback extends BluetoothGattCallback {
        private int gid;
        private boolean inConnect = true;
        private boolean connected = false;

        public GattCallback() {
            gid = gattId++;
        }

        private void reset() {
            inConnect = true;
            connected = false;
        }

        private boolean isGattDisconnected() {
            return gid < gattId - 1;
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            e(String.format("%d State:%d\t%d", gid, status, newState));
            if (isGattDisconnected()) {
                gatt.disconnect();
                return;
            }
            if (exit || !inConnect) {
                resetState();
                return;
            }
            if (BluetoothProfile.STATE_CONNECTED == newState && BluetoothGatt.GATT_SUCCESS == status) {
                connected = true;
                gatt.discoverServices();
            } else if (BluetoothProfile.STATE_DISCONNECTED == newState) {
                resetState();
                if (connected) {
                    connected = false;
                    if (bleFlow.handleEnd()) {
                        requestConfirm(FlowExtra.REQUEST_CONFIRM_DISCONNECT, null, null);
                    } else {
                        inConnect = true;
                        bleFlow.reset();
                        startScan();
                    }
                } else {
                    inConnect = true;
                    bleFlow.reset();
                    startScan();
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            e(String.format("%d onServicesDiscovered:%d", gid, status));
            if (isGattDisconnected()) {
                gatt.disconnect();
                return;
            }
            if (exit || !inConnect) return;
            if (BluetoothGatt.GATT_SUCCESS == status) {
                List<BluetoothGattService> services = gatt.getServices();
                for (int i = 0; i < services.size(); i++) {
                    e(services.get(i).getUuid());
                    for (int j = 0; j < services.get(i).getCharacteristics().size(); j++) {
                        e("\t" + services.get(i).getCharacteristics().get(j).getUuid());
                    }
                }
                bleFlow.start();
            } else {
                gatt.discoverServices();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            e(String.format("%d writed:%s\t%s\t%d", gid, characteristic.getUuid().toString(), ByteUtil.toHex(characteristic.getValue()), status));
            if (isGattDisconnected()) {
                gatt.disconnect();
                return;
            }
            if (exit || !inConnect) return;
            bleFlow.handleCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            e(String.format("%d cha changed:%s\t%s", gid, characteristic.getUuid().toString(), ByteUtil.toHex(characteristic.getValue())));
            if (isGattDisconnected()) {
                gatt.disconnect();
                return;
            }
            if (exit || !inConnect || bleFlow.handleEnd()) return;
            bleFlow.handleCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            e(String.format("%d des write:%s\t%s\t%s",
                    gid,
                    descriptor.getUuid().toString(),
                    ByteUtil.toHex(descriptor.getValue()),
                    Integer.toHexString(status)));
            if (isGattDisconnected()) {
                gatt.disconnect();
                return;
            }
            if (exit || !inConnect) return;
            bleFlow.handleDescriptorWrite(gatt, descriptor, status);
        }
    }

    private class RequestRunnable implements Runnable {
        private int request;
        private Intent data;

        public RequestRunnable(int request, Intent data) {
            this.request = request;
            this.data = data;
        }

        @Override
        public void run() {
            if (null != listener) {
                listener.onRequestConfirm(request, data);
            }
        }
    }

}