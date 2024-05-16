package com.example.opensourceprj;

import android.bluetooth.BluetoothSocket;
import android.os.Environment;
import android.os.SystemClock;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ConnectedThread extends Thread{
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    private static String connectedDeviceAddr = null;
    private ArrayList<BLEdata_storage> datalist = new ArrayList<>();

    public ConnectedThread(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        connectedDeviceAddr = socket.getRemoteDevice().getAddress();

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

        try{
            File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/store_test.csv");
            if (!file.exists()) {
                file.createNewFile();
            }

            while (mmSocket.isConnected()) {
                bytes = mmInStream.available();

                if(bytes != 0) {
                    buffer = new byte[1024];
                    SystemClock.sleep(100);
                    bytes = mmInStream.available();
                    bytes = mmInStream.read(buffer, 0, bytes);
                    /*
                    String sensingData = new String(buffer, StandardCharsets.UTF_8);

                    FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
                    BufferedWriter bw = new BufferedWriter(fw);

                    String[] data = sensingData.split(",", 5);
                    String sensorTeam = data[0];
                    String macAddr = data[1];
                    int OTP = Integer.valueOf(data[2]);
                    String pmData = data[3];
                    long sensingTime = Integer.valueOf(data[4]);

                    BLEdata_storage bleData = new BLEdata_storage(sensorTeam, macAddr, sensingTime, Integer.valueOf(OTP), pmData);
                    datalist.add(bleData);

                    bw.write(String.valueOf(datalist.get(datalist.size() - 1).get_sensor_team()));
                    bw.write("," + String.valueOf(datalist.get(datalist.size() - 1).get_mac_addr()));
                    bw.write("," + String.valueOf(datalist.get(datalist.size() - 1).get_otp()));
                    bw.write("," + String.valueOf(datalist.get(datalist.size() - 1).get_pm_data()));
                    bw.write("," + String.valueOf(datalist.get(datalist.size() - 1).get_time()));

                    bw.newLine();

                    bw.close();
                    fw.close();
                     */
                }
            }
            cancel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String input) {
        byte[] bytes = input.getBytes();
        try {
            mmOutStream.write(bytes);
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
}
