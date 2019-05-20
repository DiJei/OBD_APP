package com.zul.tests.obdzulbeta.OBDService.Service01;

import com.zul.tests.obdzulbeta.OBDService.BluetoothManager;
import com.zul.tests.obdzulbeta.OBDService.OBDCommand;
import com.zul.tests.obdzulbeta.OBDService.TCPSocketManager;

import java.util.ArrayList;

public class RPMCommand extends OBDCommand {

    public RPMCommand(BluetoothManager btManage, android.os.Handler handler) {
        super("010C\r\n", btManage,  handler);
        btManage.setHandler(obdCommandHandler);
    }

    public RPMCommand(TCPSocketManager tcpSocketManager, android.os.Handler handler) {
        super("010C\r\n", tcpSocketManager,  handler);
        tcpSocketManager.setHandler(obdCommandHandler);
    }

    @Override
    public String formatAnwser(ArrayList<String> message) {
        if ( message.get(0).contains("41") && message.get(0).contains("0C")) {
            String response = message.get(0).substring(message.get(0).indexOf("41"));
            String parts[] = response.split(" ");
            String value = "";
            double A,B  = 1;
            A = Integer.parseInt(parts[2], 16);
            B = Integer.parseInt(parts[3], 16);
            A = (256 * A + B) / 4;
            value = String.valueOf((int) A);
            return "410C " + value;
        }
        else {
            return "NO DATA";
        }
    }
}
