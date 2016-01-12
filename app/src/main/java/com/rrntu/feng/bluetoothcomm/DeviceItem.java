package com.rrntu.feng.bluetoothcomm;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Matt on 5/12/2015.
 */
public class DeviceItem {

    private BluetoothDevice device;
    private String deviceName;
    private String address;
    private boolean connected;

    public BluetoothDevice getDevice() { return device; }

    public String getDeviceName() {
        return deviceName;
    }

    public void setConnected(boolean connected) { this.connected = connected; }

    public boolean getConnected() {
        return connected;
    }

    public String getAddress() {
        return address;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public DeviceItem(BluetoothDevice device){
        this.device = device;
        if (device != null) {
            this.deviceName = device.getName();
            this.address = device.getAddress();
        }
        else {
            this.deviceName = "No Devices";
            this.address = "";
        }
    }
}
