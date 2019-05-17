package com.zul.tests.obdzulbeta.OBDService.Service01;

import com.zul.tests.obdzulbeta.OBDService.BluetoothManager;
import com.zul.tests.obdzulbeta.OBDService.OBDCommand;
import com.zul.tests.obdzulbeta.OBDService.TCPSocketManager;

import java.util.ArrayList;

public class IntakeManifoldPressure extends OBDCommand {

    public IntakeManifoldPressure(BluetoothManager btManage, android.os.Handler handler) {
        super("010B\r\n", btManage,  handler);
        btManage.setHandler(obdCommandHandler);
    }

    public IntakeManifoldPressure(TCPSocketManager tcpSocketManager, android.os.Handler handler) {
        super("010B\r\n", tcpSocketManager,  handler);
        tcpSocketManager.setHandler(obdCommandHandler);
    }

    @Override
    public String formatAnwser(ArrayList<String> message) {
        if ( message.get(0).contains("41")) {
            String response = message.get(0).substring(message.get(0).indexOf("41"));
            String parts[] = response.split(" ");
            String value = "";
            double A = 1;
            A = Integer.parseInt(parts[2], 16);
            value = String.valueOf((int) A);
            return "410B " + value;
        }
        else {
            return "NO DATA";
        }
    }

}
