package com.zul.tests.obdzulbeta;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final int REQUEST_ENABLE_BT = 10;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final String[] conections = new String[] {"WIFI", "BT", "BLE"};
    Spinner dropdown = null;
    String selectedConnection = "";
    String deviceBT = "";
    Set<BluetoothDevice> pairedDevices;

    private static final String SHARED_PREFS = "SHARED_PREFS";
    private static final String CONECTION_TYPE = "CONECTION_TYPE";
    private static final String BT = "BT";
    private static final String CAR_INFO = "CAR_INFO";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String carInfo = sharedPreferences.getString(CAR_INFO,"");
        String connectionType = sharedPreferences.getString(CONECTION_TYPE,"");
        deviceBT = sharedPreferences.getString(BT,"");

        if (!carInfo.equals("")) {
            //Parte para tela de Serviços e conecta com OBD
            Intent intent = new Intent(MainActivity.this, ServiceActivity.class);
            intent.putExtra("CAR_INFO", carInfo);
            intent.putExtra("CONECTION_TYPE", connectionType);
            intent.putExtra("BT_DEVICE" ,deviceBT);
            startActivity(intent);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, conections);
        Spinner dropdown = (Spinner)findViewById(R.id.conectionType);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(this);

        if (mBluetoothAdapter == null)
            Toast.makeText(getApplicationContext(), "Bluetooth não disponivel", Toast.LENGTH_SHORT).show();
    }

    //Performing action onItemSelected and onNothing selected
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position,long id) {
        selectedConnection = conections[position];
        if (selectedConnection.equals("BT")) {
            if(!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                
            }
            else {
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

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        selectedConnection = "WIFI";
    }

    /*Check if user has already connect*/
    public void startService(View view) {
        Intent intent = new Intent(MainActivity.this, LoginScreen.class);
        intent.putExtra("CONECTION_TYPE", selectedConnection );
        if (selectedConnection.equals("BT"))
            intent.putExtra("BT_DEVICE", deviceBT );
        startActivity(intent);
    }

    public void showCarList(View view) throws IOException, PackageManager.NameNotFoundException {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1);
        View row = getLayoutInflater().inflate(R.layout.car_list_view, null);
        ListView listCar = (ListView) row.findViewById(R.id.deviceList);
        File[] directories = new File(String.valueOf(this.getFilesDir())).listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });

        if(directories.length < 1) {
            Toast.makeText(getApplicationContext(),"Sem usuários", Toast.LENGTH_LONG).show();
            return;
        }

        for (int x = 0; x < directories.length; x ++)
            mArrayAdapter.add(directories[x].getName());

        listCar.setAdapter(mArrayAdapter);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                mBluetoothAdapter.enable();
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
    }


}
