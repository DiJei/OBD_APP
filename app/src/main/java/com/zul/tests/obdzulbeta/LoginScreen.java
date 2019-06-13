package com.zul.tests.obdzulbeta;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import android.widget.Toast;


import java.io.File;






public class LoginScreen extends AppCompatActivity {

    EditText year,engine,model,brand,fuelType = null;
    String connectionType = "";
    String btDevice = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        Bundle extras = getIntent().getExtras();
        brand    = (EditText)findViewById(R.id.brandText);
        model    = (EditText)findViewById(R.id.modelText);
        engine   = (EditText)findViewById(R.id.engineText);
        year     = (EditText)findViewById(R.id.yearText);
        fuelType = (EditText)findViewById(R.id.fuelType);
        connectionType = extras.getString("CONECTION_TYPE");
        btDevice = extras.getString("BT_DEVICE");

    }

    public  void   beginService(View view)  {

        String brandCar = brand.getText().toString();
        String modelCar = model.getText().toString();
        String engineCar = engine.getText().toString();
        String yearCar = year.getText().toString();
        String fuelTypeCar = fuelType.getText().toString();

        if (brandCar.length() < 1) {
            Toast.makeText(getApplicationContext(), "Digite todas as informações", Toast.LENGTH_LONG).show();
            return;
        }
        if (modelCar.length() < 1) {
            Toast.makeText(getApplicationContext(), "Digite todas as informações", Toast.LENGTH_LONG).show();
            return;
        }
        if (engineCar.length() < 1) {
            Toast.makeText(getApplicationContext(), "Digite todas as informações", Toast.LENGTH_LONG).show();
            return;
        }
        if (yearCar.length() < 1) {
            Toast.makeText(getApplicationContext(), "Digite todas as informações", Toast.LENGTH_LONG).show();
            return;
        }
        if (fuelTypeCar.length() < 1) {
            Toast.makeText(getApplicationContext(), "Digite todas as informações", Toast.LENGTH_LONG).show();
            return;
        }

        String carLine = brandCar + "-" + modelCar + "-" + yearCar + "-" + engineCar + "-" + fuelTypeCar ;



        File newdir = new File(getExternalFilesDir(null) + "/" + carLine);
        if(newdir.exists()) {
            Toast.makeText(getApplicationContext(), "Carro já existe", Toast.LENGTH_LONG).show();
            return;
        }

        newdir.mkdirs();

        if (!newdir.exists()) {
            Toast.makeText(getApplicationContext(), "Problema na craicação", Toast.LENGTH_LONG).show();
            return;
        }
        else {
            Toast.makeText(getApplicationContext(), "Carro criado", Toast.LENGTH_LONG).show();
        }


        //Parte para tela de Serviços e conecta com OBD
        Intent intent = new Intent(LoginScreen.this, ServiceActivity.class);
        intent.putExtra("CAR_INFO", carLine);
        intent.putExtra("CONECTION_TYPE", connectionType);
        intent.putExtra("BT_DEVICE" ,btDevice);
        startActivity(intent);
    }
}
