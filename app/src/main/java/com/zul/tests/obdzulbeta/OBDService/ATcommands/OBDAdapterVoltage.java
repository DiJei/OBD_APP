package com.zul.tests.obdzulbeta.OBDService.ATcommands;

import com.zul.tests.obdzulbeta.OBDService.BluetoothManager;
import com.zul.tests.obdzulbeta.OBDService.OBDCommand;
import com.zul.tests.obdzulbeta.OBDService.TCPSocketManager;

import java.util.ArrayList;

public class OBDAdapterVoltage extends OBDCommand {

    public OBDAdapterVoltage(BluetoothManager btManage, android.os.Handler handler) {
        super("ATRV\r\n", btManage,  handler);
    }

    public OBDAdapterVoltage(TCPSocketManager tcpSocketManager, android.os.Handler handler) {
        super("ATRV\r\n", tcpSocketManager,  handler);
        tcpSocketManager.setHandler(obdCommandHandler);
    }

    @Override
    public String formatAnwser(ArrayList<String> message) {
        if ( message.get(0).contains("V")) {
            String response = message.get(0).substring(message.get(0).indexOf("V"));
            String value =  message.get(0).substring(1);
            return value;
        }
        else {
            return "NO DATA";
        }
    }

}
