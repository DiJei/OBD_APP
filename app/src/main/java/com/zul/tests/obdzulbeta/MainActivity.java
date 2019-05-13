package com.zul.tests.obdzulbeta;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    String FILENAME = "listCars";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    /*Check if user has already connect*/
    public void startService(View view) {
        Intent intent = new Intent(MainActivity.this, LoginScreen.class);
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



        String yourFilePath = this.getFilesDir() + "/" + FILENAME;
        File yourFile = new File( yourFilePath );


        if (yourFile.exists()) {

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
