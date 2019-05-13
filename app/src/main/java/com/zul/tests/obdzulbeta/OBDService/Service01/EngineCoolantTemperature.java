package com.zul.tests.obdzulbeta.OBDService.Service01;

import com.zul.tests.obdzulbeta.OBDService.BluetoothManager;
import com.zul.tests.obdzulbeta.OBDService.OBDCommand;
import com.zul.tests.obdzulbeta.OBDService.TCPSocketManager;

import java.util.ArrayList;

public class EngineCoolantTemperature extends OBDCommand {
    public EngineCoolantTemperature(BluetoothManager btManage, android.os.Handler handler) {
        super("0105\r\n", btManage,  handler);
    }

    public EngineCoolantTemperature(TCPSocketManager tcpSocketManager, android.os.Handler handler) {
        super("0105\r\n", tcpSocketManager,  handler);
        tcpSocketManager.setHandler(obdCommandHandler);
    }

    @Override
    public String formatAnwser(ArrayList<String> message) {
        if ( message.get(0).contains("41")) {
            String response = message.get(1).substring(message.get(0).indexOf("41"));
            String parts[] = response.split(" ");
            String value = "";
            double A  = 1;
            A = Integer.parseInt(parts[2], 16);
            A = A - 40;
            value = String.valueOf((int) A);
            return value;
        }
        else {
            return "NO DATA";
        }
    }
}
