package com.example.opensourceprj;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class ServerCrawlingActivity extends AppCompatActivity {
    private Button btn_back;
    private ImageButton btn_refresh;
    private WebView web_data;
    private TextView text_1jo_data;
    private TextView text_2jo_data;
    private TextView text_3jo_data;
    private TextView text_4jo_data;
    private TextView text_5jo_data;

    private final String server_URL = "http://203.255.81.72:10021/dustsensor/sensingpage/"; // 서버 url

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_crawling);

        new NetworkTask().execute();

        btn_back = findViewById(R.id.Btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_refresh = findViewById(R.id.Btn_refresh);
        btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new NetworkTask().execute();
            }
        });

        web_data = findViewById(R.id.Web_server_data);

        setWebViewPermissions(web_data);

        // 웹페이지 호출
        web_data.loadUrl(server_URL);
    }

    private class NetworkTask extends AsyncTask<Void, Void, Document> {

        @Override
        protected Document doInBackground(Void... voids) {
            try {
                return Jsoup.connect(server_URL).get();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Document document) {
            if (document != null) {
                crawlData(document);
            } else {
                // 요청 실패 처리
            }
        }
    }

    public void crawlData(Document doc) {
        Elements sensorData = doc.getElementsByTag("tr");

        if (!sensorData.isEmpty()) {
            text_1jo_data = findViewById(R.id.Text_1jo);
            text_2jo_data = findViewById(R.id.Text_2jo);
            text_3jo_data = findViewById(R.id.Text_3jo);
            text_4jo_data = findViewById(R.id.Text_4jo);
            text_5jo_data = findViewById(R.id.Text_5jo);
            ArrayList<Integer> serverData = new ArrayList<>();

            for(Element row: sensorData) {
                Elements rowDatas = row.select("th");

                if(rowDatas.first().text().equals("2조")) {
                    for(Element data: rowDatas) {
                        if(data.text().equals("2조")) continue;
                        serverData.add(Integer.valueOf(data.text()));
                    }
                }
            }

            text_1jo_data.setText(String.valueOf(serverData.get(0)));
            text_2jo_data.setText(String.valueOf(serverData.get(1)));
            text_3jo_data.setText(String.valueOf(serverData.get(2)));
            text_4jo_data.setText(String.valueOf(serverData.get(3)));
            text_5jo_data.setText(String.valueOf(serverData.get(4)));

            Log.d("Tag", "isNull? : " + "Non Null");
        } else {
            Log.d("Tag", "isNull? : " + "Null");
        }
    }

    public void setWebViewPermissions(WebView webView) {
        webView.setWebViewClient(new WebViewClient()); // 새 창 띄우지 않기
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setLoadWithOverviewMode(true); // WebView 화면크기에 맞추도록 설정
        webView.getSettings().setUseWideViewPort(true); // wide viewport 설정 - setLoadWithOverviewMode와 같이 사용
        webView.getSettings().setSupportZoom(true); // 줌 설정 여부
        webView.getSettings().setBuiltInZoomControls(true); // 줌 확대/축소 버튼 여부
        webView.getSettings().setJavaScriptEnabled(true); // 자바스크립트 사용여부
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true); // javascript가 window.open()를 사용할 수 있도록 설정
        webView.getSettings().setSupportZoom(true); // 멀티 윈도우 사용 여부
        webView.getSettings().setDomStorageEnabled(true); // 로컬 스토리지(localStorage) 사용여부
    }
}