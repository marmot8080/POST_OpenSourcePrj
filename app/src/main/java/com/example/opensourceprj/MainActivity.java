package com.example.opensourceprj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Button btn_pair_device, btn_scan_advertisement, btn_check_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_pair_device = findViewById(R.id.Btn_pair_device);
        btn_pair_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PairDeviceActivity.class);
                startActivity(intent);
            }
        });

        btn_scan_advertisement = findViewById(R.id.Btn_scan_advertisement);
        btn_scan_advertisement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScanAdvertisementActivity.class);
                startActivity(intent);
            }
        });

        btn_check_data = findViewById(R.id.Btn_check_data);
        btn_check_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                startActivity(intent);
            }
        });
    }
}
