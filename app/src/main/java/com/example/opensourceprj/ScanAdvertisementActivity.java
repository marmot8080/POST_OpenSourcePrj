package com.example.opensourceprj;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ScanAdvertisementActivity extends AppCompatActivity {
    private Retrofit retrofit;
    private comm_data service;
    private BluetoothAdapter blead;
    private CustomDialog customDialog;
    private static ArrayList<BLEdata_storage> datalist = new ArrayList<>();

    private ToggleButton toggle_btn_scan;
    private Button btn_delete_all, btn_delete_latest_value, btn_back;
    private TextView tv_data, text_1jo_data, text_2jo_data, text_3jo_data, text_4jo_data, text_5jo_data;
    private Switch switch_directly_send;
    private Toast toast;

    private String androidID;  // 핸드폰 고유 id
    private String location = null; // 현재 위치
    private static final String mode = "advertising";
    private static final String TYPE_DUST_SENSOR = "dustsensor";
    private static final String TYPE_AIR_SENSOR = "airquality";
    private static final String server_URL = "http://203.255.81.72:10021/dustsensor/sensingpage/"; // 서버 url
    private static final String FILE_NAME = "/scan_data.csv";
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_advertisement);

        // 객체 생성
        blead = BluetoothAdapter.getDefaultAdapter();
        toast = Toast.makeText(ScanAdvertisementActivity.this, null, Toast.LENGTH_SHORT);

        androidID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        try { // 애플리케이션 시작 시 파일을 읽어 TextView 설정
            File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + FILE_NAME);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileReader fr = new FileReader(file.getAbsoluteFile());
            BufferedReader br = new BufferedReader(fr);

            tv_data = findViewById(R.id.Text_view_data);
            tv_data.setText("");
            tv_data.setMovementMethod(new ScrollingMovementMethod());

            String line;
            while ((line = br.readLine()) != null) {
                tv_data.setText(tv_data.getText() + line + "\n");
            }

            fr.close();
            br.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 서버 데이터 크롤링
        new NetworkTask().execute();

        Gson gson = new GsonBuilder().setLenient().create();
        retrofit = new Retrofit.Builder()
                .baseUrl("http://203.255.81.72:10021/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        service = retrofit.create(comm_data.class);

        // 버튼이 invisible하게 기본 설정
        btn_delete_all = findViewById(R.id.Btn_delete_all);
        btn_delete_latest_value = findViewById(R.id.Btn_delete_latest_value);
        btn_delete_all.setVisibility(View.GONE);
        btn_delete_latest_value.setVisibility(View.GONE);

        btn_back = findViewById(R.id.Btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void onLocation(View v) {
        String wifiData = NetworkManager.getWifiData(ScanAdvertisementActivity.this);
        tv_data = findViewById(R.id.Text_view_data);
        
        if(wifiData != null) {
            comm_data service = retrofit.create(comm_data.class);

            Call<String> call = null;
            call = service.location(wifiData);

            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    tv_data.setText(response.body().toString());
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                }
            });
        }
        else tv_data.setText("스캔 실패");
    }

    public void onToggleScan(View v) {
        boolean on = ((ToggleButton) v).isChecked();

        if (on) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                toast.setText("No permission");
                toast.show();
            }

            if (blead.isEnabled()) {
                // bluetooth 스캔 시작
                blead.startLeScan(scancallback_le);
            } else {
                toast.setText("Bluetooth is off");
                toast.show();
                ((ToggleButton) v).setChecked(false);
            }
        } else {
            blead.stopLeScan(scancallback_le);
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
        customDialog = new CustomDialog(ScanAdvertisementActivity.this, "파일 내용을 전부 삭제하시겠습니까?", "취소", "삭제");
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

                    tv_data = findViewById(R.id.Text_view_data);
                    tv_data.setText("");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        customDialog.show();
    }

    public void onDeleteLatest(View v) {
        customDialog = new CustomDialog(ScanAdvertisementActivity.this, "최근 데이터를 삭제하시겠습니까?", "취소", "삭제");
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

                    tv_data = findViewById(R.id.Text_view_data);
                    tv_data.setText("");
                    fr = new FileReader(file.getAbsoluteFile());
                    br = new BufferedReader(fr);
                    while ((line = br.readLine()) != null) {
                        tv_data.setText(tv_data.getText() + line + "\n");
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

    public void onSendData(View v) {
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
                toast.setText("파일이 비어있습니다.");
                toast.show();
            } else {
                br.close();
                fr.close();

                customDialog = new CustomDialog(ScanAdvertisementActivity.this,
                        "저장된 데이터를 서버에 전송하시겠습니까?",
                        "취소",
                        "전송");
                customDialog.setDialogListener(new CustomDialog.CustomDialogInterface() {
                    @Override
                    public void cancelClicked() {

                    }

                    @Override
                    public void acceptClicked() {
                        if (NetworkManager.getConnectivityStatus(ScanAdvertisementActivity.this) != NetworkManager.NOT_CONNECTED) {
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

                                tv_data = findViewById(R.id.Text_view_data);
                                tv_data.setText("");
                                br.close();
                                br = new BufferedReader(new FileReader(file.getAbsoluteFile()));
                                while ((line = br.readLine()) != null) {
                                    tv_data.setText(tv_data.getText() + line + "\n");
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
                            // 서버 데이터 업데이트
                            new NetworkTask().execute();
                        } else {
                            toast.setText("NETWORK NOT CONNECTED");
                            toast.show();
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

    private BluetoothAdapter.LeScanCallback scancallback_le = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            String MacAddr = device.getAddress();
            String sensorTeam = checkRaspPiAddr(MacAddr);
            String sensorType = getSensorType(scanRecord);

            if (sensorTeam != null && sensorType != null) {
                String hexData = byteArrayToHex(scanRecord);
                String sensingTime = String.valueOf(extractSensingTime(hexData));
                String OTP = extractOTP(hexData);
                String sensorData;
                if(sensorType.equals(TYPE_DUST_SENSOR)) sensorData = extractDustSensorData(hexData);
                else sensorData = extractAirSensorData(hexData);
                getLocation(); // location 값 획득

                switch_directly_send = findViewById(R.id.Switch_directly_send);

                if (switch_directly_send.isChecked() == true && location != null) {
                    if (NetworkManager.getConnectivityStatus(ScanAdvertisementActivity.this) != NetworkManager.NOT_CONNECTED) {
                        try {
                            File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + FILE_NAME);
                            if (!file.exists()) {
                                file.createNewFile();
                            }
                            FileReader fr = new FileReader(file.getAbsoluteFile());
                            BufferedReader br = new BufferedReader(fr);

                            Call<String> call;
                            if(sensorType == TYPE_DUST_SENSOR) call = service.dust_sensing(sensorTeam, mode, MacAddr, androidID, sensingTime, OTP, location, sensorData);
                            else call = service.air_sensing(sensorTeam, mode, MacAddr, androidID, sensingTime, OTP, location, sensorData);

                            call.enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    Log.d("ServerCommunicationSuccess", response.body().toString());
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Log.d("ServerCommunicationFail", "failed to communicate with server", t);
                                }
                            });
                            br.close();
                            fr.close();

                            toast.setText(sensorTeam + ": " + sensingTime);
                            toast.show();
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        // 서버 데이터 업데이트
                        new NetworkTask().execute();
                    } else {
                        toggle_btn_scan = findViewById(R.id.Toggle_btn_scan);
                        toggle_btn_scan.setChecked(false);
                        blead.stopLeScan(scancallback_le);

                        toast.setText("NETWORK NOT CONNECTED");
                        toast.show();
                    }
                } else if(location != null) {
                    BLEdata_storage data = new BLEdata_storage(sensorType, sensorTeam, mode, MacAddr, sensingTime, OTP, location, sensorData);
                    datalist.add(data);

                    try {
                        File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + FILE_NAME);
                        if (!file.exists()) {
                            file.createNewFile();
                        }

                        FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
                        BufferedWriter bw = new BufferedWriter(fw);

                        bw.write(String.valueOf(datalist.get(datalist.size() - 1).get_sensor_type()));
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
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    try {
                        tv_data = findViewById(R.id.Text_view_data);
                        tv_data.setText("");
                        String line;
                        BufferedReader br = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory().getAbsoluteFile() + FILE_NAME));
                        while ((line = br.readLine()) != null) {
                            tv_data.setText(tv_data.getText() + line + "\n");
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    };

    private String byteArrayToHex(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%x ", b)); // 각 바이트를 16진수 문자열로 변환하여 추가
        }
        return sb.toString();
    }

    private String getSensorType(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%s", b));
        }
        if(sb.toString().contains(TYPE_DUST_SENSOR)) return TYPE_DUST_SENSOR;
        else if(sb.toString().contains(TYPE_AIR_SENSOR)) return TYPE_AIR_SENSOR;
        else return null;
    }

    private String extractOTP(String hexData) {
        String OTP = null;
        StringBuilder sb = new StringBuilder();
        String regExp = "f0 f0 ([0-9a-fA-F]+ [0-9a-fA-F]+ [0-9a-fA-F]+)"; // OTP 추출 정규표현식

        Pattern pattern = Pattern.compile(regExp);
        Matcher matcher = pattern.matcher(hexData);

        if (matcher.find()) {
            sb.append(matcher.group(1));
            OTP = sb.toString(); // 매칭된 문자열을 추출하여 반환

            OTP = OTP.replaceAll("\\s", ""); // 공백 제거
        }

        return OTP;
    }

    private String extractSensingTime(String hexData) {
        String sensingTime = null;
        StringBuilder sb = new StringBuilder();
        String regExp = "99 99 ([0-9a-fA-F]+) ([0-9a-fA-F]+) ([0-9a-fA-F]+) ([0-9a-fA-F]+) ([0-9a-fA-F]+)"; // sensingTime 추출 정규표현식

        Pattern pattern = Pattern.compile(regExp);
        Matcher matcher = pattern.matcher(hexData);

        if (matcher.find()) {
            // 16진수 String to Decimal 변환
            sb.append(Integer.parseInt(matcher.group(1), 16));

            for(int i = 2; i <= 5; i++) {
                if(Integer.parseInt(matcher.group(i), 16) < 10) sb.append('0');
                sb.append(Integer.parseInt(matcher.group(i), 16));
            }

            sensingTime = sb.toString();
        }

        return sensingTime;
    }

    private String extractDustSensorData(String hexData) {
        String sensorData = null;
        StringBuilder sb = new StringBuilder();
        String regExp = "fd ([0-9a-fA-F]+) ([0-9a-fA-F]+) ([0-9a-fA-F]+)"; // sensor data 추출 정규표현식

        Pattern pattern = Pattern.compile(regExp);
        Matcher matcher = pattern.matcher(hexData);

        if (matcher.find()) {
            sb.append(Integer.parseInt(matcher.group(1), 16));
            sb.append('/');
            sb.append(Integer.parseInt(matcher.group(2), 16));
            sb.append('/');
            sb.append(Integer.parseInt(matcher.group(3), 16));
            sensorData = sb.toString(); // 매칭된 문자열을 추출하여 반환
        }

        return sensorData;
    }

    private String extractAirSensorData(String hexData) {
        String sensorData = null;
        StringBuilder sb = new StringBuilder();
        String regExp = "fd ([0-9a-fA-F]+) ([0-9a-fA-F]+)"; // sensor data 추출 정규표현식

        Pattern pattern = Pattern.compile(regExp);
        Matcher matcher = pattern.matcher(hexData);

        if (matcher.find()) {
            sb.append(Integer.parseInt(matcher.group(1), 16));
            sb.append(Integer.parseInt(matcher.group(2), 16));
            sensorData = sb.toString(); // 매칭된 문자열을 추출하여 반환
        }

        return sensorData;
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

    private class NetworkTask extends AsyncTask<Void, Void, Document> {

        @Override
        protected Document doInBackground(Void... voids) {
            try {
                return Jsoup.connect(server_URL).get();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Document document) {
            if (document != null) {
                crawlData(document);
            } else {
                // 요청 실패 처리
            }
        }
    }

    public void crawlData(Document doc) {
        Elements sensorData = doc.getElementsByTag("tr");

        if (!sensorData.isEmpty()) {
            text_1jo_data = findViewById(R.id.Text_1jo);
            text_2jo_data = findViewById(R.id.Text_2jo);
            text_3jo_data = findViewById(R.id.Text_3jo);
            text_4jo_data = findViewById(R.id.Text_4jo);
            text_5jo_data = findViewById(R.id.Text_5jo);
            ArrayList<Integer> serverData = new ArrayList<>();

            for(Element row: sensorData) {
                Elements rowDatas = row.select("th");

                if(rowDatas.first().text().equals("2조")) {
                    for(Element data: rowDatas) {
                        if(data.text().equals("2조")) continue;
                        serverData.add(Integer.valueOf(data.text()));
                    }
                }
            }

            text_1jo_data.setText(String.valueOf(serverData.get(0)));
            text_2jo_data.setText(String.valueOf(serverData.get(1)));
            text_3jo_data.setText(String.valueOf(serverData.get(2)));
            text_4jo_data.setText(String.valueOf(serverData.get(3)));
            text_5jo_data.setText(String.valueOf(serverData.get(4)));

            Log.d("Tag", "isNull? : " + "Non Null");
        } else {
            Log.d("Tag", "isNull? : " + "Null");
        }
    }

    public void getLocation() {
        String wifiData = NetworkManager.getWifiData(ScanAdvertisementActivity.this);

        if(wifiData != null) {
            comm_data service = retrofit.create(comm_data.class);

            Call<String> call = null;
            call = service.location(wifiData);

            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    location = response.body().toString();
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    location = null;
                }
            });
        } else location = null;
    }
}