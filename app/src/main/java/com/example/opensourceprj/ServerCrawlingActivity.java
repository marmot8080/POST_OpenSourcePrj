package com.example.opensourceprj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ServerCrawlingActivity extends AppCompatActivity {
    private Button btn_back;

    private final String server_URL = "http://203.255.81.72:10021/dustsensor/sensingpage/"; // 서버 url

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_crawling);

        btn_back = findViewById(R.id.Btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
