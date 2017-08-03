package com.syber.hypoxia.bt;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

import com.orhanobut.logger.Logger;
import com.syber.base.io.IOUtils;
import com.syber.base.util.ByteUtil;
import com.syber.hypoxia.helo.IBleManager;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okio.Buffer;

/**
 * Created by liangtg on 16-11-15.
 */

public class HypoxiaSPPFlow implements SPPManager.SPPFlow {
    private byte[] cmd_get_time = {0x55, (byte) 0xAA, 0x06, (byte) 0xB4, 0x00, (byte) 0xB9};
    private byte[] cmd_get_state = {0x55, (byte) 0xAA, 0x06, (byte) 0xBC, 0x00, (byte) 0xC1};
    private byte[] cmd_get_mac = {0x55, (byte) 0xAA, 0x06, (byte) 0xB1, 0x00, (byte) 0xB6};
    private byte[] cmd_set_time = {0x55, (byte) 0xaa, 0x0b, (byte) 0xb2, 0x10, 0x07, 0x05, 0x10, 0x06, 0x00, (byte) 0xee};

    private byte[] cmd_data_length = {0x55, (byte) 0xAA, 0x06, (byte) 0xBA, 0x00, (byte) 0xBF};
    private byte[] cmd_sync_bp = {0x55, (byte) 0xAA, 0x06, (byte) 0xBA, 0x05, (byte) 0xC4};
    private byte[] cmd_sync_hypoxia = {0x55, (byte) 0xAA, 0x06, (byte) 0xBA, 0x06, (byte) 0xC5};
    private byte[] cmd_ack = {0x55, (byte) 0xaa, 0x08, (byte) 0xb5, (byte) 0xba, 0x00, 0x00, 0x76};
    private IBleManager btManager;
    private Buffer buffer = new Buffer();
    private BluetoothSocket socket;
    private Executor executor = Executors.newSingleThreadExecutor();


    public HypoxiaSPPFlow(IBleManager btManager) {
        this.btManager = btManager;
    }

    private static void log(Object object) {
        Log.d("flow", "" + object);
    }

    private static int readUByte(Buffer buffer) {
        return buffer.readByte() & 0xFF;
    }


    private void writeAndRead(byte[] cmd, int length) throws IOException {
        write(cmd);
        read(length);
    }

    void read(int length) throws IOException {
        buffer.readFrom(socket.getInputStream(), length);
        byte[] bytes = buffer.readByteArray();
        logcmd(bytes);
        buffer.clear();
        buffer.write(bytes);
//            byte[] array = new byte[length];
//            for (int i = 0; i < length; i++) {
//                array[i] = (byte) socket.getInputStream().read();
//                Log.d("flow", "read:\t" + ByteUtil.toHex(array));
//            }
//            socket.getInputStream().read(array);
//            Log.d("flow", "read:\t" + ByteUtil.toHex(array));
    }

    void write(byte[] cmd) throws IOException {
        executor.execute(new WriteRunnable(cmd));
    }

    @Override
    public String getDeviceName() {
        return "RCxxxx-Debug-BT";
    }

    @Override
    public void onSocketConnected(BluetoothSocket socket) {
        Logger.d("connected");
        this.socket = socket;
        new WorkThread().start();
    }

    @Override
    public void onRequestConfirmed(int request, int result) {
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
    }

    private void logcmd(byte[] array) {
        log(ByteUtil.toHex(array));
    }

    private class WorkThread extends Thread {

        public WorkThread() {
        }

        @Override
        public void run() {
            try {
//                readMAC();
                readState();
                readTime();
                writeTime();
                syncData();
            } catch (Exception e) {
                Log.e("flow", null, e);
            }
            IOUtils.closeSilenty(socket);
        }

        private void readMAC() throws IOException {
            writeAndRead(cmd_get_mac, 20);
        }

        private void readState() throws IOException {
            writeAndRead(cmd_get_state, 11);
        }

        private void writeTime() throws IOException {
            setTime();
            writeAndRead(cmd_set_time, 11);
            byte[] array = buffer.readByteArray();
        }

        private void readTime() throws IOException {
            writeAndRead(cmd_get_time, 11);
            byte[] array = buffer.readByteArray();
        }

        private void syncData() throws IOException {
            writeAndRead(cmd_data_length, 9);
            byte[] array = buffer.readByteArray();
            int bpLength = array[4] & 0xFF;
            int hLength = array[6] & 0xFF;
            syncBP(bpLength);
            syncHypoxia(hLength);
            btManager.requestConfirm(FlowExtra.REQUEST_END, HypoxiaSPPFlow.this, null);
        }

        private void syncBP(int length) throws IOException {
            if (length <= 0) return;
            write(cmd_sync_bp);
            for (int i = 0; i < length; i++) {
                read(3);
                read((buffer.getByte(2) & 0xFF) - 3);
                buffer.skip(6);
                Intent data = new Intent();
                data.putExtra(FlowExtra.KEY_TIME,
                        String.format("20%02d-%02d-%02d %02d:%02d:%02d",
                                readUByte(buffer),
                                readUByte(buffer),
                                readUByte(buffer),
                                readUByte(buffer),
                                readUByte(buffer),
                                readUByte(buffer)));
                int sys = readUByte(buffer);
                sys += readUByte(buffer) << 8;
                data.putExtra(FlowExtra.KEY_SYS, sys);
                data.putExtra(FlowExtra.KEY_DIA, readUByte(buffer));
                data.putExtra(FlowExtra.KEY_PUL, readUByte(buffer));
                buffer.clear();
                btManager.requestConfirm(FlowExtra.RESULT_BP, HypoxiaSPPFlow.this, data);
            }
        }

        private void syncHypoxia(int length) throws IOException {
            if (length <= 0) return;
            write(cmd_sync_hypoxia);
            for (int i = 0; i < length; i++) {
                read(3);
                read((buffer.getByte(2) & 0xFF) - 3);
                buffer.skip(6);
                Intent data = new Intent();
                data.putExtra(FlowExtra.KEY_MODE, readUByte(buffer));
                data.putExtra(FlowExtra.KEY_START_TIME,
                        String.format("20%02d-%02d-%02d %02d:%02d:%02d",
                                readUByte(buffer),
                                readUByte(buffer),
                                readUByte(buffer),
                                readUByte(buffer),
                                readUByte(buffer),
                                readUByte(buffer)));
                data.putExtra(FlowExtra.KEY_END_TIME,
                        String.format("20%02d-%02d-%02d %02d:%02d:%02d",
                                readUByte(buffer),
                                readUByte(buffer),
                                readUByte(buffer),
                                readUByte(buffer),
                                readUByte(buffer),
                                readUByte(buffer)));
                buffer.clear();
                btManager.requestConfirm(FlowExtra.RESULT_HYPOXIA, HypoxiaSPPFlow.this, data);
            }
        }
    }

    private class WriteRunnable implements Runnable {
        byte[] cmd;

        public WriteRunnable(byte[] cmd) {
            this.cmd = cmd;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            Log.d("flow", "send:\t" + ByteUtil.toHex(cmd));
            try {
                socket.getOutputStream().write(cmd);
                socket.getOutputStream().flush();
            } catch (IOException e) {
            }
        }
    }


}
