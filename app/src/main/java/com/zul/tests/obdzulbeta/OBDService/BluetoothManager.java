package com.zul.tests.obdzulbeta.OBDService;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothManager {
    private static final int MESSAGE_READ_WIFI = 2;
    ConnectedThread mConnectedThread = null;
    ConnectThread   mConnectThread   = null;
    BluetoothAdapter mBluetoothAdapter = null;
    UUID BT_UUID = null;
    private final BluetoothSocket btSocket = null;
    android.os.Handler mHandler = null;
    BluetoothSocket mmSocket = null;

    public BluetoothManager( BluetoothAdapter bluetoothAdapter, UUID BTUUID, android.os.Handler handler) {
        mBluetoothAdapter = bluetoothAdapter;
        BT_UUID = BTUUID;
        mHandler = handler;
    }

    public void startClient(BluetoothDevice device) {
        mConnectThread = new ConnectThread(device, mBluetoothAdapter, BT_UUID);
        mConnectThread.start();

    }

    public void write(byte[] b) {
        if (mConnectedThread != null) {
            mConnectedThread.write(b);
        }
    }

    //Thread to establish connection
    private class ConnectThread extends Thread {
        private final BluetoothDevice mmDevice;
        private final BluetoothAdapter mmAdapter;
        private final UUID MY_UUID;


        //Manager Thead to estabalish
        private  ConnectThread(BluetoothDevice device, BluetoothAdapter mBluetoothAdapter, UUID BTUUID) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;
            mmAdapter = mBluetoothAdapter;
            MY_UUID = BTUUID;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mmAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            mConnectedThread = new ConnectedThread(mmSocket, mHandler);
            mConnectedThread.start();
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    //Thread to handle connection
    class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private transient final android.os.Handler myHandler;
        public ConnectedThread(BluetoothSocket socket, android.os.Handler handler) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            myHandler = handler;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[20];  // buffer store for the stream

            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    mmInStream.read(buffer,0,6);
                    // Send the obtained bytes to the UI activity
                    String readMessage = new String(buffer,"UTF-8");

                    Message readMsg = myHandler.obtainMessage();
                    Bundle bundle2 = new Bundle();
                    bundle2.putString("Key", readMessage);
                    readMsg.setData(bundle2);
                    readMsg.what = MESSAGE_READ_WIFI;
                    myHandler.sendMessage(readMsg);
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
            return;
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    public boolean isConnected() {
        if (mmSocket != null)
            return  mmSocket.isConnected();
        return false;
    }
}
