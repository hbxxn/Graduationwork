package com.example.setting;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        WebView webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new BridgeInterface(), "Android");
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                //안드로이드 -> 자바스크립트 함수 호출!
                webView.loadUrl("javascript:sample2_execDaumPostcode();");

            }
        });
        //최소 웹뷰 로드
        webView.loadUrl("https://searchaddress-b449e.web.app");
    }

    // Assume this is called when address is selected
    public void onAddressSelected(String newAddress) {
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentById(R.id.homeFragment);
        if (homeFragment != null) {
            homeFragment.updateAddress(newAddress);
        }
    }

    private class BridgeInterface {
        @JavascriptInterface
        public void processDATA(String data){
            //다음(카카오) 주소 검색 결과 값이 브릿지 통로를 통해 전달 받는다. (from Javascript)
            Intent intent = new Intent();
            intent.putExtra("data", data);
            setResult(RESULT_OK,intent);
            finish();

        }
    }
}