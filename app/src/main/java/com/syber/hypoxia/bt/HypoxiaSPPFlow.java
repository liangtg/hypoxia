package com.syber.hypoxia.bt;

import android.bluetooth.BluetoothSocket;

import com.orhanobut.logger.Logger;
import com.syber.base.io.IOUtils;
import com.syber.hypoxia.helo.IBleManager;

import java.io.IOException;
import java.util.Calendar;

import okio.Buffer;

/**
 * Created by liangtg on 16-11-15.
 */

public class HypoxiaSPPFlow implements SPPManager.SPPFlow {
    private byte[] cmd_get_time = {0x55, (byte) 0xAA, 0x06, (byte) 0xB4, 0x00, (byte) 0xB9};
    private byte[] cmd_set_time = {0x55, (byte) 0xaa, 0x0b, (byte) 0xb2, 0x10, 0x07, 0x05, 0x10, 0x06, 0x00, (byte) 0xee};

    private byte[] cmd_sync_bp = {0x55, (byte) 0xAA, 0x06, (byte) 0xBA, 0x00, (byte) 0xBF};
    private byte[] cmd_sync_hypoxia = {0x55, (byte) 0xAA, 0x06, (byte) 0xBA, 0x01, (byte) 0xC0};
    private byte[] cmd_ack = {0x55, (byte) 0xaa, 0x08, (byte) 0xb5, (byte) 0xba, 0x00, 0x00, 0x76};
    private IBleManager btManager;

    public HypoxiaSPPFlow(IBleManager btManager) {
        this.btManager = btManager;
    }

    @Override
    public String getDeviceName() {
        return "RCxxxx-Debug-BT";
    }

    @Override
    public void onSocketConnected(BluetoothSocket socket) {
        Logger.d("connected");
        new WorkThread(socket).start();
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

    private class WorkThread extends Thread {
        BluetoothSocket socket;
        Buffer buffer = new Buffer();

        public WorkThread(BluetoothSocket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                readTime();
                writeTime();
                readBP();
            } catch (Exception e) {
            }
            IOUtils.closeSilenty(socket);
        }

        private void writeTime() throws IOException {
            setTime();
            write(cmd_set_time);
            read(11);
            buffer.readByteArray();
        }

        private void readTime() throws IOException {
            write(cmd_get_time);
            read(11);
            buffer.readByteArray();
        }

        private void readBP() throws IOException {
            write(cmd_sync_bp);
            read(16);
        }

        void read(int length) throws IOException {
            buffer.readFrom(socket.getInputStream(), length);
        }

        void write(byte[] cmd) throws IOException {
            socket.getOutputStream().write(cmd);
            socket.getOutputStream().flush();
        }

    }

}
