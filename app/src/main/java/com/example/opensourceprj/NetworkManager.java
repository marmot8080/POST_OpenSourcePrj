package com.example.opensourceprj;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.List;

public class NetworkManager {
    public static final int WIFI_CONNECTED = 1;     // 와이파이 연결 상태
    public static final int MOBILE_CONNECTED = 2;   // 데이터 연결 상태
    public static final int NOT_CONNECTED = 3; // 미연결 상태
    
    public static int getConnectivityStatus(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        //networkInfo 값이 null일 수도 있기 때문에
        if (networkInfo != null) {
            int type = networkInfo.getType();

            if (type == ConnectivityManager.TYPE_MOBILE) {
                return MOBILE_CONNECTED;
            }else if(type == ConnectivityManager.TYPE_WIFI) {
                return WIFI_CONNECTED;
            }
        } return NOT_CONNECTED;
    }

    public List<ScanResult> getScanResults(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        wifiManager.startScan();

        return wifiManager.getScanResults();
    }
}