package com.rrntu.feng.bluetoothcomm;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidplot.ui.SeriesRenderer;
import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.BarRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DisplayFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DisplayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DisplayFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private Handler handler;

    private XYPlot dPlot = null;
    private SimpleXYSeries dSeries = null;
    private TextView tv_connected = null;

    private File folder;
    private SimpleDateFormat dateFormatter;
    private SimpleDateFormat timeFormatter;

    BluetoothMessage message = new BluetoothMessage();

    public DisplayFragment() {
        // Required empty public constructor
    }

    public static DisplayFragment newInstance() {
        DisplayFragment fragment = new DisplayFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        dateFormatter.setLenient(false);
        timeFormatter = new SimpleDateFormat("HH:mm:ss");
        timeFormatter.setLenient(false);

        folder = new File(Environment.getExternalStorageDirectory(), "/BluetoothCommApp/");
        if (!folder.exists())
            folder.mkdir();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_display, container, false);

        tv_connected = (TextView) view.findViewById(R.id.connected);

        dPlot = (XYPlot) view.findViewById(R.id.plot);

        dSeries = new SimpleXYSeries("CoMDAT");
        for (int i = 0; i < 3; i++)
            dSeries.addLast(150 + 100 * i, 1);

        dPlot.setRangeBoundaries(0, 1, BoundaryMode.FIXED);
        dPlot.setDomainBoundaries(100, 400, BoundaryMode.FIXED);
        MyBarFormatter formatter = new MyBarFormatter(Color.argb(255, 255, 255, 0), Color.LTGRAY);
        dPlot.addSeries(dSeries, formatter);
        MyBarRenderer renderer = ((MyBarRenderer)dPlot.getRenderer(MyBarRenderer.class));
        renderer.setBarWidthStyle(BarRenderer.BarWidthStyle.FIXED_WIDTH);
        renderer.setBarWidth(50);
        dPlot.setDomainStepValue(5);
        dPlot.setTicksPerRangeLabel(1);
        dPlot.setDomainLabel("Frequency");
        dPlot.getDomainLabelWidget().pack();
        dPlot.setRangeLabel("Magnitude");
        dPlot.getRangeLabelWidget().pack();
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void startReceivingData() {

        try {
            Date today = new Date();
            String now = dateFormatter.format(today);
            File file = new File(folder.getAbsoluteFile() + File.separator + "DataLog_" + now + ".csv");
            Common.fos = new FileOutputStream(file, true);
            String temp = "Timestamp, Id, Freq1, Mag1, Freq2, Mag2, Freq3, Mag3\n";
            Common.fos.write(temp.getBytes());
        } catch (FileNotFoundException e) {
            Log.d("FileOutputStream", e.toString());
        } catch (IOException e) {
            Log.d("FileOutputStream", e.toString());
        }

        handler = this.getView().getHandler();
        BluetoothSocketListener bsl = new BluetoothSocketListener(handler, dPlot, dSeries, tv_connected);
        Thread messageListener = new Thread(bsl);
        messageListener.start();
        handler.postDelayed(checkAlive, 2500);
        Log.d("BluetoothSocketListener", "start");
    }

    public void clearDisplay() {
        for (int i = 0; i < 3; i++)
            dSeries.setXY(150 + 100 * i, 1, i);
        dPlot.redraw();
        tv_connected.setText(getResources().getString(R.string.connected));
        tv_connected.setTextColor(0xFFFF0000);
    }

    class MyBarFormatter extends BarFormatter {
        public MyBarFormatter(int fillColor, int borderColor) {
            super(fillColor, borderColor);
        }

        @Override
        public Class<? extends SeriesRenderer> getRendererClass() {
            return MyBarRenderer.class;
        }

        @Override
        public SeriesRenderer getRendererInstance(XYPlot plot) {
            return new MyBarRenderer(plot);
        }
    }

    class MyBarRenderer extends BarRenderer<MyBarFormatter> {

        public MyBarRenderer(XYPlot plot) {
            super(plot);
        }

        /**
         * Implementing this method to allow us to inject our
         * special selection getFormatter.
         * @param index index of the point being rendered.
         * @param series XYSeries to which the point being rendered belongs.
         * @return
         */
        @Override
        public MyBarFormatter getFormatter(int index, XYSeries series) {
            return getFormatter(series);
        }
    }

    private class BluetoothSocketListener implements Runnable {

        private XYPlot dPlot = null;
        private SimpleXYSeries dSeries = null;
        private TextView tv_connected = null;
        private Handler handler;

        public BluetoothSocketListener(Handler handler, XYPlot dPlot, SimpleXYSeries dSeries, TextView tv_connected) {
            this.handler = handler;
            this.dPlot = dPlot;
            this.dSeries = dSeries;
            this.tv_connected = tv_connected;
        }

        public void run() {
            int bufferSize = 16;
            byte[] buffer = new byte[bufferSize];
            try {
                InputStream instream = Common.BTSocket.getInputStream();
                int bytesRead = -1;
                while (Common.receiving) {
                    bytesRead = instream.available();
                    if (bytesRead > 0) {
                        instream.read(buffer);
                        message.Message = buffer;
                        message.getMsg();
                        Date today = new Date();
                        String now = timeFormatter.format(today);
                        Common.fos.write((now + ",").getBytes());
                        for (int i = 0; i < 3; i++) {
                            String temp = "," + message.freq[i] + "," + message.mag[i];
                            Common.fos.write(temp.getBytes());
                        }
                        Common.fos.write("\n".getBytes());
                        Common.fos.flush();
                        handler.post(new MessagePoster(dPlot, dSeries, tv_connected, message.freq, message.mag));
                        byte[] data = new byte[bufferSize];
                        data[0] = (byte) 0xFF;
                        Common.sendData(data);
                        Common.BTSocket.getInputStream();
                    }
                }
            } catch (IOException e) {
                Log.d("BLUETOOTH_COMMS", e.getMessage());
            }
        }
    }

    private class MessagePoster implements Runnable {

        private XYPlot dPlot = null;
        private SimpleXYSeries dSeries = null;
        private TextView tv_connected;
        private int[] freq;
        private float[] mag;

        public MessagePoster(XYPlot dPlot, SimpleXYSeries dSeries, TextView tv_connected, int[] freq, float[] mag) {
            this.dPlot = dPlot;
            this.dSeries = dSeries;
            this.tv_connected = tv_connected;
            this.freq = freq;
            this.mag = mag;
        }

        public void run() {
            for (int i = 0; i < 3; i++) {
                dSeries.setXY(freq[i], mag[i], i);
                //Log.d("run()", String.valueOf(i) + " " + String.valueOf(freq[i]) + " " + String.valueOf(mag[i]));
            }
            dPlot.redraw();
            tv_connected.setText(" CoMDAT Connected");
            tv_connected.setTextColor(0xFF00FF00);
            Common.isAlive = true;
        }
    }

    private Runnable checkAlive = new Runnable( ) {
        public void run ( ) {
            if (!Common.isAlive && Common.connectedDevice != null) {
                Common.receiving = false;
                handler.removeCallbacks(this);
                Common.showDialog(1, "Connection Lost", "Lost connection from " + Common.connectedDevice.getDeviceName());
                Common.disconnectDevice();
                return;
            }
            Common.isAlive = false;
            handler.postDelayed(this, 2500);
        }
    };
}
