package com.zul.tests.obdzulbeta.OBDService;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class BluetoothManager {
    private static final int MESSAGE_READ_BT = 1;
    private static final int THREAD_READY = 7;
    ConnectedThread mConnectedThread = null;
    ConnectThread   mConnectThread   = null;
    BluetoothAdapter mBluetoothAdapter = null;
    UUID BT_UUID = null;
    private final BluetoothSocket btSocket = null;
    android.os.Handler mHandler = null;
    BluetoothSocket mmSocket = null;
    byte[] buffer;

    ArrayList<String> vinCar = new ArrayList<>();
    int vinCount = 0;
    boolean vinEnable = false;

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


    public void setHandler(android.os.Handler handle) {
        mHandler = handle;
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
                    //Send conection problem
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            mConnectedThread = new ConnectedThread(mmSocket);
            mConnectedThread.start();

            Message readMsg = mHandler.obtainMessage();
            Bundle bundle2 = new Bundle();
            bundle2.putString("DATA", "OK");
            readMsg.setData(bundle2);
            readMsg.what = THREAD_READY;
            mHandler.sendMessage(readMsg);

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
        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

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

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                ArrayList<String> receive = new ArrayList<>();
                buffer = new byte[36];
                String message = "";

                try {
                    int data = mmInStream.available();
                    if (data > 0) {


                        mmInStream.read(buffer);
                        message = new String(buffer, 0, data);

                        message = message.replace("\n", "");
                        message = message.replace("\r", "");
                        message = message.replace(">", "");
                        message = message.replace(" ", "");


                        if (message.contains("4902")) {
                            vinEnable = true;
                        }
                        if (vinEnable) {
                            vinCar.add(message);
                            vinCount = vinCount + 1;
                        }


                        if (!message.equals("") && !message.contains("SEARCHING") && !message.contains("CANERROR") && (vinEnable == false)) {
                            receive.add(message);
                            Message readMsg = mHandler.obtainMessage();
                            Bundle bundle2 = new Bundle();
                            bundle2.putStringArrayList("DATA", receive);
                            readMsg.setData(bundle2);
                            readMsg.what = MESSAGE_READ_BT;
                            mHandler.sendMessage(readMsg);
                        }
                        else if (vinCount >= 3) {
                            Message readMsg = mHandler.obtainMessage();
                            Bundle bundle2 = new Bundle();
                            bundle2.putStringArrayList("DATA", vinCar);
                            readMsg.setData(bundle2);
                            readMsg.what = MESSAGE_READ_BT;
                            mHandler.sendMessage(readMsg);
                            vinCount = 0;
                            vinEnable = false;
                            vinCar = new ArrayList<>();
                        }
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
                mmOutStream.flush();
            } catch (IOException e) {
                Log.d("BT", e.toString());
            }
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

    public void stopBTmanager() {
        mConnectedThread.cancel();
    }

    public String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits);
    }

    public String encodeHexString(byte[] byteArray) {
        StringBuffer hexStringBuffer = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            hexStringBuffer.append(byteToHex(byteArray[i]));
        }
        return hexStringBuffer.toString();
    }


}
