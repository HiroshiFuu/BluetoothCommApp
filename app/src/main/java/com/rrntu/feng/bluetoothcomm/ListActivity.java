package com.rrntu.feng.bluetoothcomm;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidplot.xy.XYPlot;

import org.w3c.dom.Text;


public class ListActivity extends AppCompatActivity implements DeviceListFragment.OnFragmentInteractionListener, DisplayFragment.OnFragmentInteractionListener {

    private DeviceListFragment mDeviceListFragment;
    private BluetoothAdapter BTAdapter;
    private DisplayFragment mDisplayFragment;

    public static int REQUEST_BLUETOOTH = 1;

    private FragmentManager fragmentManager;
    private Fragment fragmentDeviceList, fragmentDisplay;
    private XYPlot plot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        // Phone does not support Bluetooth so let the user know and exit.
        if (BTAdapter == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        if (!BTAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_BLUETOOTH);
        }

        fragmentManager = getSupportFragmentManager();

        mDeviceListFragment = DeviceListFragment.newInstance(BTAdapter);
        mDisplayFragment = DisplayFragment.newInstance();

        if (savedInstanceState == null) {
            fragmentManager.beginTransaction().add(android.R.id.content, mDisplayFragment, "Display").commit();
            fragmentManager.executePendingTransactions();
            fragmentManager.beginTransaction().add(android.R.id.content, mDeviceListFragment, "DeviceList").commit();
            fragmentManager.executePendingTransactions();
            fragmentDeviceList = fragmentManager.findFragmentByTag("DeviceList");
            fragmentDisplay = fragmentManager.findFragmentByTag("Display");
            fragmentManager.beginTransaction().hide(fragmentDisplay).show(fragmentDeviceList).commit();
        }
        else {
            //fragmentManager.beginTransaction().hide(fragmentDisplay).show(fragmentDeviceList).commit();
        }
//        fragmentManager.beginTransaction().replace(R.id.container, mDeviceListFragment).commit();
        Common.setActivity(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_disconnect) {
            if (Common.disconnectDevice()) {
                new AlertDialog.Builder(this)
                        .setTitle("Disconnected")
                        .setMessage("You have disconnected to device.")
                        .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
            else {
                    new AlertDialog.Builder(this)
                            .setTitle("Not Connected")
                            .setMessage("You did not connect to any device.")
                            .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
            }
            mDisplayFragment.clearDisplay();
            switchToDeviceList();
        }

        if (id == R.id.action_exit) {
            System.exit(0);
        }

        if (id == R.id.action_switch) {
            Fragment fragment1 = fragmentDeviceList;
            Fragment fragment2 = null;
            if (fragment1.isVisible()) {
                fragment2 = fragmentDisplay;
            }
            else {
                fragment1 = fragmentDisplay;
                fragment2 = fragmentDeviceList;
            }
            fragmentManager.beginTransaction().hide(fragment1).show(fragment2).commit();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        XYPlot dPlot = (XYPlot) mDisplayFragment.getView().findViewById(R.id.plot);
        TextView textView = (TextView) mDisplayFragment.getView().findViewById(R.id.connected);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 2));
            dPlot.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 8));
        }
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, (float)0.8));
            dPlot.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, (float)9.2));
        }
    }

    public void showDialog(int action, String title, String message) {
        switch (action) {
            case 1: new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    @Override
    public void onFragmentInteraction(String id) {

    }

    public void switchToDeviceList() {
        fragmentManager.beginTransaction().hide(fragmentDisplay).show(fragmentDeviceList).commit();
        fragmentManager.executePendingTransactions();
    }

    public void switchToDisplay() {
        fragmentManager.beginTransaction().hide(fragmentDeviceList).show(fragmentDisplay).commit();
        fragmentManager.executePendingTransactions();
    }

    @Override
    public void onFragmentInteraction_StartDisplay() {
        switchToDisplay();
        Common.receiving = true;
        mDisplayFragment.startReceivingData();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
