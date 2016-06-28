package com.syber.hypoxia;

import com.syber.base.util.ByteUtil;

import java.util.UUID;

/**
 * Created by liangtg on 16-6-28.
 */
public enum HeloCMD {
    MATCH(new byte[]{0x12, 0x34, 0x0B, 0x11, 0x04, 0x11, 0x11, 0x11, 0x11, 0x64, 0, 0, 0, 0x43, 0x21}, Helo.SERVICE1, Helo.S1N1),
    RESPONSE_BIND(new byte[]{0x12, 0x34, 0x0B, 0x13, 0x04, 0x11, 0x11, 0x11, 0x11, 0x66, 0, 0, 0, 0x43, 0x21}, Helo.SERVICE1, Helo.S1N1),
    GET_BP(new byte[]{0x12, 0x34, 0x0A, 0x0C, 0x16, 0, 0, 0, 0x43, 0x21}, Helo.SERVICE0, Helo.S0N2),
    GET_HR(new byte[]{0x12, 0x34, 0x0A, 0x02, 0x0C, 0, 0, 0, 0x43, 0x21}, Helo.SERVICE0, Helo.S0N2),
    GET_ECG(new byte[]{0x12, 0x34, 0x0A, 0x0D, 0x17, 0, 0, 0, 0x43, 0x21}, Helo.SERVICE0, Helo.S0N2),
    GET_BATTERY(new byte[]{0x43, 0x21, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0}, Helo.SERVICE0, Helo.S0N2),
    REQUEST_UNBIND(new byte[]{0x12, 0x34, 0x0A, 0x9, 0x13, 0, 0, 0, 0x43, 0x21}, Helo.SERVICE1, Helo.S1N1),;
    public final byte[] cmd;
    public final UUID service;
    public final UUID cha;

    HeloCMD(byte[] cmd, UUID service, UUID cha) {
        this.cmd = cmd;
        this.service = service;
        this.cha = cha;
    }

    public byte cmdByte() {
        return cmd[3];
    }

    @Override
    public String toString() {
        return "HeloCMD{" +
                "cmd=" + ByteUtil.toHex(cmd) +
                ", service=" + service +
                ", cha=" + cha +
                '}';
    }
}
