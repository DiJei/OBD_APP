package com.zul.tests.obdzulbeta.OBDService;

import android.os.Bundle;
import android.os.Message;

import java.util.ArrayList;

public abstract class OBDCommand {

    String cmd;
    BluetoothManager mBluetoothManager = null;
    TCPSocketManager mWifiTCPSocketManager = null;
    private static final int MESSAGE_READ_WIFI = 1;
    private static final int MESSAGE_READ_BT = 2;
    private static final int OBD_COMMAND = 0;
    ArrayList<String> message;
    android.os.Handler requestHandler;

    //Bluetooth
    public OBDCommand(String command, BluetoothManager btManager, android.os.Handler handler) {
        cmd = command;
        mBluetoothManager = btManager;
        requestHandler = handler;
    }

    //WiFi
    public OBDCommand(String command, TCPSocketManager tcpManager, android.os.Handler handler) {
        cmd = command;
        mWifiTCPSocketManager = tcpManager;
        requestHandler = handler;
    }


    //Send cmd
    public  void OBDSendPID()  {
        if (mBluetoothManager != null) {
            byte[] b;
            b = cmd.getBytes();
            mBluetoothManager.write(b);
        } else if (mWifiTCPSocketManager != null) {
            mWifiTCPSocketManager.sendData(cmd);
        }
    }

    //Each command has unique way to format message
    //Then send to Service
    public abstract String formatAnwser(ArrayList<String> message);



    public  final android.os.Handler obdCommandHandler = new android.os.Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            Bundle bundle = msg.getData();
            Message readMsg = requestHandler.obtainMessage();
            if ( bundle != null)
                message  = bundle.getStringArrayList("DATA");
            switch (msg.what) {
                case MESSAGE_READ_WIFI:
                    String anwser = formatAnwser(message);

                    bundle.putString("data", anwser);
                    readMsg.setData(bundle);
                    readMsg.what = OBD_COMMAND;
                    requestHandler.sendMessage(readMsg);

                    break;
                case MESSAGE_READ_BT:
                    break;
                 default:
                     break;
            }
        }
    };
}
