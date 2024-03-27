package com.example.opensourceprj;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class DataTransmissionActivity extends AppCompatActivity {
    Button btn_store;
    Button btn_write;
    Button btn_show;
    EditText ed_rssi;
    EditText ed_pm1_0;
    EditText ed_pm25;
    EditText ed_pm10;
    TextView tv;
    ArrayList<BLEdata_storage> datalist = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_transmission);

        ed_rssi = findViewById(R.id.Ed_rssi);
        ed_pm1_0 = findViewById(R.id.Ed_pm1_0);
        ed_pm25 = findViewById(R.id.Ed_pm25);
        ed_pm10 = findViewById(R.id.Ed_pm10);
        btn_write.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                int rssi = Integer.valueOf(ed_rssi.getText().toString());
                int pm1_0 = Integer.valueOf(ed_pm1_0.getText().toString());
                int pm2_5 = Integer.valueOf(ed_pm25.getText().toString());
                int pm10 = Integer.valueOf(ed_pm10.getText().toString());

                BLEdata_storage data = new BLEdata_storage(rssi, pm1_0, pm2_5, pm10, System.currentTimeMillis());

                datalist.add(data);
            }
        });

        btn_store.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view) {


                try {
                    File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/store_test.csv");
                    if(!file.exists()) {
                        file.createNewFile();
                    }

                    FileWriter fw = new FileWriter(file.getAbsoluteFile());
                    BufferedWriter bw = new BufferedWriter(fw);

                    for(int j = 0; j< datalist.size(); j++){

                        bw.write(String.valueOf(datalist.get(j).get_rssi()));
                        bw.write("," + String.valueOf(datalist.get(j).get_p01()));
                        bw.write("," + String.valueOf(datalist.get(j).get_p25()));
                        bw.write("," + String.valueOf(datalist.get(j).get_p10()));
                        bw.write("," + String.valueOf(datalist.get(j).get_time()));

                        bw.newLine();
                    }

                    bw.close();
                    fw.close();
                }catch (IOException e) {
                    throw new RuntimeException(e);
                }


                datalist.clear();
            }

        });

        btn_show.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view) {

                try{
                    tv = findViewById(R.id.Txt_tv);
                    tv.setText("");
                    String line;
                    BufferedReader br = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/store_test.csv"));
                    while((line = br.readLine())!=null) {
                        tv.setText(tv.getText()+line+"\n");
                    }
                }catch (IOException e){
                    throw new RuntimeException(e);
                }
            }

        });
    }
}
