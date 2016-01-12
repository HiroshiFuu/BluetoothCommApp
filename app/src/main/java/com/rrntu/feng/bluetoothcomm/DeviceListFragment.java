package com.rrntu.feng.bluetoothcomm;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class DeviceListFragment extends Fragment implements AbsListView.OnItemClickListener{

    private ArrayList <DeviceItem>deviceItemList;

    private OnFragmentInteractionListener mListener;
    private static BluetoothAdapter BTAdapter;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ArrayAdapter<DeviceItem> mAdapter;

    private final BroadcastReceiver bReciever = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Create a new device item
                DeviceItem newDevice = new DeviceItem(device);
                // Add it to our adapter
                mAdapter.add(newDevice);
            }
        }
    };

    private ToggleButton scan;

    //private ConnectThread SocketConnect;
    //private UUID uuid = UUID.fromString("4c9e60b8-007a-4476-b8f0-fb4e73f0eade");
    //private BluetoothSocket BTSocket;
    //private DeviceItem connectedDevice = null;
    //private ManageConnectThread connectedThread = new ManageConnectThread();

    public static DeviceListFragment newInstance(BluetoothAdapter adapter) {
        DeviceListFragment fragment = new DeviceListFragment();
        BTAdapter = adapter;
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DeviceListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("DEVICELIST", "Super called for DeviceListFragment onCreate\n");
        deviceItemList = new ArrayList<DeviceItem>();

        Set<BluetoothDevice> pairedDevices = BTAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                DeviceItem newDevice = new DeviceItem(device);
                deviceItemList.add(newDevice);
            }
        }

        // If there are no devices, add an item that states so. It will be handled in the view.
        if(deviceItemList.size() == 0) {
            deviceItemList.add(new DeviceItem(null));
        }

        Log.d("DEVICELIST", "DeviceList populated\n");

        mAdapter = new DeviceListAdapter(getActivity(), deviceItemList, BTAdapter);

        Log.d("DEVICELIST", "Adapter created\n");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deviceitem_list, container, false);
        scan = (ToggleButton) view.findViewById(R.id.scan);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        scan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                if (isChecked) {
                    mAdapter.clear();
                    getActivity().registerReceiver(bReciever, filter);
                    BTAdapter.startDiscovery();
                } else {
                    getActivity().unregisterReceiver(bReciever);
                    BTAdapter.cancelDiscovery();
                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DeviceItem device = deviceItemList.get(position);

        //Log.d("onItemClick", " position: " + position + " id: " + id + " name: " + device.getDeviceName() + "\n");
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            // mListener.onFragmentInteraction(device.getDeviceName());

            if (scan.isChecked()) {
                scan.setChecked(false);
            }

            if (Common.connectedDevice != device){
                if (Common.connectedDevice != null) {
                    Common.connectedDevice.setConnected(false);
                    Common.connectedDevice = null;
                    Common.SocketConnect.cancel();
                }
                Common.SocketConnect = new ConnectThread(device);
                Common.BTSocket = Common.SocketConnect.connect();
                if (Common.BTSocket != null) {
                    Common.connectedDevice = device;
                    Log.d("Common.connectedDevice", "device connected " + Common.connectedDevice.getDeviceName());
                }
                else Common.showDialog(1, "Connection Lost", "Could not connect to the selected device.");
            }
            if (Common.connectedDevice != null)
                if (Common.connectedDevice.getConnected()) {
                    sendSelfInfo();
                    mListener.onFragmentInteraction_StartDisplay();
                }
        }
    }

    /**
     *
     */
    public void sendSelfInfo() {
        if (Common.connectedDevice != null) {
            try {
                byte[] data = new byte[20];
                data[0] = (byte) 0xCA;
                data[1] = (byte) 0x01;
                String message = "CoMDAT Android";
                byte[] bytesData = message.getBytes();
                for (int i = 2; i < 20; i++) {
                    if (i - 2 < bytesData.length)
                        data[i] = bytesData[i - 2];
                    else data[i] = 0x07;
                }
                Common.connectedThread.sendData(Common.BTSocket, data);
                //Log.d("data sent : ", Common.getHexString(data));
            } catch (IOException e) {
                Log.d("sendData()", e.toString());
                Common.showDialog(1, "Communication Error", "Couldn't connect to " + Common.connectedDevice.getDeviceName());
                Common.disconnectDevice();
            }
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(String id);
        public void onFragmentInteraction_StartDisplay();
    }
}
