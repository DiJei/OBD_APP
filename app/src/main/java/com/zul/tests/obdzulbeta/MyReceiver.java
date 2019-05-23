package com.zul.tests.obdzulbeta;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.widget.Toast;

import java.util.Set;


public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {


        if (false) {
            //intent.getAction().equals("android.bluetooth.adapter.action.DISCOVERY_STARTED")
            Toast.makeText(context, "Nenhum dispositivo pareado", Toast.LENGTH_SHORT).show();

            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null)
                return;
            if (!mBluetoothAdapter.isEnabled())
                return;
            Set<BluetoothDevice> pairedDevices;
            Intent i = new Intent(context, ServiceActivity.class);
            pairedDevices = mBluetoothAdapter.getBondedDevices();
            i.putExtra("CAR_INFO", "CAR" );
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().contains("OBD")) {
                        i.putExtra("CONECTION_TYPE", "BT" );
                        i.putExtra("BT_DEVICE", device.getName() + " " + device.getAddress());
                        context.startActivity(i);
                    }
                }
            }
        }



        String action = intent.getAction();
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            Toast.makeText(context, "Device found", Toast.LENGTH_SHORT).show();
        }
        else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            Toast.makeText(context, "Device is now connected", Toast.LENGTH_SHORT).show();
        }
        else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            Toast.makeText(context, "Done searching", Toast.LENGTH_SHORT).show();
        }
        else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
            Toast.makeText(context, "Device is about to disconnect", Toast.LENGTH_SHORT).show();
        }
        else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            Toast.makeText(context, "Device has disconnected", Toast.LENGTH_SHORT).show();
        }
        else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
            //android.bluetooth.devicepicker.action.DEVICE_SELECTED
            Toast.makeText(context, "Pairing recquest", Toast.LENGTH_SHORT).show();
        }


    }
}