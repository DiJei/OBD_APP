package com.zul.tests.obdzulbeta;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.zul.tests.obdzulbeta.OBDService.OBDService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import android.os.Handler;


public class ServiceActivity extends AppCompatActivity {
    double cons_ant = 0;
    double amount_fuel;
    int count = 0;
    String obd_protocol = "";
    String carInfo = "";
    String FILENAME = "listCars";
    TextView status = null;
    OBDService myOBDService  = null;
    private static final int OBD_COMMAND = 0;
    private static final int PROTOCOL_OBD = 5;
    String connectionType = "";
    String deviceBT = "";
    String tempCool, RPM, battery, alternator, MAP, IAT, speed = "0";
    String pendingCodes, troubleCodes,  permanentCodes = " ";
    ArrayList<ItemPID> dataList = new ArrayList<>();
    ListView pidListView = null;
    Handler handler;
    int countDTC;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        countDTC = 10;
        amount_fuel = 0;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);
        Bundle extras = getIntent().getExtras();
        carInfo = extras.getString("CAR_INFO");
        connectionType = extras.getString("CONECTION_TYPE");
        deviceBT = extras.getString("BT_DEVICE");
        status = (TextView) findViewById(R.id.statusText);
        pidListView = (ListView)findViewById(R.id.listOfPIDs);
        if (connectionType.equals("WIFI"))
            myOBDService  = new OBDService(uiHandler, "192.168.0.10", 35000);
        else if (connectionType.equals("BT")) {
            String[] parts = deviceBT.split(" ");
            myOBDService = new OBDService(uiHandler, parts[1]);
        }
        myOBDService.configOBD();
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
                    handler.removeCallbacksAndMessages(null);
                    count = 10;
                    countDTC = 0;
                    getDTC();
                }
            }
        });


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
                            else
                                count +=1;
                        }
                        else if (parts[0].contains("41") || parts[0].contains("ATRV")) {
                            if(parts[0].contains("05")) {
                                count = 1;
                                tempCool = parts[1];
                            }
                            else if(parts[0].contains("0C")) {
                                count = 2;
                                RPM = parts[1];
                            }
                            else if(parts[0].contains("0D")) {
                                count = 4;
                                speed = parts[1];
                            }
                            else if(parts[0].contains("0B")) {
                                count = 5;
                                MAP = parts[1];
                            }
                            else if(parts[0].contains("0F")) {
                                count = 6;
                                IAT = parts[1];
                            }
                            else if(parts[0].contains("ATRV")) {
                                count = 3;
                                if (!RPM.equals("")) {
                                    if (Integer.parseInt(RPM) > 1000) {
                                        alternator = parts[1];
                                    } else if (Integer.parseInt(RPM) < 100) {
                                        battery = parts[1];
                                    }
                                }
                            }
                        }
                        else if(parts[0].contains("43")) {
                            if (parts.length > 1)
                                troubleCodes = parts[1];
                            else
                                troubleCodes = "";
                            countDTC = 1;
                        }
                        else if(parts[0].contains("47")) {
                            if (parts.length > 1)
                                pendingCodes = parts[1];
                            else
                                pendingCodes = "";
                            countDTC = 2;
                        }
                        else if(parts[0].contains("4A")) {
                            if (parts.length > 1)
                                permanentCodes = parts[1];
                            else
                                permanentCodes = "";
                            countDTC = 3;
                        }

                        getData();
                        getDTC();
                    }
                    break;
                case PROTOCOL_OBD:
                    if ( bundle != null)
                        message  = bundle.getString("data");
                    obd_protocol = message;
                    status.setText("pronto");

                    //-- Start Loop  read data--//
                    handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getData();
                            handler.postDelayed(this, 1000);
                        }
                    }, 1000);
                    //--------------------------//
                    break;
                default:
                    break;
            }
        }
    };


    //Calculte fuel consumption on delta 600 sec
    public double calculateFuelConsumption(int rpm, int airTemp, int pressure) {
        double K = 28.98/8.314;
        if (airTemp == 0)
            airTemp = 1;
        float IMAP  = (rpm * pressure)/ (airTemp * 2);
        double MAP_C = (IMAP/60) * (80/10) * 1.6 * K;
        double cons = MAP_C /(14.7*720);
        double  reuslt =  (cons - cons_ant)*0.6;
        cons_ant = cons;
        return reuslt;
    }

    public void getData() {
        if (countDTC > 9)
        switch (count) {
            case 0:
                myOBDService.getCoolTemp();
                break;
            case 1:
                myOBDService.getRPM();
                break;
            case 2:
                myOBDService.getAdapterVoltage();
                break;
            case 3:
                myOBDService.getSpeed();
                break;
            case 4:
                myOBDService.getMAP();
                break;
            case 5:
                myOBDService.getIAT();
                break;
            case 6:
            case 7:
                //UPDATE LIST VIEW
                updateValue(0, tempCool);
                updateValue(1,battery);
                updateValue(2,alternator);
                //--Calculate instant fuel Consumption---//
                amount_fuel += calculateFuelConsumption(Integer.parseInt(RPM),Integer.parseInt(IAT),Integer.parseInt(MAP));
                updateValue(3, String.format("%.2f",amount_fuel) );
                //----------------
                count = 0;
                break;
             default:
                 break;
        }
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
            case 4:
            case 5:
                showDTCcodes();
                countDTC = 10;
                count = 0;
                //-- Start Loop  read data--//
                handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getData();
                        handler.postDelayed(this, 1000);
                    }
                }, 1000);
                //--------------------------//
                break;
            default:
                break;
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

}
