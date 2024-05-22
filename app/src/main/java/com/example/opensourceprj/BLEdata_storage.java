package com.example.opensourceprj;

public class BLEdata_storage {
    private String sensorTeam, macAddr, time, otp, pmData;

    BLEdata_storage(String sensorTeam, String macAddr, String time, String otp, String pmData){
        this.sensorTeam = sensorTeam;
        this.macAddr = macAddr;
        this.time = time;
        this.otp = otp;
        this.pmData = pmData;
    }

    public String get_sensor_team() {return sensorTeam;}

    public String get_mac_addr() {return macAddr;}

    public String get_otp(){
        return otp;
    }

    public String get_pm_data() {return pmData;}

    public String get_time(){
        return time;
    }
}
