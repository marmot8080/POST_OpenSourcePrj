package com.example.opensourceprj;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

public class WebViewActivity extends AppCompatActivity {
    private ImageButton btn_back;
    private WebView web_dust_data, web_air_data;

    // 서버 url
    private static final String server_dust_URL = "http://203.255.81.72:10021/dustsensor_v2/sensingpage/";
    private static final String server_air_URL = "http://203.255.81.72:10021/airquality/sensingpage/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        btn_back = findViewById(R.id.Btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        web_air_data = findViewById(R.id.Web_airSen_server_data);
        web_dust_data = findViewById(R.id.Web_dustSen_server_data);


        setWebViewPermissions(web_air_data);
        setWebViewPermissions(web_dust_data);

        // 웹페이지 호출
        web_air_data.loadUrl(server_air_URL);
        web_dust_data.loadUrl(server_dust_URL);
    }

    public void onToggleChange(View v) {
        boolean on = ((ToggleButton) v).isChecked();
        web_dust_data = findViewById(R.id.Web_dustSen_server_data);
        web_air_data = findViewById(R.id.Web_airSen_server_data);

        if (on) {
            web_dust_data.setVisibility(View.GONE);
            web_air_data.setVisibility(View.VISIBLE);
        } else {
            web_dust_data.setVisibility(View.VISIBLE);
            web_air_data.setVisibility(View.GONE);
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