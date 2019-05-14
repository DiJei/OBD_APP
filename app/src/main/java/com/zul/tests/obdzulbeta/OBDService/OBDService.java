package com.zul.tests.obdzulbeta.OBDService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Message;
import com.zul.tests.obdzulbeta.OBDService.ATcommands.ATcommand;
import com.zul.tests.obdzulbeta.OBDService.ATcommands.OBDAdapterVoltage;
import com.zul.tests.obdzulbeta.OBDService.Service01.EngineCoolantTemperature;
import com.zul.tests.obdzulbeta.OBDService.Service01.RPMCommand;
import com.zul.tests.obdzulbeta.OBDService.Service01.SpeedCommand;

import java.util.ArrayList;
import java.util.UUID;


public class OBDService {

    ArrayList<String> listOfPIDs01 = new ArrayList<>();
    private static final int MESSAGE_READ_OBD = 1;
    private static final int THREAD_READY = 7;
    Boolean getResponse = false;
    private static final int PROTOCOL_OBD = 5;
    android.os.Handler uiHandler = null;
    private BluetoothAdapter mBTAdapter;
    private static UUID BTUUID = null;
    BluetoothManager mBluetoothManager = null;
    TCPSocketManager mWifiTCPSocketManager = null;
    String ip = "";
    ArrayList<String> configList = new ArrayList();
    int port = 0;
    ArrayList<String> message;
    int count = 0;
    private String[] ProtocolList = {
            "Auto",
            "SAE J1850 PWM",
            "SAE J1850 VPW",
            "ISO 9141 – 2",
            "ISO 14230 – 4 KWP ( 5 BAUD INIT )",
            "ISO 14230 – 4 KWP ( FAST INIT )",
            "ISO 15765 – 4 CAN ( 11 BIT ID, 500 KBAUD )",
            "ISO 15765 – 4 CAN ( 29 BIT ID, 500 KBAUD )",
            "ISO 15765 – 4 CAN ( 11 BIT ID, 250 KBAUD )",
            "ISO 15765 – 4 CAN ( 29 BIT ID, 250 KBAUD )",
            "SAE J1939 CAN ( 29 BIT ID, 250 KBAUD",
            "Not Found"
    };

    /** Constant <code>dtcLetters={'P', 'C', 'B', 'U'}</code> */
    protected final static char[] dtcLetters = {'P', 'C', 'B', 'U'};
    /** Constant <code>hexArray="0123456789ABCDEF".toCharArray()</code> */
    protected final static char[] hexArray = "0123456789ABCDEF".toCharArray();


    //For Bluetooth Device
    public  OBDService(android.os.Handler handler, String device) {
        uiHandler = handler;
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice btDevice = mBTAdapter.getRemoteDevice(device);
        BTUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        mBluetoothManager = new BluetoothManager(mBTAdapter, BTUUID, obdHandler);
        mBluetoothManager.startClient(btDevice);
    }

    //For WiFi TCP Device
    public  OBDService(android.os.Handler handler, String ips, int ports) {
        uiHandler = handler;
        ip = ips;
        port = ports;
        mWifiTCPSocketManager = new TCPSocketManager("192.168.0.10", 35000, obdHandler);
    }

    public void sendATCommand(String msg) {
        ATcommand myATcommand;
        if (mWifiTCPSocketManager != null)
            myATcommand = new ATcommand(msg,mWifiTCPSocketManager,obdHandler);
        else
            myATcommand = new ATcommand(msg,mBluetoothManager,obdHandler);
        myATcommand.OBDSendPID();
    }

    public void getSpeed() {
        SpeedCommand mySpeedCommand;
        if(mWifiTCPSocketManager != null)
         mySpeedCommand = new SpeedCommand(mWifiTCPSocketManager, uiHandler);
        else
            mySpeedCommand = new SpeedCommand(mBluetoothManager, uiHandler);
        mySpeedCommand.OBDSendPID();
    }

    public void getCoolTemp() {
        EngineCoolantTemperature myEngineCoolantTemperature;
        if (mWifiTCPSocketManager != null)
            myEngineCoolantTemperature = new EngineCoolantTemperature(mWifiTCPSocketManager, uiHandler);
        else
            myEngineCoolantTemperature = new EngineCoolantTemperature(mBluetoothManager, uiHandler);
        myEngineCoolantTemperature.OBDSendPID();
    }

    public void getRPM() {
        RPMCommand myRPMCommand;
        if (mWifiTCPSocketManager != null)
             myRPMCommand = new RPMCommand(mWifiTCPSocketManager, uiHandler);
        else
            myRPMCommand = new RPMCommand(mBluetoothManager, uiHandler);
        myRPMCommand.OBDSendPID();
    }

    public void getAdapterVoltage() {
        OBDAdapterVoltage myOBDAdapterVoltage;
        if (mWifiTCPSocketManager != null)
            myOBDAdapterVoltage = new OBDAdapterVoltage(mWifiTCPSocketManager, uiHandler);
        else
            myOBDAdapterVoltage = new OBDAdapterVoltage(mBluetoothManager, uiHandler);
        myOBDAdapterVoltage.OBDSendPID();
    }

    public void configOBD() {
        configList = new ArrayList();
        configList.add("ATE0");
        configList.add("ATST19");
        configList.add("ATH1");
        configList.add("ATSP0");
        configList.add("0100");
        configList.add("0120");
        configList.add("0140");
        configList.add("0160");
        configList.add("0180");
        configList.add("ATDPN");
        sendATCommand("ATZ");
    }

    public void stopOBDService() {
        if (mBluetoothManager != null) {
            mBluetoothManager.stopBTmanager();
        }
    }

    private  final android.os.Handler obdHandler = new android.os.Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {

            if(msg.what == MESSAGE_READ_OBD) {
                Bundle bundle = msg.getData();
                if ( bundle != null)
                    message  = bundle.getStringArrayList("DATA");
                switch (count) {
                    case 0:
                        sendATCommand(configList.get(count));
                        count += 1;
                        break;
                    case 1:
                        int x;
                        for(x = 0; x < message.size(); x++) {
                            if (message.get(x).contains("OK")) {
                                sendATCommand(configList.get(count));
                                count += 1;
                            }
                            else
                                sendATCommand(configList.get(count - 1));
                        }
                        break;
                    case 2:
                    case 3:
                    case 4:
                        if (message.get(0).contains("OK")) {
                            sendATCommand(configList.get(count));
                            count += 1;
                        }
                        else
                            sendATCommand(configList.get(count));
                        break;
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                        getResponse = false;
                        for (x = 0; x < message.size(); x ++) {
                            if (message.get(x).contains("41")) {
                                sendATCommand(configList.get(count));
                                count += 1;
                                getResponse = true;
                                listOfPIDs01.add(message.get(x).substring(message.get(x).indexOf("41") + 4).replace(" ", ""));
                            }
                            else if( message.get(x).contains("NO DATA")) {
                                sendATCommand(configList.get(count));
                                count += 1;
                                getResponse = true;
                                listOfPIDs01.add(" ");
                            }
                        }
                        if (!getResponse)
                            sendATCommand(configList.get(count));
                        break;
                    case 10:
                        Bundle uiBundle = new Bundle();
                        Message readMsg = uiHandler.obtainMessage();
                        if((message.get(0).contains("ERROR")) || (message.get(0).contains("NO")))
                            sendATCommand(configList.get(count - 1));
                        else    {
                            String send = ProtocolList[Integer.parseInt(message.get(0).substring(message.get(0).indexOf("A") + 1))];
                            uiBundle.putString("data", send);
                            readMsg.what = PROTOCOL_OBD;
                            uiHandler.sendMessage(readMsg);
                            count += 1;
                        }
                        break;
                }
            }
            else if(msg.what == THREAD_READY) {
                configOBD();
            }
        }
    };
}
