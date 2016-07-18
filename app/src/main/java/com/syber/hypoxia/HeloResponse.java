package com.syber.hypoxia;

/**
 * Created by liangtg on 16-6-28.
 */
public class HeloResponse {
    public static final byte MATCH = 0x37;
    public static final byte REQUEST_BIND = 0x23;
    public static final byte BP = 0x41;
    public static final byte HR = 0x32;
    public static final byte ECG = 0x42;
    public static final byte BINDED = 0x37;
    public static final byte BATTERY = 0x43;
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
