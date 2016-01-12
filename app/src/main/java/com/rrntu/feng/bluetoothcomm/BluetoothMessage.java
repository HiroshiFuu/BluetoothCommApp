package com.rrntu.feng.bluetoothcomm;

import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by FENG0 on 10/1/2016.
 */
public class BluetoothMessage {

    public byte[] Message;
    public byte StartId;
    public byte MsgId;
    public int deviceId;
    public byte[] MsgData;
    public byte EndId;
    public int[] freq;
    public float[] mag;
    public int amp_freq;
    public int amp_mag;

    public BluetoothMessage()
    {
        Message = new byte[16];
        MsgData = new byte[12];
        StartId = 0;
        MsgId = 0;
        EndId = 0;
        deviceId = -1;
        freq = new int[3];
        mag = new float[3];
        for (int i = 0; i < 3; i++) {
            freq[i] = 0;
            mag[i] = 0;
        }
        amp_freq = 100;
        amp_mag = 100;
    }

    public void getMsg()
    {
        byte StartId_Overhead = Message[0];
        StartId = Message[1];
        for (int i = 0; i < 12; i++)
            MsgData[i] = Message[i + 2];
        for (int i = 0; i < 12; i += 4) {
            int upper = (MsgData[i] & 0xFF) * 256;
            int lower = MsgData[i + 1] & 0xFF;
            freq[i / 4] = (upper + lower) / amp_freq;
            upper = (MsgData[i + 2] & 0xFF) * 256;
            lower = MsgData[i + 3] & 0xFF;
            mag[i / 4] = (float)((upper + lower) * 1.0 / amp_mag);
        }
    }

    public static byte[] float2ByteArray (float value)
    {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    public static float ByteArray2float (byte[] array)
    {
        return ByteBuffer.wrap(array).getFloat();
    }
}
