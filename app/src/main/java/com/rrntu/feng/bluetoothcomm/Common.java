package com.rrntu.feng.bluetoothcomm;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by FENG0 on 8/1/2016.
 */
public class Common {

    private static ListActivity listactivity;

    public static ConnectThread SocketConnect;
    public static UUID PANUServiceClass_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static BluetoothSocket BTSocket = null;
    public static DeviceItem connectedDevice = null;
    public static ManageConnectThread connectedThread = new ManageConnectThread();
    public static boolean receiving = false;
    public static boolean isAlive = false;
    public static FileOutputStream fos;

    public static void setActivity(ListActivity activity) {
        listactivity = activity;
    }

    public static void showDialog(int action, String title, String message) {
        listactivity.showDialog(action, title, message);
    }

    private static final char[] hexArray = (
            "000102030405060708090A0B0C0D0E0F"+
            "101112131415161718191A1B1C1D1E1F"+
            "202122232425262728292A2B2C2D2E2F"+
            "303132333435363738393A3B3C3D3E3F"+
            "404142434445464748494A4B4C4D4E4F"+
            "505152535455565758595A5B5C5D5E5F"+
            "606162636465666768696A6B6C6D6E6F"+
            "707172737475767778797A7B7C7D7E7F"+
            "808182838485868788898A8B8C8D8E8F"+
            "909192939495969798999A9B9C9D9E9F"+
            "A0A1A2A3A4A5A6A7A8A9AAABACADAEAF"+
            "B0B1B2B3B4B5B6B7B8B9BABBBCBDBEBF"+
            "C0C1C2C3C4C5C6C7C8C9CACBCCCDCECF"+
            "D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF"+
            "E0E1E2E3E4E5E6E7E8E9EAEBECEDEEEF"+
            "F0F1F2F3F4F5F6F7F8F9FAFBFCFDFEFF").toCharArray();

    public static String getHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = (bytes[j] & 0xFF) * 2;
            hexChars[j * 2] = hexArray[v];
            hexChars[j * 2 + 1] = hexArray[v + 1];
        }
        return new String(hexChars);
    }

    /**
     *
     */
    public static void sendData(byte[] data) {
        if (connectedDevice != null) {
            try {
                connectedThread.sendData(BTSocket, data);
                Log.d("data sent : ", Common.getHexString(data));
            } catch (IOException e) {
                Log.d("sendData()", e.toString());
                Common.showDialog(1, "Communication Error", "Couldn't connect to " + Common.connectedDevice.getDeviceName());
                disconnectDevice();
            }
        }
    }

    /**
     *
     */
    public static byte[] receiveData() {
        try {
            byte[] buffer = connectedThread.receiveData(BTSocket);
            if (buffer != null)
                Log.d("rev data : ", Common.getHexString(buffer));
            return buffer;
        } catch (IOException e) {
            Log.d("receiveData()", e.toString());
        }
        return null;
    }

    /**
     *
     */
    public static boolean disconnectDevice() {
        Common.receiving = false;
        if (Common.connectedDevice != null) {
            Common.connectedDevice.setConnected(false);
            Common.connectedDevice = null;
            Common.SocketConnect.cancel();
            listactivity.switchToDeviceList();
            try {
                fos.close();
                Log.d("disconnectDevice()", "file closed");
            } catch (IOException e) {
                Log.d("FILECLODE", e.toString());
            }
            fos = null;
            Log.d("Common.connectedDevice", "device disconnected");
            return true;
        }
        return false;
    }
}
