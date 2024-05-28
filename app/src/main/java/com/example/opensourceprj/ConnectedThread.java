package com.example.opensourceprj;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Environment;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ConnectedThread extends Thread{
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private Retrofit retrofit;
    private comm_data service;

    private Context context;
    private TextView text_view_data;

    private String id;  // 핸드폰 고유 id
    private String location = null;    // 현재 위치
    private static final String mode = "connection";
    private static final String sensorTeam = "2jo";
    private static final String TYPE_DUST_SENSOR = "dustsensor";
    private static final String TYPE_AIR_SENSOR = "airquality";

    private static String connectedDeviceAddr = null;
    private ArrayList<BLEdata_storage> datalist = new ArrayList<>();

    public ConnectedThread(Context context, View view, String id, String location, BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        connectedDeviceAddr = socket.getRemoteDevice().getAddress();

        this.context = context;
        text_view_data = (TextView) view;
        this.id = id;
        this.location = location;

        Gson gson = new GsonBuilder().setLenient().create();
        retrofit = new Retrofit.Builder()
                .baseUrl("http://203.255.81.72:10021/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        service = retrofit.create(comm_data.class);

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        int bytes;

        try {
            String systemSec = String.valueOf(System.currentTimeMillis() / 1000);
            byte[] systemSecBytes = systemSec.getBytes();
            mmOutStream.write(systemSecBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try{
            File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/scan_data.csv");
            if (!file.exists()) {
                file.createNewFile();
            }

            datalist = new ArrayList<>();
            int count = 0;
            while (mmSocket.isConnected() && count < 60) {
                bytes = mmInStream.available();

                if(bytes != 0) {
                    buffer = new byte[1024];
                    SystemClock.sleep(100);
                    bytes = mmInStream.available();
                    bytes = mmInStream.read(buffer, 0, bytes);
                    String sensingData = new String(buffer, StandardCharsets.UTF_8);

                    String sensorType = getSensorType(sensingData);

                    String[] data = sensingData.split("!", 4);
                    String sensorData = data[0];
                    String sensingTime = data[1];
                    String OTP = data[2];
                    String macAddr = data[3].substring(0, 17);

                    BLEdata_storage bleData = new BLEdata_storage(sensorType, sensorTeam, mode, macAddr, sensingTime, OTP, location, sensorData);
                    datalist.add(bleData);

                    FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
                    BufferedWriter bw = new BufferedWriter(fw);

                    bw.write(datalist.get(datalist.size() - 1).get_sensor_type());
                    bw.write("," + datalist.get(datalist.size() - 1).get_sensor_team());
                    bw.write("," + datalist.get(datalist.size() - 1).get_mode());
                    bw.write("," + datalist.get(datalist.size() - 1).get_mac_addr());
                    bw.write("," + datalist.get(datalist.size() - 1).get_otp());
                    bw.write("," + datalist.get(datalist.size() - 1).get_key());
                    bw.write("," + datalist.get(datalist.size() - 1).get_sensor_data());
                    bw.write("," + datalist.get(datalist.size() - 1).get_time());

                    bw.newLine();

                    bw.close();
                    fw.close();

                    FileReader fr = new FileReader(file.getAbsoluteFile());
                    BufferedReader br = new BufferedReader(fr);

                    String line = datalist.get(datalist.size() - 1).get_sensor_type() +
                            "," + datalist.get(datalist.size() - 1).get_sensor_team() +
                            "," + datalist.get(datalist.size() - 1).get_mode() +
                            "," + datalist.get(datalist.size() - 1).get_mac_addr() +
                            "," + datalist.get(datalist.size() - 1).get_otp() +
                            "," + datalist.get(datalist.size() - 1).get_key() +
                            "," + datalist.get(datalist.size() - 1).get_sensor_data() +
                            "," + datalist.get(datalist.size() - 1).get_time();
                    text_view_data.append(line + "\n");

                    br.close();
                    fr.close();

                    count++;
                }
            }
            cancel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        try {
            mmSocket.close();
            connectedDeviceAddr = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getConnectedDeviceAddr() {
        return connectedDeviceAddr;
    }

    public String getSensorType(String data) {
        if(data.contains("/")) return TYPE_DUST_SENSOR;
        else return TYPE_AIR_SENSOR;
    }
}
