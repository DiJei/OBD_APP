package com.zul.tests.obdzulbeta;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zul.tests.obdzulbeta.OBDService.OBDService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ServiceActivity extends AppCompatActivity {
    String obd_protocol = "";
    String carInfo = "";
    String FILENAME = "listCars";
    TextView status = null;
    OBDService myOBDService  = null;
    private static final int OBD_COMMAND = 0;
    private static final int PROTOCOL_OBD = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);
        Bundle extras = getIntent().getExtras();
        carInfo = extras.getString("CAR_INFO");
        status = (TextView) findViewById(R.id.statusText);
        myOBDService  = new OBDService(uiHandler, "192.168.0.10", 35000);
        myOBDService.configOBD();

    }

    public void deleteProfile(View view) throws IOException {
        ArrayList<String> lines = new ArrayList();
        String filePath = this.getFilesDir() + "/" + FILENAME;
        File file = new File( filePath );

        if (file.exists()) {

            FileInputStream fis = openFileInput(FILENAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.equals(carInfo) == false)
                    lines.add(line);
            }

            file.delete();

            FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_APPEND);
            int x;
            for (x = 0; x < lines.size(); x++)
                fos.write((lines.get(x) + "\n").getBytes());
            fos.close();

            Intent intent = new Intent(ServiceActivity.this, MainActivity.class);
            startActivity(intent);
        }

    }


    private  final android.os.Handler uiHandler = new android.os.Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            Bundle bundle = msg.getData();
            Message readMsg = uiHandler.obtainMessage();
            String message = "";
            switch (msg.what) {
                case OBD_COMMAND:
                    if ( bundle != null)
                        message  = bundle.getString("data");
                    break;
                case PROTOCOL_OBD:
                    if ( bundle != null)
                        message  = bundle.getString("data");
                    obd_protocol = message;
                    status.setText("pronto");
                    break;
                default:
                    break;
            }
        }
    };
}
