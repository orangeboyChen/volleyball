package com.nowcent.volleyball.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.nowcent.volleyball.R;
import com.nowcent.volleyball.utils.DataUtils;
import com.nowcent.volleyball.utils.Utils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.Map;

@EActivity(R.layout.activity_verify)
public class VerifyActivity extends AppCompatActivity {

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_verify);
//    }

    @ViewById(R.id.verifyWebView)
    WebView webView;

    @ViewById(R.id.verifyEarliestTextView)
    TextView earliestTextView;

    @ViewById(R.id.verifyLatestTextView)
    TextView latestTextView;

    @ViewById(R.id.verifyTotalTextView)
    TextView totalTextView;

    @ViewById(R.id.verifyRefreshBtn)
    Button refreshBtn;

    @ViewById(R.id.verifyToolbar)
    Toolbar toolbar;

    @AfterViews
    void init(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        );

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_48px);
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

        webView.loadUrl("file:///android_asset/verify.html");

        updateCaptchaInfo();

//        webView.setWebViewClient(new WebViewClient(){
//            @Nullable
//            @Override
//            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//                String url = request.getUrl().toString();
//                if(url.contains("analyze")){
//                    Map<String, String> requestHeaders = request.getRequestHeaders();
////                    loadUrl(url, requestHeaders);
//                    return null;
//                }
//
//                return super.shouldInterceptRequest(view, request);
//            }
//
//
//
//        });


        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                if(consoleMessage.sourceId().equals("file:///android_asset/verify.html")){
                    JSONObject responseJson = JSONObject.parseObject(consoleMessage.message());
                    DataUtils.saveCaptcha(getApplication(), responseJson);
                    Log.e("consoleMessage", consoleMessage.message());
                    updateCaptchaInfo();
                    webView.reload();
                }
                return super.onConsoleMessage(consoleMessage);
            }
        });
    }


    @Click(R.id.verifyRefreshBtn)
    void refresh(){
        webView.reload();
        updateCaptchaInfo();
    }

    @UiThread
    void updateCaptchaInfo(){
        Map<String, Long> captchaInfo = DataUtils.getCaptchaInfo(this);

        long captchaTotal = captchaInfo.get("size");
        totalTextView.setText(captchaTotal + "个可用验证码");

        if(captchaTotal == 0){
            earliestTextView.setText("最早过期时间: 无");
            latestTextView.setText("最晚过期时间: 无");

        }
        else {
            long earliestExpireTime = captchaInfo.get("earliestExpireTime");
            long latestExpireTime = captchaInfo.get("latestExpireTime");
            earliestTextView.setText("最早过期时间: " + Utils.getTimeString(earliestExpireTime));
            latestTextView.setText("最晚过期时间: " + Utils.getTimeString(latestExpireTime));
        }

    }

//    @Background
//    void loadUrl(String url, Map<String, String> requestHeaders){
//        CloseableHttpClient client = HttpClients.createDefault();
//        HttpGet httpGet = new HttpGet(url);
//        requestHeaders.forEach(httpGet::setHeader);
//
//        try {
//            CloseableHttpResponse execute = client.execute(httpGet);
//            String responseStr = EntityUtils.toString(execute.getEntity());
//
//            responseStr = responseStr.split("\\(")[1].replaceAll("\\);", "");
//            Log.i("webCaptcha", responseStr);
//
//            JSONObject responseJson = JSONObject.parseObject(responseStr);
//
//            if(responseJson.getBoolean("success")){
//                JSONObject resultJson = responseJson.getJSONObject("result");
//
//                if(resultJson.getIntValue("code") == 0){
//                    DataUtils.saveCaptcha(this, resultJson);
//                    Log.e("captcha", resultJson.toJSONString());
//                }
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


}