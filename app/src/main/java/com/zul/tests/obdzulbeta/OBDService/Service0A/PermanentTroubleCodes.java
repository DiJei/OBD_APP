package com.zul.tests.obdzulbeta.OBDService.Service0A;

import com.zul.tests.obdzulbeta.OBDService.BluetoothManager;
import com.zul.tests.obdzulbeta.OBDService.OBDCommand;
import com.zul.tests.obdzulbeta.OBDService.TCPSocketManager;

import java.util.ArrayList;

public class PermanentTroubleCodes extends OBDCommand {

    public PermanentTroubleCodes(BluetoothManager btManage, android.os.Handler handler) {
        super("0A\r\n", btManage,  handler);
        btManage.setHandler(obdCommandHandler);
    }

    public PermanentTroubleCodes(TCPSocketManager tcpSocketManager, android.os.Handler handler) {
        super("0A\r\n", tcpSocketManager,  handler);
        tcpSocketManager.setHandler(obdCommandHandler);
    }

    @Override
    public String formatAnwser(ArrayList<String> message) {
        if ( message.get(0).contains("4A")) {
            String dtcs = message.get(0).substring(message.get(0).indexOf("4A"));
            String listOfDTC = "";
            dtcs = dtcs.replace(" ", "");

            for (int x = 4; x < dtcs.length() ;x +=4 ) {
                String temp = "";
                byte b1 = hexStringToByteArray(dtcs.charAt(x));
                int ch1 = ((b1 & 0xC0) >> 6);
                int ch2 = ((b1 & 0x30) >> 4);
                temp += dtcLetters[ch1];
                temp += hexArray[ch2];
                temp += dtcs.substring(x+1, x + 4);
                if (temp.equals("P0000")) {
                    listOfDTC += "";
                }
                else
                    listOfDTC += temp + " ";
            }
            return "4A " + listOfDTC;
        }
        else if (message.get(0).contains("CAN"))
            return message.get(0);
        else {
            return "";
        }
    }

}
