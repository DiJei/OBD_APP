package com.zul.tests.obdzulbeta;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.zul.tests.obdzulbeta.OBDService.OBDService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;



public class ServiceActivity extends AppCompatActivity {
    MyReceiver myReceiver;
    double amount_fuel;
    int count = 0;
    String btAdress = "";
    String obd_protocol = "";
    String carInfo = "";
    String FILENAME = "listCars";
    TextView status = null;
    OBDService myOBDService  = null;
    int countDTC;
    String connectionType = "";
    String deviceBT = "";
    String tempCool, RPM, battery, alternator, MAP, IAT, speed = "0";
    String pendingCodes, troubleCodes,  permanentCodes = " ";
    ArrayList<ItemPID> dataList = new ArrayList<>();
    ListView pidListView = null;
    private static final int OBD_COMMAND = 0;
    private static final int PROTOCOL_OBD = 5;
    private static final int BT_THREAD_READY = 7;
    Button logginButton = null;

    private static final String SHARED_PREFS = "SHARED_PREFS";
    private static final String CONECTION_TYPE = "CONECTION_TYPE";
    private static final String BT = "BT";
    private static final String CAR_INFO = "CAR_INFO";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        countDTC = 10;
        amount_fuel = 0;
        count = 10;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);


        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        carInfo = sharedPreferences.getString(CAR_INFO,"");
        connectionType = sharedPreferences.getString(CONECTION_TYPE,"");
        deviceBT = sharedPreferences.getString(BT,"");

        //Get Data from MainActivity or LoginActivity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            carInfo = extras.getString("CAR_INFO");
            connectionType = extras.getString("CONECTION_TYPE");
            if (connectionType.equals("BT"))
                deviceBT = extras.getString("BT_DEVICE");
        }

        //Save the setting on shared preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CAR_INFO,carInfo);
        editor.putString(CONECTION_TYPE,connectionType);
        editor.putString(BT,deviceBT);
        editor.apply();


        status = (TextView) findViewById(R.id.statusText);
        pidListView = (ListView) findViewById(R.id.listOfPIDs);
        logginButton = (Button) findViewById(R.id.logButton);

        if (connectionType.equals("WIFI")) {
            if (myOBDService == null)
                myOBDService = new OBDService(uiHandler, "192.168.0.10", 35000);
            if(!isLogOn())
                myOBDService.configOBD();

        } else if (connectionType.equals("BT")) {
            String[] parts = deviceBT.split(" ");
            btAdress = parts[1];
            if (myOBDService == null)
                if(!isLogOn())
                    myOBDService = new OBDService(uiHandler, parts[1],true);
                else
                    myOBDService = new OBDService(uiHandler, parts[1], false);
        }

        dataList.add(new ItemPID( "Temperatura da água", "x", "°C"));
        dataList.add(new ItemPID( "Bateria", "x", "V"));
        dataList.add(new ItemPID( "Alternador", "x", "V"));
        dataList.add(new ItemPID( "Litros de combústivel usados", "x", "L"));
        dataList.add(new ItemPID( "", "DTC", ""));

        ArrayAdapter<ItemPID> adapter = new ItemPIDArrayAdapter(this, 0, dataList);
        pidListView.setAdapter(adapter);
        pidListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(position == 4) {
                    //In Case receiver is already not registed
                    try {

                        unregisterReceiver(myReceiver);
                    } catch(IllegalArgumentException e) {

                        e.printStackTrace();
                    }

                    Intent serviceIntent = new Intent(ServiceActivity.this, OBDReaderService.class);
                    stopService(serviceIntent);
                    countDTC = 0;
                    Runnable startDTC = new Runnable() {
                        public void run() {
                            getDTC();
                        }
                    };
                    Runnable initBT = new Runnable() {
                        public void run() {
                            myOBDService = new OBDService(uiHandler, btAdress,false);
                        }
                    };
                    Handler handler2 = new Handler();
                    handler2.postDelayed(initBT, 1500);
                    Handler handler = new Handler();
                    handler.postDelayed(startDTC, 3000);
                }
            }
        });


         tempCool = "0";
         RPM = "0";
         battery = "0";
         alternator = "0";
         MAP= "0";
         IAT= "0";
         speed = "0";
         pendingCodes= " ";
         troubleCodes= " ";
         permanentCodes = " ";

    }


    @Override
    protected void onResume() {
        super.onResume();
        //Register receiver
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(OBDReaderService.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);
    }




    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("ConectionType", connectionType);
        if(connectionType.equals("BT"))
            outState.putString("BTDevice", deviceBT);

        outState.putString("CarInfo", carInfo);
    }


    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        connectionType = savedInstanceState.getString("ConectionType");
        btAdress = savedInstanceState.getString("BTDevice");

    }


    public boolean isLogOn() {

        File file = new File(this.getFilesDir() + "/" + carInfo +  "/info.txt");
        if(!file.exists())
            return false;
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(this.getFilesDir() + "/" + carInfo +  "/info.txt"));
            String line = reader.readLine();
            while (line != null) {
                if(line.contains("LOG")) {
                    if(line.substring(4).equals("on")) {
                        reader.close();
                        return true;
                    }
                    reader.close();
                    return false;
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
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



    private void updateValue(int index, String val) {
        View v = pidListView.getChildAt(index - pidListView.getFirstVisiblePosition());
        if(v == null)
            return;
        TextView someText = (TextView) v.findViewById(R.id.textVal);
        someText.setText(val);
    }

    public void showDTCcodes() {
        //stop continus reading
        //Show DTC LIST:
        AlertDialog.Builder builder = new AlertDialog.Builder(ServiceActivity.this);
        View row = getLayoutInflater().inflate(R.layout.devices_list,null);
        ListView listBT = (ListView)row.findViewById(R.id.deviceList);
        builder.setTitle("Escolha dispositivo");
        final ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(ServiceActivity.this, android.R.layout.simple_list_item_activated_1);
        listBT.setAdapter(mArrayAdapter);

        mArrayAdapter.add("Códigos de falhas:");
        mArrayAdapter.add(troubleCodes);
        mArrayAdapter.add("Códigos de falhas pendentes:");
        mArrayAdapter.add(pendingCodes);
        mArrayAdapter.add("Códigos de falhas permamentes:");
        mArrayAdapter.add(permanentCodes);
        builder.setAdapter(mArrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Inicia nova activiy para conexão
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
        //--------------
    }


    public void getDTC() {
        switch(countDTC) {
            case 0:
                myOBDService.getTroubleCodes();
                break;
            case 1:
                myOBDService.getPendingCodes();
                break;
            case 2:
                myOBDService.getPermanetCodes();
               break;
            case 3:
                showDTCcodes();
                countDTC = 10;
                //-------------//
                Intent serviceIntent = new Intent(ServiceActivity.this, OBDReaderService.class);
                serviceIntent.putExtra("BT_DEVICE", deviceBT);
                serviceIntent.putExtra("CAR_INFO", carInfo);
                serviceIntent.putExtra("CONECTION_TYPE", connectionType);
                myOBDService.stopOBDService();
                startService(serviceIntent);
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(OBDReaderService.MY_ACTION);
                registerReceiver(myReceiver, intentFilter);
                //-------------//
                break;
            default:
                break;
        }
    }


    @Override
    protected void onStop() {

        //In Case receiver is already not registed
        try {
            unregisterReceiver(myReceiver);
        } catch(IllegalArgumentException e) {

            e.printStackTrace();
        }
        super.onStop();
    }


    //Stop/Start read log
    public void startStopLog(View view) {

        Intent serviceIntent = new Intent(ServiceActivity.this, OBDReaderService.class);

        // Stop logging
        if (logginButton.getText().equals("STOP LOG")) {
            stopService(serviceIntent);
            //In Case receiver is already not registed
            try {

                unregisterReceiver(myReceiver);
            } catch(IllegalArgumentException e) {

                e.printStackTrace();
            }
            logginButton.setText("START LOG");
            setLogOFF();
        }
        else if(logginButton.getText().equals("START LOG")) {
            //Start service reader
            serviceIntent.putExtra("BT_DEVICE", deviceBT);
            serviceIntent.putExtra("CAR_INFO", carInfo);
            serviceIntent.putExtra("CONECTION_TYPE", connectionType);
            startService(serviceIntent);
            //Start listerner
            myReceiver = new MyReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(OBDReaderService.MY_ACTION);
            registerReceiver(myReceiver, intentFilter);
            logginButton.setText("STOP LOG");
            setLogON();
        }
    }

    public void backToMainActivity(View view) {
        Intent intent = new Intent(ServiceActivity.this, MainActivity.class);
        if (isLogOn()) {
            Intent serviceIntent = new Intent(ServiceActivity.this, OBDReaderService.class);
            stopService(serviceIntent);
            //In Case receiver is already not registed
            try {
                unregisterReceiver(myReceiver);
            } catch(IllegalArgumentException e) {

                e.printStackTrace();
            }
        }
        //Save the setting on shared preferences
        myOBDService.stopOBDService();
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CAR_INFO,"");
        editor.apply();
        startActivity(intent);
    }


    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {

            ArrayList<String> listOfData;
            listOfData = arg1.getStringArrayListExtra("DATA");

            updateValue(0, listOfData.get(0));
            updateValue(1, listOfData.get(1));
            updateValue(2, listOfData.get(2));
            updateValue(3, listOfData.get(3));
        }

    }


    //custom ArrayAdapter
    class ItemPIDArrayAdapter extends ArrayAdapter<ItemPID> {

        private Context context;
        private List<ItemPID> pidProperties;

        //constructor, call on creation
        public ItemPIDArrayAdapter(Context context, int resource, ArrayList<ItemPID> objects) {
            super(context, resource, objects);

            this.context = context;
            this.pidProperties = objects;
        }

        //called when rendering the list
        public View getView(int position, View convertView, ViewGroup parent) {

            //get the property we are displaying
            ItemPID item = pidProperties.get(position);

            //get the inflater and inflate the XML layout for each item
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.activity_listview, null);

            TextView def =  (TextView) view.findViewById(R.id.textDef);
            TextView val =  (TextView) view.findViewById(R.id.textVal);
            TextView unit = (TextView) view.findViewById(R.id.textUnit);

            def.setText(item.getDef());
            val.setText(item.getValue());
            unit.setText(item.getUnites());

            return view;
        }
    }


    public void createInfoFIle() throws FileNotFoundException {
        File file = new File(this.getFilesDir() + "/" + carInfo + "/info.txt");
        //If already exist dont need to write over again
        if (file.exists())
            return;
        //Create info file for the car
        PrintWriter printWriter = new PrintWriter(file);
        printWriter.println (carInfo);
        printWriter.println (myOBDService.getService01());
        printWriter.println (myOBDService.getService09());
        printWriter.println (myOBDService.getVIN());
        printWriter.println ("LOG:off");
        printWriter.close ();

    }

    public void setLogON() {
        try {
            File fileToChange = new File(this.getFilesDir() + "/" + carInfo + "/info.txt");

            // input the file content to the StringBuffer "input"
            BufferedReader file = new BufferedReader(new FileReader(fileToChange));
            StringBuffer inputBuffer = new StringBuffer();
            String line;

            while ((line = file.readLine()) != null) {
                inputBuffer.append(line);
                inputBuffer.append('\n');
            }
            file.close();
            String inputStr = inputBuffer.toString();


            // logic to replace lines in the string (could use regex here to be generic)
            inputStr = inputStr.replace("LOG:off","LOG:on");



            // write the new string with the replaced line OVER the same file
            FileOutputStream fileOut = new FileOutputStream( fileToChange );
            fileOut.write(inputStr.getBytes());
            fileOut.close();

        } catch (Exception e) {

        }
    }

    public void setLogOFF() {
        try {
            File fileToChange = new File(this.getFilesDir() + "/" + carInfo + "/info.txt");

            // input the file content to the StringBuffer "input"
            BufferedReader file = new BufferedReader(new FileReader(fileToChange));
            StringBuffer inputBuffer = new StringBuffer();
            String line;

            while ((line = file.readLine()) != null) {
                inputBuffer.append(line);
                inputBuffer.append('\n');
            }
            file.close();
            String inputStr = inputBuffer.toString();


            // logic to replace lines in the string (could use regex here to be generic)
            inputStr = inputStr.replace("LOG:on","LOG:off");


            // write the new string with the replaced line OVER the same file
            FileOutputStream fileOut = new FileOutputStream( fileToChange );
            fileOut.write(inputStr.getBytes());
            fileOut.close();

        } catch (Exception e) {

        }
    }

    private  final android.os.Handler uiHandler = new android.os.Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            Bundle bundle = msg.getData();
            String message = "";
            switch (msg.what) {
                case OBD_COMMAND:
                    if ( bundle != null) {
                        message = bundle.getString("data");
                        String parts[] = message.split(" ");
                        if (parts[0].contains("CAN") || parts[0].contains("NO")) {
                            if (countDTC != 10) {
                                countDTC += 1;
                            }
                        }
                        else if(parts[0].contains("43")) {
                            if (parts.length > 1)
                                troubleCodes = parts[1];
                            else
                                troubleCodes = " ";
                            countDTC = 1;
                        }
                        else if(parts[0].contains("47")) {
                            if (parts.length > 1)
                                pendingCodes = parts[1];
                            else
                                pendingCodes = " ";
                            countDTC = 2;
                        }
                        else if(parts[0].contains("4A")) {
                            if (parts.length > 1)
                                permanentCodes = parts[1];
                            else
                                permanentCodes = " ";
                            countDTC = 3;
                        }

                        if(countDTC < 8)
                            getDTC();
                    }
                    break;
                case PROTOCOL_OBD:
                    if ( bundle != null)
                        message  = bundle.getString("data");
                    obd_protocol = message;
                    status.setText("pronto");
                    //----Create Info file----//
                    try {
                        createInfoFIle();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    //-- Start Loop Service read data--//
                    Intent serviceIntent = new Intent(ServiceActivity.this, OBDReaderService.class);
                    serviceIntent.putExtra("BT_DEVICE", deviceBT);
                    serviceIntent.putExtra("CAR_INFO", carInfo);
                    serviceIntent.putExtra("CONECTION_TYPE", connectionType);
                    myOBDService.stopOBDService();
                    startService(serviceIntent);
                    setLogON();
                    //--------------------------//
                    break;
                case BT_THREAD_READY:
                    getDTC();
                    break;
                default:
                    break;
            }
        }
    };
}
