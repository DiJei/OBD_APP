package com.zul.tests.obdzulbeta.OBDService.ATcommands;

import com.zul.tests.obdzulbeta.OBDService.BluetoothManager;
import com.zul.tests.obdzulbeta.OBDService.OBDCommand;
import com.zul.tests.obdzulbeta.OBDService.TCPSocketManager;

import java.util.ArrayList;

public class ATcommand extends OBDCommand {

    public ATcommand(String cmd, BluetoothManager btManage, android.os.Handler handler) {
        super(cmd + "\r\n", btManage,  handler);
    }

    public ATcommand(String cmd ,TCPSocketManager tcpSocketManager, android.os.Handler handler) {
        super(cmd +"\r\n", tcpSocketManager,  handler);
    }

    @Override
    public String formatAnwser(ArrayList<String> message) {
        if (message.size() <= 1)
            return "OK";
        if ( message.get(1).contains("OK")) {
            return "ok";
        }
        else {
            return "NO DATA";
        }
    }
}
