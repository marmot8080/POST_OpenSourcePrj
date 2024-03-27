package com.example.opensourceprj;

import android.os.Environment;
import android.view.View;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;

public class Data_Transmission_Activity {

    bt_store.setOnClickListener(new View.OnClickListener(){

        public void onClick(View view) {


            try {
                File file = new File(Enviroment.getExternalStorageDirectory().getAbsoluteFile() + "/store_test.csv");
                if(!file.exists()) {
                    file.createNewFile();
                }

                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);

                for(int j = 0; j< datalist.size(); j++){

                    bw.write(String.valueOf(datalist.get(j).get_rssi)));
                    bw.write("," + String.valueOf(datalist.get(j).get_p01()));
                    bw.write("," + String.valueOf(datalist.get(j).get_p25()));
                    bw.write("," + String.valueOf(datalist.get(j).get_p10()));
                    bw.write("," + String.valueOf(datalist.get(j).get_ptime()));

                    bw.newLine();
                }

                bw.close();
                fw.close();
            }catch (IOException e) {
                throw new RuntimeException(e);
            }


            datalist.clear();
        }

    })




    bt_write.setOnClickListener(new View.OnClickListener(){
        public void onClick(View view){
            int rssi = Integer.valueOf(ed_rssi.getText().toString());
            int pm1_0 = Integer.valueOf(ed_pm1_0.getText().toString());
            int pm2_5 = Integer.valueOf(ed_pm25.getText().toString());
            int pm10 = Integer.valueOf(ed_pm10.getText().toString());

            BLEdata_storage data = new BLEdata_storage(rssi, pm1_0, pm2_5, pm10, System.currentTimeMillis());

            datalist.add(data);
        }
    });

    bt_show.setOnClickListener(new View.OnClickListener(){

        public void onClick(View view) {

            try{
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
