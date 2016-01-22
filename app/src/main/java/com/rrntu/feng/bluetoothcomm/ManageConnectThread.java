package com.rrntu.feng.bluetoothcomm;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by FENG on 26/12/2015.
 */
public class ManageConnectThread {

    private BluetoothSocket socket;

    public ManageConnectThread() { }

    public void sendData(BluetoothSocket socket, byte[] data) throws IOException{
        ByteArrayOutputStream output = new ByteArrayOutputStream(16);
        output.write(data);
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(output.toByteArray());
    }

    public byte[] receiveData(BluetoothSocket socket) throws IOException {
        byte[] buffer = new byte[16];
        ByteArrayInputStream input = new ByteArrayInputStream(buffer);
        InputStream inputStream = socket.getInputStream();
        inputStream.read(buffer);
        return buffer;
    }
}
