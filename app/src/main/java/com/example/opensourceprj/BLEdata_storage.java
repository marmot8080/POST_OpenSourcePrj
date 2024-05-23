package com.example.opensourceprj;

public class BLEdata_storage {
    private String sensorType, sensorTeam, macAddr, time, otp, pmData;

    BLEdata_storage(String sensorType, String sensorTeam, String macAddr, String time, String otp, String pmData){
        this.sensorType = sensorType;
        this.sensorTeam = sensorTeam;
        this.macAddr = macAddr;
        this.time = time;
        this.otp = otp;
        this.pmData = pmData;
    }

    public String get_sensor_type() {return sensorType;}

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
