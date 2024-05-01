package com.example.opensourceprj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class ServerCrawlingActivity extends AppCompatActivity {
    private Button btn_back;

    private final String server_URL = "http://203.255.81.72:10021/dustsensor/sensingpage/"; // 서버 url

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Bundle bundle = msg.getData();	//new Thread에서 작업한 결과물 받기
            textView.setText(bundle.getString("temperature"));	//받아온 데이터 textView에 출력
        };

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

        new Thread(){
            @Override
            public void run(){
                try{
                  // start Crawling
                    //Document doc = Jsoup.connect(server_URL).get();	//URL 웹사이트에 있는 html 코드 Crawling

                    /*Elements sensDataNum = doc.select(".");	//끌어온 html에서 클래스네임이 "" 인 값만 선택해서 빼오기
                    isEmpty = sensDataNum.isEmpty(); //빼온 값 null체크
                    Log.d("Tag", "isNull? : " + isEmpty); //로그캣 출력
                    if(isEmpty == false) { //null값이 아니면 크롤링 실행
                        tem = temele.get(0).text().substring();
                        bundle.putString("", tem); //bundle 이라는 자료형에 뽑아낸 결과값 담아서 main Thread로 보내기
                    */
                } catch (IOException e) {

                    throw new RuntimeException(e);
                }
            }
        }.start();


    }
}
