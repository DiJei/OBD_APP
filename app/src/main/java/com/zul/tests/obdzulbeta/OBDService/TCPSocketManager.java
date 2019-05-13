package com.zul.tests.obdzulbeta.OBDService;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class TCPSocketManager {

    private static final String TAG = "TagWifi";
    private static final int MESSAGE_READ_WIFI = 1;
    String ip = "";
    int port = 0;
    android.os.Handler mHandler;
    Socket s = null;

    public TCPSocketManager(String ip_address, int port_address, android.os.Handler handler) {
        ip = ip_address;
        port = port_address;
        mHandler = handler;

    }

    public void sendData(String msg) {
        SocketSender sender = new SocketSender();
        sender.execute(msg);

    }

    public void setHandler(android.os.Handler handle) {
        mHandler = handle;
    }

    private class SocketSender extends AsyncTask<String, Void, Void>
    {
        PrintWriter pw;
        Socket s;

        @Override
        protected Void doInBackground(String... voids) {
            String message = voids[0];
            ArrayList<String> receive =  new ArrayList<>();
            String incomingData = new String();
            try {
                s = new Socket(ip, port);
                pw = new PrintWriter(s.getOutputStream());
                pw.write(message);
                pw.flush();
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(s.getInputStream()));
                //get response from server
                while ((incomingData =  inFromServer.readLine()) != null && !incomingData.equals("")) {
                    receive.add(incomingData);

                }
                inFromServer.close();
                pw.close();
                s.close();
                Message readMsg = mHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("DATA",receive);
                readMsg.setData(bundle);
                readMsg.what = MESSAGE_READ_WIFI;
                mHandler.sendMessage(readMsg);
            } catch (IOException e) {
                Log.d(TAG, "Error: could not send message to OBD");
                e.printStackTrace();
            }
            return null;
        }
    }

}
