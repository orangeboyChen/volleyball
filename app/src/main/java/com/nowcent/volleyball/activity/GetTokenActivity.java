package com.nowcent.volleyball.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.nowcent.volleyball.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.File;

@EActivity(R.layout.activity_get_token)
public class GetTokenActivity extends AppCompatActivity {

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_get_token);
//    }

    @ViewById(R.id.getTokenToolbar)
    Toolbar toolbar;

    @ViewById(R.id.getTokenWebView)
    WebView webView;

    @AfterViews
    void init(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(this.getResources().getColor(R.color.colorPrimary));
//        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );

        Resources resources = getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) toolbar.getLayoutParams();
        layoutParams.setMargins(0, height, 0, 0);

        ConstraintLayout.LayoutParams layoutParam2 = (ConstraintLayout.LayoutParams) webView.getLayoutParams();

        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        toolbar.measure(w, h);
        int toolbarHeight = toolbar.getMeasuredHeight();

        layoutParam2.setMargins(0, height + toolbarHeight, 0, 0);


        Log.e("height", String.valueOf(toolbarHeight));



        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_48px);
        toolbar.setNavigationOnClickListener((l) -> {
            onBackPressed();
        });

        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        CookieSyncManager.createInstance(getApplicationContext());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeSessionCookies(null);
        cookieManager.removeAllCookie();
        cookieManager.flush();
        WebStorage.getInstance().deleteAllData();


        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAppCacheMaxSize(1024*1024*8);//存储的最大容量
        webView.getSettings().setAppCachePath(appCachePath);

        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);







        webView.setWebViewClient(new WebViewClient(){
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String s = request.getRequestHeaders().get("Access-Token");
                if(s != null && !s.isEmpty()){
                    Intent intent = new Intent(getApplicationContext(), MainActivity_.class);
                    intent.putExtra("token", s);
                    Log.e("tokkken", s);
                    setResult(666, intent);
                    finish();
                }
                return super.shouldInterceptRequest(view, request);
            }


        });
//        webView.loadUrl("http://volleyball.nowcent.cn/privacy.html");
        webView.loadUrl("https://lhwtt.ydmap.cn/user/login");
        webView.getSettings().setDefaultTextEncodingName("UTF-8");

    }


}