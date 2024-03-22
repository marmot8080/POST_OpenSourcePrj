package com.example.opensourceprj;

import static android.widget.Toast.makeText;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ble {
    private static MainActivity mainActivity = new MainActivity();

    // 객체 생성
    private static BluetoothAdapter blead = BluetoothAdapter.getDefaultAdapter();

    private static final String raspberryPiAddr = "B8:27:EB:7F:E7:58";
    private static String OTP_data = null;

    public static void startScan() {
        blead.startLeScan(scancallback_le);
    }

    public static void stopScan() {
        blead.stopLeScan(scancallback_le);
    }

    private static BluetoothAdapter.LeScanCallback scancallback_le = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            String MacAdd = device.getAddress();

            if(MacAdd.equals(raspberryPiAddr)) {
                String data = byteArrayToHex(scanRecord);

                OTP_data = extractOTP(data);
            } else {
                Toast.makeText(mainActivity.getApplicationContext(), "주소가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private static String byteArrayToHex(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b)); // 각 바이트를 16진수 문자열로 변환하여 추가
        }
        return sb.toString();
    }

    private static String extractOTP(String hexData) {
        String OTP = null;
        String regExp = "99 88 99 ([\\w]{2} [\\w]{2} [\\w]{2} [\\w]{2} [\\w]{2})+ 99 88 99";

        Pattern pattern = Pattern.compile(regExp);
        Matcher matcher = pattern.matcher(hexData);

        if (matcher.find()) {
            OTP = matcher.group(1); // 그룹 1을 추출하여 반환

            OTP = OTP.replaceAll("\\s", ""); // 공백 제거
        } else {
            Toast.makeText(mainActivity.getApplicationContext(), "OTP를 읽어오지 못했습니다.", Toast.LENGTH_SHORT).show();
        }

        return OTP;
    }

    public static String getOTP(){
        return OTP_data;
    }
}
