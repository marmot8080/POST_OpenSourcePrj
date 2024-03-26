package com.example.opensourceprj;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity {
    private Retrofit retrofit;
    private comm_data service;
    private BluetoothAdapter blead;
    private CustomDialog customDialog;

    private ToggleButton toggle_btn_scan;

    private String OTP_data = null;
    private static final String user = "2jo, minwoo, taeho, hyungwoo, bogu, wuyixin"; // 팀명
    private static final String raspberryPiAddr = "B8:27:EB:7F:E7:58"; // 라즈베리파이 Mac address

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            customDialog = new CustomDialog(MainActivity.this,
                    "앱 권한이 설정되어있지 않습니다.\n설정 페이지로 이동하시겠습니까?",
                    "취소",
                    "확인");

            customDialog.setDialogListener(new CustomDialog.CustomDialogInterface() {
                @Override
                public void cancelClicked() {

                }

                @Override
                public void acceptClicked() {
                    Intent settingIntent = new Intent(Settings.ACTION_SETTINGS);
                    startActivity(settingIntent);
                }
            });

            customDialog.show();
        }

        // 객체 생성
        blead = BluetoothAdapter.getDefaultAdapter();

        if (blead == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not available", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth is available", Toast.LENGTH_SHORT).show();
        }

        // 블루투스 기능 비활성화 시 팝업 메시지 생성
        if (!blead.isEnabled()) {
            customDialog = new CustomDialog(MainActivity.this,
                    "블루투스 기능이 꺼져있습니다.\n블루투스 기능을 활성화 해주십시오.",
                    "취소",
                    "확인");

            customDialog.show();
        }

        Gson gson = new GsonBuilder().setLenient().create();
        retrofit = new Retrofit.Builder()
                .baseUrl("http://203.255.81.72:10021/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        service = retrofit.create(comm_data.class);
    }

    public void onToggleClicked(View v){
        boolean on = ((ToggleButton) v).isChecked();

        if(on) {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
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

            if(MacAdd.equals(raspberryPiAddr)) {
                toggle_btn_scan = findViewById(R.id.Toggle_btn_scan);

                blead.stopLeScan(scancallback_le);
                toggle_btn_scan.setChecked(false);

                String data = byteArrayToHex(scanRecord);
                OTP_data = extractOTP(data);

                customDialog = new CustomDialog(MainActivity.this,
                        "OTP를 읽어왔습니다.\nPOST 요청을 보내시겠습니까?",
                        "취소",
                        "보내기");

                customDialog.setDialogListener(new CustomDialog.CustomDialogInterface() {
                    @Override
                    public void cancelClicked() {

                    }

                    @Override
                    public void acceptClicked() {
                        Call<String> call = service.post(user, OTP_data);

                        call.enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                Log.e("testSuccess", response.body().toString());
                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {
                                Log.e("testFail", "failed to communicate with server", t);
                            }
                        });
                    }
                });

                customDialog.show();
            }
        }
    };

    private String byteArrayToHex(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b)); // 각 바이트를 16진수 문자열로 변환하여 추가
        }
        return sb.toString();
    }

    private String extractOTP(String hexData) {
        String OTP = null;
        String regExp = "998899([0-9a-fA-F]+)998899"; // OTP 추출 정규표현식

        Pattern pattern = Pattern.compile(regExp);
        Matcher matcher = pattern.matcher(hexData);

        if (matcher.find()) {
            OTP = matcher.group(1); // 매칭된 문자열을 추출하여 반환
        }
        return OTP;
    }
}