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
 * Created by liangtg on 16-12-5.
 */

public class HypoxiaSPPTrainingFlow implements SPPManager.SPPFlow {
    private byte[] cmd_get_time = {0x55, (byte) 0xAA, 0x06, (byte) 0xB4, 0x00, (byte) 0xB9};
    private byte[] cmd_get_state = {0x55, (byte) 0xAA, 0x06, (byte) 0xBC, 0x00, (byte) 0xC1};
    private byte[] cmd_set_time = {0x55, (byte) 0xaa, 0x0b, (byte) 0xb2, 0x10, 0x07, 0x05, 0x10, 0x06, 0x00, (byte) 0xee};
    private byte[] cmd_start_process = {0x55, (byte) 0xaa, 0x06, (byte) 0xb6, 0x00, (byte) 0xbb};

    private IBleManager btManager;
    private Buffer buffer = new Buffer();
    private BluetoothSocket socket;
    private Executor executor = Executors.newSingleThreadExecutor();
    private Object signal = new Object();
    private int mode = 1;

    public HypoxiaSPPTrainingFlow(IBleManager btManager) {
        this.btManager = btManager;
    }

    private static void log(Object object) {
        Log.d("flow", "" + object);
    }

    private static int readUByte(Buffer buffer) {
        return buffer.readByte() & 0xFF;
    }

    public void setMode(int mode) {
        this.mode = mode;
        cmd_start_process[4] = (byte) mode;
        cmd_start_process[5] = (byte) (0xBB + mode);
    }

    private void writeAndRead(byte[] cmd, int length) throws IOException {
        write(cmd);
        read(length);
    }

    void read(int length) throws IOException {
        buffer.readFrom(socket.getInputStream(), length);
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
        if (FlowExtra.REPORT_STATE_INFO == request) {
            log("notify start...");
            synchronized (signal) {
                signal.notifyAll();
            }
            log("notify ... end");
        }
    }

    private class WorkThread extends Thread {
        public WorkThread() {
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                readState();
                readTime();
                writeTime();
                startProcess();
                Thread.sleep(1000);//1秒后关闭socket,太快就连不上了
            } catch (Exception e) {
                Log.e("flow", "run failed", e);
            }
            IOUtils.closeSilenty(socket);
            btManager.requestConfirm(FlowExtra.REPORT_STATE_DISCONNECT, HypoxiaSPPTrainingFlow.this, null);
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

        private void readState() throws IOException {
            writeAndRead(cmd_get_state, 11);
            logcmd(buffer.readByteArray());
        }

        private void writeTime() throws IOException {
            setTime();
            writeAndRead(cmd_set_time, 11);
            byte[] array = buffer.readByteArray();
            logcmd(array);
        }

        private void readTime() throws IOException {
            writeAndRead(cmd_get_time, 11);
            byte[] array = buffer.readByteArray();
            logcmd(array);
        }

        private void startProcess() throws IOException {
            write(cmd_start_process);
            buffer.readFrom(socket.getInputStream(), 6);
            boolean read = true;
            while (socket.isConnected()) {
                if (!read) {
                    write(cmd_start_process);
                    buffer.readFrom(socket.getInputStream(), 6);
                    read = true;
                }
                byte[] array = buffer.readByteArray();
                logcmd(array);
                if (array[3] == (byte) 0xb6) {//血压结果
                    buffer.write(array, 4, 2);
                    read(17);
                    byte[] result = buffer.readByteArray();
                    logcmd(result);
                    buffer.write(result);
                    Intent data = new Intent();
                    data.putExtra(FlowExtra.KEY_MODE, readUByte(buffer));
                    String time = String.format("20%02d-%02d-%02d %02d:%02d:%02d",
                            readUByte(buffer),
                            readUByte(buffer),
                            readUByte(buffer),
                            readUByte(buffer),
                            readUByte(buffer),
                            readUByte(buffer));
                    data.putExtra(FlowExtra.KEY_START_TIME, time);
                    time = String.format("20%02d-%02d-%02d %02d:%02d:%02d",
                            readUByte(buffer),
                            readUByte(buffer),
                            readUByte(buffer),
                            readUByte(buffer),
                            readUByte(buffer),
                            readUByte(buffer));
                    data.putExtra(FlowExtra.KEY_END_TIME, time);
                    buffer.clear();
                    btManager.requestConfirm(FlowExtra.RESULT_HYPOXIA, HypoxiaSPPTrainingFlow.this, data);
                    break;
                } else if (array[3] == (byte) 0xbd) {//压力数据
                    Intent data = new Intent();
                    data.putExtra(FlowExtra.KEY_SYS, array[4] & 0xFF);
                    btManager.requestConfirm(FlowExtra.PROGRESS_BP, HypoxiaSPPTrainingFlow.this, data);
                } else if (array[3] == (byte) 0xbe) {//错误信息
                    Intent data = new Intent();
                    data.putExtra(FlowExtra.KEY_ERROR, array[4] & 0xFF);
                    btManager.requestConfirm(FlowExtra.REPORT_STATE_INFO, HypoxiaSPPTrainingFlow.this, data);
                    read = false;
                    waitSignal();
                }
                if (read) {
                    buffer.readFrom(socket.getInputStream(), 6);
                }
            }
        }

        private void waitSignal() {
            log("wait start......");
            synchronized (signal) {
                try {
                    signal.wait();
                } catch (InterruptedException e) {
                }
            }
            log("wait ....end");
        }

        private void logcmd(byte[] array) {
            log(ByteUtil.toHex(array));
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
                log("send success");
            } catch (IOException e) {
                Log.e("flow", "send failed", e);
            }
        }
    }

}
