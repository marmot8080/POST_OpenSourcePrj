package com.example.opensourceprj;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class PairDeviceActivity extends AppCompatActivity {
    private final String TAG = "PairDeviceActivity";
    private String androidID;  // 핸드폰 고유 id
    private String location = null;
    private static final String TYPE_DUST_SENSOR = "dustsensor";
    private static final String TYPE_AIR_SENSOR = "airquality";
    private final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String FILE_NAME = "/scan_data.csv";

    private BluetoothAdapter blead;
    private Retrofit retrofit;
    private comm_data service;
    private static ArrayList<BLEdata_storage> datalist = new ArrayList<>();
    private BluetoothSocket btSocket = null;
    private ConnectedThread connectedThread = null;
    private CustomDialog customDialog;

    private Set<BluetoothDevice> pairedDevices;
    private ArrayAdapter<String> btArrayAdapter;
    private ArrayList<String> deviceAddressArray;

    private TextView text_view_status, text_view_data;
    private Button btn_back, btn_delete_all, btn_delete_latest_value;
    private ListView list_view_paired_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair_device);

        try { // 애플리케이션 시작 시 파일을 읽어 TextView 설정
            File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + FILE_NAME);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileReader fr = new FileReader(file.getAbsoluteFile());
            BufferedReader br = new BufferedReader(fr);

            text_view_data = findViewById(R.id.Text_view_data);
            text_view_data.setText("");
            text_view_data.setMovementMethod(new ScrollingMovementMethod());

            String line;
            while ((line = br.readLine()) != null) {
                text_view_data.setText(text_view_data.getText() + line + "\n");
            }

            fr.close();
            br.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Gson gson = new GsonBuilder().setLenient().create();
        retrofit = new Retrofit.Builder()
                .baseUrl("http://203.255.81.72:10021/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        service = retrofit.create(comm_data.class);

        list_view_paired_adapter = findViewById(R.id.List_view_paired_adapter);
        list_view_paired_adapter.setVisibility(View.GONE);

        btn_delete_all = findViewById(R.id.Btn_delete_all);
        btn_delete_latest_value = findViewById(R.id.Btn_delete_latest_value);
        btn_delete_all.setVisibility(View.GONE);
        btn_delete_latest_value.setVisibility(View.GONE);

        blead = BluetoothAdapter.getDefaultAdapter();

        androidID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

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

    public void onTogglePaired(View v) {
        boolean on = ((ToggleButton) v).isChecked();

        if (on) {
            list_view_paired_adapter = findViewById(R.id.List_view_paired_adapter);
            list_view_paired_adapter.setVisibility(View.VISIBLE);

            btArrayAdapter.clear();
            if(deviceAddressArray != null && !deviceAddressArray.isEmpty()) deviceAddressArray.clear();

            if(connectedThread == null || connectedThread.getConnectedDeviceAddr() == null) text_view_status.setText("");

            pairedDevices = blead.getBondedDevices();
            if(pairedDevices.size() > 0) {
                for(BluetoothDevice device: pairedDevices) {
                    String deviceMacAddr = device.getAddress();
                    String deviceName = checkRaspPiAddr(deviceMacAddr);

                    if(checkRaspPiAddr(deviceMacAddr) != null) {
                        btArrayAdapter.add(deviceName);
                        deviceAddressArray.add(deviceMacAddr);
                    }
                }
            }
        } else {
            list_view_paired_adapter = findViewById(R.id.List_view_paired_adapter);
            list_view_paired_adapter.setVisibility(View.GONE);
        }
    }

    public void onLocation(View v) {
        String wifiData = NetworkManager.getWifiData(PairDeviceActivity.this);

        if (wifiData != null) {
            comm_data service = retrofit.create(comm_data.class);

            Call<String> call = null;
            call = service.location(wifiData);

            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    customDialog = new CustomDialog(PairDeviceActivity.this, "현재 위치를 " + response.body().toString() + "로 저장하시겠습니까?", "아니오", "예");
                    customDialog.setDialogListener(new CustomDialog.CustomDialogInterface() {

                        @Override
                        public void cancelClicked() {
                            location = response.body().toString();
                        }

                        @Override
                        public void acceptClicked() {
                            location = null;
                        }
                    });
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    location = null;
                }
            });
        }
        if(location == null){
            customDialog = new CustomDialog(PairDeviceActivity.this, "현재 위치를 읽어오지 못했습니다.\n임시 위치로 2-1을 설정하시겠습니까?", "아니오", "예");
            customDialog.setDialogListener(new CustomDialog.CustomDialogInterface() {

                @Override
                public void cancelClicked() {
                    location = null;
                }

                @Override
                public void acceptClicked() {
                    location = "2-1";
                }
            });
            customDialog.show();
        }
    }

    public void onSend(View view) {
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + FILE_NAME);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileReader fr = new FileReader(file.getAbsoluteFile());
            BufferedReader br = new BufferedReader(fr);

            // 파일이 비어있으면 토스트 메시지 출력
            if (br.readLine() == "") {
                br.close();
                fr.close();
                Toast.makeText(PairDeviceActivity.this, "파일이 비어있습니다.", Toast.LENGTH_SHORT).show();
            } else {
                br.close();
                fr.close();

                customDialog = new CustomDialog(PairDeviceActivity.this,
                        "저장된 데이터를 서버에 전송하시겠습니까?",
                        "취소",
                        "전송");
                customDialog.setDialogListener(new CustomDialog.CustomDialogInterface() {
                    @Override
                    public void cancelClicked() {

                    }

                    @Override
                    public void acceptClicked() {
                        if (NetworkManager.getConnectivityStatus(PairDeviceActivity.this) != NetworkManager.NOT_CONNECTED) {
                            try {
                                FileReader fr = new FileReader(file.getAbsoluteFile());
                                BufferedReader br = new BufferedReader(fr);

                                datalist = new ArrayList<>();

                                String line;
                                while ((line = br.readLine()) != null) {
                                    String[] data = line.split(",", 8);
                                    String sensorType = data[0];
                                    String sensorTeam = data[1];
                                    String collectMode = data[2];
                                    String macAddr = data[3];
                                    String OTP = data[4];
                                    String key = data[5];
                                    String sensorData = data[6];
                                    String sensingTime = data[7];

                                    Call<String> call;
                                    if(sensorType.equals(TYPE_DUST_SENSOR)) call = service.dust_sensing(sensorTeam, collectMode, macAddr, androidID, sensingTime, OTP, key, sensorData);
                                    else call = service.air_sensing(sensorTeam, collectMode, macAddr, androidID, sensingTime, OTP, key, sensorData);

                                    call.enqueue(new Callback<String>() {
                                        @Override
                                        public void onResponse(Call<String> call, Response<String> response) {
                                            Log.d("ServerCommunicationSuccess", response.body().toString());
                                        }

                                        @Override
                                        public void onFailure(Call<String> call, Throwable t) {
                                            // 서버 전송에 실패한 데이터들은 datalist에 저장
                                            datalist.add(new BLEdata_storage(sensorType, sensorTeam, collectMode, macAddr, sensingTime, OTP, key, sensorData));
                                            Log.d("ServerCommunicationFail", "failed to communicate with server", t);
                                        }
                                    });

                                    Thread.sleep(500);
                                }

                                FileWriter fw = new FileWriter(file.getAbsoluteFile(), false);
                                BufferedWriter bw = new BufferedWriter(fw);

                                bw.write("");
                                // 서버 전송에 실패한 datalist를 csv파일에 다시 저장
                                if (!datalist.isEmpty()) {
                                    for (int i = 0; i < datalist.size(); i++) {
                                        bw.write(String.valueOf(datalist.get(i).get_sensor_type()));
                                        bw.write("," + datalist.get(i).get_sensor_team());
                                        bw.write("," + datalist.get(i).get_mode());
                                        bw.write("," + datalist.get(i).get_mac_addr());
                                        bw.write("," + datalist.get(i).get_otp());
                                        bw.write("," + datalist.get(i).get_key());
                                        bw.write("," + datalist.get(i).get_sensor_data());
                                        bw.write("," + datalist.get(i).get_time());

                                        bw.newLine();
                                    }
                                }

                                text_view_data = findViewById(R.id.Text_view_data);
                                text_view_data.setText("");
                                br.close();
                                br = new BufferedReader(new FileReader(file.getAbsoluteFile()));
                                while ((line = br.readLine()) != null) {
                                    text_view_data.setText(text_view_data.getText() + line + "\n");
                                }

                                bw.close();
                                br.close();
                                fw.close();
                                fr.close();
                            } catch (FileNotFoundException e) {
                                throw new RuntimeException(e);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            Toast.makeText(PairDeviceActivity.this, "NETWORK NOT CONNECTED", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                customDialog.show();
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

            if(connectedThread == null || connectedThread.getConnectedDeviceAddr() == null) {
                BluetoothDevice device = blead.getRemoteDevice(address);

                try {
                    btSocket = createBluetoothSocket(device);
                } catch (IOException e) {
                    text_view_status.setText("connection failed!");
                    e.printStackTrace();
                }

                try {
                    if(location != null) {
                        btSocket.connect();

                        connectedThread = new ConnectedThread(PairDeviceActivity.this, text_view_data, androidID, location, btSocket);
                        text_view_status.setText("connected to " + name);
                        connectedThread.start();
                    } else Toast.makeText(PairDeviceActivity.this, "위치 정보를 확인해주세요.", Toast.LENGTH_SHORT).show();
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
                            if(location != null) {
                                btSocket.connect();

                                connectedThread = new ConnectedThread(PairDeviceActivity.this, text_view_data, androidID, location, btSocket);
                                text_view_status.setText("connected to " + name);
                                connectedThread.start();
                            } else Toast.makeText(PairDeviceActivity.this, "위치 정보를 확인해주세요.", Toast.LENGTH_SHORT).show();
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

    public void onToggleDelete(View v) {
        boolean on = ((ToggleButton) v).isChecked();
        btn_delete_all = findViewById(R.id.Btn_delete_all);
        btn_delete_latest_value = findViewById(R.id.Btn_delete_latest_value);

        if (on) {
            btn_delete_all.setVisibility(View.VISIBLE);
            btn_delete_latest_value.setVisibility(View.VISIBLE);
        } else {
            btn_delete_all.setVisibility(View.GONE);
            btn_delete_latest_value.setVisibility(View.GONE);
        }
    }

    public void onDeleteAll(View v) {
        customDialog = new CustomDialog(PairDeviceActivity.this, "파일 내용을 전부 삭제하시겠습니까?", "취소", "삭제");
        customDialog.setDialogListener(new CustomDialog.CustomDialogInterface() {
            @Override
            public void cancelClicked() {

            }

            @Override
            public void acceptClicked() {
                try {
                    File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + FILE_NAME);
                    if (!file.exists()) {
                        file.createNewFile();
                    }

                    FileWriter fw = new FileWriter(file.getAbsoluteFile(), false);
                    BufferedWriter bw = new BufferedWriter(fw);

                    bw.write("");

                    text_view_data = findViewById(R.id.Text_view_data);
                    text_view_data.setText("");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        customDialog.show();
    }

    public void onDeleteLatest(View v) {
        customDialog = new CustomDialog(PairDeviceActivity.this, "최근 데이터를 삭제하시겠습니까?", "취소", "삭제");
        customDialog.setDialogListener(new CustomDialog.CustomDialogInterface() {
            @Override
            public void cancelClicked() {

            }

            @Override
            public void acceptClicked() {
                try {
                    File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + FILE_NAME);
                    if (!file.exists()) {
                        file.createNewFile();
                    }

                    FileReader fr = new FileReader(file.getAbsoluteFile());
                    BufferedReader br = new BufferedReader(fr);

                    datalist = new ArrayList<>();

                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] data = line.split(",", 8);
                        String sensorType = data[0];
                        String sensorTeam = data[1];
                        String collectMode = data[2];
                        String macAddr = data[3];
                        String OTP = data[4];
                        String key = data[5];
                        String sensorData = data[6];
                        String sensingTime = data[7];

                        datalist.add(new BLEdata_storage(sensorType, sensorTeam, collectMode, macAddr, sensingTime, OTP, key, sensorData));
                    }
                    br.close();
                    fr.close();

                    FileWriter fw = new FileWriter(file.getAbsoluteFile(), false);
                    BufferedWriter bw = new BufferedWriter(fw);
                    if (!datalist.isEmpty()) {
                        for (int i = 0; i < datalist.size() - 1; i++) {
                            bw.write(String.valueOf(datalist.get(i).get_sensor_type()));
                            bw.write("," + datalist.get(i).get_sensor_team());
                            bw.write("," + datalist.get(i).get_mode());
                            bw.write("," + datalist.get(i).get_mac_addr());
                            bw.write("," + datalist.get(i).get_otp());
                            bw.write("," + datalist.get(i).get_key());
                            bw.write("," + datalist.get(i).get_sensor_data());
                            bw.write("," + datalist.get(i).get_time());

                            bw.newLine();
                        }
                    }
                    bw.close();
                    fw.close();

                    text_view_data = findViewById(R.id.Text_view_data);
                    text_view_data.setText("");
                    fr = new FileReader(file.getAbsoluteFile());
                    br = new BufferedReader(fr);
                    while ((line = br.readLine()) != null) {
                        text_view_data.setText(text_view_data.getText() + line + "\n");
                    }

                    br.close();
                    fr.close();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        customDialog.show();
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
        String sensor = "";

        switch(raspPiAddr){
            case "D8:3A:DD:79:8F:97":
                sensor = "dust sensor 1";
                break;
            case "D8:3A:DD:79:8F:B9":
                sensor = "dust sensor 2";
                break;
            case "D8:3A:DD:79:8F:54":
                sensor = "dust sensor 3";
                break;
            case "D8:3A:DD:79:8F:80":
                sensor = "dust sensor 4";
                break;
            case "D8:3A:DD:C1:89:70":
                sensor = "air quality sensor 1";
                break;
            case "air quality sensor 2":
                sensor = "air quality sensor 2";
                break;
            case "air quality sensor 3":
                sensor = "air quality sensor 3";
                break;
            case "air quality sensor 4":
                sensor = "air quality sensor 4";
                break;
            default:
                break;
        }

        return sensor;
    }
}
