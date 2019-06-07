package com.zul.tests.obdzulbeta;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import com.zul.tests.obdzulbeta.OBDService.OBDService;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.zul.tests.obdzulbeta.App.CHANNEL_ID;

public class OBDReaderService extends Service {
    final static String MY_ACTION = "OBD_SERVICE";
    private static final int OBD_COMMAND = 0;
    OBDService myOBDService  = null;
    String tempCool, RPM, battery, alternator, MAP, IAT, speed, volt = "0";
    int count = 0;
    int countDTC = 0;
    double amount_fuel;
    double cons_ant;
    String carInfo = "";
    int tryOuts = 0;
    boolean flag_write = true;
    File logFile = null;

    Handler handler = new Handler();
    private Runnable periodicUpdate = new Runnable () {
        @Override
        public void run() {
            handler.postDelayed(periodicUpdate, 1000);
           if (count == 0) {
               count = 1;
               myOBDService.getCoolTemp();
           }
           else if (count > 6) {
               count = 0;
           }
           else {
               tryOuts += 1;
               if (tryOuts > 3) {
                   count = 0;
                   tryOuts = 0;
               }
           }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int starId) {
        Bundle extras =  intent.getExtras();

        carInfo = extras.getString("CAR_INFO");;
        String conectionType = extras.getString("CONECTION_TYPE");
        if (conectionType.equals("BT")) {
            String deviceBT = extras.getString("BT_DEVICE");
            String[] parts = deviceBT.split(" ");
            myOBDService = new OBDService(uiHandler, parts[1], false);
        }
        else if (conectionType.equals("WIFI")) {
            myOBDService  = new OBDService(uiHandler, "192.168.0.10", 35000);
        }
        /*   init values  */
        tempCool = "0";
        RPM = "0";
        battery = "0";
        alternator = "0";
        MAP = "0";
        IAT = "0";
        volt = "0";
        count = 0;
        countDTC = 0;
        amount_fuel = 0;
        cons_ant = 0;

        try {
            logFile = createLogFile();
            updateLogFile("START " + getHour() + "\n");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Intent notificationIntent = new Intent(this, ServiceActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("OBD SERVICE")
                .setContentText(" reading ")
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent)
                .build();



        handler.post(periodicUpdate);
        startForeground(1, notification);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;

    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        myOBDService.stopOBDService();
        updateLogFile("STOP " + getHour() + "\n");
        flag_write = false;
        super.onDestroy();
    }

    public String getHour() {
        Calendar cal = Calendar.getInstance();
        Date date=cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String formattedDate = dateFormat.format(date);
        return formattedDate;
    }


    public File createLogFile() throws FileNotFoundException {
        Date day = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String logName = "log-"+df.format(day)+".txt";
        File file = new File(this.getFilesDir() + "/" + carInfo + "/" + logName);

        if(file.exists())
            return file;
        else {
            //to force the creation of file
            PrintWriter printWriter = new PrintWriter(file);
            printWriter.print("");
            return file;
        }
    }

    public void updateLogFile(String line)  {
        if (!flag_write)
            return;
        try {
            // Open given file in append mode.
            BufferedWriter out = new BufferedWriter(
                    new FileWriter(logFile.getPath(), true));
            out.write(line);
            out.close();
        }
        catch (IOException e) {

        }

    }



    //Calculte fuel consumption on delta 600 sec
    public double calculateFuelConsumption(int rpm, int airTemp, int pressure) {
        double K = 28.98/8.314;
        if (airTemp == 0)
            airTemp = 1;
        float IMAP  = (rpm * pressure)/ (airTemp * 2);
        double MAP_C = (IMAP/60) * (80/10) * 1.6 * K;
        double cons = MAP_C /(14.7*720);

        double  dls =  (cons - cons_ant);
        if (dls < 0)
            dls *= -1;

        double result = amount_fuel + cons_ant + dls/2;

        cons_ant = cons;
        return result;
    }


    public void getData() {
            switch (count) {
                case 2:
                    myOBDService.getRPM();
                    break;
                case 3:
                    myOBDService.getAdapterVoltage();
                    break;
                case 4:
                    myOBDService.getSpeed();
                    break;
                case 5:
                    myOBDService.getMAP();
                    break;
                case 6:
                    myOBDService.getIAT();
                    break;
                case 7:
                    //--Calculate instant fuel Consumption---//
                    amount_fuel += calculateFuelConsumption(Integer.parseInt(RPM), Integer.parseInt(IAT), Integer.parseInt(MAP));
                    //updateValue(3, String.format("%.2f", amount_fuel));1
                    //----------------

                    //Update Log File
                    String line = tempCool + " " + speed + " " + RPM  + " " + MAP + " " + IAT + " " + volt + "\n";
                    updateLogFile(line);
                    //Send data for UI
                    Intent intent = new Intent();
                    intent.setAction(MY_ACTION);
                    ArrayList<String> listOFdata = new ArrayList<>();
                    listOFdata.add(tempCool);
                    listOFdata.add(battery);
                    listOFdata.add(alternator);
                    listOFdata.add( String.format("%.2f", amount_fuel));
                    intent.putStringArrayListExtra("DATA", listOFdata);
                    sendBroadcast(intent);
                    //Reset order of commands
                    count = 0;
                default:
                    break;
            }

    }




    private  final android.os.Handler uiHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
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
                                count = 2;
                                tempCool = parts[1];
                            }
                            else if(parts[0].contains("0C")) {
                                count = 3;
                                RPM = parts[1];
                            }
                            else if(parts[0].contains("0D")) {
                                count = 5;
                                speed = parts[1];
                            }
                            else if(parts[0].contains("0B")) {
                                count = 6;
                                MAP = parts[1];
                            }
                            else if(parts[0].contains("0F")) {
                                count = 7;
                                IAT = parts[1];
                            }
                            else if(parts[0].contains("ATRV")) {
                                count = 4;
                                if (!RPM.equals("")) {
                                    volt = parts[1];
                                    if (Integer.parseInt(RPM) > 1000) {
                                        alternator = parts[1];
                                    } else if (Integer.parseInt(RPM) < 100) {
                                        battery = parts[1];
                                    }
                                }
                            }
                            if (count > 7)
                                count = 7;
                        }
                        if(count < 10)
                            getData();
                    }
                    break;
                default:
                    break;

            }
        }
    };


}
