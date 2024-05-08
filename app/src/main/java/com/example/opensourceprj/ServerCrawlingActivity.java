package com.example.opensourceprj;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ServerCrawlingActivity extends AppCompatActivity {
    private Button btn_back;
    private WebView web_data;

    private static final String server_URL = "http://203.255.81.72:10021/dustsensor/sensingpage/"; // 서버 url

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

        web_data = findViewById(R.id.Web_server_data);

        setWebViewPermissions(web_data);

        // 웹페이지 호출
        web_data.loadUrl(server_URL);
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