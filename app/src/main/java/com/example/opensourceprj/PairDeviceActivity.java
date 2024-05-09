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
import java.util.Set;
import java.util.UUID;

public class PairDeviceActivity extends AppCompatActivity {
    private final String TAG = "PairDeviceActivity";
    private final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FC");

    private BluetoothAdapter blead;
    private BluetoothSocket btSocket = null;
    private ConnectedThread connectedThread;

    private Set<BluetoothDevice> pairedDevices;
    private ArrayAdapter<String> btArrayAdapter;
    private ArrayList<String> deviceAddressArray;

    private TextView text_view_status;
    private Button btn_paired, btn_send;
    private ListView list_view_paired_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair_device);

        text_view_status = findViewById(R.id.Text_view_status);
        list_view_paired_adapter = findViewById(R.id.List_view_paired_adapter);

        btArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        deviceAddressArray = new ArrayList<>();
        list_view_paired_adapter.setAdapter(btArrayAdapter);

        list_view_paired_adapter.setOnItemClickListener(new myOnItemClickListener());
    }

    public void onClickButtonPaired(View view) {
        btArrayAdapter.clear();
        if(deviceAddressArray != null && !deviceAddressArray.isEmpty()) deviceAddressArray.clear();

        pairedDevices = blead.getBondedDevices();
        if(pairedDevices.size() > 0) {
            for(BluetoothDevice device: pairedDevices) {
                String deviceName = device.getName();
                String deviceMacAddr = device.getAddress();
                btArrayAdapter.add(deviceName);
                deviceAddressArray.add(deviceMacAddr);
            }
        }
    }

    public void onClickButtonSend(View view) {
        if(connectedThread != null) connectedThread.write("connected");
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

            BluetoothDevice device = blead.getRemoteDevice(address);

            try {
                btSocket = createBluetoothSocket(device);
            } catch (IOException e) {
                text_view_status.setText("connection failed!");
                e.printStackTrace();
            }

            try {
                btSocket.connect();
            } catch (IOException e) {
                try {
                    btSocket.close();
                } catch (IOException ex) {
                    Log.e(TAG, "unable to close() socket during connection failure", ex);
                }
            }
            connectedThread = new ConnectedThread(btSocket);
            text_view_status.setText("connected to" + name);
            connectedThread.start();
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
}
