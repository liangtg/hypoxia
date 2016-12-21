package com.syber.hypoxia.bt;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;

import com.orhanobut.logger.Logger;
import com.syber.base.io.IOUtils;
import com.syber.hypoxia.helo.BTManager;
import com.syber.hypoxia.helo.IBleManager;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by liangtg on 16-11-15.
 */

public class SPPManager implements IBleManager {
    public static final int OPEN_BLUETOOTH = 1366;
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static Executor executor = Executors.newSingleThreadExecutor();
    private volatile boolean exit = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private BluetoothSocket socket;
    private SPPFlow sppFlow;
    private BTReceiver receiver = new BTReceiver();
    private Activity activity;
    private boolean discory = false;
    private BTManager.RequestListener listener;
    private SparseArray<SPPFlow> requestArray = new SparseArray<>();

    public SPPManager(Activity activity) {
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_FOUND);// 用BroadcastReceiver来取得搜索结果
        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        activity.registerReceiver(receiver, intent);
        this.activity = activity;
    }

    public void setFlow(SPPFlow flow) {
        sppFlow = flow;
    }

    public void start() {
        exit = false;
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            enableBluetooth(activity);
        } else if (null != socket && socket.isConnected()) {
            sppFlow.onSocketConnected(socket);
        } else {
            startScan();
        }
    }

    private void startScan() {
        discory = true;
        BluetoothAdapter.getDefaultAdapter().startDiscovery();
    }

    private void stopScan() {
        discory = false;
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
    }

    public void stop() {
        exit = true;
        activity.unregisterReceiver(receiver);
        stopScan();
        if (null != socket) {
//            IOUtils.closeSilenty(socket);
            new DelayCloseThread(socket).start();
            socket = null;
        }
    }

    public void enableBluetooth(Activity activity) {
        activity.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), OPEN_BLUETOOTH);
    }

    public boolean handleEnableResult(int requestCode, int resultCode, Intent data) {
        return OPEN_BLUETOOTH == requestCode && resultCode == Activity.RESULT_OK;
    }

    @Override
    public BluetoothGatt getBlutoothGatt() {
        return null;
    }

    @Override
    public void requestConfirm(int request, BTFlow flow, Intent data) {
        if (!exit && null != listener) {
            this.requestArray.put(request, (SPPFlow) flow);
            handler.post(new RequestRunnable(request, data));
        }
    }

    private void reportState(int state) {
        if (!exit && null != listener) {
            handler.post(new RequestRunnable(state, null));
        }
    }

    public void setRequestListener(BTManager.RequestListener listener) {
        this.listener = listener;
    }

    @Override
    public void setRequestConfirmed(int request, int result) {
        if (exit || null == socket) return;
        this.requestArray.get(request).onRequestConfirmed(request, result);
        requestArray.remove(request);
    }

    private void onDeviceFounded(BluetoothDevice device) {
        Logger.d(String.format("found:%s\t%s", device.getName(), device.getAddress()));
        if (!discory) return;
        if (null != sppFlow && sppFlow.getDeviceName().equals(device.getName())) {
            stopScan();
            executor.execute(new ConnectDeviceRunnable(device));
        }
    }

    public interface SPPFlow extends BTFlow {
        String getDeviceName();

        void onSocketConnected(BluetoothSocket socket);

        void onRequestConfirmed(int request, int result);
    }

    private class ConnectDeviceRunnable implements Runnable {
        BluetoothDevice device;

        public ConnectDeviceRunnable(BluetoothDevice device) {
            this.device = device;
        }

        @Override
        public void run() {
            try {
                socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                socket.connect();
            } catch (IOException e) {
                Log.e("flow", null, e);
                if (null != socket) {
                    IOUtils.closeSilenty(socket);
                    socket = null;
                }
            }
            if (!exit) {
                if (null == socket) {
                    reportState(FlowExtra.REPORT_STATE_CONNECT_FAILED);
                    startScan();
                } else {
                    sppFlow.onSocketConnected(socket);
                    reportState(FlowExtra.REPORT_STATE_CONNECTED);
                }
            }
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

    private class BTReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.d(intent.toString());
            if (exit) return;
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                if (BluetoothAdapter.STATE_ON == state) {
                    if (discory) startScan();
                } else if (BluetoothAdapter.STATE_OFF == state) {
                    stopScan();
                }
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                onDeviceFounded(intent.<BluetoothDevice>getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (discory) startScan();
            }
        }
    }

    private class DelayCloseThread extends Thread {
        BluetoothSocket socket;

        public DelayCloseThread(BluetoothSocket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            IOUtils.closeSilenty(socket);
        }
    }


}
