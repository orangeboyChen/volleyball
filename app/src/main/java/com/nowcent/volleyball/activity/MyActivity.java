package com.nowcent.volleyball.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.nowcent.volleyball.R;
import com.nowcent.volleyball.utils.DataUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chene
 */
@EActivity(R.layout.activity_my)
public class MyActivity extends AppCompatActivity {

    @ViewById(R.id.myWebView)
    WebView webView;

    @AfterViews
    void init(){

        String token = DataUtils.getSavedToken(this);

        //WebView设置
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        CookieSyncManager.createInstance(getApplicationContext());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeSessionCookies(null);
        cookieManager.removeAllCookie();
        cookieManager.flush();
        WebStorage.getInstance().deleteAllData();

        cookieManager.setCookie("public_account", token);



        webView.getSettings().setDomStorageEnabled(true);

        //存储的最大容量
        webView.getSettings().setAppCacheMaxSize(1024*1024*8);
        webView.getSettings().setAppCachePath(appCachePath);

        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);







        Map<String, String> headers = new HashMap<>(1);
        headers.put("Access-Token", token);

        webView.setWebViewClient(new WebViewClient(){
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//                String s = request.getRequestHeaders().get("Access-Token");
//                if(s != null && !s.isEmpty()){
//                    Intent intent = new Intent(getApplicationContext(), MainActivity_.class);
//                    intent.putExtra("token", s);
//                    setResult(666, intent);
//                    finish();
//                }

                System.out.println(request.getRequestHeaders().get("Access-Token"));
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                System.out.println("yes " + request.getUrl().toString());
                view.loadUrl(request.getUrl().toString(), headers);
//                return super.shouldOverrideUrlLoading(view, request);
                return false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                System.out.println("yes2");

                return super.shouldOverrideUrlLoading(view, url);
            }
        });


        Log.e("token", token);
        webView.getSettings().setDefaultTextEncodingName("UTF-8");



        webView.loadUrl("https://lhwtt.ydmap.cn/", headers);


    }


}