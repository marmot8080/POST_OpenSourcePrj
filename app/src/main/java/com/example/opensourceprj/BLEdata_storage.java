package com.example.opensourceprj;

import android.view.View;

public class BLEdata_storage {
    private int RSSI;
    private int p01;
    private int p25;
    private int p10;
    private long time;

    BLEdata_storage(int RSSI, int p01, int p25, int p10, long time){
        this.RSSI = RSSI;
        this.p01 = p01;
        this.p25 = p25;
        this.p10 = p10;
        this.time = time;
    }

    public int get_rssi(){
        return RSSI;
    }

    public int get_p01() {
        return p01;
    }

    public int get_p25(){
        return p25;
    }

    public int get_p10(){
        return p10;
    }

    public long get_time(){
        return time;
    }
}
