package com.syber.hypoxia.helo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

/**
 * Created by liangtg on 16-7-15.
 */
public class BleHelper implements BluetoothAdapter.LeScanCallback {
    public static final int OPEN_BLUETOOTH = 1366;
    private Handler handler = new Handler(Looper.getMainLooper());
    private String deviceName;
    private BleFlow bleFlow;
    private Context context;
    private boolean exit = false;
    private StartScanRunnable startScanRunnable = new StartScanRunnable();
    private StopScanRunnable stopScanRunnable = new StopScanRunnable();
    private GattCallback gattCallback = new GattCallback();
    private BluetoothGatt bluetoothGatt;

    public BleHelper(String deviceName, BleFlow bleFlow) {
        this.deviceName = deviceName;
        this.bleFlow = bleFlow;
    }

    public void startFlow(Activity activity) {
        context = activity;
        if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            enableBluetooth(activity);
        } else {
            startScan();
        }
    }

    private void startScan() {
        handler.removeCallbacks(startScanRunnable);
        handler.removeCallbacks(stopScanRunnable);
        startScanRunnable.run();
    }

    public void endFlow() {
        exit = true;
        stopScan();
        if (null != bluetoothGatt) bluetoothGatt.disconnect();
    }

    private void stopScan() {
        handler.removeCallbacks(startScanRunnable);
        handler.removeCallbacks(stopScanRunnable);
        stopScanRunnable.run();
    }

    public void enableBluetooth(Activity activity) {
        activity.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), OPEN_BLUETOOTH);
    }

    public boolean handleEnableResult(int requestCode, int resultCode, Intent data) {
        return OPEN_BLUETOOTH == requestCode && resultCode == Activity.RESULT_OK;
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (exit) return;
        if (deviceName.equals(device.getName())) {
            stopScan();
            bluetoothGatt = device.connectGatt(context, false, gattCallback);
        }
    }

    private class StopScanRunnable implements Runnable {
        @Override
        public void run() {
            BluetoothAdapter.getDefaultAdapter().stopLeScan(BleHelper.this);
            if (!exit) {
                handler.postDelayed(startScanRunnable, 500);
            }
        }
    }

    private class StartScanRunnable implements Runnable {
        @Override
        public void run() {
            BluetoothAdapter.getDefaultAdapter().startLeScan(BleHelper.this);
            handler.postDelayed(stopScanRunnable, 10 * 1000);
        }
    }

    private class GattCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (exit) return;
            if (BluetoothProfile.STATE_CONNECTED == newState && BluetoothGatt.GATT_SUCCESS == status) {
                gatt.discoverServices();
            } else if (BluetoothProfile.STATE_DISCONNECTED == newState) {
                bluetoothGatt = null;
                startScan();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (exit) return;
            if (BluetoothGatt.GATT_SUCCESS == status) {
                bleFlow.start();
            } else {
                gatt.discoverServices();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (exit) return;
            bleFlow.handleCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (exit) return;
            bleFlow.handleCharacteristicChanged(gatt, characteristic);
        }
    }

}
