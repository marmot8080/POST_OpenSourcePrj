package com.example.opensourceprj;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataTransmissionActivity extends AppCompatActivity {
    CustomDialog customDialog;

    private BluetoothAdapter blead;

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
            "D8:3A:DD:42:A8:FB",
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
    private static final List<String> raspberryPiAddrList_1 = new ArrayList<>(Arrays.asList(raspberryPiAddr_1));
    private static final List<String> raspberryPiAddrList_2 = new ArrayList<>(Arrays.asList(raspberryPiAddr_2));
    private static final List<String> raspberryPiAddrList_3 = new ArrayList<>(Arrays.asList(raspberryPiAddr_3));
    private static final List<String> raspberryPiAddrList_4 = new ArrayList<>(Arrays.asList(raspberryPiAddr_4));
    private static final List<String> raspberryPiAddrList_5 = new ArrayList<>(Arrays.asList(raspberryPiAddr_5));

    private ToggleButton toggle_btn_scan;
    private TextView tv_data;
    private Button btn_clear;

    private static ArrayList<BLEdata_storage> datalist = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_transmission);

        toggle_btn_scan = findViewById(R.id.Toggle_btn_scan);

        // 객체 생성
        blead = BluetoothAdapter.getDefaultAdapter();

        if (blead == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not available", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth is available", Toast.LENGTH_SHORT).show();
        }

        // 블루투스 기능 비활성화 시 팝업 메시지 생성
        if (!blead.isEnabled()) {
            customDialog = new CustomDialog(DataTransmissionActivity.this,
                    "블루투스 기능이 꺼져있습니다.\n블루투스 기능을 활성화 해주십시오.",
                    "취소",
                    "확인");

            customDialog.show();
        }

        btn_clear = findViewById(R.id.Btn_clear);
        btn_clear.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view) {
                customDialog = new CustomDialog(DataTransmissionActivity.this,
                        "수집된 데이터를 삭제하시겠습니까?",
                        "취소",
                        "삭제");

                customDialog.setDialogListener(new CustomDialog.CustomDialogInterface() {
                    @Override
                    public void cancelClicked() {

                    }

                    @Override
                    public void acceptClicked() {
                        try {
                            File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/store_test.csv");
                            if(!file.exists()) {
                                file.createNewFile();
                            }

                            FileWriter fw = new FileWriter(file.getAbsoluteFile(), false);
                            BufferedWriter bw = new BufferedWriter(fw);

                            datalist.clear(); // datalist 초기화

                            bw.write(""); // 파일 내용 삭제

                            // TextView 삭제
                            tv_data = findViewById(R.id.Txt_tv);
                            tv_data.setText("");

                            bw.close();
                            fw.close();
                        }catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                customDialog.show();
            }

        });
    }

    public void onToggleClicked(View v){
        boolean on = ((ToggleButton) v).isChecked();

        if(on) {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "No permission", Toast.LENGTH_SHORT).show();
            }

            if(blead.isEnabled()) {
                // bluetooth 스캔 시작
                blead.startLeScan(scancallback_le);
            } else {
                Toast.makeText(this, "Bluetooth is off", Toast.LENGTH_SHORT).show();
                ((ToggleButton) v).setChecked(false);
            }
        } else {
            blead.stopLeScan(scancallback_le);
        }
    }

    private BluetoothAdapter.LeScanCallback scancallback_le = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            String MacAdd = device.getAddress();

            if(raspberryPiAddrList_1.contains(MacAdd) ||
                    raspberryPiAddrList_2.contains(MacAdd) ||
                    raspberryPiAddrList_3.contains(MacAdd) ||
                    raspberryPiAddrList_4.contains(MacAdd) ||
                    raspberryPiAddrList_5.contains(MacAdd)) {
                String hexData = byteArrayToHex(scanRecord);
                int sensingTime = Integer.valueOf(extractSensingTime(hexData));

                if(datalist.isEmpty() || datalist.get(datalist.size() - 1).get_time() != sensingTime){
                    String OTP_data = extractOTP(hexData);
                    String pmData = extractSensorData(hexData);
                    String[] pmDataResult = pmData.split("/", 3);

                    BLEdata_storage data = new BLEdata_storage(rssi, Integer.valueOf(pmDataResult[0]), Integer.valueOf(pmDataResult[1]), Integer.valueOf(pmDataResult[2]), sensingTime);
                    datalist.add(data);

                    try {
                        File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/store_test.csv");
                        if(!file.exists()) {
                            file.createNewFile();
                        }

                        FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
                        BufferedWriter bw = new BufferedWriter(fw);

                        bw.write(String.valueOf(datalist.get(datalist.size()-1).get_rssi()));
                        bw.write("," + String.valueOf(datalist.get(datalist.size()-1).get_p01()));
                        bw.write("," + String.valueOf(datalist.get(datalist.size()-1).get_p25()));
                        bw.write("," + String.valueOf(datalist.get(datalist.size()-1).get_p10()));
                        bw.write("," + String.valueOf(datalist.get(datalist.size()-1).get_time()));

                        bw.newLine();

                        bw.close();
                        fw.close();
                    }catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    try{
                        tv_data = findViewById(R.id.Txt_tv);
                        tv_data.setText("");
                        String line;
                        BufferedReader br = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/store_test.csv"));
                        while((line = br.readLine())!=null) {
                            tv_data.setText(tv_data.getText()+line+"\n");
                        }
                    }catch (IOException e){
                        throw new RuntimeException(e);
                    }
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

    private String extractSensorData(String hexData) {
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
}
