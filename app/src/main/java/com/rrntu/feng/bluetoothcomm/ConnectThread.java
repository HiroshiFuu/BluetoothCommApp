package com.rrntu.feng.bluetoothcomm;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by FENG on 26/12/2015.
 */
public class ConnectThread{
    private final BluetoothDevice BTDevice;
    private final BluetoothSocket BTSocket;
    private final DeviceItem deviceItem;

    public ConnectThread(DeviceItem deviceItem) {

        BluetoothSocket tmp = null;
        this.deviceItem = deviceItem;
        this.BTDevice = deviceItem.getDevice();

        try {
            //Log.d("getUuids()",UUID.fromString("00001101-0000-1000-8000-00805F9B34FB").toString());
            //tmp = BTDevice.createRfcommSocketToServiceRecord(BTDevice.getUuids()[0].getUuid());
            tmp = this.BTDevice.createRfcommSocketToServiceRecord(Common.PANUServiceClass_UUID);
        }
        catch (IOException e) {
            Log.d("CONNECTTHREAD", "Could not start listening for RFCOMM");
        }
        this.BTSocket = tmp;
    }

    public BluetoothSocket connect() {

        try {
            BTSocket.connect();
            deviceItem.setConnected(true);
        } catch(IOException e) {
            Log.d("CONNECTTHREAD","Could not connect: " + e.toString());
            try {
                BTSocket.close();
            } catch(IOException close) {
                Log.d("CONNECTTHREAD", "Could not close connection:" + e.toString());
            }
            return null;
        }
        return BTSocket;
    }

    public boolean cancel() {
        try {
            BTSocket.close();
            deviceItem.setConnected(false);
        } catch(IOException e) {
            return false;
        }
        return true;
    }
}
