package com.zul.tests.obdzulbeta;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    String FILENAME = "listCars";
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final String[] conections = new String[] {"WIFI", "BT", "BLE"};
    Spinner dropdown = null;
    String selectedConnection = "";
    String deviceBT = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, conections);
        Spinner dropdown = (Spinner)findViewById(R.id.conectionType);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(this);

        if (mBluetoothAdapter == null)
            Toast.makeText(getApplicationContext(), "Bluetooth não disponivel", Toast.LENGTH_SHORT).show();
        else {
            mBluetoothAdapter.enable();
        }
}

    //Performing action onItemSelected and onNothing selected
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position,long id) {
        selectedConnection = conections[position];
        Set<BluetoothDevice> pairedDevices;
        if (selectedConnection.equals("BT")) {
            ArrayList<String> devices_list = new ArrayList<>();

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View row = getLayoutInflater().inflate(R.layout.devices_list,null);
            ListView listBT = (ListView)row.findViewById(R.id.deviceList);
            builder.setTitle("Escolha dispositivo");
            final ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_activated_1);
            listBT.setAdapter(mArrayAdapter);
            pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    mArrayAdapter.add(device.getName() + " " + device.getAddress());
                }
                builder.setAdapter(mArrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Inicia nova activiy para conexão
                        deviceBT = mArrayAdapter.getItem(which);
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
            else
                Toast.makeText(getApplicationContext(), "Nenhum dispositivo pareado", Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        selectedConnection = "WIFI";
    }

    /*Check if user has already connect*/
    public void startService(View view) {
        Intent intent = new Intent(MainActivity.this, LoginScreen.class);
        intent.putExtra("CONECTION_TYPE", selectedConnection );
        startActivity(intent);
    }

    public void showCarList(View view) throws IOException, PackageManager.NameNotFoundException {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1);
        View row = getLayoutInflater().inflate(R.layout.car_list_view, null);
        ArrayList<String> listOfCars = new ArrayList<>();

        ListView listCar = (ListView) row.findViewById(R.id.deviceList);
        builder.setTitle("Escolha carro");
        String line;

        String filePath = this.getFilesDir() + "/" + FILENAME;
        File file = new File( filePath );


        if (file.exists()) {

            FileInputStream fis = openFileInput(FILENAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            while ((line = bufferedReader.readLine()) != null)
                mArrayAdapter.add(line);
            listCar.setAdapter(mArrayAdapter);

            if(mArrayAdapter.getCount() < 1) {
                Toast.makeText(getApplicationContext(),"Sem usuários", Toast.LENGTH_LONG).show();
                return;
            }

        builder.setAdapter(mArrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), mArrayAdapter.getItem(which), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this, ServiceActivity.class);
                intent.putExtra("CAR_INFO", mArrayAdapter.getItem(which));
                intent.putExtra("CONECTION_TYPE", selectedConnection );
                intent.putExtra("BT_DEVICE", deviceBT);
                startActivity(intent);
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
        else {
            Toast.makeText(getApplicationContext(),"Sem usuários", Toast.LENGTH_LONG).show();
        }

    }
}
