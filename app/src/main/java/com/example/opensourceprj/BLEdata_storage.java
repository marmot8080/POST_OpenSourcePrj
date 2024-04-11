package com.example.opensourceprj;

import android.view.View;

public class BLEdata_storage {
    private String sensorTeam;
    private String macAddr;
    private long time;
    private int otp;
    private String pmData;

    BLEdata_storage(String sensorTeam, String macAddr, long time, int otp, String pmData){
        this.sensorTeam = sensorTeam;
        this.macAddr = macAddr;
        this.time = time;
        this.otp = otp;
        this.pmData = pmData;
    }

    public String get_sensor_team() {return sensorTeam;}

    public String get_mac_addr() {return macAddr;}

    public int get_otp(){
        return otp;
    }

    public String get_pm_data() {return pmData;}

    public long get_time(){
        return time;
    }
}
