package com.syber.hypoxia;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.orhanobut.logger.Logger;
import com.squareup.otto.Bus;
import com.syber.base.util.ByteUtil;
import com.syber.hypoxia.data.User;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by liangtg on 16-6-28.
 */
public class HeloService extends Service implements BluetoothAdapter.LeScanCallback {
    private boolean destoryed;
    private AtomicInteger atomic = new AtomicInteger();
    private boolean matched;
    private boolean serviceDiscovered;
    private boolean connected;
    private Notification.Builder builder;
    private SHandler handler;
    private BluetoothGatt sgatt;
    private GattCallback callback;
    private Bus bus = new Bus();
    private Meassurement meassurement;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        callback = new GattCallback(this);
        handler = new SHandler(this);
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            BluetoothAdapter.getDefaultAdapter().enable();
        }
        String mac = BluetoothAdapter.getDefaultAdapter().getAddress();
        String[] value = mac.split(":");
        int sum = 0, b = 0;
        for (int i = 2; i < value.length; i++) {
            b = Integer.parseInt(value[i], 16);
            HeloCMD.MATCH.cmd[i + 3] = (byte) b;
            Log.e("mac", "" + value[i]);
            sum += b;
        }
        sum += HeloCMD.MATCH.cmd[2] + HeloCMD.MATCH.cmd[3] + HeloCMD.MATCH.cmd[4];
        String s = Integer.toHexString(sum);
        Log.e("mac", "sum" + s);
        StringBuffer sb = new StringBuffer(s);
        if (sb.length() % 2 == 1) sb.insert(0, "0");
        for (int i = 0; i < sb.length() / 2; i++) {
            HeloCMD.MATCH.cmd[9 + i] = Byte.parseByte(sb.substring(sb.length() - (i + 1) * 2, sb.length() - i * 2));
        }

//        BluetoothAdapter.getDefaultAdapter().startLeScan(new UUID[]{Helo.SERVICE0, Helo.SERVICE1, Helo.SERVICE2}, this);
        builder = new Notification.Builder(this);
        builder.setAutoCancel(false);
        builder.setContentTitle("Helo手环");
        builder.setContentText("当前测量");
        builder.setDefaults(Notification.DEFAULT_SOUND);
        builder.setSmallIcon(R.drawable.app1);
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        builder.setContentIntent(PendingIntent.getActivity(this, 0, intent, 0));
        startForeground(1, builder.build());
        handler.sendEmptyMessageDelayed(SHandler.MSG_SCAN, 1000);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Logger.d("scan:" + device.getName() + "\t" + device.getAddress());
        if ("HeloHL01".equals(device.getName())) {
            BluetoothAdapter.getDefaultAdapter().stopLeScan(this);
            sgatt = device.connectGatt(this, false, callback);
        }
    }

    @Override
    public void onDestroy() {
        destoryed = true;
        BluetoothAdapter.getDefaultAdapter().stopLeScan(this);
        if (null != sgatt) {
            sgatt.disconnect();
            sgatt = null;
        }
    }

    public void onConnectionStateChange(int status, int newState) {
        connected = BluetoothProfile.STATE_CONNECTED == newState;
        if (connected) {
            if (!serviceDiscovered) {
                sgatt.discoverServices();
            } else {
                if (!matched) handler.sendMessageDelayed(Message.obtain(handler, SHandler.MSG_WRITE, 0, 0, HeloCMD.MATCH), 0);
            }
        } else {
            serviceDiscovered = false;
            matched = false;
            if (null != meassurement) {
                meassurement.quit();
                meassurement = null;
            }
            if (null != sgatt) {
                sgatt.disconnect();
                sgatt = null;
                handler.removeMessages(SHandler.MSG_SCAN);
                handler.sendEmptyMessageDelayed(SHandler.MSG_SCAN, 1000);
            }
        }
    }

    public void onServiceDiscovered() {
        if (serviceDiscovered) {
            if (null == meassurement) {
                meassurement = new Meassurement();
                meassurement.start();
            }
        } else {
            serviceDiscovered = true;
            handler.sendMessageDelayed(Message.obtain(handler, SHandler.MSG_ENABLE, 0, 0, HeloCMD.MATCH), 100);
            handler.sendMessageDelayed(Message.obtain(handler, SHandler.MSG_ENABLE, 0, 0, HeloCMD.GET_BP), 500);
            handler.sendMessageDelayed(Message.obtain(handler, SHandler.MSG_ENABLE, 0, 0, HeloCMD.RESPONSE_BATTERY), 1000);
            handler.sendMessageDelayed(Message.obtain(handler, SHandler.MSG_WRITE, 0, 0, HeloCMD.MATCH), 1500);
        }
    }

    public void onCharacteristicWrite(byte[] value) {
        HeloResponse response = new HeloResponse(value);
        if (response.verify() && HeloCMD.RESPONSE_BIND.cmdByte() == response.cmd()) {
            handler.sendMessageDelayed(Message.obtain(handler, SHandler.MSG_WRITE, 0, 0, HeloCMD.MATCH), 500);
        }
    }

    public void onCharacteristicChanged(byte[] value) {
        HeloResponse response = new HeloResponse(value);
        if (response.verify()) {
            if (null == meassurement) {
                if (HeloResponse.MATCH == response.cmd()) {
                    if (response.byteValue() == 1) {
                        matched = true;
                        onServiceDiscovered();
                    } else {
                        builder.setContentTitle("手环已绑定在其他设备");
                        startForeground(1, builder.build());
                    }
                } else if (HeloResponse.REQUEST_BIND == response.cmd()) {
                    Message.obtain(handler, SHandler.MSG_WRITE, 0, 0, HeloCMD.RESPONSE_BIND).sendToTarget();
                } else if (HeloResponse.BINDED == response.cmd()) {
                    onServiceDiscovered();
                }
            } else {
                meassurement.postResponse(response);
            }
        }
    }

    private void writeCMD(HeloCMD cmd) {
        Logger.d("ready write:" + cmd);
        if (null == sgatt) return;
        BluetoothGattCharacteristic characteristic = sgatt.getService(cmd.service).getCharacteristic(cmd.cha);
        characteristic.setValue(cmd.cmd);
        sgatt.writeCharacteristic(characteristic);
    }

    public void enableNotify(HeloCMD cmd) {
        BluetoothGattCharacteristic characteristic = sgatt.getService(cmd.service).getCharacteristic(cmd.cha);
        boolean e = sgatt.setCharacteristicNotification(characteristic, true);
        Logger.d("enabled:" + cmd.cha + "\t" + e);
    }

    private static class SHandler extends Handler {
        private static final int MSG_CONN_CHANGED = 1;
        private static final int MSG_CHARA_WRITE = 2;
        private static final int MSG_CHARA_CHANGED = 3;
        private static final int MSG_SERVICE_DISCOVERED = 4;
        private static final int MSG_ENABLE = 5;
        private static final int MSG_WRITE = 6;
        private static final int MSG_SCAN = 7;
        private WeakReference<HeloService> reference;

        public SHandler(HeloService service) {
            this.reference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            HeloService service = reference.get();
            if (null == service) return;
            if (MSG_CHARA_CHANGED == msg.what) {
                service.onCharacteristicChanged((byte[]) msg.obj);
            } else if (MSG_CHARA_WRITE == msg.what) {
                service.onCharacteristicWrite((byte[]) msg.obj);
            } else if (MSG_CONN_CHANGED == msg.what) {
                service.onConnectionStateChange(msg.arg1, msg.arg2);
            } else if (MSG_SERVICE_DISCOVERED == msg.what) {
                service.onServiceDiscovered();
            } else if (MSG_ENABLE == msg.what) {
                service.enableNotify((HeloCMD) msg.obj);
            } else if (MSG_WRITE == msg.what) {
                service.writeCMD((HeloCMD) msg.obj);
            } else if (MSG_SCAN == msg.what) {
                if (!service.destoryed) BluetoothAdapter.getDefaultAdapter().startLeScan(service);
            }
        }
    }

    private static class GattCallback extends BluetoothGattCallback {
        private WeakReference<HeloService> reference;

        public GattCallback(HeloService service) {
            this.reference = new WeakReference<HeloService>(service);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Logger.d(String.format("connect:%d", newState));
            HeloService service = reference.get();
            if (null == service || null == service.sgatt) return;
            Message.obtain(service.handler, SHandler.MSG_CONN_CHANGED, status, newState).sendToTarget();
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Logger.d("service discovered");
            HeloService service = reference.get();
            if (null == service || null == service.sgatt) return;
            service.handler.sendEmptyMessageDelayed(SHandler.MSG_SERVICE_DISCOVERED, 500);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Logger.d("writed:" + ByteUtil.toHex(characteristic.getValue()));
            HeloService service = reference.get();
            if (null == service || null == service.sgatt) return;
            Message.obtain(service.handler, SHandler.MSG_CHARA_WRITE, 0, 0, characteristic.getValue()).sendToTarget();
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Logger.d("receive:" + ByteUtil.toHex(characteristic.getValue()));
            HeloService service = reference.get();
            if (null == service || null == service.sgatt) return;
            Message.obtain(service.handler, SHandler.MSG_CHARA_CHANGED, 0, 0, characteristic.getValue()).sendToTarget();
        }
    }

    private class Meassurement extends HandlerThread {
        private int sys, dia, pul;
        private Handler handler;
        private Runnable exitRunnable = new Runnable() {
            @Override
            public void run() {
                quit();
                if (User.isSignIn() && sys > 0 && dia > 0 && pul > 0) {
//                    IRequester.getInstance().addBP(bus, IApplication.dateFormat.format(new Date()), sys, dia, pul);
                    Logger.d(String.format("exit:%d-%d-%d", sys, dia, pul));
                }
                if (meassurement == Meassurement.this) {
                    meassurement = null;
                    HeloService.this.handler.sendEmptyMessage(SHandler.MSG_SERVICE_DISCOVERED);
                }
            }
        };
        private Runnable getBPRunnable = new Runnable() {
            @Override
            public void run() {
                writeCMD(HeloCMD.GET_BP);
//                handler.postDelayed(this, 30 * 1000);
            }
        };

        private Runnable getBatteryRunnable = new Runnable() {
            @Override
            public void run() {
                writeCMD(HeloCMD.GET_BATTERY);
//                handler.postDelayed(this, 10 * 1000);
            }
        };

        public Meassurement() {
            super("meassure#" + atomic.incrementAndGet());
        }

        @Override
        protected void onLooperPrepared() {
            Logger.d("meassurement start");
            handler = new Handler(getLooper());
            handler.postDelayed(exitRunnable, 120 * 1000);
            handler.post(getBPRunnable);
        }

        public void postResponse(HeloResponse response) {
            getLooper();
            handler.post(new ResponseRunnable(response));
        }

        private class ResponseRunnable implements Runnable {
            private HeloResponse response;

            public ResponseRunnable(HeloResponse response) {
                this.response = response;
            }

            @Override
            public void run() {
                if (response.verify()) {
                    if (HeloResponse.BP == response.cmd()) {
                        handler.removeCallbacks(getBPRunnable);
                        sys = response.intValue(4);
                        dia = response.intValue(5);
                        handler.removeCallbacks(getBatteryRunnable);
                        handler.postDelayed(getBatteryRunnable, 10 * 1000);
                    } else if (HeloResponse.BATTERY == response.cmd()) {
                        handler.removeCallbacks(getBatteryRunnable);
                        pul = response.intValue(0);
                    }
                }
            }
        }


    }

}
