package com.example.opensourceprj;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PairDeviceActivity extends AppCompatActivity {
    private final String TAG = "PairDeviceActivity";
    private final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String[] raspberryPiAddr_1 = {
            "D8:3A:DD:42:AC:7F",
            "D8:3A:DD:42:AC:64",
            "B8:27:EB:DA:F2:5B",
            "B8:27:EB:0C:F3:83"
    }; // 1조 라즈베리파이 Mac address
    private static final String[] raspberryPiAddr_2 = {
            "D8:3A:DD:79:8F:97",
            "D8:3A:DD:79:8F:B9",
            "D8:3A:DD:79:8F:54",
            "D8:3A:DD:79:8F:80"
    }; // 2조 라즈베리파이 Mac address
    private static final String[] raspberryPiAddr_3 = {
            "D8:3A:DD:79:8E:D9",
            "D8:3A:DD:42:AC:9A",
            "D8:3A:DD:42:AB:FB",
            "D8:3A:DD:79:8E:9B"
    }; // 3조 라즈베리파이 Mac address
    private static final String[] raspberryPiAddr_4 = {
            "D8:3A:DD:78:A7:1A",
            "D8:3A:DD:79:8E:BF",
            "D8:3A:DD:79:8E:92",
            "D8:3A:DD:79:8F:59"
    }; // 4조 라즈베리파이 Mac address
    private static final String[] raspberryPiAddr_5 = {
            "B8:27:EB:47:8D:50",
            "B8:27:EB:D3:40:06",
            "B8:27:EB:E4:D0:FC",
            "B8:27:EB:57:71:7D"
    }; // 5조 라즈베리파이 Mac address

    private static final String[] raspberryPiAddr_ta = {
            "B8:27:EB:7F:E7:58"
    }; // ta 라즈베리파이 Mac address
    private static final List<String> raspberryPiAddrList_1 = new ArrayList<>(Arrays.asList(raspberryPiAddr_1));
    private static final List<String> raspberryPiAddrList_2 = new ArrayList<>(Arrays.asList(raspberryPiAddr_2));
    private static final List<String> raspberryPiAddrList_3 = new ArrayList<>(Arrays.asList(raspberryPiAddr_3));
    private static final List<String> raspberryPiAddrList_4 = new ArrayList<>(Arrays.asList(raspberryPiAddr_4));
    private static final List<String> raspberryPiAddrList_5 = new ArrayList<>(Arrays.asList(raspberryPiAddr_5));
    private static final List<String> raspberryPiAddrList_ta = new ArrayList<>(Arrays.asList(raspberryPiAddr_ta));

    private BluetoothAdapter blead;
    private BluetoothSocket btSocket = null;
    private ConnectedThread connectedThread = null;
    private CustomDialog customDialog;

    private Set<BluetoothDevice> pairedDevices;
    private ArrayAdapter<String> btArrayAdapter;
    private ArrayList<String> deviceAddressArray;

    private TextView text_view_status;
    private Button btn_back;
    private ListView list_view_paired_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair_device);

        blead = BluetoothAdapter.getDefaultAdapter();

        text_view_status = findViewById(R.id.Text_view_status);
        list_view_paired_adapter = findViewById(R.id.List_view_paired_adapter);

        btArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        deviceAddressArray = new ArrayList<>();
        list_view_paired_adapter.setAdapter(btArrayAdapter);

        list_view_paired_adapter.setOnItemClickListener(new myOnItemClickListener());

        btn_back = findViewById(R.id.Btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void onClickButtonPaired(View view) {
        btArrayAdapter.clear();
        if(deviceAddressArray != null && !deviceAddressArray.isEmpty()) deviceAddressArray.clear();

        if(connectedThread == null || connectedThread.getConnectedDeviceAddr() == null) text_view_status.setText("");

        pairedDevices = blead.getBondedDevices();
        if(pairedDevices.size() > 0) {
            for(BluetoothDevice device: pairedDevices) {
                String deviceName = device.getName();
                String deviceMacAddr = device.getAddress();

                if(checkRaspPiAddr(deviceMacAddr) != null) {
                    btArrayAdapter.add(deviceName);
                    deviceAddressArray.add(deviceMacAddr);
                }
            }
        }
    }

    public void onClickButtonSend(View view) {
        if(connectedThread == null || connectedThread.getConnectedDeviceAddr() == null) text_view_status.setText("");

        if(connectedThread != null && connectedThread.getConnectedDeviceAddr() != null) connectedThread.write("connected");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public class myOnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Toast.makeText(getApplicationContext(), btArrayAdapter.getItem(position), Toast.LENGTH_SHORT).show();

            text_view_status.setText("try...");

            final String name = btArrayAdapter.getItem(position);
            final String address = deviceAddressArray.get(position);
            boolean flag = true;

            if(connectedThread == null || connectedThread.getConnectedDeviceAddr() == null) {
                BluetoothDevice device = blead.getRemoteDevice(address);

                try {
                    btSocket = createBluetoothSocket(device);
                } catch (IOException e) {
                    text_view_status.setText("connection failed!");
                    e.printStackTrace();
                }

                try {
                    btSocket.connect();

                    connectedThread = new ConnectedThread(btSocket);
                    text_view_status.setText("connected to " + name);
                    connectedThread.start();
                } catch (IOException e) {
                    try {
                        e.printStackTrace();
                        Toast.makeText(PairDeviceActivity.this, "Unable to connect device", Toast.LENGTH_SHORT).show();
                        text_view_status.setText("connection failed!");
                        btSocket.close();
                    } catch (IOException ex) {
                        Log.e(TAG, "unable to close() socket during connection failure", ex);
                    }
                }
            } else if (address.equals(connectedThread.getConnectedDeviceAddr())) {
                Toast.makeText(PairDeviceActivity.this, "Already Connected", Toast.LENGTH_SHORT).show();
            } else {
                customDialog = new CustomDialog(PairDeviceActivity.this, "현재 기기와의 연결을 끊고 새 기기와 연결하시겠습니까?", "취소", "연결");
                customDialog.setDialogListener(new CustomDialog.CustomDialogInterface() {
                    @Override
                    public void cancelClicked() {

                    }

                    @Override
                    public void acceptClicked() {
                        connectedThread.stop();
                        connectedThread.cancel();

                        BluetoothDevice device = blead.getRemoteDevice(address);

                        try {
                            btSocket = createBluetoothSocket(device);
                        } catch (IOException e) {
                            text_view_status.setText("connection failed!");
                            e.printStackTrace();
                        }

                        try {
                            btSocket.connect();

                            connectedThread = new ConnectedThread(btSocket);
                            text_view_status.setText("connected to" + name);
                            connectedThread.start();
                        } catch (IOException e) {
                            try {
                                Toast.makeText(PairDeviceActivity.this, "Unable to connect device", Toast.LENGTH_SHORT).show();
                                text_view_status.setText("connection failed!");
                                btSocket.close();
                            } catch (IOException ex) {
                                Log.e(TAG, "unable to close() socket during connection failure", ex);
                            }
                        }
                    }
                });

                customDialog.show();
            }
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BT_MODULE_UUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection", e);
        }

        return device.createInsecureRfcommSocketToServiceRecord(BT_MODULE_UUID);
    }

    private String checkRaspPiAddr(String raspPiAddr) {
        String sensorTeam = null;

        if(raspberryPiAddrList_1.contains(raspPiAddr)) sensorTeam = "1jo";
        else if(raspberryPiAddrList_2.contains(raspPiAddr)) sensorTeam = "2jo";
        else if(raspberryPiAddrList_3.contains(raspPiAddr)) sensorTeam = "3jo";
        else if(raspberryPiAddrList_4.contains(raspPiAddr)) sensorTeam = "4jo";
        else if(raspberryPiAddrList_5.contains(raspPiAddr)) sensorTeam = "5jo";
        else if(raspberryPiAddrList_ta.contains(raspPiAddr)) sensorTeam = "ta";

        return sensorTeam;
    }
}
