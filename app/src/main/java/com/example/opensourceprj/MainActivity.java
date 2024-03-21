package com.example.opensourceprj;

import static android.widget.Toast.makeText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

    private Button btn_test;

    public static final String user = "POST";
    private static String data = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 객체 생성
        BluetoothAdapter blead = BluetoothAdapter.getDefaultAdapter();

        if(blead == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not available", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth is available", Toast.LENGTH_SHORT).show();
        }

        if(!blead.isEnabled()) {
            blead.enable();
        }

        // bluetooth 스캔 시작
        ble.startScan();

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "페어링 기기 검색 중...", Toast.LENGTH_SHORT).show();

                data = ble.getOTP();

                if(data != null) {
                    Toast.makeText(getApplicationContext(), "페어링 기기 찾음", Toast.LENGTH_SHORT).show();
                    ble.stopScan();
                }
                else handler.postDelayed(this, 500);
            }
        }, 500);

        Gson gson = new GsonBuilder().setLenient().create();
        retrofit = new Retrofit.Builder()
                .baseUrl("http://203.255.81.72:10021/commtest_get/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        service = retrofit.create(comm_data.class);

        btn_test = (Button) findViewById(R.id.Btn_test);

        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(data != null) {
                    Call<String> call = service.get(user, data);

                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            Log.e("test", response.body().toString());
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Log.e("fail", "fail", t);
                        }
                    });

                    Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "data가 비어있습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}