package com.zul.tests.obdzulbeta.OBDService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Message;
import com.zul.tests.obdzulbeta.OBDService.ATcommands.ATcommand;
import com.zul.tests.obdzulbeta.OBDService.ATcommands.OBDAdapterVoltage;
import com.zul.tests.obdzulbeta.OBDService.Service01.EngineCoolantTemperature;
import com.zul.tests.obdzulbeta.OBDService.Service01.IntakeAirTemperature;
import com.zul.tests.obdzulbeta.OBDService.Service01.IntakeManifoldPressure;
import com.zul.tests.obdzulbeta.OBDService.Service01.RPMCommand;
import com.zul.tests.obdzulbeta.OBDService.Service01.SpeedCommand;
import com.zul.tests.obdzulbeta.OBDService.Service03.TroubleCodes;
import com.zul.tests.obdzulbeta.OBDService.Service07.PendingTroubleCodes;
import com.zul.tests.obdzulbeta.OBDService.Service0A.PermanentTroubleCodes;

import java.util.ArrayList;
import java.util.UUID;


public class OBDService {

    Boolean init = true;
    ArrayList<String> listOfPIDs01 = new ArrayList<>();
    ArrayList<String> listOfPIDs09 = new ArrayList<>();
    String  vehicleVIN = "";
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
    ArrayList<String> vinByte = new ArrayList();
    String protocolNumber = null;

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


    //For Bluetooth Device
    public  OBDService(android.os.Handler handler, String device, Boolean INIT, String protocol) {
        uiHandler = handler;
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice btDevice = mBTAdapter.getRemoteDevice(device);
        BTUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        mBluetoothManager = new BluetoothManager(mBTAdapter, BTUUID, obdHandler);
        if(!mBluetoothManager.isConnected()) {
            mBluetoothManager.startClient(btDevice);
        }
        init = INIT;
        protocolNumber = protocol;
    }

    //For WiFi TCP Device
    public  OBDService(android.os.Handler handler, String ips, int ports, String protocol) {
        uiHandler = handler;
        ip = ips;
        port = ports;
        mWifiTCPSocketManager = new TCPSocketManager("192.168.0.10", 35000, obdHandler);
        protocolNumber = protocol;
    }


    /*-------------------COMMMANDS---------------------*/
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


    public void getMAP() {
        IntakeManifoldPressure myIntakeManifoldPressure;
        if (mWifiTCPSocketManager != null)
            myIntakeManifoldPressure = new IntakeManifoldPressure(mWifiTCPSocketManager, uiHandler);
        else
            myIntakeManifoldPressure = new IntakeManifoldPressure(mBluetoothManager, uiHandler);
        myIntakeManifoldPressure.OBDSendPID();
    }

    public void getIAT() {
        IntakeAirTemperature myIntakeAirTemperature;
        if (mWifiTCPSocketManager != null)
            myIntakeAirTemperature = new IntakeAirTemperature(mWifiTCPSocketManager, uiHandler);
        else
            myIntakeAirTemperature = new IntakeAirTemperature(mBluetoothManager, uiHandler);
        myIntakeAirTemperature.OBDSendPID();
    }

    public void getTroubleCodes() {
        TroubleCodes myTroubleCodes;
        if (mWifiTCPSocketManager != null)
            myTroubleCodes = new TroubleCodes(mWifiTCPSocketManager, uiHandler);
        else
            myTroubleCodes = new TroubleCodes(mBluetoothManager, uiHandler);
        myTroubleCodes.OBDSendPID();
    }

    public void getPendingCodes() {
        PendingTroubleCodes myPendingTroubleCodes;
        if (mWifiTCPSocketManager != null)
            myPendingTroubleCodes = new PendingTroubleCodes(mWifiTCPSocketManager, uiHandler);
        else
            myPendingTroubleCodes = new PendingTroubleCodes(mBluetoothManager, uiHandler);
        myPendingTroubleCodes.OBDSendPID();
    }

    public void getPermanetCodes() {
        PermanentTroubleCodes myPermanentTroubleCodes;
        if (mWifiTCPSocketManager != null)
            myPermanentTroubleCodes = new PermanentTroubleCodes(mWifiTCPSocketManager, uiHandler);
        else
            myPermanentTroubleCodes = new PermanentTroubleCodes(mBluetoothManager, uiHandler);
        myPermanentTroubleCodes.OBDSendPID();
    }
    /*-------------------------------------------------*/




    public String decodeVIN(ArrayList<String> message) {

        StringBuilder output = new StringBuilder("");
        String hexStr;


        hexStr = message.get(0).replace(" ", "");
        hexStr = hexStr.substring(13);
        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        hexStr = message.get(1).replace(" ", "");
        hexStr = hexStr.substring(5);
        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        hexStr = message.get(2).replace(" ", "");
        hexStr = hexStr.substring(5);
        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }

        return output.toString();

    }

    public void stopOBDService() {
        if (mBluetoothManager != null) {
            mBluetoothManager.stopBTmanager();
        }
    }

    public void setBlueToothTimeout(int time) {
        if (mBluetoothManager != null)
         mBluetoothManager.setTimer(time);
    }

    public ArrayList<String> getService01() {
        return listOfPIDs01;
    }

    public ArrayList getService09() {
        return listOfPIDs09;
    }

    public String getVIN() {
        return vehicleVIN;
    }


    public void configOBD() {
        configList = new ArrayList();
        configList.add("ATZ");        // 0  Reset
        configList.add("ATE0");       // 1  Echo OFF

        configList.add("ATST32");     // 2  Timeout 100ms
        configList.add("ATH1");       // 3  Headers ON

        if(protocolNumber.equals("nope"))
            configList.add("ATSP0");      // 4  Protocol AUTO
        else
            configList.add("ATSP" + protocolNumber);      // 4  Protocol AUTO

        configList.add("0100");       // 5  List Of PID 01
        configList.add("0120");       // 6  List Of PID 01
        configList.add("0140");       // 7  List Of PID 01
        configList.add("0160");       // 8  List Of PID 01
        configList.add("0180");       // 9  List Of PID 01

        configList.add("0900");       // 10 List Of PID 09
        configList.add("0902");       // 11 Car Chassi

        configList.add("ATDPN");      // 12 Get protocol number


        sendATCommand(configList.get(count));

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
                        count += 1;
                        sendATCommand("ATE0");
                        break;
                    case 1:
                        int x;
                        for(x = 0; x < message.size(); x++) {
                            if (message.get(x).contains("OK")) {
                                count += 1;
                                sendATCommand(configList.get(count));
                                break;
                            }
                            else {
                                sendATCommand(configList.get(count));
                                break;
                            }
                        }
                        break;
                    case 2:
                    case 3:
                    case 4:
                        if (message.get(0).contains("OK")) {
                            count += 1;
                            sendATCommand(configList.get(count));
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
                                count += 1;
                                getResponse = true;
                                if(!protocolNumber.equals("nope")) {
                                    count = 12;
                                    sendATCommand(configList.get(count));
                                }

                                listOfPIDs01.add(message.get(x).substring(message.get(x).indexOf("41") + 4).replace(" ", ""));
                                sendATCommand(configList.get(count));
                                break;
                            }
                            else if (message.get(x).contains("NO")) {
                                count += 1;
                                getResponse = true;
                                listOfPIDs01.add(" ");
                                sendATCommand(configList.get(count));
                                break;
                            }
                        }
                        if (!getResponse)
                            sendATCommand(configList.get(count));
                        break;
                    case 10:
                        getResponse = false;
                        for (x = 0; x < message.size(); x ++) {
                            if (message.get(x).contains("49")) {
                                count += 1;
                                getResponse = true;
                                listOfPIDs09.add(message.get(x).substring(message.get(x).indexOf("49") + 4).replace(" ", ""));
                                sendATCommand(configList.get(count));
                                break;
                            } else if (message.get(x).contains("NO")) {
                                count += 1;
                                getResponse = true;
                                listOfPIDs09.add(" ");
                                sendATCommand(configList.get(count));
                                break;
                            }
                        }
                        if (!getResponse)
                            sendATCommand(configList.get(count ));
                        break;
                    case 11:
                        getResponse = true;
                        for (x = 0; x < message.size(); x++) {
                            if (message.get(x).contains("ERROR") || message.get(x).contains("STOPPED")) {
                                getResponse = false;
                                break;
                            }
                            else
                                vinByte.add(message.get(x));
                        }
                        if (vinByte.size() < 3) {
                            vinByte = new ArrayList<>();
                            sendATCommand(configList.get(count));
                        }
                        else if(getResponse) {
                            if (message.get(0).contains("49")) {
                                vehicleVIN = decodeVIN(vinByte);
                                count += 1;
                                sendATCommand(configList.get(count));
                            }
                        }
                        break;
                    case 12:
                        Bundle uiBundle = new Bundle();
                        Message readMsg = uiHandler.obtainMessage();
                        if((message.get(0).contains("ERROR")) || (message.get(0).contains("NO")))
                            sendATCommand(configList.get(count));
                        else    {
                            String send = ProtocolList[Integer.parseInt(message.get(0).substring(message.get(0).indexOf("A") + 1))] + " " + message.get(0).substring(message.get(0).indexOf("A") + 1);
                            uiBundle.putString("data", send);
                            readMsg.what = PROTOCOL_OBD;
                            readMsg.setData(uiBundle);
                            uiHandler.sendMessage(readMsg);
                            count += 1;
                        }
                        break;
                }
            }
            else if(msg.what == THREAD_READY) {
                if(init)
                    configOBD();
            }
        }
    };
}
